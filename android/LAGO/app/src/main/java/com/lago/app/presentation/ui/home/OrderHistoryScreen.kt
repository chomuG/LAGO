package com.lago.app.presentation.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.lago.app.R
import com.lago.app.presentation.theme.*

data class OrderHistoryItem(
    val date: String,
    val stockName: String,
    val orderType: String, // "매수", "매도"
    val shares: Int,
    val amount: String
)

enum class OrderType(val displayName: String) {
    ALL("전체"),
    BUY("매수"),
    SELL("매도")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderHistoryScreen(
    onBackClick: () -> Unit = {}
) {
    var selectedDate by remember { mutableStateOf("2025년 8월") }
    var selectedOrderType by remember { mutableStateOf(OrderType.ALL) }
    var isDateDropdownExpanded by remember { mutableStateOf(false) }
    var isOrderTypeDropdownExpanded by remember { mutableStateOf(false) }
    
    val orderHistory = listOf(
        OrderHistoryItem("8.05", "삼성전자", "매수", 5, "주문 72,500원"),
        OrderHistoryItem("", "삼성전자", "구매", 2, "주문 71,900원"),
        OrderHistoryItem("", "삼성전자", "구매", 7, "주문 71,800원"),
        OrderHistoryItem("8.06", "삼성전자", "구매", 33, "주문 69,500원"),
        OrderHistoryItem("", "삼성전자", "구매", 7, "주문 71,800원"),
        OrderHistoryItem("", "삼성전자", "구매", 7, "주문 71,800원"),
        OrderHistoryItem("", "삼성전자", "구매", 7, "주문 71,800원"),
        OrderHistoryItem("", "삼성전자", "구매", 7, "주문 71,800원")
    )
    
    val dateOptions = listOf("2025년 8월", "2025년 7월", "2025년 6월")
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F7F7))
    ) {
        // Top Bar
        TopAppBar(
            title = {
                Text(
                    text = "나의 주문 내역",
                    style = HeadEb20,
                    color = Color.Black
                )
            },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_chevron_left),
                        contentDescription = "뒤로가기",
                        tint = Color.Black
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.White
            )
        )
        
        // Filter Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Date Filter
            Box {
                Card(
                    modifier = Modifier
                        .clickable { isDateDropdownExpanded = true },
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = selectedDate,
                            style = SubtitleSb14,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            painter = painterResource(id = R.drawable.ic_chevron_right),
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                
                DropdownMenu(
                    expanded = isDateDropdownExpanded,
                    onDismissRequest = { isDateDropdownExpanded = false }
                ) {
                    dateOptions.forEach { date ->
                        DropdownMenuItem(
                            text = { Text(date) },
                            onClick = {
                                selectedDate = date
                                isDateDropdownExpanded = false
                            }
                        )
                    }
                }
            }
            
            // Order Type Filter
            Box {
                Card(
                    modifier = Modifier
                        .clickable { isOrderTypeDropdownExpanded = true },
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = selectedOrderType.displayName,
                            style = SubtitleSb14,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            painter = painterResource(id = R.drawable.ic_chevron_right),
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                
                DropdownMenu(
                    expanded = isOrderTypeDropdownExpanded,
                    onDismissRequest = { isOrderTypeDropdownExpanded = false }
                ) {
                    OrderType.values().forEach { orderType ->
                        DropdownMenuItem(
                            text = { Text(orderType.displayName) },
                            onClick = {
                                selectedOrderType = orderType
                                isOrderTypeDropdownExpanded = false
                            }
                        )
                    }
                }
            }
        }
        
        // Order History List
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            items(orderHistory) { item ->
                OrderHistoryItemRow(item)
            }
            
            item {
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}

@Composable
private fun OrderHistoryItemRow(item: OrderHistoryItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                if (item.date.isNotEmpty()) {
                    Text(
                        text = item.date,
                        style = TitleB16,
                        color = Color.Black
                    )
                }
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.stockName,
                        style = SubtitleSb16,
                        color = Color.Black
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    // Order type badge
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = when (item.orderType) {
                                "매수" -> Color(0xFFE3F2FD)
                                else -> Color(0xFFE8F5E8)
                            }
                        ),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = item.orderType,
                            style = BodyR12,
                            color = when (item.orderType) {
                                "매수" -> MainBlue
                                else -> Color(0xFF4CAF50)
                            },
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
                
                Text(
                    text = "${item.shares}주 구매",
                    style = BodyR14,
                    color = Color.Gray
                )
            }
            
            Text(
                text = item.amount,
                style = SubtitleSb16,
                color = Color.Black
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun OrderHistoryScreenPreview() {
    LagoTheme {
        OrderHistoryScreen()
    }
}