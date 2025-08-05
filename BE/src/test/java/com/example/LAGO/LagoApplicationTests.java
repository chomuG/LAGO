package com.example.LAGO;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * LAGO 애플리케이션 테스트 클래스
 * 지침서 명세: 테스트코드/Mock 데이터/QA 필수 구현
 */
@SpringBootTest
@ActiveProfiles("test")
class LagoApplicationTests {

	@Test
	void contextLoads() {
		// Spring Boot 컨텍스트 로드 테스트
		// 테스트용 H2 데이터베이스와 기본 설정으로 실행
	}

}
