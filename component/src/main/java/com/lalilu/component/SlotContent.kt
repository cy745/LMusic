package com.lalilu.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.koin.compose.currentKoinScope
import org.koin.core.parameter.ParametersDefinition
import org.koin.core.parameter.ParametersHolder
import org.koin.core.qualifier.Qualifier
import org.koin.core.qualifier.named
import org.koin.core.scope.Scope


/**
 * 需要注意Koin注入的时候不能定义默认值
 */
fun interface SlotContent {
    @Composable
    fun Content(modifier: Modifier)
}

@Composable
fun slot(
    modifier: Modifier = Modifier,
    key: String,
    parameters: ParametersDefinition? = null,
    elseContent: @Composable (modifier: Modifier) -> Unit = {
        UnlinkSlot(modifier = it, key = key)
    }
) {
    val content = koinInjectOrNull<SlotContent?>(
        qualifier = named(key),
        parameters = parameters
    )
    content?.Content(modifier) ?: elseContent(modifier)
}

/**
 * 需确保函数的调用顺序和参数顺序一致
 */
class SlotParamContext {
    private val array = mutableListOf<Any?>()
    fun <T : Any> value(value: T) = array.add(value)
    fun <T : Any> values(vararg value: T) = value.forEach { array.add(it) }
    fun <T : Any?> funcT(func: (T) -> Unit) = array.add(func)
    fun <T : Any?> funcK(func: () -> T) = array.add(func)
    fun <T : Any?, K : Any?> funcTK(func: (T) -> K) = array.add(func)
    fun build(): ParametersHolder = ParametersHolder(array)
}

/**
 * 构建自定义的参数注入
 */
@Stable
fun slotParams(block: SlotParamContext.() -> Unit): () -> ParametersHolder = {
    SlotParamContext().apply(block).build()
}

@Composable
private inline fun <reified T> koinInjectOrNull(
    qualifier: Qualifier? = null,
    scope: Scope = currentKoinScope(),
    noinline parameters: ParametersDefinition? = null,
): T? {
    val params: ParametersHolder? = parameters?.invoke()
    return remember(qualifier, scope, params) {
        runCatching {
            scope.getOrNull<T>(T::class, qualifier, parameters)
        }.getOrElse {
            it.printStackTrace()
            null
        }
    }
}

@Composable
private fun UnlinkSlot(
    modifier: Modifier = Modifier,
    key: String
) {
    Box(
        modifier = modifier
    ) {
        Card(
            modifier = Modifier.padding(16.dp),
            shape = RoundedCornerShape(8.dp),
            elevation = 0.dp,
            backgroundColor = MaterialTheme.colors.onBackground.copy(0.1f)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    modifier = Modifier,
                    text = "Unsupported content, Please upgrade to latest version.",
                    style = MaterialTheme.typography.subtitle1,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colors.onBackground
                )
                Text(
                    modifier = Modifier,
                    text = "UnlinkSlot: $key",
                    style = MaterialTheme.typography.subtitle2,
                    color = MaterialTheme.colors.onBackground.copy(0.7f)
                )
            }
        }
    }
}