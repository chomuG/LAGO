package com.lago.app.presentation.viewmodel.historychallenge;

import com.lago.app.data.cache.RealTimeStockCache;
import com.lago.app.data.local.prefs.UserPreferences;
import com.lago.app.domain.repository.ChartRepository;
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
public final class HistoryChallengeChartViewModel_Factory implements Factory<HistoryChallengeChartViewModel> {
  private final Provider<ChartRepository> chartRepositoryProvider;

  private final Provider<AnalyzeChartPatternUseCase> analyzeChartPatternUseCaseProvider;

  private final Provider<UserPreferences> userPreferencesProvider;

  private final Provider<RealTimeStockCache> realTimeCacheProvider;

  public HistoryChallengeChartViewModel_Factory(Provider<ChartRepository> chartRepositoryProvider,
      Provider<AnalyzeChartPatternUseCase> analyzeChartPatternUseCaseProvider,
      Provider<UserPreferences> userPreferencesProvider,
      Provider<RealTimeStockCache> realTimeCacheProvider) {
    this.chartRepositoryProvider = chartRepositoryProvider;
    this.analyzeChartPatternUseCaseProvider = analyzeChartPatternUseCaseProvider;
    this.userPreferencesProvider = userPreferencesProvider;
    this.realTimeCacheProvider = realTimeCacheProvider;
  }

  @Override
  public HistoryChallengeChartViewModel get() {
    return newInstance(chartRepositoryProvider.get(), analyzeChartPatternUseCaseProvider.get(), userPreferencesProvider.get(), realTimeCacheProvider.get());
  }

  public static HistoryChallengeChartViewModel_Factory create(
      Provider<ChartRepository> chartRepositoryProvider,
      Provider<AnalyzeChartPatternUseCase> analyzeChartPatternUseCaseProvider,
      Provider<UserPreferences> userPreferencesProvider,
      Provider<RealTimeStockCache> realTimeCacheProvider) {
    return new HistoryChallengeChartViewModel_Factory(chartRepositoryProvider, analyzeChartPatternUseCaseProvider, userPreferencesProvider, realTimeCacheProvider);
  }

  public static HistoryChallengeChartViewModel newInstance(ChartRepository chartRepository,
      AnalyzeChartPatternUseCase analyzeChartPatternUseCase, UserPreferences userPreferences,
      RealTimeStockCache realTimeCache) {
    return new HistoryChallengeChartViewModel(chartRepository, analyzeChartPatternUseCase, userPreferences, realTimeCache);
  }
}
