package com.lago.app.data.repository;

import com.lago.app.data.remote.api.HistoryChallengeApiService;
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
public final class HistoryChallengeRepositoryImpl_Factory implements Factory<HistoryChallengeRepositoryImpl> {
  private final Provider<HistoryChallengeApiService> apiServiceProvider;

  public HistoryChallengeRepositoryImpl_Factory(
      Provider<HistoryChallengeApiService> apiServiceProvider) {
    this.apiServiceProvider = apiServiceProvider;
  }

  @Override
  public HistoryChallengeRepositoryImpl get() {
    return newInstance(apiServiceProvider.get());
  }

  public static HistoryChallengeRepositoryImpl_Factory create(
      Provider<HistoryChallengeApiService> apiServiceProvider) {
    return new HistoryChallengeRepositoryImpl_Factory(apiServiceProvider);
  }

  public static HistoryChallengeRepositoryImpl newInstance(HistoryChallengeApiService apiService) {
    return new HistoryChallengeRepositoryImpl(apiService);
  }
}
