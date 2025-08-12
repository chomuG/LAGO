package com.lago.app.presentation.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.lago.app.presentation.theme.*
import com.lago.app.presentation.ui.widget.LagoCard

data class SettingsItem(
    val title: String,
    val subtitle: String? = null,
    val icon: ImageVector,
    val action: SettingsAction
)

sealed class SettingsAction {
    object Notifications : SettingsAction()
    object Theme : SettingsAction()
    object Language : SettingsAction()
    object Privacy : SettingsAction()
    object About : SettingsAction()
    object Logout : SettingsAction()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit = {}
) {
    var showLogoutDialog by remember { mutableStateOf(false) }
    
    val settingsItems = remember {
        listOf(
            SettingsItem(
                title = "알림 설정",
                subtitle = "가격 알림, 뉴스 알림 등을 관리하세요",
                icon = Icons.Default.Notifications,
                action = SettingsAction.Notifications
            ),
            SettingsItem(
                title = "테마 설정", 
                subtitle = "라이트 모드, 다크 모드",
                icon = Icons.Default.Settings,
                action = SettingsAction.Theme
            ),
            SettingsItem(
                title = "언어 설정",
                subtitle = "한국어",
                icon = Icons.Default.Edit,
                action = SettingsAction.Language
            ),
            SettingsItem(
                title = "개인정보 보호",
                subtitle = "데이터 사용 및 개인정보 설정",
                icon = Icons.Default.Lock,
                action = SettingsAction.Privacy
            ),
            SettingsItem(
                title = "앱 정보",
                subtitle = "버전 1.0.0",
                icon = Icons.Default.Info,
                action = SettingsAction.About
            ),
            SettingsItem(
                title = "로그아웃",
                subtitle = null,
                icon = Icons.Default.Clear,
                action = SettingsAction.Logout
            )
        )
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "설정",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    ) 
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.semantics {
                            contentDescription = "뒤로가기"
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = null
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            items(settingsItems.size) { index ->
                val item = settingsItems[index]
                SettingsItemCard(
                    item = item,
                    onClick = {
                        when (item.action) {
                            SettingsAction.Logout -> showLogoutDialog = true
                            else -> {
                                // TODO: Handle other settings actions
                            }
                        }
                    }
                )
            }
        }
    }
    
    // Logout confirmation dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = {
                Text("로그아웃")
            },
            text = {
                Text("정말 로그아웃하시겠습니까?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        // TODO: Handle logout
                    }
                ) {
                    Text("로그아웃", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showLogoutDialog = false }
                ) {
                    Text("취소")
                }
            }
        )
    }
}

@Composable
private fun SettingsItemCard(
    item: SettingsItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    LagoCard(
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                contentDescription = "${item.title}${item.subtitle?.let { ", $it" } ?: ""}"
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = null,
                tint = when (item.action) {
                    SettingsAction.Logout -> Color.Red
                    else -> MainBlue
                },
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = when (item.action) {
                        SettingsAction.Logout -> Color.Red
                        else -> Gray900
                    }
                )
                
                item.subtitle?.let { subtitle ->
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Gray600
                    )
                }
            }
            
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                tint = Gray400,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}