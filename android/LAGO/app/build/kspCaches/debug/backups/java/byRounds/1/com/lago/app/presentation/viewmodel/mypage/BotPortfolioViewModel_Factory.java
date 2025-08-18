package com.lago.app.presentation.viewmodel.mypage;

import com.lago.app.data.cache.RealTimeStockCache;
import com.lago.app.data.remote.websocket.SmartStockWebSocketService;
import com.lago.app.domain.repository.UserRepository;
import com.lago.app.util.HybridPriceCalculator;
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
public final class BotPortfolioViewModel_Factory implements Factory<BotPortfolioViewModel> {
  private final Provider<UserRepository> userRepositoryProvider;

  private final Provider<RealTimeStockCache> realTimeStockCacheProvider;

  private final Provider<SmartStockWebSocketService> smartWebSocketServiceProvider;

  private final Provider<HybridPriceCalculator> hybridPriceCalculatorProvider;

  public BotPortfolioViewModel_Factory(Provider<UserRepository> userRepositoryProvider,
      Provider<RealTimeStockCache> realTimeStockCacheProvider,
      Provider<SmartStockWebSocketService> smartWebSocketServiceProvider,
      Provider<HybridPriceCalculator> hybridPriceCalculatorProvider) {
    this.userRepositoryProvider = userRepositoryProvider;
    this.realTimeStockCacheProvider = realTimeStockCacheProvider;
    this.smartWebSocketServiceProvider = smartWebSocketServiceProvider;
    this.hybridPriceCalculatorProvider = hybridPriceCalculatorProvider;
  }

  @Override
  public BotPortfolioViewModel get() {
    return newInstance(userRepositoryProvider.get(), realTimeStockCacheProvider.get(), smartWebSocketServiceProvider.get(), hybridPriceCalculatorProvider.get());
  }

  public static BotPortfolioViewModel_Factory create(
      Provider<UserRepository> userRepositoryProvider,
      Provider<RealTimeStockCache> realTimeStockCacheProvider,
      Provider<SmartStockWebSocketService> smartWebSocketServiceProvider,
      Provider<HybridPriceCalculator> hybridPriceCalculatorProvider) {
    return new BotPortfolioViewModel_Factory(userRepositoryProvider, realTimeStockCacheProvider, smartWebSocketServiceProvider, hybridPriceCalculatorProvider);
  }

  public static BotPortfolioViewModel newInstance(UserRepository userRepository,
      RealTimeStockCache realTimeStockCache, SmartStockWebSocketService smartWebSocketService,
      HybridPriceCalculator hybridPriceCalculator) {
    return new BotPortfolioViewModel(userRepository, realTimeStockCache, smartWebSocketService, hybridPriceCalculator);
  }
}
