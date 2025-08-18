package com.lago.app.domain.usecase;

import com.lago.app.domain.repository.NewsRepository;
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
public final class GetHistoryChallengeNewsDetailUseCase_Factory implements Factory<GetHistoryChallengeNewsDetailUseCase> {
  private final Provider<NewsRepository> newsRepositoryProvider;

  public GetHistoryChallengeNewsDetailUseCase_Factory(
      Provider<NewsRepository> newsRepositoryProvider) {
    this.newsRepositoryProvider = newsRepositoryProvider;
  }

  @Override
  public GetHistoryChallengeNewsDetailUseCase get() {
    return newInstance(newsRepositoryProvider.get());
  }

  public static GetHistoryChallengeNewsDetailUseCase_Factory create(
      Provider<NewsRepository> newsRepositoryProvider) {
    return new GetHistoryChallengeNewsDetailUseCase_Factory(newsRepositoryProvider);
  }

  public static GetHistoryChallengeNewsDetailUseCase newInstance(NewsRepository newsRepository) {
    return new GetHistoryChallengeNewsDetailUseCase(newsRepository);
  }
}
