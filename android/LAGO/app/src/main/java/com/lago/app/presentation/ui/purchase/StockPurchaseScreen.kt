package com.lago.app.presentation.ui.purchase

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lago.app.presentation.theme.*
import com.lago.app.presentation.viewmodel.purchase.PurchaseViewModel
import kotlin.math.abs

@Composable
fun StockPurchaseScreen(
    stockCode: String,
    action: String = "buy", // "buy" or "sell"
    accountType: Int = 0, // 0=실시간모의투자, 1=역사챌린지, 2=자동매매봇
    viewModel: PurchaseViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {},
    onTransactionComplete: () -> Unit = {}
) {
    val isPurchaseType = action == "buy"
    val uiState by viewModel.uiState.collectAsState()
    var showConfirmDialog by remember { mutableStateOf(false) }
    
    // 고정된 앱바 높이와 버튼 높이 설정
    val appBarHeight = 60.dp
    val buttonHeight = 56.dp

    LaunchedEffect(stockCode, isPurchaseType, accountType) {
        android.util.Log.d("StockPurchaseScreen", "🔄 LaunchedEffect 실행: stockCode=$stockCode, isPurchaseType=$isPurchaseType, accountType=$accountType")
        viewModel.loadStockInfo(stockCode, isPurchaseType, accountType)
    }

    // UI State 변화 로깅
    LaunchedEffect(uiState) {
        android.util.Log.d("StockPurchaseScreen", "📊 UI State 업데이트: " +
                "stockName=${uiState.stockName}, " +
                "currentPrice=${uiState.currentPrice}, " +
                "accountBalance=${uiState.accountBalance}, " +
                "holdingQuantity=${uiState.holdingQuantity}, " +
                "isLoading=${uiState.isLoading}, " +
                "errorMessage=${uiState.errorMessage}"
        )
    }

    // 계산: 현재 가격 기준으로 몇 주인지
    val currentPrice = uiState.currentPrice.takeIf { it > 0 } ?: 1 // 0 방지
    val shareCount = if (uiState.purchaseAmount > 0) uiState.purchaseAmount / currentPrice else 0L

    Scaffold(
        topBar = {
            PurchaseTopBar(
                stockName = uiState.stockName,
                transactionType = if (isPurchaseType) "구매" else "판매",
                onBackClick = onNavigateBack,
                height = appBarHeight
            )
        },
        containerColor = Color.White,
        bottomBar = {
            // 고정된 구매/판매 버튼 - 시스템 네비게이션 바 위에 위치
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .windowInsetsPadding(WindowInsets.navigationBars) // 시스템 네비게이션 바 패딩
                    .padding(
                        horizontal = 16.dp,
                        vertical = 16.dp
                    )
            ) {
                Button(
                    onClick = { showConfirmDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(buttonHeight),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isPurchaseType) MainPink else MainBlue
                    ),
                    shape = RoundedCornerShape(8.dp),
                    enabled = true
                ) {
                    Text(
                        if (isPurchaseType) "구매하기" else "판매하기",
                        style = TitleB16,
                        color = Color.White
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .windowInsetsPadding(WindowInsets.statusBars) // 상태표시줄 패딩 추가
                .background(Color.White)
        ) {
            // 메인 컨텐츠 영역 - 스크롤 제거하고 완전 고정 레이아웃
            Column(
                modifier = Modifier
                    .weight(1f) // 남은 공간을 모두 차지
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // 📍 고정 영역: 종목 정보 카드
                StockInfoCard(
                    stockName = uiState.stockName,
                    currentPrice = uiState.currentPrice,
                    holdingInfo = uiState.holdingInfo,
                    isPurchaseType = isPurchaseType,
                    holdingQuantity = uiState.holdingQuantity,
                    accountBalance = uiState.accountBalance
                )

                // 🎯 가변 영역: 중간 여백
                Spacer(modifier = Modifier.weight(0.3f)) // 🔥 가변 영역

                // 📍 고정 영역: 거래 입력 영역 (하단에 밀착)
                TransactionShareInput(
                    shareCount = shareCount,
                    percentage = uiState.percentage,
                    onShareChange = { newShareCount ->
                        val newAmount = newShareCount * currentPrice
                        viewModel.onAmountChange(newAmount)
                    },
                    isPurchaseType = isPurchaseType,
                    holdingQuantity = uiState.holdingQuantity,
                    currentPrice = currentPrice,
                    accountBalance = uiState.accountBalance
                )

                Spacer(modifier = Modifier.height(16.dp)) // 하단 최소 여백
            }
        }
    }

    if (showConfirmDialog) {
        TransactionConfirmDialog(
            stockName = uiState.stockName,
            quantity = shareCount.toInt(),
            totalPrice = shareCount * currentPrice,
            currentPrice = currentPrice,
            isPurchaseType = isPurchaseType,
            onConfirm = {
                viewModel.executeTrade()
                showConfirmDialog = false
                onTransactionComplete()
            },
            onDismiss = { showConfirmDialog = false }
        )
    }
}

@Composable
private fun PurchaseTopBar(
    stockName: String,
    transactionType: String,
    onBackClick: () -> Unit,
    height: androidx.compose.ui.unit.Dp = 60.dp
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(height) // 고정된 높이
            .background(Color.White)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBackClick) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "뒤로가기",
                tint = Gray900
            )
        }

        Text(
            text = if (stockName.isNotBlank()) "$stockName $transactionType" else transactionType,
            style = TitleB18,
            color = Gray900,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center
        )

        Box(modifier = Modifier.size(48.dp)) // 균형 맞추기
    }
}

@Composable
private fun StockInfoCard(
    stockName: String,
    currentPrice: Int,
    holdingInfo: String?,
    isPurchaseType: Boolean,
    holdingQuantity: Int,
    accountBalance: Long = 0L
) {
    // 컴포넌트 렌더링 시 값 로깅
    LaunchedEffect(stockName, currentPrice, accountBalance, holdingQuantity) {
        android.util.Log.d("StockInfoCard", "🎨 StockInfoCard 렌더링: " +
                "stockName='$stockName', " +
                "currentPrice=$currentPrice, " +
                "accountBalance=$accountBalance, " +
                "holdingQuantity=$holdingQuantity, " +
                "isPurchaseType=$isPurchaseType"
        )
    }
    
    Column {
        if (isPurchaseType) {
            Text(
                text = "보유 현금 : ${String.format("%,d", accountBalance)}원",
                style = BodyR14,
                color = Gray600,
                modifier = Modifier.padding(top = 8.dp)
            )
        } else {
            Text(
                text = "보유 주식 : ${holdingQuantity}주 (${String.format("%,d", holdingQuantity * currentPrice)}원)",
                style = BodyR14,
                color = Gray600,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
private fun TransactionShareInput(
    shareCount: Long,
    percentage: Float,
    onShareChange: (Long) -> Unit,
    isPurchaseType: Boolean,
    holdingQuantity: Int,
    currentPrice: Int,
    accountBalance: Long = 0L
) {
    val pricePerShare = currentPrice.toLong()

    val availableCash = accountBalance
    val maxShares = if (isPurchaseType) {
        if (pricePerShare > 0) {
            (availableCash / pricePerShare).coerceAtLeast(1L)
        } else 0L
    } else {
        holdingQuantity.toLong().coerceAtLeast(0L)
    }

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // 🎯 가변 영역 1: 대표 영역 (몇 주 / 금액) - 위쪽 여백 흡수
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f), // 🔥 가변 영역: 화면 크기에 따라 늘어남
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "${shareCount}주",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = Gray900
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${String.format("%,d", shareCount * pricePerShare)}원",
                style = BodyR14,
                color = Gray700
            )
        }

        // 🎯 가변 영역 2: 중간 여백 - 중간 여백 흡수
        Spacer(modifier = Modifier.weight(0.5f)) // 🔥 가변 영역

        // 📍 고정 영역 시작: 최대/보유 & 단가
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp), // 🔧 고정 간격
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (isPurchaseType) {
                Text(
                    text = "최대 ${maxShares}주",
                    style = BodyR14,
                    color = Gray600
                )
            } else {
                Text(
                    text = "보유 ${holdingQuantity}주",
                    style = BodyR14,
                    color = Gray600
                )
            }

            Text(
                text = "1주당 ${String.format("%,d", currentPrice)}원",
                style = BodyR14,
                color = Gray600
            )
        }

        // 📍 고정 영역: 퍼센트 버튼
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp), // 🔧 고정 간격
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            listOf(10f, 25f, 50f, 100f).forEach { pct ->
                FilterChip(
                    selected = abs(percentage - pct) < 1f,
                    onClick = {
                        val targetShares = if (isPurchaseType) {
                            val targetAmount = (availableCash * pct / 100).toLong()
                            if (pricePerShare > 0) targetAmount / pricePerShare else 0L
                        } else {
                            (holdingQuantity * pct / 100).toLong()
                        }
                        onShareChange(targetShares.coerceAtMost(maxShares))
                    },
                    label = {
                        Text("${pct.toInt()}%", style = SubtitleSb14)
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Gray200,
                        selectedLabelColor = Gray900
                    )
                )
            }
        }

        // 📍 고정 영역: 키패드 (구매하기 버튼과 붙어있게)
        NumberKeypad(
            currentValue = shareCount.toString(),
            onValueChange = { newSharesString ->
                val parsed = newSharesString.toLongOrNull() ?: 0L
                onShareChange(parsed.coerceAtMost(maxShares))
            }
        )
    }
}

@Composable
private fun NumberKeypad(
    currentValue: String,
    onValueChange: (String) -> Unit
) {
    val keys = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf("C", "0", "⌫")
    )

    // 고정된 키패드 설정
    val keypadButtonHeight = 52.dp
    val keypadSpacing = 6.dp

    Column(
        verticalArrangement = Arrangement.spacedBy(keypadSpacing)
    ) {
        keys.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                row.forEach { key ->
                    Button(
                        onClick = {
                            when (key) {
                                "C" -> onValueChange("0")
                                "⌫" -> {
                                    if (currentValue.length > 1) {
                                        onValueChange(currentValue.dropLast(1))
                                    } else {
                                        onValueChange("0")
                                    }
                                }
                                else -> {
                                    if (currentValue == "0") {
                                        onValueChange(key)
                                    } else {
                                        onValueChange(currentValue + key)
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(keypadButtonHeight),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = when (key) {
                                "C" -> Gray400
                                "⌫" -> MainBlue
                                else -> Gray100
                            }
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = key,
                            style = TitleB18,
                            color = if (key == "C" || key == "⌫") Color.White else Gray900
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TransactionConfirmDialog(
    stockName: String,
    quantity: Int,
    totalPrice: Long,
    currentPrice: Int,
    isPurchaseType: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = Gray900
                )
            ) {
                Text(if (isPurchaseType) "구매" else "판매", style = TitleB16)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = Gray600
                )
            ) {
                Text("취소", style = TitleB16)
            }
        },
        text = {
            Column(
                modifier = Modifier.padding(0.dp)
            ) {
                // 제목
                Text(
                    text = "$stockName ${if (isPurchaseType) "구매" else "판매"} 확인",
                    style = TitleB18,
                    color = Gray900,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                // 내용
                Text(
                    text = "가격 : ${String.format("%,d", currentPrice)} 원",
                    style = BodyR14,
                    color = Gray700,
                    modifier = Modifier.padding(start = 8.dp)
                )
                Text(
                    text = "수량 : $quantity 주",
                    style = BodyR14,
                    color = Gray700,
                    modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                )
                Text(
                    text = "총 금액 : ${String.format("%,d", totalPrice)} 원",
                    style = SubtitleSb16,
                    color = Gray900,
                    modifier = Modifier.padding(start = 8.dp, top = 8.dp)
                )
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 0.dp
    )
}