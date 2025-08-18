package com.lago.app.presentation.viewmodel.mypage;

import com.lago.app.data.cache.RealTimeStockCache;
import com.lago.app.data.local.prefs.UserPreferences;
import com.lago.app.data.remote.websocket.SmartStockWebSocketService;
import com.lago.app.data.scheduler.SmartUpdateScheduler;
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
public final class MyPageViewModel_Factory implements Factory<MyPageViewModel> {
  private final Provider<UserRepository> userRepositoryProvider;

  private final Provider<UserPreferences> userPreferencesProvider;

  private final Provider<RealTimeStockCache> realTimeStockCacheProvider;

  private final Provider<SmartStockWebSocketService> smartWebSocketServiceProvider;

  private final Provider<SmartUpdateScheduler> smartUpdateSchedulerProvider;

  private final Provider<HybridPriceCalculator> hybridPriceCalculatorProvider;

  public MyPageViewModel_Factory(Provider<UserRepository> userRepositoryProvider,
      Provider<UserPreferences> userPreferencesProvider,
      Provider<RealTimeStockCache> realTimeStockCacheProvider,
      Provider<SmartStockWebSocketService> smartWebSocketServiceProvider,
      Provider<SmartUpdateScheduler> smartUpdateSchedulerProvider,
      Provider<HybridPriceCalculator> hybridPriceCalculatorProvider) {
    this.userRepositoryProvider = userRepositoryProvider;
    this.userPreferencesProvider = userPreferencesProvider;
    this.realTimeStockCacheProvider = realTimeStockCacheProvider;
    this.smartWebSocketServiceProvider = smartWebSocketServiceProvider;
    this.smartUpdateSchedulerProvider = smartUpdateSchedulerProvider;
    this.hybridPriceCalculatorProvider = hybridPriceCalculatorProvider;
  }

  @Override
  public MyPageViewModel get() {
    return newInstance(userRepositoryProvider.get(), userPreferencesProvider.get(), realTimeStockCacheProvider.get(), smartWebSocketServiceProvider.get(), smartUpdateSchedulerProvider.get(), hybridPriceCalculatorProvider.get());
  }

  public static MyPageViewModel_Factory create(Provider<UserRepository> userRepositoryProvider,
      Provider<UserPreferences> userPreferencesProvider,
      Provider<RealTimeStockCache> realTimeStockCacheProvider,
      Provider<SmartStockWebSocketService> smartWebSocketServiceProvider,
      Provider<SmartUpdateScheduler> smartUpdateSchedulerProvider,
      Provider<HybridPriceCalculator> hybridPriceCalculatorProvider) {
    return new MyPageViewModel_Factory(userRepositoryProvider, userPreferencesProvider, realTimeStockCacheProvider, smartWebSocketServiceProvider, smartUpdateSchedulerProvider, hybridPriceCalculatorProvider);
  }

  public static MyPageViewModel newInstance(UserRepository userRepository,
      UserPreferences userPreferences, RealTimeStockCache realTimeStockCache,
      SmartStockWebSocketService smartWebSocketService, SmartUpdateScheduler smartUpdateScheduler,
      HybridPriceCalculator hybridPriceCalculator) {
    return new MyPageViewModel(userRepository, userPreferences, realTimeStockCache, smartWebSocketService, smartUpdateScheduler, hybridPriceCalculator);
  }
}
