package com.lago.app.data.repository;

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
public final class StockListRepositoryImpl_Factory implements Factory<StockListRepositoryImpl> {
  private final Provider<ChartApiService> apiServiceProvider;

  private final Provider<UserPreferences> userPreferencesProvider;

  public StockListRepositoryImpl_Factory(Provider<ChartApiService> apiServiceProvider,
      Provider<UserPreferences> userPreferencesProvider) {
    this.apiServiceProvider = apiServiceProvider;
    this.userPreferencesProvider = userPreferencesProvider;
  }

  @Override
  public StockListRepositoryImpl get() {
    return newInstance(apiServiceProvider.get(), userPreferencesProvider.get());
  }

  public static StockListRepositoryImpl_Factory create(Provider<ChartApiService> apiServiceProvider,
      Provider<UserPreferences> userPreferencesProvider) {
    return new StockListRepositoryImpl_Factory(apiServiceProvider, userPreferencesProvider);
  }

  public static StockListRepositoryImpl newInstance(ChartApiService apiService,
      UserPreferences userPreferences) {
    return new StockListRepositoryImpl(apiService, userPreferences);
  }
}
