package com.lago.app.data.repository;

import com.lago.app.data.cache.ChartMemoryCache;
import com.lago.app.data.local.prefs.UserPreferences;
import com.lago.app.data.remote.api.ChartApiService;
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
public final class ChartRepositoryImpl_Factory implements Factory<ChartRepositoryImpl> {
  private final Provider<ChartApiService> apiServiceProvider;

  private final Provider<UserPreferences> userPreferencesProvider;

  private final Provider<ChartMemoryCache> memoryCacheProvider;

  public ChartRepositoryImpl_Factory(Provider<ChartApiService> apiServiceProvider,
      Provider<UserPreferences> userPreferencesProvider,
      Provider<ChartMemoryCache> memoryCacheProvider) {
    this.apiServiceProvider = apiServiceProvider;
    this.userPreferencesProvider = userPreferencesProvider;
    this.memoryCacheProvider = memoryCacheProvider;
  }

  @Override
  public ChartRepositoryImpl get() {
    return newInstance(apiServiceProvider.get(), userPreferencesProvider.get(), memoryCacheProvider.get());
  }

  public static ChartRepositoryImpl_Factory create(Provider<ChartApiService> apiServiceProvider,
      Provider<UserPreferences> userPreferencesProvider,
      Provider<ChartMemoryCache> memoryCacheProvider) {
    return new ChartRepositoryImpl_Factory(apiServiceProvider, userPreferencesProvider, memoryCacheProvider);
  }

  public static ChartRepositoryImpl newInstance(ChartApiService apiService,
      UserPreferences userPreferences, ChartMemoryCache memoryCache) {
    return new ChartRepositoryImpl(apiService, userPreferences, memoryCache);
  }
}
