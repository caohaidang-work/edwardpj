package com.zionhuang.music.ui.screens.library

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.zionhuang.music.LocalDatabase
import com.zionhuang.music.LocalPlayerAwareWindowInsets
import com.zionhuang.music.R
import com.zionhuang.music.constants.ArtistSortType
import com.zionhuang.music.constants.PlaylistSortType
import com.zionhuang.music.constants.SongSortType
import com.zionhuang.music.db.entities.PlaylistEntity
import com.zionhuang.music.ui.component.ArtistGridItem
import com.zionhuang.music.ui.component.YouTubeGridItem
import com.zionhuang.music.ui.screens.Screens
import com.zionhuang.music.viewmodels.HomeViewModel

@Composable
fun UnifiedLibraryScreen(
    navController: NavController,
    homeViewModel: HomeViewModel = hiltViewModel()
) {
    val database = LocalDatabase.current
    val coroutineScope = rememberCoroutineScope()

    // Lấy dữ liệu Local Database
    val localPlaylists by database.playlists(PlaylistSortType.CREATE_DATE, true).collectAsState(initial = emptyList())
    val localSongs by database.songs(SongSortType.CREATE_DATE, true).collectAsState(initial = emptyList())
    val artists by database.artists(ArtistSortType.CREATE_DATE, true).collectAsState(initial = emptyList())

    // Lấy dữ liệu YouTube Music
    val accountPlaylists by homeViewModel.accountPlaylists.collectAsState()

    // Lọc riêng playlist "Nhạc đã thích" của YouTube (mặc định ID là "LM")
    val ytLikedPlaylist = accountPlaylists?.find { it.id == "LM" }
    val ytRegularPlaylists = accountPlaylists?.filter { it.id != "LM" } ?: emptyList()

    var showCreatePlaylistDialog by remember { mutableStateOf(false) }
    val contentPadding = LocalPlayerAwareWindowInsets.current.asPaddingValues()

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = contentPadding
        ) {

            // ========================================================
            // PHẦN 1: TẤT CẢ PLAYLIST & BÀI HÁT YÊU THÍCH
            // ========================================================
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SectionHeader("Thư viện của bạn")

                    // Nút Tạo Playlist
                    FilledTonalButton(
                        onClick = { showCreatePlaylistDialog = true },
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Icon(painterResource(R.drawable.add), contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Tạo mới")
                    }
                }
            }

            item {
                LazyRow(contentPadding = PaddingValues(horizontal = 16.dp)) {

                    // 1. THẺ CỐ ĐỊNH: Bài hát yêu thích (Tự động cập nhật khi thả tim)
                    item {
                        CustomPlaylistCard(
                            title = "Bài hát\nyêu thích",
                            subtitle = "${localSongs.size} bài",
                            iconId = R.drawable.favorite,
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            // Mở trang danh sách bài hát mặc định của app khi bấm vào
                            onClick = { navController.navigate(Screens.Songs.route) }
                        )
                    }

                    // 2. THẺ CỐ ĐỊNH: Nhạc đã thích từ YouTube (Nếu có)
                    if (ytLikedPlaylist != null) {
                        item {
                            CustomPlaylistCard(
                                title = "Nhạc đã thích",
                                subtitle = "YouTube",
                                iconId = R.drawable.favorite_border,
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                onClick = { navController.navigate("online_playlist/${ytLikedPlaylist.id}") }
                            )
                        }
                    }

                    // 3. Các Playlist tự tạo trên điện thoại
                    items(localPlaylists) { playlistInfo ->
                        CustomPlaylistCard(
                            title = playlistInfo.playlist.name,
                            subtitle = "Cá nhân",
                            iconId = R.drawable.queue_music,
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            onClick = { navController.navigate("local_playlist/${playlistInfo.playlist.id}") }
                        )
                    }

                    // 4. Các Playlist khác từ tài khoản YouTube
                    items(ytRegularPlaylists) { ytItem ->
                        YouTubeGridItem(
                            item = ytItem,
                            isActive = false,
                            isPlaying = false,
                            coroutineScope = coroutineScope,
                            modifier = Modifier
                                .padding(end = 12.dp)
                                .clickable { navController.navigate("online_playlist/${ytItem.id}") }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // ========================================================
            // PHẦN 2: NGHỆ SĨ ĐÃ LƯU
            // ========================================================
            if (artists.isNotEmpty()) {
                item { SectionHeader("Nghệ sĩ") }
                item {
                    LazyRow(contentPadding = PaddingValues(horizontal = 16.dp)) {
                        items(artists) { artist ->
                            ArtistGridItem(
                                artist = artist,
                                modifier = Modifier
                                    .width(120.dp)
                                    .clickable { navController.navigate("artist/${artist.id}") }
                            )
                        }
                    }
                }
            }
        }

        // ========================================================
        // HỘP THOẠI TẠO PLAYLIST
        // ========================================================
        if (showCreatePlaylistDialog) {
            var playlistName by remember { mutableStateOf("") }

            AlertDialog(
                onDismissRequest = { showCreatePlaylistDialog = false },
                title = { Text("Tạo Playlist mới") },
                text = {
                    OutlinedTextField(
                        value = playlistName,
                        onValueChange = { playlistName = it },
                        label = { Text("Tên playlist") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (playlistName.isNotBlank()) {
                                database.query {
                                    insert(PlaylistEntity(name = playlistName))
                                }
                                showCreatePlaylistDialog = false
                            }
                        },
                        enabled = playlistName.isNotBlank()
                    ) {
                        Text("Tạo")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCreatePlaylistDialog = false }) {
                        Text("Hủy")
                    }
                }
            )
        }
    }
}

// Widget tiêu đề chung
@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

// Widget thiết kế Thẻ Playlist vuông vức đồng bộ
@Composable
fun CustomPlaylistCard(
    title: String,
    subtitle: String,
    iconId: Int,
    containerColor: androidx.compose.ui.graphics.Color,
    contentColor: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .padding(end = 12.dp)
            .size(120.dp) // Kích thước bằng với ảnh bìa YouTube
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(8.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = painterResource(iconId),
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(32.dp)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = contentColor,
                maxLines = 2,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = contentColor.copy(alpha = 0.8f)
            )
        }
    }
}