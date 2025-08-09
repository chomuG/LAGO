package com.lago.app.presentation.ui.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lago.app.R
import com.lago.app.presentation.theme.LagoTheme
import com.lago.app.data.local.prefs.UserPreferences
import android.content.SharedPreferences

@Composable
fun LoginScreen(
    userPreferences: UserPreferences,
    onKakaoLoginClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.weight(1f))
        
        // 로고 이미지
        Image(
            painter = painterResource(id = R.drawable.login_text_image),
            contentDescription = "Login text",
            modifier = Modifier
                .width(230.dp)
                .padding(bottom = 30.dp)
        )
        Image(
            painter = painterResource(id = R.drawable.login_screen),
            contentDescription = "Login Image",
            modifier = Modifier
                .size(270.dp)
                .padding(bottom = 60.dp)
        )
        
        Spacer(modifier = Modifier.weight(1f))
        
        // 카카오 로그인 버튼
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clickable { onKakaoLoginClick() },
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFFEE500) // 카카오 노란색
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.kakao_login_button),
                    contentDescription = "Kakao Login",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(32.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    LagoTheme {
        val mockSharedPrefs = object : SharedPreferences {
            override fun getAll(): MutableMap<String, *> = mutableMapOf<String, Any>()
            override fun getString(key: String?, defValue: String?): String? = defValue
            override fun getStringSet(key: String?, defValues: MutableSet<String>?): MutableSet<String>? = defValues
            override fun getInt(key: String?, defValue: Int): Int = defValue
            override fun getLong(key: String?, defValue: Long): Long = defValue
            override fun getFloat(key: String?, defValue: Float): Float = defValue
            override fun getBoolean(key: String?, defValue: Boolean): Boolean = defValue
            override fun contains(key: String?): Boolean = false
            override fun edit(): SharedPreferences.Editor = object : SharedPreferences.Editor {
                override fun putString(key: String?, value: String?) = this
                override fun putStringSet(key: String?, values: MutableSet<String>?) = this
                override fun putInt(key: String?, value: Int) = this
                override fun putLong(key: String?, value: Long) = this
                override fun putFloat(key: String?, value: Float) = this
                override fun putBoolean(key: String?, value: Boolean) = this
                override fun remove(key: String?) = this
                override fun clear() = this
                override fun commit() = true
                override fun apply() {}
            }
            override fun registerOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {}
            override fun unregisterOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {}
        }
        LoginScreen(userPreferences = UserPreferences(mockSharedPrefs))
    }
}