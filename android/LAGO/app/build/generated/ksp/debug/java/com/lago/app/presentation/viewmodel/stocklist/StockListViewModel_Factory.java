package com.lago.app.presentation.viewmodel.stocklist;

import com.lago.app.data.cache.FavoriteCache;
import com.lago.app.data.cache.RealTimeStockCache;
import com.lago.app.data.remote.websocket.SmartStockWebSocketService;
import com.lago.app.data.scheduler.SmartUpdateScheduler;
import com.lago.app.domain.repository.ChartRepository;
import com.lago.app.domain.repository.HistoryChallengeRepository;
import com.lago.app.domain.repository.MockTradeRepository;
import com.lago.app.domain.repository.StockListRepository;
import com.lago.app.domain.usecase.GetHistoryChallengeNewsUseCase;
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
public final class StockListViewModel_Factory implements Factory<StockListViewModel> {
  private final Provider<StockListRepository> stockListRepositoryProvider;

  private final Provider<HistoryChallengeRepository> historyChallengeRepositoryProvider;

  private final Provider<ChartRepository> chartRepositoryProvider;

  private final Provider<SmartStockWebSocketService> smartWebSocketServiceProvider;

  private final Provider<SmartUpdateScheduler> smartUpdateSchedulerProvider;

  private final Provider<RealTimeStockCache> realTimeCacheProvider;

  private final Provider<GetHistoryChallengeNewsUseCase> getHistoryChallengeNewsUseCaseProvider;

  private final Provider<MockTradeRepository> mockTradeRepositoryProvider;

  private final Provider<FavoriteCache> favoriteCacheProvider;

  public StockListViewModel_Factory(Provider<StockListRepository> stockListRepositoryProvider,
      Provider<HistoryChallengeRepository> historyChallengeRepositoryProvider,
      Provider<ChartRepository> chartRepositoryProvider,
      Provider<SmartStockWebSocketService> smartWebSocketServiceProvider,
      Provider<SmartUpdateScheduler> smartUpdateSchedulerProvider,
      Provider<RealTimeStockCache> realTimeCacheProvider,
      Provider<GetHistoryChallengeNewsUseCase> getHistoryChallengeNewsUseCaseProvider,
      Provider<MockTradeRepository> mockTradeRepositoryProvider,
      Provider<FavoriteCache> favoriteCacheProvider) {
    this.stockListRepositoryProvider = stockListRepositoryProvider;
    this.historyChallengeRepositoryProvider = historyChallengeRepositoryProvider;
    this.chartRepositoryProvider = chartRepositoryProvider;
    this.smartWebSocketServiceProvider = smartWebSocketServiceProvider;
    this.smartUpdateSchedulerProvider = smartUpdateSchedulerProvider;
    this.realTimeCacheProvider = realTimeCacheProvider;
    this.getHistoryChallengeNewsUseCaseProvider = getHistoryChallengeNewsUseCaseProvider;
    this.mockTradeRepositoryProvider = mockTradeRepositoryProvider;
    this.favoriteCacheProvider = favoriteCacheProvider;
  }

  @Override
  public StockListViewModel get() {
    return newInstance(stockListRepositoryProvider.get(), historyChallengeRepositoryProvider.get(), chartRepositoryProvider.get(), smartWebSocketServiceProvider.get(), smartUpdateSchedulerProvider.get(), realTimeCacheProvider.get(), getHistoryChallengeNewsUseCaseProvider.get(), mockTradeRepositoryProvider.get(), favoriteCacheProvider.get());
  }

  public static StockListViewModel_Factory create(
      Provider<StockListRepository> stockListRepositoryProvider,
      Provider<HistoryChallengeRepository> historyChallengeRepositoryProvider,
      Provider<ChartRepository> chartRepositoryProvider,
      Provider<SmartStockWebSocketService> smartWebSocketServiceProvider,
      Provider<SmartUpdateScheduler> smartUpdateSchedulerProvider,
      Provider<RealTimeStockCache> realTimeCacheProvider,
      Provider<GetHistoryChallengeNewsUseCase> getHistoryChallengeNewsUseCaseProvider,
      Provider<MockTradeRepository> mockTradeRepositoryProvider,
      Provider<FavoriteCache> favoriteCacheProvider) {
    return new StockListViewModel_Factory(stockListRepositoryProvider, historyChallengeRepositoryProvider, chartRepositoryProvider, smartWebSocketServiceProvider, smartUpdateSchedulerProvider, realTimeCacheProvider, getHistoryChallengeNewsUseCaseProvider, mockTradeRepositoryProvider, favoriteCacheProvider);
  }

  public static StockListViewModel newInstance(StockListRepository stockListRepository,
      HistoryChallengeRepository historyChallengeRepository, ChartRepository chartRepository,
      SmartStockWebSocketService smartWebSocketService, SmartUpdateScheduler smartUpdateScheduler,
      RealTimeStockCache realTimeCache,
      GetHistoryChallengeNewsUseCase getHistoryChallengeNewsUseCase,
      MockTradeRepository mockTradeRepository, FavoriteCache favoriteCache) {
    return new StockListViewModel(stockListRepository, historyChallengeRepository, chartRepository, smartWebSocketService, smartUpdateScheduler, realTimeCache, getHistoryChallengeNewsUseCase, mockTradeRepository, favoriteCache);
  }
}
