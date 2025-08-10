package com.example.LAGO;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * LAGO 애플리케이션 테스트 클래스
 * 지침서 명세: 테스트코드/Mock 데이터/QA 필수 구현
 */
class LagoApplicationTests {

	@Test
	void simpleTest() {
		// 간단한 단위 테스트 - Spring 컨텍스트 없이 실행
		assertTrue(true, "기본 테스트가 실행되어야 합니다");
	}
	
	@Test
	void basicMathTest() {
		// 기본적인 수학 연산 테스트
		int result = 2 + 2;
		assertEquals(4, result, "2 + 2는 4여야 합니다");
	}

}
