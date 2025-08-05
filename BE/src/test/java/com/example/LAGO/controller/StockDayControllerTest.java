package com.example.LAGO.controller;

import com.example.LAGO.dto.StockDayDto;
import com.example.LAGO.service.StockDayService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StockDayController.class)
class StockDayControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StockDayService stockDayService;

    @Test
    @DisplayName("GET /api/stock-day 호출 테스트")
    void testGetStockDays() throws Exception {
        // given: Mocking 서비스 리턴값
        Mockito.when(stockDayService.getStockDays(
                eq(1), eq(LocalDate.of(2025, 7, 1)), eq(LocalDate.of(2025, 7, 31))
        )).thenReturn(Collections.emptyList());

        // when & then: GET 요청 및 검증
        mockMvc.perform(get("/api/stock-day")
                        .param("stockId", "1")
                        .param("start", "2024-07-01")
                        .param("end", "2024-07-31"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]")); // 빈 리스트 반환 확인
    }
}
