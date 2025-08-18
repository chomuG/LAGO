package com.lago.app.domain.usecase;

import com.lago.app.domain.repository.ChartRepository;
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
public final class AnalyzeChartPatternUseCase_Factory implements Factory<AnalyzeChartPatternUseCase> {
  private final Provider<ChartRepository> chartRepositoryProvider;

  public AnalyzeChartPatternUseCase_Factory(Provider<ChartRepository> chartRepositoryProvider) {
    this.chartRepositoryProvider = chartRepositoryProvider;
  }

  @Override
  public AnalyzeChartPatternUseCase get() {
    return newInstance(chartRepositoryProvider.get());
  }

  public static AnalyzeChartPatternUseCase_Factory create(
      Provider<ChartRepository> chartRepositoryProvider) {
    return new AnalyzeChartPatternUseCase_Factory(chartRepositoryProvider);
  }

  public static AnalyzeChartPatternUseCase newInstance(ChartRepository chartRepository) {
    return new AnalyzeChartPatternUseCase(chartRepository);
  }
}
