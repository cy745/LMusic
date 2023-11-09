package com.lalilu.register

import com.android.build.api.instrumentation.AsmClassVisitorFactory
import com.android.build.api.instrumentation.ClassContext
import com.android.build.api.instrumentation.ClassData
import org.objectweb.asm.ClassVisitor


/**
 * 扫描需要进行注册的各个子类
 */
abstract class InjectClassVisitorFactory : AsmClassVisitorFactory<CollectParameter> {

    override fun createClassVisitor(
        classContext: ClassContext,
        nextClassVisitor: ClassVisitor
    ): ClassVisitor = nextClassVisitor

    override fun isInstrumentable(classData: ClassData): Boolean {
        return false
    }
}