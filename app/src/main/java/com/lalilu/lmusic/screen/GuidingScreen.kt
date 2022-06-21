package com.lalilu.lmusic.screen

import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.SPUtils
import com.funny.data_saver.core.rememberDataSaverState
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.lalilu.R
import com.lalilu.common.HapticUtils
import com.lalilu.lmusic.Config
import com.lalilu.lmusic.MainActivity
import com.lalilu.lmusic.screen.component.card.PlayingCard
import com.lalilu.lmusic.screen.component.settings.SettingStateSeekBar
import com.lalilu.lmusic.utils.getActivity
import com.lalilu.ui.NewSeekBar
import com.lalilu.ui.OnSeekBarCancelListener
import com.lalilu.ui.OnSeekBarScrollToThresholdListener
import kotlin.system.exitProcess

@Composable
@OptIn(ExperimentalAnimationApi::class)
fun GuidingScreen(
    navController: NavHostController = rememberAnimatedNavController()
) {

    val currentBackStack by navController.currentBackStackEntryAsState()
    val destination = currentBackStack?.destination?.route.let {
        GuidingNavGraph.fromRoute(it)
    }

    Surface {
        Column(
            Modifier
                .fillMaxSize()
                .background(color = MaterialTheme.colors.background)
                .statusBarsPadding()
        ) {
            Row(
                modifier = Modifier
                    .padding(20.dp)
                    .height(48.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AnimatedVisibility(visible = (GuidingNavGraph.getIndex(destination) != 0)) {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_arrow_left_s_line),
                            contentDescription = "navigateUp"
                        )
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    AnimatedContent(targetState = destination, transitionSpec = {
                        (slideInVertically { height -> height } + fadeIn() with
                                slideOutVertically { height -> -height } + fadeOut()).using(
                            SizeTransform(clip = false)
                        )
                    }) {
                        Text(text = it?.title ?: "", fontSize = 22.sp)
                    }
                    Text(text = "${GuidingNavGraph.getIndex(destination) + 1} / ${GuidingNavGraph.values().size}")
                }
            }
            AnimatedNavHost(
                navController = navController,
                startDestination = GuidingNavGraph.Agreement.name,
                exitTransition = { ExitTransition.None },
                enterTransition = {
                    fadeIn(
                        animationSpec = tween(
                            durationMillis = 700,
                        )
                    ) + slideInVertically { 100 }
                }
            ) {
                composable(
                    route = GuidingNavGraph.Agreement.name
                ) {
                    AgreementPage(navController)
                }
                composable(
                    route = GuidingNavGraph.SeekbarGuiding.name
                ) {
                    SeekbarGuidingPage(navController)
                }
            }
        }
    }
}

@Composable
fun AgreementPage(navController: NavController) {
    Column(
        modifier = Modifier
            .padding(horizontal = 20.dp)
            .fillMaxSize()
    ) {
        ActionCard(
            onReject = { exitProcess(0) },
            onConfirm = { GuidingNavGraph.Agreement.getNext()?.navigate(navController) }
        ) {
            """
            使用本应用的用户应知晓以下内容：

            1. 本应用所提供的网络歌词获取功能需要使用网络权限，如无此需求可拒绝网络权限授予；
            2. 本应用所涉及的网络接口调用均不以获取用户个人唯一标识为前提，以此确保用户个人信息和隐私安全；
            3. 本应用本体及代码基于AGPL-3.0开源协议进行开源，任何个人与组织不得将此应用本体及代码应用于商业行为；

            未来此协议可能有扩充的可能性，认可本协议内容即视为同意未来的变更。
            不会加广告，不会收费，大可放心。
                
            酷安@邱邱邱Qiu  v1.4.12  2022/05/17
            """
        }
    }
}

@Composable
fun SeekbarGuidingPage(navController: NavController) {
    val context = LocalContext.current

    val seekbarHandler = rememberDataSaverState(
        Config.KEY_SETTINGS_SEEKBAR_HANDLER,
        Config.DEFAULT_SETTINGS_SEEKBAR_HANDLER
    )

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                ActionCard(onConfirm = {
                    context.getActivity()?.apply {
                        SPUtils.getInstance(this.packageName, AppCompatActivity.MODE_PRIVATE)
                            .put(Config.KEY_REMEMBER_IS_GUIDING_OVER, true)

                        if (!ActivityUtils.isActivityExistsInStack(MainActivity::class.java)) {
                            ActivityUtils.startActivity(MainActivity::class.java)
                        }
                        finishAfterTransition()
                    }
                }) {
                    """
                    来看看这个神奇的进度条，
                    竟然不仅可以在它上面滑动调节进度，
                    还可以在它上面单击/双击/长按进行切歌，
                    只需要单击/双击/长按它的左右侧就能
                    """
                }
            }
            item {
                Surface(shape = RoundedCornerShape(10.dp)) {
                    SettingStateSeekBar(
                        state = seekbarHandler,
                        selection = stringArrayResource(id = R.array.seekbar_handler).toList(),
                        titleRes = R.string.preference_player_settings_seekbar_handler,
                        paddingValues = PaddingValues(vertical = 20.dp, horizontal = 20.dp)
                    )
                }
            }
            item {
                Surface(shape = RoundedCornerShape(10.dp)) {
                    PlayingCard(
                        mediaItem = MediaItem.Builder()
                            .setMediaId("26200")
                            .setMediaMetadata(
                                MediaMetadata.Builder()
                                    .setTitle("测试")
                                    .build()
                            ).build()
                    )
                }
            }
        }

        AndroidView(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(horizontal = 50.dp, vertical = 72.dp)
                .height(48.dp),
            factory = {
                NewSeekBar(it).apply {
                    maxValue = 4 * 45 * 1000F
                    cancelListeners.add(OnSeekBarCancelListener {
                        HapticUtils.haptic(this)
                    })

                    scrollListeners.add(object : OnSeekBarScrollToThresholdListener({ 300f }) {
                        override fun onScrollToThreshold() {
                            HapticUtils.haptic(this@apply)
                        }

                        override fun onScrollRecover() {
                            HapticUtils.haptic(this@apply)
                        }
                    })
                }
            })
    }
}

@Composable
fun ActionCard(
    text: String? = null,
    rejectTitle: String = "拒绝",
    confirmTitle: String = "确认",
    onReject: (() -> Unit)? = null,
    onConfirm: (() -> Unit)? = null,
    getText: () -> String = { "" },
) {
    Surface(shape = RoundedCornerShape(10.dp)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp, start = 15.dp, end = 15.dp, bottom = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = text ?: getText().trimIndent(),
                fontSize = 14.sp,
                lineHeight = 20.sp
            )
            Row(modifier = Modifier.align(Alignment.End)) {
                onReject?.let {
                    TextButton(onClick = it) {
                        Text(text = rejectTitle)
                    }
                }
                onConfirm?.let {
                    TextButton(
                        onClick = onConfirm,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = Color(0xFF3EA22C)
                        )
                    ) {
                        Text(text = confirmTitle)
                    }
                }
            }
        }
    }
}

enum class GuidingNavGraph(
    val title: String
) {
    Agreement(title = "用户协议 | 隐私协议"),
    SeekbarGuiding(title = "基础引导教程");


    fun getNext(): GuidingNavGraph? {
        return Companion.getNext(this)
    }

    fun navigate(navController: NavController) {
        navController.navigate(this.name)
    }

    companion object {
        fun fromRoute(route: String?): GuidingNavGraph? {
            val target = route?.substringBefore("/")
            return values().find { it.name == target }
        }

        fun fromIndex(index: Int): GuidingNavGraph? {
            return values().getOrNull(index)
        }

        fun getIndex(destination: GuidingNavGraph?): Int {
            destination ?: return -1
            return values().indexOf(destination)
        }

        fun getNext(destination: GuidingNavGraph?): GuidingNavGraph? {
            return fromIndex(getIndex(destination) + 1)
        }
    }
}
