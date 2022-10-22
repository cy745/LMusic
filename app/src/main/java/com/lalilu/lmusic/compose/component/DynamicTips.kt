package com.lalilu.lmusic.compose.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideIn
import androidx.compose.animation.slideOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.transform.CircleCropTransformation
import com.lalilu.lmusic.utils.extension.dayNightTextColor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

object DynamicTips : CoroutineScope {
    override val coroutineContext: CoroutineContext = Dispatchers.Default

    private val title = mutableStateOf("")
    private val subTitle = mutableStateOf("播放中")
    private val imageData = mutableStateOf<Any?>(null)
    private val show = mutableStateOf(false)

    fun push(title: String, imageData: Any? = null) = launch {
        this@DynamicTips.title.value = title
        this@DynamicTips.imageData.value = imageData
    }

    @OptIn(ExperimentalAnimationApi::class)
    @Composable
    fun Content(modifier: Modifier = Modifier) {
        LaunchedEffect(title.value) {
            if (title.value.isNotEmpty()) {
                show.value = true
                delay(2500)
                if (isActive) {
                    show.value = false
                }
            }
        }

        AnimatedVisibility(
            visible = show.value,
            modifier = modifier
                .widthIn(max = 300.dp)
                .padding(top = 100.dp),
            enter = fadeIn() + scaleIn(initialScale = 0.8f) + slideIn(
                animationSpec = SpringSpec(dampingRatio = Spring.DampingRatioMediumBouncy)
            ) { IntOffset(0, -100) },
            exit = fadeOut() + scaleOut(targetScale = 0.8f) + slideOut(
                animationSpec = TweenSpec(easing = EaseIn)
            ) { IntOffset(0, -100) }
        ) {
            Surface(
                modifier = Modifier,
                shape = RoundedCornerShape(36.dp),
                elevation = 1.dp
            ) {
                Row(
                    modifier = Modifier
                        .animateContentSize()
                        .height(72.dp)
                        .padding(start = 8.dp, end = 30.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(imageData.value)
                            .size(200)
                            .transformations(CircleCropTransformation())
                            .build(),
                        modifier = Modifier.size(56.dp),
                        contentScale = ContentScale.Crop,
                        contentDescription = "Icon"
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = title.value,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.subtitle1
                        )
                        if (subTitle.value.isNotEmpty()) {
                            Text(
                                text = subTitle.value,
                                color = dayNightTextColor(0.5f),
                                style = MaterialTheme.typography.subtitle2
                            )
                        }
                    }
                }
            }
        }
    }
}