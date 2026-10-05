package com.zionhuang.music.ui.screens

import android.annotation.SuppressLint
import android.webkit.CookieManager
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.zionhuang.innertube.YouTube
import com.zionhuang.music.constants.InnerTubeCookieKey
import com.zionhuang.music.utils.rememberPreference
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

@SuppressLint("SetJavaScriptEnabled")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoogleLoginScreen(navController: NavController) {
    // Lấy state của cookie từ DataStore
    var innerTubeCookie by rememberPreference(InnerTubeCookieKey, "")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Đăng nhập Google") }
            )
        }
    ) { innerPadding ->
        AndroidView(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            factory = { context ->
                WebView(context).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true

                    webViewClient = object : WebViewClient() {
                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)

                            // Lấy toàn bộ cookie từ domain youtube.com
                            val cookieManager = CookieManager.getInstance()
                            val cookies = cookieManager.getCookie("https://youtube.com")

                            // YouTube Music xác thực thành công khi có cookie SAPISID
                            if (cookies != null && cookies.contains("SAPISID")) {
                                // Lưu vào DataStore
                                innerTubeCookie = cookies
                                // Cập nhật cookie cho client InnerTube
                                YouTube.cookie = cookies

                                // Đăng nhập xong thì quay lại màn hình trước
                                navController.popBackStack()
                            }
                        }
                    }

                    // URL đăng nhập trỏ thẳng tới YouTube Music
                    val loginUrl = "https://accounts.google.com/ServiceLogin?service=youtube&continue=https://music.youtube.com/"
                    loadUrl(loginUrl)
                }
            }
        )
    }
}