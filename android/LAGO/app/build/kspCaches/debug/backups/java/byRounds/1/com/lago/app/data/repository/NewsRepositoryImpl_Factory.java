package com.lago.app.data.repository;

import com.lago.app.data.remote.NewsApiService;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata("javax.inject.Singleton")
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
public final class NewsRepositoryImpl_Factory implements Factory<NewsRepositoryImpl> {
  private final Provider<NewsApiService> newsApiServiceProvider;

  public NewsRepositoryImpl_Factory(Provider<NewsApiService> newsApiServiceProvider) {
    this.newsApiServiceProvider = newsApiServiceProvider;
  }

  @Override
  public NewsRepositoryImpl get() {
    return newInstance(newsApiServiceProvider.get());
  }

  public static NewsRepositoryImpl_Factory create(Provider<NewsApiService> newsApiServiceProvider) {
    return new NewsRepositoryImpl_Factory(newsApiServiceProvider);
  }

  public static NewsRepositoryImpl newInstance(NewsApiService newsApiService) {
    return new NewsRepositoryImpl(newsApiService);
  }
}
