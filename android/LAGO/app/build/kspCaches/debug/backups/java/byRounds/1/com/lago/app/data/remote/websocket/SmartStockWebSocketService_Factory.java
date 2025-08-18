package com.lago.app.data.remote.websocket;

import com.google.gson.Gson;
import com.lago.app.data.cache.RealTimeStockCache;
import com.lago.app.data.local.prefs.UserPreferences;
import com.lago.app.data.remote.RemoteDataSource;
import com.lago.app.data.scheduler.SmartUpdateScheduler;
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
public final class SmartStockWebSocketService_Factory implements Factory<SmartStockWebSocketService> {
  private final Provider<UserPreferences> userPreferencesProvider;

  private final Provider<RealTimeStockCache> realTimeCacheProvider;

  private final Provider<SmartUpdateScheduler> smartUpdateSchedulerProvider;

  private final Provider<Gson> gsonProvider;

  private final Provider<RemoteDataSource> remoteDataSourceProvider;

  public SmartStockWebSocketService_Factory(Provider<UserPreferences> userPreferencesProvider,
      Provider<RealTimeStockCache> realTimeCacheProvider,
      Provider<SmartUpdateScheduler> smartUpdateSchedulerProvider, Provider<Gson> gsonProvider,
      Provider<RemoteDataSource> remoteDataSourceProvider) {
    this.userPreferencesProvider = userPreferencesProvider;
    this.realTimeCacheProvider = realTimeCacheProvider;
    this.smartUpdateSchedulerProvider = smartUpdateSchedulerProvider;
    this.gsonProvider = gsonProvider;
    this.remoteDataSourceProvider = remoteDataSourceProvider;
  }

  @Override
  public SmartStockWebSocketService get() {
    return newInstance(userPreferencesProvider.get(), realTimeCacheProvider.get(), smartUpdateSchedulerProvider.get(), gsonProvider.get(), remoteDataSourceProvider.get());
  }

  public static SmartStockWebSocketService_Factory create(
      Provider<UserPreferences> userPreferencesProvider,
      Provider<RealTimeStockCache> realTimeCacheProvider,
      Provider<SmartUpdateScheduler> smartUpdateSchedulerProvider, Provider<Gson> gsonProvider,
      Provider<RemoteDataSource> remoteDataSourceProvider) {
    return new SmartStockWebSocketService_Factory(userPreferencesProvider, realTimeCacheProvider, smartUpdateSchedulerProvider, gsonProvider, remoteDataSourceProvider);
  }

  public static SmartStockWebSocketService newInstance(UserPreferences userPreferences,
      RealTimeStockCache realTimeCache, SmartUpdateScheduler smartUpdateScheduler, Gson gson,
      RemoteDataSource remoteDataSource) {
    return new SmartStockWebSocketService(userPreferences, realTimeCache, smartUpdateScheduler, gson, remoteDataSource);
  }
}
