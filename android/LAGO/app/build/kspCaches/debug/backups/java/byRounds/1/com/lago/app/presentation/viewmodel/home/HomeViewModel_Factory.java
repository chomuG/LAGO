package com.lago.app.presentation.viewmodel.home;

import com.lago.app.data.cache.RealTimeStockCache;
import com.lago.app.data.local.prefs.UserPreferences;
import com.lago.app.data.remote.websocket.SmartStockWebSocketService;
import com.lago.app.data.scheduler.SmartUpdateScheduler;
import com.lago.app.data.service.CloseDataService;
import com.lago.app.data.service.InitialPriceService;
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
public final class HomeViewModel_Factory implements Factory<HomeViewModel> {
  private final Provider<UserRepository> userRepositoryProvider;

  private final Provider<UserPreferences> userPreferencesProvider;

  private final Provider<RealTimeStockCache> realTimeStockCacheProvider;

  private final Provider<SmartStockWebSocketService> smartWebSocketServiceProvider;

  private final Provider<SmartUpdateScheduler> smartUpdateSchedulerProvider;

  private final Provider<CloseDataService> closeDataServiceProvider;

  private final Provider<HybridPriceCalculator> hybridPriceCalculatorProvider;

  private final Provider<InitialPriceService> initialPriceServiceProvider;

  public HomeViewModel_Factory(Provider<UserRepository> userRepositoryProvider,
      Provider<UserPreferences> userPreferencesProvider,
      Provider<RealTimeStockCache> realTimeStockCacheProvider,
      Provider<SmartStockWebSocketService> smartWebSocketServiceProvider,
      Provider<SmartUpdateScheduler> smartUpdateSchedulerProvider,
      Provider<CloseDataService> closeDataServiceProvider,
      Provider<HybridPriceCalculator> hybridPriceCalculatorProvider,
      Provider<InitialPriceService> initialPriceServiceProvider) {
    this.userRepositoryProvider = userRepositoryProvider;
    this.userPreferencesProvider = userPreferencesProvider;
    this.realTimeStockCacheProvider = realTimeStockCacheProvider;
    this.smartWebSocketServiceProvider = smartWebSocketServiceProvider;
    this.smartUpdateSchedulerProvider = smartUpdateSchedulerProvider;
    this.closeDataServiceProvider = closeDataServiceProvider;
    this.hybridPriceCalculatorProvider = hybridPriceCalculatorProvider;
    this.initialPriceServiceProvider = initialPriceServiceProvider;
  }

  @Override
  public HomeViewModel get() {
    return newInstance(userRepositoryProvider.get(), userPreferencesProvider.get(), realTimeStockCacheProvider.get(), smartWebSocketServiceProvider.get(), smartUpdateSchedulerProvider.get(), closeDataServiceProvider.get(), hybridPriceCalculatorProvider.get(), initialPriceServiceProvider.get());
  }

  public static HomeViewModel_Factory create(Provider<UserRepository> userRepositoryProvider,
      Provider<UserPreferences> userPreferencesProvider,
      Provider<RealTimeStockCache> realTimeStockCacheProvider,
      Provider<SmartStockWebSocketService> smartWebSocketServiceProvider,
      Provider<SmartUpdateScheduler> smartUpdateSchedulerProvider,
      Provider<CloseDataService> closeDataServiceProvider,
      Provider<HybridPriceCalculator> hybridPriceCalculatorProvider,
      Provider<InitialPriceService> initialPriceServiceProvider) {
    return new HomeViewModel_Factory(userRepositoryProvider, userPreferencesProvider, realTimeStockCacheProvider, smartWebSocketServiceProvider, smartUpdateSchedulerProvider, closeDataServiceProvider, hybridPriceCalculatorProvider, initialPriceServiceProvider);
  }

  public static HomeViewModel newInstance(UserRepository userRepository,
      UserPreferences userPreferences, RealTimeStockCache realTimeStockCache,
      SmartStockWebSocketService smartWebSocketService, SmartUpdateScheduler smartUpdateScheduler,
      CloseDataService closeDataService, HybridPriceCalculator hybridPriceCalculator,
      InitialPriceService initialPriceService) {
    return new HomeViewModel(userRepository, userPreferences, realTimeStockCache, smartWebSocketService, smartUpdateScheduler, closeDataService, hybridPriceCalculator, initialPriceService);
  }
}
