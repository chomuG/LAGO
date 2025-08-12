package com.example.LAGO.realtime;


import com.example.LAGO.realtime.dto.TickData;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

// 실시간 틱 데이터 바로 전송
@Service
@RequiredArgsConstructor
public class RealTimeDataBroadcaster {

    private final SimpMessagingTemplate messagingTemplate;

    public void sendRealTimeData(TickData tickData) {
        // WebSocket으로 프론트엔드에 즉시 전송

        /**
         * 특정 종목 구독
         * /topic/stocks/{종목코드}
         */
        messagingTemplate.convertAndSend(
                "/topic/stocks/" + tickData.getCode(),
                tickData
        );

        /**
         * 전체 종목 구독
         */
        messagingTemplate.convertAndSend("/topic/stocks/all", tickData);
    }
}
