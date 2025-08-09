package com.example.LAGO.service;

import com.example.LAGO.domain.InvestmentTerm;
import com.example.LAGO.dto.InvestmentTermDto;
import com.example.LAGO.repository.InvestmentTermRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 투자 용어 서비스
 * INVESTMENT_TERM 테이블 비즈니스 로직 처리
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class InvestmentTermService {

    private final InvestmentTermRepository investmentTermRepository;

    /**
     * 모든 투자 용어 조회
     *
     * @return 투자 용어 리스트
     */
    public List<InvestmentTermDto> getAllInvestmentTerms() {
        log.debug("모든 투자 용어 조회 요청");
        
        List<InvestmentTerm> investmentTerms = investmentTermRepository.findAllOrderByTermId();
        
        log.debug("투자 용어 {}개 조회됨", investmentTerms.size());
        
        return investmentTerms.stream()
                .map(InvestmentTermDto::new)
                .collect(Collectors.toList());
    }


    /**
     * 키워드로 투자 용어 검색 (용어명만 검색)
     *
     * @param keyword 검색 키워드
     * @return 검색된 투자 용어 리스트
     */
    public List<InvestmentTermDto> searchInvestmentTerms(String keyword) {
        log.debug("투자 용어 검색 요청 - keyword: {}", keyword);
        
        List<InvestmentTerm> investmentTerms = investmentTermRepository.findByTermContaining(keyword);
        
        log.debug("검색된 투자 용어 {}개", investmentTerms.size());
        
        return investmentTerms.stream()
                .map(InvestmentTermDto::new)
                .collect(Collectors.toList());
    }
}