package com.lago.app.presentation.viewmodel.portfolio;

import com.lago.app.data.cache.RealTimeStockCache;
import com.lago.app.domain.repository.MockTradeRepository;
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
public final class PortfolioViewModel_Factory implements Factory<PortfolioViewModel> {
  private final Provider<MockTradeRepository> mockTradeRepositoryProvider;

  private final Provider<UserRepository> userRepositoryProvider;

  private final Provider<RealTimeStockCache> realTimeStockCacheProvider;

  private final Provider<HybridPriceCalculator> hybridPriceCalculatorProvider;

  public PortfolioViewModel_Factory(Provider<MockTradeRepository> mockTradeRepositoryProvider,
      Provider<UserRepository> userRepositoryProvider,
      Provider<RealTimeStockCache> realTimeStockCacheProvider,
      Provider<HybridPriceCalculator> hybridPriceCalculatorProvider) {
    this.mockTradeRepositoryProvider = mockTradeRepositoryProvider;
    this.userRepositoryProvider = userRepositoryProvider;
    this.realTimeStockCacheProvider = realTimeStockCacheProvider;
    this.hybridPriceCalculatorProvider = hybridPriceCalculatorProvider;
  }

  @Override
  public PortfolioViewModel get() {
    return newInstance(mockTradeRepositoryProvider.get(), userRepositoryProvider.get(), realTimeStockCacheProvider.get(), hybridPriceCalculatorProvider.get());
  }

  public static PortfolioViewModel_Factory create(
      Provider<MockTradeRepository> mockTradeRepositoryProvider,
      Provider<UserRepository> userRepositoryProvider,
      Provider<RealTimeStockCache> realTimeStockCacheProvider,
      Provider<HybridPriceCalculator> hybridPriceCalculatorProvider) {
    return new PortfolioViewModel_Factory(mockTradeRepositoryProvider, userRepositoryProvider, realTimeStockCacheProvider, hybridPriceCalculatorProvider);
  }

  public static PortfolioViewModel newInstance(MockTradeRepository mockTradeRepository,
      UserRepository userRepository, RealTimeStockCache realTimeStockCache,
      HybridPriceCalculator hybridPriceCalculator) {
    return new PortfolioViewModel(mockTradeRepository, userRepository, realTimeStockCache, hybridPriceCalculator);
  }
}
