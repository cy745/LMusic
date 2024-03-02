package com.lalilu.register

import com.android.build.api.instrumentation.AsmClassVisitorFactory
import com.android.build.api.instrumentation.ClassContext
import com.android.build.api.instrumentation.ClassData
import com.android.build.api.instrumentation.InstrumentationParameters
import org.objectweb.asm.ClassVisitor

private val registerInfoMap: LinkedHashMap<String, RegisterInfo?> = linkedMapOf()
private val classesMap: LinkedHashMap<String, ClassInfo> = linkedMapOf()

/**
 * 扫描需要进行注册的各个子类
 */
abstract class ScanClassVisitorFactory : AsmClassVisitorFactory<TempParameter> {

    override fun createClassVisitor(
        classContext: ClassContext,
        nextClassVisitor: ClassVisitor
    ): ClassVisitor {
        val info = registerInfoMap[classContext.currentClassData.className]
        requireNotNull(info) { "registerConfig is null, please check your registerConfig" }

        val classInfo = classesMap[classContext.currentClassData.className]
        requireNotNull(classInfo) { "classInfo is null, please check your classInfo" }

        return ScanClassVisitor(nextClassVisitor, info, classInfo)
    }

    override fun isInstrumentable(classData: ClassData): Boolean {
        val registerInfo = RegisterConfig.registerInfo.values

        val info = registerInfo.firstOrNull { classData.interfaces.contains(it.baseInterface) }
        registerInfoMap[classData.className] = info

        if (info != null) {
            val classInfo = ClassInfo(classData.className)
            classesMap[classData.className] = classInfo
            info.classSet.add(classInfo)
        }
        return info != null
    }
}