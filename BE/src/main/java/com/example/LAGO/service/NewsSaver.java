package com.example.LAGO.service;

import com.example.LAGO.domain.News;
import com.example.LAGO.repository.NewsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 뉴스 건별 저장을 위한 별도 컴포넌트
 * AOP 프록시가 정상 작동하도록 분리된 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NewsSaver {
    
    private final NewsRepository newsRepository;
    
    /**
     * 건별 독립 트랜잭션으로 뉴스 저장
     * REQUIRES_NEW로 별도 트랜잭션 생성하여 실패해도 다른 건에 영향 없음
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public boolean saveOne(News news) {
        try {
            // 즉시 flush해서 해당 건만 실패하도록
            News saved = newsRepository.saveAndFlush(news);
            log.debug("뉴스 저장 완료: {} (ID: {})", news.getTitle(), saved.getId());
            return true;
            
        } catch (DataIntegrityViolationException e) {
            // 제약 위반 (중복 URL, NOT NULL 위반 등)
            log.warn("제약 위반으로 뉴스 저장 스킵 - URL: {}, 제목: {}, 오류: {}", 
                    news.getUrl(), news.getTitle(), e.getMostSpecificCause().getMessage());
            return false;
            
        } catch (Exception e) {
            // 기타 예상치 못한 오류
            log.error("알 수 없는 오류로 뉴스 저장 실패 - URL: {}, 제목: {}, 오류: {}", 
                    news.getUrl(), news.getTitle(), e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * 업서트 방식으로 뉴스 저장 (중복 처리)
     * 동일 URL이 있으면 업데이트, 없으면 삽입
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public boolean upsertNews(News news) {
        try {
            // URL로 기존 뉴스 확인
            if (news.getUrl() != null && !news.getUrl().trim().isEmpty()) {
                News existing = newsRepository.findByUrl(news.getUrl()).orElse(null);
                
                if (existing != null) {
                    // 기존 뉴스 업데이트 (필요한 필드만)
                    existing.setSentiment(news.getSentiment());
                    existing.setConfidenceLevel(news.getConfidenceLevel());
                    existing.setTradingSignal(news.getTradingSignal());
                    existing.setSummary(news.getSummary());
                    
                    News updated = newsRepository.saveAndFlush(existing);
                    log.debug("뉴스 업데이트 완료: {} (ID: {})", existing.getTitle(), updated.getId());
                    return true;
                }
            }
            
            // 새로운 뉴스 저장
            News saved = newsRepository.saveAndFlush(news);
            log.debug("새 뉴스 저장 완료: {} (ID: {})", news.getTitle(), saved.getId());
            return true;
            
        } catch (Exception e) {
            log.error("뉴스 업서트 실패 - URL: {}, 제목: {}, 오류: {}", 
                    news.getUrl(), news.getTitle(), e.getMessage(), e);
            return false;
        }
    }
}