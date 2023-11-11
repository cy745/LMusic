package com.lalilu.register

import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.commons.AdviceAdapter

class InjectMethodVisitor(
    access: Int,
    name: String?,
    descriptor: String?,
    methodVisitor: MethodVisitor,
    private val info: RegisterInfo
) : AdviceAdapter(Opcodes.ASM9, methodVisitor, access, name, descriptor) {

    override fun onMethodExit(opcode: Int) {
        val managerAsmClassName = info.targetManagerClass.replace('.', '/')

        for (classInfo in info.classSet) {
            // 跳过无法注入的类
            if (!classInfo.isObject && !classInfo.isAbleToCreate) continue
            if (classInfo.isInterface || classInfo.isAbstract) continue

            val itemAsmClassName = classInfo.className.replace('.', '/')

            // TODO 需要适配非object的Manager对象
            // 首先需要获取到对象自身
            mv.visitFieldInsn(
                Opcodes.GETSTATIC,
                managerAsmClassName,
                "INSTANCE",
                "L$managerAsmClassName;"
            )

            mv.visitLdcInsn(itemAsmClassName) //类名

            when {
                // 若为单例对象，则直接获取单例
                classInfo.isObject -> {
                    mv.visitFieldInsn(
                        Opcodes.GETSTATIC,
                        itemAsmClassName,
                        "INSTANCE",
                        "L${itemAsmClassName};"
                    )
                }

                // 若拥有无参构造函数，则实例化以后调用其构造函数
                classInfo.isAbleToCreate -> {
                    mv.visitTypeInsn(Opcodes.NEW, itemAsmClassName)
                    mv.visitInsn(Opcodes.DUP)
                    mv.visitMethodInsn(
                        Opcodes.INVOKESPECIAL,
                        itemAsmClassName,
                        "<init>",
                        "()V",
                        false
                    )
                }
            }

            // 进行组件的注入，实际所需的操作栈参数为3个，第一个为调用的对象，其他为函数声明的参数
            // <targetManagerClass>.<registerMethod>(String,<baseInterface>)
            mv.visitMethodInsn(
                Opcodes.INVOKEVIRTUAL,
                info.registerMethodClass.replace('.', '/'),
                info.registerMethod,
                "(Ljava/lang/String;Ljava/lang/Object;)V",
                false
            )
        }
    }
}