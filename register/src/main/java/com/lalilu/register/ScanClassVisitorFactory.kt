package com.lalilu.register

import com.android.build.api.instrumentation.AsmClassVisitorFactory
import com.android.build.api.instrumentation.ClassContext
import com.android.build.api.instrumentation.ClassData
import org.objectweb.asm.ClassVisitor


/**
 * 扫描需要进行注册的各个子类
 */
abstract class ScanClassVisitorFactory : AsmClassVisitorFactory<CollectParameter> {

    override fun createClassVisitor(
        classContext: ClassContext,
        nextClassVisitor: ClassVisitor
    ): ClassVisitor = nextClassVisitor

    override fun isInstrumentable(classData: ClassData): Boolean {
        val result = classData.className.startsWith("com.lalilu")

        if (result) {
            println(
                """
                --------------------------------------------------------------------------------
                className: ${classData.className}
                classAnnotations: ${classData.classAnnotations.joinToString(", ")}
                superClasses: ${classData.superClasses.joinToString(", ")}
                interfaces: ${classData.interfaces.joinToString(", ")}
                
            """.trimIndent()
            )
        }
        return false
    }
}