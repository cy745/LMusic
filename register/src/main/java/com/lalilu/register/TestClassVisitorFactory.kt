package com.lalilu.register

import com.android.build.api.instrumentation.AsmClassVisitorFactory
import com.android.build.api.instrumentation.ClassContext
import com.android.build.api.instrumentation.ClassData
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

abstract class TestClassVisitorFactory : AsmClassVisitorFactory<CollectParameter> {
    override fun createClassVisitor(
        classContext: ClassContext,
        nextClassVisitor: ClassVisitor
    ): ClassVisitor {
        return TestClassVisitor(nextClassVisitor)
    }

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
        return result
    }
}

class TestClassVisitor(nextVisitor: ClassVisitor) : ClassVisitor(Opcodes.ASM9, nextVisitor) {
    override fun visitMethod(
        access: Int,
        name: String?,
        descriptor: String?,
        signature: String?,
        exceptions: Array<out String>?
    ): MethodVisitor {
        return super.visitMethod(access, name, descriptor, signature, exceptions)
    }
}