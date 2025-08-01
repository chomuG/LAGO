package com.lago.app.presentation.ui.purchase

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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

@Composable
fun StockPurchaseScreen(
    stockCode: String,
    isPurchaseType: Boolean = true, // true: 구매, false: 판매
    viewModel: PurchaseViewModel = hiltViewModel(),
    onBackClick: () -> Unit,
    onTransactionComplete: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showConfirmDialog by remember { mutableStateOf(false) }

    LaunchedEffect(stockCode, isPurchaseType) {
        viewModel.loadStockInfo(stockCode, isPurchaseType)
    }

    Scaffold(
        topBar = {
            PurchaseTopBar(
                stockName = uiState.stockName,
                transactionType = if (isPurchaseType) "구매" else "판매",
                onBackClick = onBackClick
            )
        },
        containerColor = Color.White
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // 종목 정보
            StockInfoCard(
                stockName = uiState.stockName,
                currentPrice = uiState.currentPrice,
                holdingInfo = uiState.holdingInfo,
                isPurchaseType = isPurchaseType,
                holdingQuantity = uiState.holdingQuantity
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 구매/판매 수량 입력
            TransactionAmountInput(
                amount = uiState.purchaseAmount,
                totalPrice = uiState.totalPrice,
                percentage = uiState.percentage,
                onAmountChange = { viewModel.onAmountChange(it) },
                onPercentageClick = { viewModel.onPercentageClick(it) },
                isPurchaseType = isPurchaseType,
                holdingQuantity = uiState.holdingQuantity,
                currentPrice = uiState.currentPrice
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 구매/판매하기 버튼
            Button(
                onClick = { showConfirmDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isPurchaseType) MainPink else MainBlue
                ),
                shape = RoundedCornerShape(8.dp),
                enabled = true // 항상 활성화
            ) {
                Text(
                    if (isPurchaseType) "구매하기" else "판매하기",
                    style = TitleB16,
                    color = Color.White
                )
            }
            
            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    // 거래 확인 다이얼로그
    if (showConfirmDialog) {
        TransactionConfirmDialog(
            stockName = uiState.stockName,
            quantity = uiState.purchaseQuantity,
            totalPrice = uiState.totalPrice,
            currentPrice = uiState.currentPrice,
            isPurchaseType = isPurchaseType,
            onConfirm = {
                viewModel.purchaseStock()
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
    onBackClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(vertical = 16.dp, horizontal = 16.dp),
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
            text = "$stockName $transactionType",
            style = TitleB18,
            color = Gray900,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center
        )

        // 뒤로가기 버튼과 동일한 크기의 여백
        Box(modifier = Modifier.size(48.dp))
    }
}

@Composable
private fun StockInfoCard(
    stockName: String,
    currentPrice: Int,
    holdingInfo: String?,
    isPurchaseType: Boolean,
    holdingQuantity: Int
) {
    Column {
        Text(
            text = stockName,
            style = TitleB20,
            color = Gray900
        )

        Text(
            text = "1주 = ${String.format("%,d", currentPrice)}원",
            style = BodyR14,
            color = Gray700,
            modifier = Modifier.padding(top = 4.dp)
        )

        if (isPurchaseType) {
            // 구매시 보유현금을 원화로 표시
            Text(
                text = "보유 현금 : ${String.format("%,d", 2000000)}원",
                style = BodyR14,
                color = Gray600,
                modifier = Modifier.padding(top = 8.dp)
            )
        } else {
            // 판매시 보유주식을 주 단위와 원화로 표시
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
private fun TransactionAmountInput(
    amount: Long,
    totalPrice: Long,
    percentage: Float,
    onAmountChange: (Long) -> Unit,
    onPercentageClick: (Float) -> Unit,
    isPurchaseType: Boolean,
    holdingQuantity: Int,
    currentPrice: Int
) {
    val pricePerShare = currentPrice.toLong()
    val shares = if (amount > 0 && pricePerShare > 0) (amount / pricePerShare) else 0L
    
    // 구매시: 보유현금으로 살 수 있는 최대 주 수, 판매시: 보유 주식 수
    val availableCash = 2000000L // 보유현금 200만원
    val maxShares = if (isPurchaseType) {
        if (pricePerShare > 0) {
            (availableCash / pricePerShare).coerceAtLeast(1L) // 최소 1주는 보장
        } else {
            0L // 가격이 0이면 0주
        }
    } else {
        holdingQuantity.toLong().coerceAtLeast(0L) // 최소 0주
    }
    
    Column {
        // 금액 표시 (위쪽)
        Text(
            text = String.format("%,d", amount),
            style = MaterialTheme.typography.headlineLarge.copy(
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold
            ),
            color = Gray900,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 슬라이더와 주 수 표시
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 슬라이더
            Slider(
                value = shares.toFloat(),
                onValueChange = { newShares ->
                    val newAmount = if (pricePerShare > 0) (newShares.toLong() * pricePerShare) else 0L
                    onAmountChange(newAmount)
                },
                valueRange = 0f..maxShares.coerceAtLeast(1L).toFloat(),
                steps = (maxShares.coerceAtLeast(1L).toInt() - 1).coerceAtLeast(0),
                colors = SliderDefaults.colors(
                    thumbColor = MainBlue,
                    activeTrackColor = MainBlue,
                    inactiveTrackColor = Gray300
                ),
                modifier = Modifier.weight(1f)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // 주 수 표시
            Card(
                modifier = Modifier.padding(start = 8.dp),
                colors = CardDefaults.cardColors(containerColor = Gray100),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "${shares}주",
                    style = TitleB16,
                    color = Gray900,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 퍼센트 버튼들 (구매시: 보유현금 기준, 판매시: 보유주식 기준)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            listOf(10f, 25f, 50f, 100f).forEach { percent ->
                FilterChip(
                    selected = kotlin.math.abs(percentage - percent) < 1f,
                    onClick = { 
                        val targetShares = if (pricePerShare <= 0) {
                            0L // 가격이 0이면 0주
                        } else if (isPurchaseType) {
                            // 구매시: 보유현금의 퍼센트로 계산하고 소수점 버림
                            val availableCash = 2000000L // 임시로 200만원으로 설정
                            val targetAmount = (availableCash * percent / 100).toLong()
                            targetAmount / pricePerShare
                        } else {
                            // 판매시: 보유주식의 퍼센트로 계산하고 소수점 버림
                            (holdingQuantity * percent / 100).toLong()
                        }
                        val cappedShares = targetShares.coerceAtMost(maxShares)
                        val newAmount = cappedShares * pricePerShare
                        onAmountChange(newAmount)
                    },
                    label = {
                        Text(
                            "${percent.toInt()}%",
                            style = SubtitleSb14
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Gray200,
                        selectedLabelColor = Gray900
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 주 수 입력 키패드
        NumberKeypad(
            currentValue = shares.toString(),
            onValueChange = { newSharesString ->
                val newShares = newSharesString.toLongOrNull() ?: 0L
                val cappedShares = newShares.coerceAtMost(maxShares)
                val newAmount = if (pricePerShare > 0) cappedShares * pricePerShare else 0L
                onAmountChange(newAmount)
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

    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp)
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
                            .height(48.dp),
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
        title = {
            Text(
                text = "$stockName ${if (isPurchaseType) "구매" else "판매"} 확인",
                style = TitleB18,
                color = Gray900
            )
        },
        text = {
            Column {
                Text(
                    text = "가격 : ${String.format("%,d", currentPrice)} 원",
                    style = BodyR14,
                    color = Gray700
                )
                Text(
                    text = "수량 : $quantity 주",
                    style = BodyR14,
                    color = Gray700,
                    modifier = Modifier.padding(top = 4.dp)
                )
                Text(
                    text = "총 금액 : ${String.format("%,d", totalPrice)} 원",
                    style = SubtitleSb16,
                    color = Gray900,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        },
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
        containerColor = Color.White,
        shape = RoundedCornerShape(16.dp)
    )
}