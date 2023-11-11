package com.lalilu.register

import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.work.ChangeType
import org.gradle.work.Incremental
import org.gradle.work.InputChanges
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters
import org.gradle.workers.WorkerExecutor
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.security.MessageDigest
import java.util.Locale
import java.util.jar.JarEntry
import java.util.jar.JarInputStream
import java.util.jar.JarOutputStream
import java.util.zip.Deflater
import javax.inject.Inject

/**
 * 复制修改自 b7woreo/TraceX 的仓库
 * https://github.com/b7woreo/TraceX/blob/main/gradle-plugin/src/main/java/tracex/TraceClassVisitor.kt
 */
@CacheableTask
abstract class InjectTransformTask : DefaultTask() {

    @get:Internal
    abstract val allJars: ListProperty<RegularFile>

    @get:Internal
    abstract val allDirectories: ListProperty<Directory>

    @get:InputFiles
    @get:Classpath
    @get:Incremental
    val allJarsFileCollection: ConfigurableFileCollection by lazy {
        project.files(allJars)
    }

    @get:InputFiles
    @get:Classpath
    @get:Incremental
    val allDirectoriesFileCollection: ConfigurableFileCollection by lazy {
        project.files(allDirectories)
    }

    @get:OutputDirectory
    abstract val intermediate: DirectoryProperty

    @get:OutputFile
    abstract val outputJar: RegularFileProperty

    @get:Inject
    abstract val workerExecutor: WorkerExecutor

    @TaskAction
    fun transform(inputChanges: InputChanges) {
        val workQueue = workerExecutor.noIsolation()

        val changedJars = inputChanges.getFileChanges(allJarsFileCollection)
        val changedClasses = inputChanges.getFileChanges(allDirectoriesFileCollection)
        val intermediateFile = intermediate.get().asFile

        changedJars.forEach { changedJar ->
            workQueue.submit(TransformJar::class.java) {
                rootDir.set(project.rootDir)
                source.set(changedJar.file)
                normalizedPath.set(changedJar.normalizedPath)
                changeType.set(changedJar.changeType)
                intermediate.set(intermediateFile)
            }
        }

        changedClasses.forEach { changedClass ->
            workQueue.submit(TransformClass::class.java) {
                rootDir.set(project.rootDir)
                source.set(changedClass.file)
                normalizedPath.set(changedClass.normalizedPath)
                changeType.set(changedClass.changeType)
                intermediate.set(intermediateFile)
            }
        }

        workQueue.await()

        mergeClasses(
            intermediateFile,
            outputJar.get().asFile,
        )
    }

    private fun mergeClasses(
        intermediate: File,
        outputJar: File,
    ) {
        JarOutputStream(
            outputJar.outputStream()
                .buffered()
        ).use { jar ->
            jar.setLevel(Deflater.NO_COMPRESSION)

            intermediate.listFiles()?.forEach { rootDir ->
                rootDir.allFiles { child ->
                    val name = child.toRelativeString(rootDir)
                    val entry = JarEntry(name)
                    jar.putNextEntry(entry)
                    child.inputStream().use { input -> input.transferTo(jar) }
                    jar.closeEntry()
                }
            }
        }
    }

    abstract class Transform : WorkAction<Transform.Parameters> {

        protected val rootDir: File
            get() = parameters.rootDir.get().asFile

        protected val source: File
            get() = parameters.source.get().asFile

        protected val normalizedPath: String
            get() = parameters.normalizedPath.get()

        protected val changeType: ChangeType
            get() = parameters.changeType.get()

        protected val intermediate: File
            get() = parameters.intermediate.get().asFile

        protected abstract val destination: File

        protected abstract fun transform()

        final override fun execute() {
            println("[$changeType] $source($normalizedPath) -> $destination")

            when (changeType) {
                ChangeType.ADDED -> {
                    transform()
                }

                ChangeType.MODIFIED -> {
                    destination.deleteRecursively()
                    transform()
                }

                ChangeType.REMOVED -> {
                    destination.deleteRecursively()
                }

                else -> {}
            }
        }

        protected fun includeFileInTransform(relativePath: String): Boolean {
            val lowerCase = relativePath.lowercase(Locale.ROOT)
            if (!lowerCase.endsWith(".class")) {
                return false
            }

            if (lowerCase == "module-info.class" ||
                lowerCase.endsWith("/module-info.class")
            ) {
                return false
            }

            if (lowerCase.startsWith("/meta-info/") ||
                lowerCase.startsWith("meta-info/")
            ) {
                return false
            }
            return true
        }

        protected fun transform(
            input: InputStream,
            output: OutputStream,
        ) {
            val cr = ClassReader(input)
            val cw = ClassWriter(ClassWriter.COMPUTE_MAXS)
            cr.accept(InjectClassVisitor(cw), ClassReader.EXPAND_FRAMES)
            output.write(cw.toByteArray())
        }

        interface Parameters : WorkParameters {
            val rootDir: DirectoryProperty
            val source: RegularFileProperty
            val normalizedPath: Property<String>
            val changeType: Property<ChangeType>
            val intermediate: DirectoryProperty
        }
    }

    abstract class TransformJar : Transform() {

        override val destination: File
            get() = File(intermediate, source.identify())

        override fun transform() {
            JarInputStream(
                source.inputStream().buffered()
            ).use { input ->
                while (true) {
                    val entry = input.nextEntry ?: break
                    if (!includeFileInTransform(entry.name)) continue
                    val outputFile = File(destination, entry.name)
                        .also {
                            it.parentFile.mkdirs()
                            it.createNewFile()
                        }

                    outputFile.outputStream()
                        .buffered()
                        .use { output ->
                            transform(input, output)
                        }
                }
            }
        }

        private fun File.identify(): String {
            var current: File? = this
            while (current != null) {
                if (rootDir == current) {
                    return toRelativeString(rootDir).toSha256()
                }
                current = current.parentFile
            }
            return name.toSha256()
        }

        private fun String.toSha256(): String {
            val md = MessageDigest.getInstance("SHA-256")
            val bytes = md.digest(this.toByteArray())
            return bytes.joinToString("") { "%02x".format(it) }
        }
    }

    abstract class TransformClass : Transform() {

        override val destination: File
            get() = File(intermediate.resolve("classes"), normalizedPath)

        override fun transform() {
            if (!includeFileInTransform(normalizedPath)) return
            destination.parentFile.mkdirs()
            destination.createNewFile()

            source.inputStream()
                .buffered()
                .use { input ->
                    destination.outputStream()
                        .buffered()
                        .use { output ->
                            transform(input, output)
                        }
                }
        }
    }

    private fun File.allFiles(block: (File) -> Unit) {
        if (this.isFile) {
            block(this)
            return
        }

        val children = listFiles() ?: return
        children.forEach { it.allFiles(block) }
    }

}