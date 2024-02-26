package com.lalilu.component.extension

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsIgnoringVisibility
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.lalilu.component.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference
import kotlin.coroutines.CoroutineContext


interface DynamicTipsContext {
    fun show(item: DynamicTipsItem)
    fun dismiss()
}

sealed class DynamicTipsItem {
    data class Static(
        val title: String,
        val subTitle: String? = null,
        val imageData: Any? = null,
        val onClick: (() -> Unit)? = null,
        val onDismiss: (() -> Unit)? = null
    ) : DynamicTipsItem()

    data class Dynamic(
        val content: @Composable (DynamicTipsContext) -> Unit
    ) : DynamicTipsItem()

    fun show(dynamicTipsContext: DynamicTipsContext = DynamicTipsHost) {
        dynamicTipsContext.show(this)
    }
}

@OptIn(ExperimentalLayoutApi::class)
object DynamicTipsHost : DynamicTipsContext, CoroutineScope {
    override val coroutineContext: CoroutineContext = Dispatchers.Default
    private var countDownJob: Job? = null

    private var showing by mutableStateOf(false)
    private var enable by mutableStateOf(true)
    private var itemRef by mutableStateOf<WeakReference<DynamicTipsItem>?>(null)

    override fun show(item: DynamicTipsItem) {
        if (!enable) return

        this.itemRef = WeakReference(item)
        this.showing = true

        countDownJob?.cancel()
        countDownJob = launch {
            delay(5000)
            if (!isActive) return@launch

            this@DynamicTipsHost.showing = false
        }
    }

    override fun dismiss() {
        countDownJob?.cancel()
        countDownJob = null
        showing = false
    }

    @Composable
    fun BoxScope.Content() {
        AnimatedVisibility(
            modifier = Modifier
                .windowInsetsPadding(WindowInsets.statusBarsIgnoringVisibility)
                .padding(top = 20.dp)
                .align(Alignment.TopCenter),
            exit = remember { fadeOut() + slideOutVertically { -it } + scaleOut(targetScale = 0.7f) },
            enter = remember { fadeIn() + slideInVertically { -it } + scaleIn(initialScale = 0.7f) },
            visible = showing
        ) {
            val item = itemRef?.get() ?: return@AnimatedVisibility

            BlurFadeTransition(
                modifier = Modifier.align(Alignment.TopCenter),
                item = { item }
            ) {
                when (it) {
                    is DynamicTipsItem.Dynamic -> it.content(this@DynamicTipsHost)
                    is DynamicTipsItem.Static -> DefaultDynamicItemCard(
                        modifier = Modifier,
                        imageData = it.imageData,
                        title = it.title,
                        subTitle = it.subTitle,
                        onClick = it.onClick,
                        onDismiss = it.onDismiss
                    )
                }
            }
        }
    }
}


@OptIn(ExperimentalAnimationApi::class)
@Composable
fun <T> BlurFadeTransition(
    modifier: Modifier = Modifier,
    maxBlurDp: Dp = 25.dp,
    item: () -> T,
    content: @Composable (T) -> Unit,
) {
    AnimatedContent(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        transitionSpec = {
            fadeIn(spring(stiffness = Spring.StiffnessLow)) togetherWith
                    fadeOut(spring(stiffness = Spring.StiffnessLow))
        },
        contentAlignment = Alignment.TopCenter,
        targetState = item(),
        label = ""
    ) { animateItem ->
        val blurValue = transition.animateDp(
            transitionSpec = { spring(stiffness = Spring.StiffnessLow) },
            label = ""
        ) { state ->
            when (state) {
                EnterExitState.Visible -> 0.dp
                EnterExitState.PreEnter -> maxBlurDp
                EnterExitState.PostExit -> maxBlurDp
            }
        }

        Box(
            modifier = modifier
                .wrapContentWidth()
                .padding(vertical = 20.dp, horizontal = 20.dp)
                .blur(
                    radius = blurValue.value,
                    edgeTreatment = BlurredEdgeTreatment.Unbounded
                ),
            content = { content(animateItem) }
        )
    }
}

@Composable
fun DefaultDynamicItemCard(
    modifier: Modifier = Modifier,
    imageData: Any? = null,
    title: String,
    subTitle: String? = null,
    onClick: (() -> Unit)? = null,
    onDismiss: (() -> Unit)? = null,
) {
    val context = LocalContext.current
    val imageRequest = remember(imageData) {
        ImageRequest.Builder(context)
            .placeholder(R.drawable.ic_music_line_bg_48dp)
            .error(R.drawable.ic_music_line_bg_48dp)
            .data(imageData)
            .size(200)
            .build()
    }

    Surface(
        modifier = modifier.wrapContentWidth(),
        shape = RoundedCornerShape(36.dp),
        border = BorderStroke(1.dp, dayNightTextColor(0.1f)),
        elevation = 10.dp
    ) {
        Row(
            modifier = Modifier
                .wrapContentWidth()
                .height(72.dp)
                .padding(start = 8.dp, end = 30.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            AsyncImage(
                model = imageRequest,
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(color = dayNightTextColor(0.1f)),
                contentScale = ContentScale.Crop,
                contentDescription = "Icon"
            )
            Column {
                Text(
                    text = title,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.subtitle1
                )
                if (!subTitle.isNullOrBlank()) {
                    Text(
                        text = subTitle,
                        color = dayNightTextColor(0.5f),
                        style = MaterialTheme.typography.subtitle2
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun DefaultDynamicItemCardPreview() {
    DefaultDynamicItemCard(
        title = "test",
        subTitle = "test22222"
    )
}