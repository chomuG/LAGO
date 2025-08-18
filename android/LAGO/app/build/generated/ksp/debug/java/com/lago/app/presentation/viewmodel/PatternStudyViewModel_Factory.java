package com.lago.app.presentation.viewmodel;

import com.lago.app.domain.usecase.GetChartPatternsUseCase;
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
public final class PatternStudyViewModel_Factory implements Factory<PatternStudyViewModel> {
  private final Provider<GetChartPatternsUseCase> getChartPatternsUseCaseProvider;

  public PatternStudyViewModel_Factory(
      Provider<GetChartPatternsUseCase> getChartPatternsUseCaseProvider) {
    this.getChartPatternsUseCaseProvider = getChartPatternsUseCaseProvider;
  }

  @Override
  public PatternStudyViewModel get() {
    return newInstance(getChartPatternsUseCaseProvider.get());
  }

  public static PatternStudyViewModel_Factory create(
      Provider<GetChartPatternsUseCase> getChartPatternsUseCaseProvider) {
    return new PatternStudyViewModel_Factory(getChartPatternsUseCaseProvider);
  }

  public static PatternStudyViewModel newInstance(GetChartPatternsUseCase getChartPatternsUseCase) {
    return new PatternStudyViewModel(getChartPatternsUseCase);
  }
}
