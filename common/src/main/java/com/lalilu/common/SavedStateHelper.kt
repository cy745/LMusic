package com.lalilu.common

import android.os.Parcel
import android.os.Parcelable
import android.view.View.BaseSavedState
import java.lang.reflect.Field

open class SavedStateHelper : BaseSavedState {

    private lateinit var fields: List<Field>

    constructor(parcelable: Parcelable) : super(parcelable)

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        super.writeToParcel(parcel, flags)
        if (!::fields.isInitialized) {
            fields = this::class.java.declaredFields.filter { it.type.isPrimitive }
        }

        for (field in fields) {
            field.isAccessible = true
            when (field.type) {
                String::class.java -> parcel.writeString(field.get(this) as String)
                Int::class.java -> parcel.writeInt(field.get(this) as Int)
                Float::class.java -> parcel.writeFloat(field.get(this) as Float)
                Double::class.java -> parcel.writeDouble(field.get(this) as Double)
                Long::class.java -> parcel.writeLong(field.get(this) as Long)
                Boolean::class.java -> parcel.writeInt(if (field.get(this) as Boolean) 1 else 0)
            }
        }
    }

    constructor(superState: Parcel) : super(superState) {
        if (!::fields.isInitialized) {
            fields = this::class.java.declaredFields.filter { it.type.isPrimitive }
        }
        for (field in fields) {
            field.isAccessible = true
            when (field.type) {
                String::class.java -> field.set(this, superState.readString())
                Int::class.java -> field.set(this, superState.readInt())
                Float::class.java -> field.set(this, superState.readFloat())
                Double::class.java -> field.set(this, superState.readDouble())
                Long::class.java -> field.set(this, superState.readLong())
                Boolean::class.java -> field.set(this, superState.readInt() != 0)
            }
        }
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<SavedStateHelper> {
        override fun createFromParcel(parcel: Parcel): SavedStateHelper {
            return SavedStateHelper(parcel)
        }

        override fun newArray(size: Int): Array<SavedStateHelper?> {
            return arrayOfNulls(size)
        }

        inline fun <reified T : SavedStateHelper> onSave(
            parcelable: Parcelable?,
            callback: (T) -> Unit
        ): Parcelable? = parcelable
            ?.let { T::class.java.getConstructor(Parcelable::class.java).newInstance(it) }
            ?.also(callback)

        inline fun <reified T : SavedStateHelper> onRestore(
            parcelable: Parcelable,
            callback: (T) -> Unit
        ): Parcelable? = parcelable.let { T::class.java.cast(it)?.also(callback) }
    }
}