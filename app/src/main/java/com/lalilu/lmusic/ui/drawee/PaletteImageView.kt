package com.lalilu.lmusic.ui.drawee

import android.content.Context
import android.net.Uri
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import androidx.lifecycle.MutableLiveData
import androidx.palette.graphics.Palette
import coil.load

class PaletteImageView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : AppCompatImageView(context, attrs) {
    var palette: MutableLiveData<Palette> = MutableLiveData(null)
    private var blurRadius = 0

    fun blurBg(percent: Float) {
        blurRadius = (percent * 50).toInt()
    }

    override fun setImageURI(uri: Uri?) {
        this.load(uri) {
            crossfade(true)
            crossfade(500)
            target(onSuccess = {
                println(it.current.bounds.width())
            })
        }
    }
}