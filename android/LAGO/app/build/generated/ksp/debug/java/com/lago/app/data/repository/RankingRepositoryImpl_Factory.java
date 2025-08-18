package com.lago.app.data.repository;

import com.lago.app.data.local.prefs.UserPreferences;
import com.lago.app.data.remote.ApiService;
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
public final class RankingRepositoryImpl_Factory implements Factory<RankingRepositoryImpl> {
  private final Provider<ApiService> apiServiceProvider;

  private final Provider<UserPreferences> userPreferencesProvider;

  public RankingRepositoryImpl_Factory(Provider<ApiService> apiServiceProvider,
      Provider<UserPreferences> userPreferencesProvider) {
    this.apiServiceProvider = apiServiceProvider;
    this.userPreferencesProvider = userPreferencesProvider;
  }

  @Override
  public RankingRepositoryImpl get() {
    return newInstance(apiServiceProvider.get(), userPreferencesProvider.get());
  }

  public static RankingRepositoryImpl_Factory create(Provider<ApiService> apiServiceProvider,
      Provider<UserPreferences> userPreferencesProvider) {
    return new RankingRepositoryImpl_Factory(apiServiceProvider, userPreferencesProvider);
  }

  public static RankingRepositoryImpl newInstance(ApiService apiService,
      UserPreferences userPreferences) {
    return new RankingRepositoryImpl(apiService, userPreferences);
  }
}
