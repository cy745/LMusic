package com.lalilu.voyager.router.annotation

/**
 * 用于为参数的标记额外信息
 *
 * @param key       用于指定获取参数的key，默认为空，则使用参数名作为key
 * @param remark    用于指定参数的备注
 * @param required  用于指定参数是否必须，默认为必须
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class Param(
    val key: String = "",
    val remark: String = "",
    val required: Boolean = true
)
