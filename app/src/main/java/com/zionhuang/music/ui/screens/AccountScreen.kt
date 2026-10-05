package com.zionhuang.music.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.zionhuang.innertube.YouTube
import com.zionhuang.innertube.utils.parseCookieString
import com.zionhuang.music.R
import com.zionhuang.music.constants.InnerTubeCookieKey
import com.zionhuang.music.utils.rememberPreference

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountScreen(navController: NavController, scrollBehavior: TopAppBarScrollBehavior) {
    var innerTubeCookie by rememberPreference(InnerTubeCookieKey, "")
    val isLoggedIn = remember(innerTubeCookie) { "SAPISID" in parseCookieString(innerTubeCookie) }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (isLoggedIn) {
            // Đã đăng nhập
            Icon(
                painter = painterResource(R.drawable.person), // Tạm dùng icon, nếu có avatar lấy từ YouTube API thì dùng AsyncImage
                contentDescription = "Avatar",
                modifier = Modifier.size(100.dp).clip(CircleShape),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text("Đã kết nối với YouTube Music", style = MaterialTheme.typography.titleMedium)

            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = {
                    innerTubeCookie = "" // Đăng xuất: xóa cookie
                    YouTube.cookie = null
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Đăng xuất")
            }
        } else {
            // Chưa đăng nhập
            Icon(
                painter = painterResource(R.drawable.person),
                contentDescription = null,
                modifier = Modifier.size(100.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text("Bạn chưa đăng nhập", style = MaterialTheme.typography.titleMedium)
            Text("Đăng nhập để đồng bộ dữ liệu YouTube Music", style = MaterialTheme.typography.bodyMedium)

            Spacer(modifier = Modifier.height(32.dp))
            Button(onClick = { navController.navigate(Screens.GoogleLogin.route) }) {
                Text("Đăng nhập")
            }
        }
    }
}