package com.lago.app.presentation.viewmodel;

import com.lago.app.domain.usecase.GetHistoryChallengeNewsDetailUseCase;
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
public final class HistoryChallengeNewsDetailViewModel_Factory implements Factory<HistoryChallengeNewsDetailViewModel> {
  private final Provider<GetHistoryChallengeNewsDetailUseCase> getHistoryChallengeNewsDetailUseCaseProvider;

  public HistoryChallengeNewsDetailViewModel_Factory(
      Provider<GetHistoryChallengeNewsDetailUseCase> getHistoryChallengeNewsDetailUseCaseProvider) {
    this.getHistoryChallengeNewsDetailUseCaseProvider = getHistoryChallengeNewsDetailUseCaseProvider;
  }

  @Override
  public HistoryChallengeNewsDetailViewModel get() {
    return newInstance(getHistoryChallengeNewsDetailUseCaseProvider.get());
  }

  public static HistoryChallengeNewsDetailViewModel_Factory create(
      Provider<GetHistoryChallengeNewsDetailUseCase> getHistoryChallengeNewsDetailUseCaseProvider) {
    return new HistoryChallengeNewsDetailViewModel_Factory(getHistoryChallengeNewsDetailUseCaseProvider);
  }

  public static HistoryChallengeNewsDetailViewModel newInstance(
      GetHistoryChallengeNewsDetailUseCase getHistoryChallengeNewsDetailUseCase) {
    return new HistoryChallengeNewsDetailViewModel(getHistoryChallengeNewsDetailUseCase);
  }
}
