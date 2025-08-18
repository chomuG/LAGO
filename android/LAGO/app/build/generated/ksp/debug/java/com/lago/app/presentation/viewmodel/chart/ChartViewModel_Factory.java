package com.lago.app.presentation.viewmodel.chart;

import com.lago.app.data.cache.ChartMemoryCache;
import com.lago.app.data.cache.FavoriteCache;
import com.lago.app.data.cache.RealTimeStockCache;
import com.lago.app.data.local.dao.ChartCacheDao;
import com.lago.app.data.local.prefs.PatternAnalysisPreferences;
import com.lago.app.data.local.prefs.UserPreferences;
import com.lago.app.data.remote.websocket.SmartStockWebSocketService;
import com.lago.app.data.scheduler.SmartUpdateScheduler;
import com.lago.app.domain.repository.ChartRepository;
import com.lago.app.domain.repository.MockTradeRepository;
import com.lago.app.domain.repository.PortfolioRepository;
import com.lago.app.domain.usecase.AnalyzeChartPatternUseCase;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast",
    "deprecation",
    "nullness:initialization.field.uninitialized"
})
public final class ChartViewModel_Factory implements Factory<ChartViewModel> {
  private final Provider<ChartRepository> chartRepositoryProvider;

  private final Provider<AnalyzeChartPatternUseCase> analyzeChartPatternUseCaseProvider;

  private final Provider<UserPreferences> userPreferencesProvider;

  private final Provider<SmartStockWebSocketService> smartWebSocketServiceProvider;

  private final Provider<SmartUpdateScheduler> smartUpdateSchedulerProvider;

  private final Provider<ChartMemoryCache> memoryCacheProvider;

  private final Provider<RealTimeStockCache> realTimeCacheProvider;

  private final Provider<MockTradeRepository> mockTradeRepositoryProvider;

  private final Provider<PortfolioRepository> portfolioRepositoryProvider;

  private final Provider<ChartCacheDao> chartCacheDaoProvider;

  private final Provider<FavoriteCache> favoriteCacheProvider;

  private final Provider<PatternAnalysisPreferences> patternAnalysisPreferencesProvider;

  public ChartViewModel_Factory(Provider<ChartRepository> chartRepositoryProvider,
      Provider<AnalyzeChartPatternUseCase> analyzeChartPatternUseCaseProvider,
      Provider<UserPreferences> userPreferencesProvider,
      Provider<SmartStockWebSocketService> smartWebSocketServiceProvider,
      Provider<SmartUpdateScheduler> smartUpdateSchedulerProvider,
      Provider<ChartMemoryCache> memoryCacheProvider,
      Provider<RealTimeStockCache> realTimeCacheProvider,
      Provider<MockTradeRepository> mockTradeRepositoryProvider,
      Provider<PortfolioRepository> portfolioRepositoryProvider,
      Provider<ChartCacheDao> chartCacheDaoProvider, Provider<FavoriteCache> favoriteCacheProvider,
      Provider<PatternAnalysisPreferences> patternAnalysisPreferencesProvider) {
    this.chartRepositoryProvider = chartRepositoryProvider;
    this.analyzeChartPatternUseCaseProvider = analyzeChartPatternUseCaseProvider;
    this.userPreferencesProvider = userPreferencesProvider;
    this.smartWebSocketServiceProvider = smartWebSocketServiceProvider;
    this.smartUpdateSchedulerProvider = smartUpdateSchedulerProvider;
    this.memoryCacheProvider = memoryCacheProvider;
    this.realTimeCacheProvider = realTimeCacheProvider;
    this.mockTradeRepositoryProvider = mockTradeRepositoryProvider;
    this.portfolioRepositoryProvider = portfolioRepositoryProvider;
    this.chartCacheDaoProvider = chartCacheDaoProvider;
    this.favoriteCacheProvider = favoriteCacheProvider;
    this.patternAnalysisPreferencesProvider = patternAnalysisPreferencesProvider;
  }

  @Override
  public ChartViewModel get() {
    return newInstance(chartRepositoryProvider.get(), analyzeChartPatternUseCaseProvider.get(), userPreferencesProvider.get(), smartWebSocketServiceProvider.get(), smartUpdateSchedulerProvider.get(), memoryCacheProvider.get(), realTimeCacheProvider.get(), mockTradeRepositoryProvider.get(), portfolioRepositoryProvider.get(), chartCacheDaoProvider.get(), favoriteCacheProvider.get(), patternAnalysisPreferencesProvider.get());
  }

  public static ChartViewModel_Factory create(Provider<ChartRepository> chartRepositoryProvider,
      Provider<AnalyzeChartPatternUseCase> analyzeChartPatternUseCaseProvider,
      Provider<UserPreferences> userPreferencesProvider,
      Provider<SmartStockWebSocketService> smartWebSocketServiceProvider,
      Provider<SmartUpdateScheduler> smartUpdateSchedulerProvider,
      Provider<ChartMemoryCache> memoryCacheProvider,
      Provider<RealTimeStockCache> realTimeCacheProvider,
      Provider<MockTradeRepository> mockTradeRepositoryProvider,
      Provider<PortfolioRepository> portfolioRepositoryProvider,
      Provider<ChartCacheDao> chartCacheDaoProvider, Provider<FavoriteCache> favoriteCacheProvider,
      Provider<PatternAnalysisPreferences> patternAnalysisPreferencesProvider) {
    return new ChartViewModel_Factory(chartRepositoryProvider, analyzeChartPatternUseCaseProvider, userPreferencesProvider, smartWebSocketServiceProvider, smartUpdateSchedulerProvider, memoryCacheProvider, realTimeCacheProvider, mockTradeRepositoryProvider, portfolioRepositoryProvider, chartCacheDaoProvider, favoriteCacheProvider, patternAnalysisPreferencesProvider);
  }

  public static ChartViewModel newInstance(ChartRepository chartRepository,
      AnalyzeChartPatternUseCase analyzeChartPatternUseCase, UserPreferences userPreferences,
      SmartStockWebSocketService smartWebSocketService, SmartUpdateScheduler smartUpdateScheduler,
      ChartMemoryCache memoryCache, RealTimeStockCache realTimeCache,
      MockTradeRepository mockTradeRepository, PortfolioRepository portfolioRepository,
      ChartCacheDao chartCacheDao, FavoriteCache favoriteCache,
      PatternAnalysisPreferences patternAnalysisPreferences) {
    return new ChartViewModel(chartRepository, analyzeChartPatternUseCase, userPreferences, smartWebSocketService, smartUpdateScheduler, memoryCache, realTimeCache, mockTradeRepository, portfolioRepository, chartCacheDao, favoriteCache, patternAnalysisPreferences);
  }
}
