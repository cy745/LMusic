package com.lalilu.crash

import android.content.Intent
import java.io.Serializable


data class CrashModel(
    val title: String = "",
    val message: String = "",
    val causeClass: String = "",
    val causeMethod: String = "",
    val causeFile: String = "",
    val causeLine: String = "",
    val stackTrace: String = "",
    val deviceInfo: String = "",
    val buildVersion: String = ""
) : Serializable

fun CrashModel.copyTo(intent: Intent) = intent.run {
    putExtra(CrashModel::title.name, title)
    putExtra(CrashModel::message.name, message)
    putExtra(CrashModel::causeClass.name, causeClass)
    putExtra(CrashModel::causeMethod.name, causeMethod)
    putExtra(CrashModel::causeFile.name, causeFile)
    putExtra(CrashModel::causeLine.name, causeLine)
    putExtra(CrashModel::stackTrace.name, stackTrace)
    putExtra(CrashModel::deviceInfo.name, deviceInfo)
    putExtra(CrashModel::buildVersion.name, buildVersion)
}

fun Intent.toCrashModel(): CrashModel = CrashModel(
    title = getStringExtra(CrashModel::title.name) ?: "",
    message = getStringExtra(CrashModel::message.name) ?: "",
    causeClass = getStringExtra(CrashModel::causeClass.name) ?: "",
    causeMethod = getStringExtra(CrashModel::causeMethod.name) ?: "",
    causeFile = getStringExtra(CrashModel::causeFile.name) ?: "",
    causeLine = getStringExtra(CrashModel::causeLine.name) ?: "",
    stackTrace = getStringExtra(CrashModel::stackTrace.name) ?: "",
    deviceInfo = getStringExtra(CrashModel::deviceInfo.name) ?: "",
    buildVersion = getStringExtra(CrashModel::buildVersion.name) ?: "",
)