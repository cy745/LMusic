package com.lalilu.register

import java.io.Serializable

open class RegisterConfig : Serializable {
    var enable: Boolean = false
    var registerInfoList: List<Map<String, String>> = arrayListOf()

    fun convertRegisterInfo(): List<RegisterInfo> {
        return registerInfoList.mapNotNull {
            val targetManagerClass = it[TARGET_MANAGER_CLASS] ?: return@mapNotNull null
            val baseInterface = it[BASE_INTERFACE] ?: return@mapNotNull null
            val registerMethod = it[REGISTER_METHOD] ?: return@mapNotNull null
            val registerMethodClass = it[REGISTER_METHOD_CLASS] ?: targetManagerClass

            RegisterInfo(
                targetManagerClass = targetManagerClass,
                baseInterface = baseInterface,
                registerMethod = registerMethod,
                registerMethodClass = registerMethodClass
            )
        }
    }

    companion object {
        const val TARGET_MANAGER_CLASS = "TARGET_MANAGER"
        const val BASE_INTERFACE = "BASE_INTERFACE"
        const val REGISTER_METHOD = "REGISTER_METHOD"
        const val REGISTER_METHOD_CLASS = "REGISTER_METHOD_CLASS"

        val registerInfo = HashMap<String, RegisterInfo>()
    }
}