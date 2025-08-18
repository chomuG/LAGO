package com.lago.app.presentation.viewmodel.purchase;

import com.lago.app.data.local.prefs.UserPreferences;
import com.lago.app.data.remote.api.ChartApiService;
import com.lago.app.domain.repository.MockTradeRepository;
import com.lago.app.domain.repository.PortfolioRepository;
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
public final class PurchaseViewModel_Factory implements Factory<PurchaseViewModel> {
  private final Provider<MockTradeRepository> mockTradeRepositoryProvider;

  private final Provider<PortfolioRepository> portfolioRepositoryProvider;

  private final Provider<UserPreferences> userPreferencesProvider;

  private final Provider<ChartApiService> chartApiServiceProvider;

  public PurchaseViewModel_Factory(Provider<MockTradeRepository> mockTradeRepositoryProvider,
      Provider<PortfolioRepository> portfolioRepositoryProvider,
      Provider<UserPreferences> userPreferencesProvider,
      Provider<ChartApiService> chartApiServiceProvider) {
    this.mockTradeRepositoryProvider = mockTradeRepositoryProvider;
    this.portfolioRepositoryProvider = portfolioRepositoryProvider;
    this.userPreferencesProvider = userPreferencesProvider;
    this.chartApiServiceProvider = chartApiServiceProvider;
  }

  @Override
  public PurchaseViewModel get() {
    return newInstance(mockTradeRepositoryProvider.get(), portfolioRepositoryProvider.get(), userPreferencesProvider.get(), chartApiServiceProvider.get());
  }

  public static PurchaseViewModel_Factory create(
      Provider<MockTradeRepository> mockTradeRepositoryProvider,
      Provider<PortfolioRepository> portfolioRepositoryProvider,
      Provider<UserPreferences> userPreferencesProvider,
      Provider<ChartApiService> chartApiServiceProvider) {
    return new PurchaseViewModel_Factory(mockTradeRepositoryProvider, portfolioRepositoryProvider, userPreferencesProvider, chartApiServiceProvider);
  }

  public static PurchaseViewModel newInstance(MockTradeRepository mockTradeRepository,
      PortfolioRepository portfolioRepository, UserPreferences userPreferences,
      ChartApiService chartApiService) {
    return new PurchaseViewModel(mockTradeRepository, portfolioRepository, userPreferences, chartApiService);
  }
}
