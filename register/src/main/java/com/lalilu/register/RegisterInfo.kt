package com.lalilu.register

import java.io.Serializable

/**
 * [targetManagerClass]     目标注册的类
 * [baseInterface]          需要进行扫描后注册的接口
 * [registerMethod]         实际调用来自动注册方法名
 * [registerMethodClass]
 */
class RegisterInfo(
    val targetManagerClass: String,
    val baseInterface: String,
    val registerMethodClass: String,
    val registerMethod: String
) : Serializable {
    val classSet = HashSet<ClassInfo>()
}

