package com.lago.app.presentation.ui.chart

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.lago.app.presentation.theme.*
import com.lago.app.presentation.ui.widget.LagoCard
import com.lago.app.presentation.ui.widget.ElevationLevel

data class ChatMessage(
    val id: String,
    val content: String,
    val isFromAI: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIDialog(
    onDismiss: () -> Unit = {}
) {
    var messages by remember {
        mutableStateOf(
            listOf(
                ChatMessage(
                    id = "1",
                    content = "안녕하세요! 차트 패턴 분석을 도와드리겠습니다. 궁금한 점을 말씀해 주세요.",
                    isFromAI = true
                )
            )
        )
    }
    
    var currentMessage by remember { mutableStateOf("") }
    var isAnalyzing by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    
    // Auto-scroll to bottom when new message added
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = MainBlue,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "AI 차트 분석",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.semantics {
                            contentDescription = "AI 대화 닫기"
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        bottomBar = {
            MessageInputBar(
                message = currentMessage,
                onMessageChange = { currentMessage = it },
                onSendMessage = {
                    if (currentMessage.isNotBlank() && !isAnalyzing) {
                        // Add user message
                        messages = messages + ChatMessage(
                            id = System.currentTimeMillis().toString(),
                            content = currentMessage,
                            isFromAI = false
                        )
                        
                        val userMessage = currentMessage
                        currentMessage = ""
                        isAnalyzing = true
                        
                        // Simulate AI response
                        coroutineScope.launch {
                            delay(2000) // Simulate processing time
                            
                            val aiResponse = generateAIResponse(userMessage)
                            messages = messages + ChatMessage(
                                id = System.currentTimeMillis().toString(),
                                content = aiResponse,
                                isFromAI = true
                            )
                            isAnalyzing = false
                        }
                    }
                },
                isEnabled = !isAnalyzing
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(messages) { message ->
                    MessageBubble(message = message)
                }
                
                // Typing indicator
                if (isAnalyzing) {
                    item {
                        TypingIndicator()
                    }
                }
            }
        }
    }
}

@Composable
private fun MessageBubble(
    message: ChatMessage,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isFromAI) Arrangement.Start else Arrangement.End
    ) {
        LagoCard(
            modifier = Modifier.widthIn(max = 280.dp),
            backgroundColor = if (message.isFromAI) Color.White else MainBlue,
            elevation = ElevationLevel.LEVEL1
        ) {
            if (message.isFromAI) {
                Row {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = MainBlue,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = message.content,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Gray900
                    )
                }
            } else {
                Text(
                    text = message.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun TypingIndicator() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        LagoCard(
            backgroundColor = Gray100,
            elevation = ElevationLevel.LEVEL1
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = MainBlue,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                
                // Animated dots
                Row {
                    repeat(3) { index ->
                        val infiniteTransition = rememberInfiniteTransition(label = "typing")
                        val alpha by infiniteTransition.animateFloat(
                            initialValue = 0.3f,
                            targetValue = 1f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(
                                    durationMillis = 600,
                                    delayMillis = index * 200
                                ),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "alpha"
                        )
                        
                        Text(
                            text = "•",
                            modifier = Modifier.alpha(alpha),
                            color = Gray600
                        )
                        
                        if (index < 2) {
                            Spacer(modifier = Modifier.width(2.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MessageInputBar(
    message: String,
    onMessageChange: (String) -> Unit,
    onSendMessage: () -> Unit,
    isEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Color.White,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            OutlinedTextField(
                value = message,
                onValueChange = onMessageChange,
                modifier = Modifier
                    .weight(1f)
                    .semantics {
                        contentDescription = "AI에게 질문 입력"
                    },
                placeholder = {
                    Text(
                        "차트에 대해 질문해보세요...",
                        color = Gray500
                    )
                },
                enabled = isEnabled,
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MainBlue,
                    unfocusedBorderColor = Gray300
                )
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            IconButton(
                onClick = onSendMessage,
                enabled = isEnabled && message.isNotBlank(),
                modifier = Modifier.semantics {
                    contentDescription = "메시지 전송"
                }
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = null,
                    tint = if (isEnabled && message.isNotBlank()) MainBlue else Gray400
                )
            }
        }
    }
}

private fun generateAIResponse(userMessage: String): String {
    // Simple pattern matching for demo
    return when {
        userMessage.contains("패턴", ignoreCase = true) -> {
            "현재 차트에서 상승 삼각형 패턴이 형성되고 있습니다. 이는 일반적으로 강세 신호로 해석되며, 저항선 돌파 시 상승 가능성이 높습니다. 거래량 증가와 함께 확인해보시기 바랍니다."
        }
        userMessage.contains("매수", ignoreCase = true) || userMessage.contains("구매", ignoreCase = true) -> {
            "현재 주가는 20일 이평선 위에서 지지를 받고 있습니다. RSI가 50 이상을 유지하고 있어 상승 모멘텀이 지속될 가능성이 있습니다. 다만 분할 매수를 통해 리스크를 관리하시는 것을 권장합니다."
        }
        userMessage.contains("매도", ignoreCase = true) || userMessage.contains("판매", ignoreCase = true) -> {
            "현재 가격이 저항선에 근접해 있습니다. 거래량이 감소하고 있어 상승 동력이 약해질 수 있습니다. 목표 수익률에 도달했다면 부분적인 수익 실현을 고려해보세요."
        }
        userMessage.contains("추세", ignoreCase = true) -> {
            "중기 추세는 상승 추세를 유지하고 있습니다. 5일 이평선이 20일 이평선 위에 있고, 주가가 두 이평선 모두 위에서 거래되고 있어 긍정적인 신호입니다."
        }
        else -> {
            "차트를 분석한 결과, 현재 주가는 안정적인 상승 추세를 보이고 있습니다. 기술적 지표들이 전반적으로 긍정적인 신호를 나타내고 있으니, 추가적인 분석이 필요하시면 구체적인 질문을 해주세요."
        }
    }
}