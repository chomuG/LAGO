package com.lago.app.presentation.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.lago.app.R
import com.lago.app.presentation.theme.*
import com.lago.app.presentation.ui.components.CommonTopAppBar

data class OrderHistoryItem(
    val date: String,
    val month: String,
    val stockName: String,
    val orderType: String, // "매수", "매도"
    val shares: Int,
    val pricePerShare: Int
)

enum class OrderType(val displayName: String) {
    ALL("전체"),
    BUY("구매"),
    SELL("판매")
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
    
    val allOrderHistory = listOf(
        OrderHistoryItem("8.05", "2025년 8월", "삼성전자", "판매", 5, 72500),
        OrderHistoryItem("", "2025년 8월", "한화생명", "구매", 2, 275000),
        OrderHistoryItem("", "2025년 8월", "삼성전자", "판매", 3, 71800),
        OrderHistoryItem("8.06", "2025년 8월", "한화생명", "구매", 1, 273000),
        OrderHistoryItem("", "2025년 8월", "삼성전자", "구매", 10, 82000),
        OrderHistoryItem("", "2025년 8월", "한화생명", "판매", 2, 274500),
        OrderHistoryItem("", "2025년 8월", "삼성전자", "구매", 5, 81500),
        OrderHistoryItem("", "2025년 8월", "삼성전자", "판매", 8, 82200),
        OrderHistoryItem("7.28", "2025년 7월", "삼성전자", "구매", 15, 74000),
        OrderHistoryItem("", "2025년 7월", "한화생명", "판매", 3, 268000),
        OrderHistoryItem("", "2025년 7월", "삼성전자", "구매", 7, 73500),
        OrderHistoryItem("7.30", "2025년 7월", "한화생명", "구매", 2, 270000)
    )
    
    val filteredHistory = allOrderHistory.filter { item ->
        val dateMatches = item.month == selectedDate
        val orderTypeMatches = when (selectedOrderType) {
            OrderType.ALL -> true
            OrderType.BUY -> item.orderType == "구매"
            OrderType.SELL -> item.orderType == "판매"
        }
        dateMatches && orderTypeMatches
    }
    
    val dateOptions = listOf("2025년 8월", "2025년 7월", "2025년 6월")
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F7F7))
    ) {
        CommonTopAppBar(
            title = "거래 내역",
            onBackClick = onBackClick
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
                            imageVector = Icons.Default.KeyboardArrowDown,
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
                            imageVector = Icons.Default.KeyboardArrowDown,
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
            items(filteredHistory) { item ->
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
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF7F7F7))
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Date on the left
            if (item.date.isNotEmpty()) {
                Text(
                    text = item.date,
                    style = TitleB16,
                    color = Color.Black,
                    modifier = Modifier.width(45.dp)
                )
            } else {
                Spacer(modifier = Modifier.width(45.dp))
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Stock info
            Column {
                Text(
                    text = item.stockName,
                    style = SubtitleSb16,
                    color = Color.Black
                )
                
                Text(
                    text = "${item.shares}주 ${item.orderType}",
                    style = BodyR14,
                    color = when (item.orderType) {
                        "판매" -> MainBlue
                        else -> Color.Gray
                    }
                )
            }
        }
        
        Text(
            text = "주당 ${String.format("%,d", item.pricePerShare)}원",
            style = SubtitleSb16,
            color = Color.Black
        )
    }
}

@Preview(showBackground = true)
@Composable
fun OrderHistoryScreen() {
    LagoTheme {
        OrderHistoryScreen()
    }
}