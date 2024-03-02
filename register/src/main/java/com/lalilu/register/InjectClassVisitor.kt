package com.lalilu.register

import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes


class InjectClassVisitor(nextVisitor: ClassVisitor) : ClassVisitor(Opcodes.ASM9, nextVisitor) {
    private lateinit var info: RegisterInfo

    override fun visit(
        version: Int,
        access: Int,
        name: String?,
        signature: String?,
        superName: String?,
        interfaces: Array<out String>?
    ) {
        val className = name?.replace('/', '.')
        RegisterConfig.registerInfo[className]?.let {
            info = it
            println(
                """
                ------ $name -------
                [targetManagerClass: ${info.targetManagerClass}]
                [baseInterface: ${info.baseInterface}]
                [registerMethod: ${info.registerMethod}]
                [registerMethodClass: ${info.registerMethodClass}]
                [classSetSize: ${info.classSet.size}]
                """.trimIndent()
            )
        }
        super.visit(version, access, name, signature, superName, interfaces)
    }

    override fun visitMethod(
        access: Int,
        name: String?,
        descriptor: String?,
        signature: String?,
        exceptions: Array<out String>?
    ): MethodVisitor {
        var mv = super.visitMethod(access, name, descriptor, signature, exceptions)
        if (name == "<clinit>" && ::info.isInitialized) {
            println("visitMethod: $access $name $descriptor $signature")
            mv = InjectMethodVisitor(access, name, descriptor, mv, info)
        }
        return mv
    }
}
