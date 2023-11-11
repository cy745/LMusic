package com.lalilu.register

import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.FieldVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes


class ScanClassVisitor(
    nextClassVisitor: ClassVisitor,
    private val registerInfo: RegisterInfo,
    private val classInfo: ClassInfo
) : ClassVisitor(Opcodes.ASM9, nextClassVisitor) {

    override fun visit(
        version: Int,
        access: Int,
        name: String?,
        signature: String?,
        superName: String?,
        interfaces: Array<out String>?
    ) {
        val isInterface = (access and Opcodes.ACC_INTERFACE) != 0
        val isAbstract = (access and Opcodes.ACC_ABSTRACT) != 0

        classInfo.isAbstract = isAbstract
        classInfo.isInterface = isInterface

        println(
            """
                    ==============================
                    [info]: ${registerInfo.hashCode()} $registerInfo
                    [targetManagerClass: ${registerInfo.targetManagerClass}]
                    [baseInterface: ${registerInfo.baseInterface}]
                    version: $version
                    access: $access
                    name: $name
                    signature: $signature
                    superName: $superName
                    interfaces: ${interfaces?.joinToString(", ")}
                    isInterface: $isInterface
                    isAbstract: $isAbstract
                """.trimIndent()
        )

        super.visit(version, access, name, signature, superName, interfaces)
    }

    override fun visitField(
        access: Int,
        name: String?,
        descriptor: String?,
        signature: String?,
        value: Any?
    ): FieldVisitor {
        // 存在INSTANCE变量且该变量的类型为该类自身，则说明可直接获取单例对象
        if (name == "INSTANCE" &&
            (access and Opcodes.ACC_PUBLIC) != 0 &&
            descriptor == "L${classInfo.className.replace('.', '/')};"
        ) {
            classInfo.isObject = true
        }
        return super.visitField(access, name, descriptor, signature, value)
    }

    override fun visitMethod(
        access: Int,
        name: String?,
        descriptor: String?,
        signature: String?,
        exceptions: Array<out String>?
    ): MethodVisitor {
        // 存在公开的无参构造函数则说明可以被直接实例化
        if (name == "<init>" && descriptor == "()V" && (access and Opcodes.ACC_PUBLIC) != 0) {
            classInfo.isAbleToCreate = true
        }
        return super.visitMethod(access, name, descriptor, signature, exceptions)
    }
}