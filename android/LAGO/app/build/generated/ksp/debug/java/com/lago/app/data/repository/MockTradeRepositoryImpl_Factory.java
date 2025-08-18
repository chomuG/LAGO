package com.lago.app.data.repository;

import com.lago.app.data.cache.FavoriteCache;
import com.lago.app.data.local.prefs.UserPreferences;
import com.lago.app.data.remote.ApiService;
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
public final class MockTradeRepositoryImpl_Factory implements Factory<MockTradeRepositoryImpl> {
  private final Provider<ChartApiService> apiServiceProvider;

  private final Provider<ApiService> userApiServiceProvider;

  private final Provider<UserPreferences> userPreferencesProvider;

  private final Provider<FavoriteCache> favoriteCacheProvider;

  public MockTradeRepositoryImpl_Factory(Provider<ChartApiService> apiServiceProvider,
      Provider<ApiService> userApiServiceProvider,
      Provider<UserPreferences> userPreferencesProvider,
      Provider<FavoriteCache> favoriteCacheProvider) {
    this.apiServiceProvider = apiServiceProvider;
    this.userApiServiceProvider = userApiServiceProvider;
    this.userPreferencesProvider = userPreferencesProvider;
    this.favoriteCacheProvider = favoriteCacheProvider;
  }

  @Override
  public MockTradeRepositoryImpl get() {
    return newInstance(apiServiceProvider.get(), userApiServiceProvider.get(), userPreferencesProvider.get(), favoriteCacheProvider.get());
  }

  public static MockTradeRepositoryImpl_Factory create(Provider<ChartApiService> apiServiceProvider,
      Provider<ApiService> userApiServiceProvider,
      Provider<UserPreferences> userPreferencesProvider,
      Provider<FavoriteCache> favoriteCacheProvider) {
    return new MockTradeRepositoryImpl_Factory(apiServiceProvider, userApiServiceProvider, userPreferencesProvider, favoriteCacheProvider);
  }

  public static MockTradeRepositoryImpl newInstance(ChartApiService apiService,
      ApiService userApiService, UserPreferences userPreferences, FavoriteCache favoriteCache) {
    return new MockTradeRepositoryImpl(apiService, userApiService, userPreferences, favoriteCache);
  }
}
