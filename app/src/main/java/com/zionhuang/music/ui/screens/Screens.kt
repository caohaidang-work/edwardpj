package com.zionhuang.music.ui.screens

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.zionhuang.music.R

sealed class Screens(
    val route: String,
    @StringRes val titleId: Int = 0,
    @DrawableRes val iconId: Int = 0
) {
    // 3 Tab chính hiển thị trên thanh Bottom Navigation
    object Home : Screens("home", R.string.home, R.drawable.home)
    object Library : Screens("library", R.string.library, R.drawable.library_music)
    object Account : Screens("account", R.string.account, R.drawable.person)

    // Màn hình đăng nhập (không có icon ở Bottom Bar)
    object GoogleLogin : Screens("google_login")

    // Giữ lại các biến cũ để MainActivity và cấu hình cài đặt không bị lỗi Unresolved Reference
    object Songs : Screens("songs")
    object Artists : Screens("artists")
    object Albums : Screens("albums")
    object Playlists : Screens("playlists")

    companion object {
        // Danh sách gộp 3 tab
        val MainScreens = listOf(Home, Library, Account)
    }
}