package com.lalilu.register

class ClassInfo(
    val className: String,
    var isObject: Boolean = false,
    var isAbleToCreate: Boolean = false,
    var isInterface: Boolean = false,
    var isAbstract: Boolean = false
)