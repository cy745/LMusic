package com.lalilu.crash

import android.os.Parcel
import android.os.Parcelable

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
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(title)
        parcel.writeString(message)
        parcel.writeString(causeClass)
        parcel.writeString(causeMethod)
        parcel.writeString(causeFile)
        parcel.writeString(causeLine)
        parcel.writeString(stackTrace)
        parcel.writeString(deviceInfo)
        parcel.writeString(buildVersion)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<CrashModel> {
        override fun createFromParcel(parcel: Parcel): CrashModel {
            return CrashModel(parcel)
        }

        override fun newArray(size: Int): Array<CrashModel?> {
            return arrayOfNulls(size)
        }
    }
}