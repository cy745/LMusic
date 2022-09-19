package com.lalilu.common

import android.app.Activity
import android.content.Context
import android.widget.Toast
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions

object PermissionUtils {
    fun requestPermission(
        context: Activity,
        onSuccess: () -> Unit = {},
        onFailed: () -> Unit = {}
    ) {
        XXPermissions.with(context)
            .permission(Permission.READ_MEDIA_AUDIO)
            .request(object : OnPermissionCallback {
                override fun onGranted(permissions: List<String>, all: Boolean) {
                    if (all) {
                        onSuccess.invoke()
                        println("获取权限成功")
                    } else {
                        println("获取部分权限成功，但部分权限未正常授予，程序可能无法成功运作")
                    }
                }

                override fun onDenied(permissions: List<String>, never: Boolean) {
                    if (never) {
                        Toast.makeText(context, "已被被永久拒绝授权，请手动授予相关权限", Toast.LENGTH_SHORT)
                            .show()
                        XXPermissions.startPermissionActivity(context, permissions)
                    } else {
                        onFailed.invoke()
                    }
                }
            })
    }

    fun returnPermissionCheck(context: Context, requestCode: Int) {
        if (requestCode == XXPermissions.REQUEST_CODE) {
            if (XXPermissions.isGranted(context, Permission.RECORD_AUDIO) &&
                XXPermissions.isGranted(context, Permission.Group.CALENDAR)
            ) {
                Toast.makeText(context, "用户已经在权限设置页授予了相关权限", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "用户没有在权限设置页授予权限", Toast.LENGTH_SHORT).show()
            }
        }
    }
}