package com.lago.app.data.repository;

import com.lago.app.data.remote.api.PortfolioApiService;
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
public final class PortfolioRepositoryImpl_Factory implements Factory<PortfolioRepositoryImpl> {
  private final Provider<PortfolioApiService> portfolioApiServiceProvider;

  public PortfolioRepositoryImpl_Factory(
      Provider<PortfolioApiService> portfolioApiServiceProvider) {
    this.portfolioApiServiceProvider = portfolioApiServiceProvider;
  }

  @Override
  public PortfolioRepositoryImpl get() {
    return newInstance(portfolioApiServiceProvider.get());
  }

  public static PortfolioRepositoryImpl_Factory create(
      Provider<PortfolioApiService> portfolioApiServiceProvider) {
    return new PortfolioRepositoryImpl_Factory(portfolioApiServiceProvider);
  }

  public static PortfolioRepositoryImpl newInstance(PortfolioApiService portfolioApiService) {
    return new PortfolioRepositoryImpl(portfolioApiService);
  }
}
