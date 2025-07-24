package com.example.lazycolumnsnapsback.ui.theme

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * 设置状态栏、导航栏图标颜色的可复用组件
 *
 * @param isLight 是否使用浅色图标，true 表示浅色(白色)图标，false 表示深色图标
 */
@Composable
fun SystemBarIconColor(isLight: Boolean) {
    val context = LocalContext.current
    val activity = context as ComponentActivity
    val window = activity.window
    val view = LocalView.current
    val insetsController = WindowCompat.getInsetsController(window, view)

    DisposableEffect(isLight) {
        // 设置状态栏图标颜色
        insetsController.isAppearanceLightStatusBars = !isLight
        insetsController.isAppearanceLightNavigationBars = !isLight

        onDispose {}
    }
}