package com.lago.app.presentation.viewmodel;

import com.lago.app.domain.usecase.GetNewsDetailUseCase;
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
public final class NewsDetailViewModel_Factory implements Factory<NewsDetailViewModel> {
  private final Provider<GetNewsDetailUseCase> getNewsDetailUseCaseProvider;

  public NewsDetailViewModel_Factory(Provider<GetNewsDetailUseCase> getNewsDetailUseCaseProvider) {
    this.getNewsDetailUseCaseProvider = getNewsDetailUseCaseProvider;
  }

  @Override
  public NewsDetailViewModel get() {
    return newInstance(getNewsDetailUseCaseProvider.get());
  }

  public static NewsDetailViewModel_Factory create(
      Provider<GetNewsDetailUseCase> getNewsDetailUseCaseProvider) {
    return new NewsDetailViewModel_Factory(getNewsDetailUseCaseProvider);
  }

  public static NewsDetailViewModel newInstance(GetNewsDetailUseCase getNewsDetailUseCase) {
    return new NewsDetailViewModel(getNewsDetailUseCase);
  }
}
