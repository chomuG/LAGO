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
public final class GetInterestNewsUseCase_Factory implements Factory<GetInterestNewsUseCase> {
  private final Provider<NewsRepository> newsRepositoryProvider;

  public GetInterestNewsUseCase_Factory(Provider<NewsRepository> newsRepositoryProvider) {
    this.newsRepositoryProvider = newsRepositoryProvider;
  }

  @Override
  public GetInterestNewsUseCase get() {
    return newInstance(newsRepositoryProvider.get());
  }

  public static GetInterestNewsUseCase_Factory create(
      Provider<NewsRepository> newsRepositoryProvider) {
    return new GetInterestNewsUseCase_Factory(newsRepositoryProvider);
  }

  public static GetInterestNewsUseCase newInstance(NewsRepository newsRepository) {
    return new GetInterestNewsUseCase(newsRepository);
  }
}
