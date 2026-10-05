package com.zionhuang.music.models

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class GoogleAuthViewModel : ViewModel() {
    // Biến lưu trữ trạng thái người dùng
    private val _userState = MutableStateFlow<GoogleSignInAccount?>(null)
    val userState: StateFlow<GoogleSignInAccount?> = _userState.asStateFlow()

    // Khởi tạo Client để gọi Google Sign In
    fun getSignInClient(context: Context): GoogleSignInClient {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestProfile()
            // Nếu bạn cần lấy token để gọi API YouTube, hãy thêm requestServerAuthCode(YOUR_WEB_CLIENT_ID)
            .build()
        return GoogleSignIn.getClient(context, gso)
    }

    // Xử lý kết quả trả về từ màn hình đăng nhập
    fun handleSignInResult(data: Intent?) {
        try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val account = task.getResult(ApiException::class.java)
            _userState.value = account
        } catch (e: ApiException) {
            e.printStackTrace()
            _userState.value = null
        }
    }

    // Xử lý đăng xuất
    fun signOut(context: Context) {
        getSignInClient(context).signOut().addOnCompleteListener {
            _userState.value = null
        }
    }
}