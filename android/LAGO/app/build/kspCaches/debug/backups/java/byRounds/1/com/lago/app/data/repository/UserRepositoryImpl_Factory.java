package com.lago.app.data.repository;

import com.lago.app.data.local.LocalDataSource;
import com.lago.app.data.local.prefs.UserPreferences;
import com.lago.app.data.remote.RemoteDataSource;
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
public final class UserRepositoryImpl_Factory implements Factory<UserRepositoryImpl> {
  private final Provider<RemoteDataSource> remoteDataSourceProvider;

  private final Provider<LocalDataSource> localDataSourceProvider;

  private final Provider<UserPreferences> userPreferencesProvider;

  public UserRepositoryImpl_Factory(Provider<RemoteDataSource> remoteDataSourceProvider,
      Provider<LocalDataSource> localDataSourceProvider,
      Provider<UserPreferences> userPreferencesProvider) {
    this.remoteDataSourceProvider = remoteDataSourceProvider;
    this.localDataSourceProvider = localDataSourceProvider;
    this.userPreferencesProvider = userPreferencesProvider;
  }

  @Override
  public UserRepositoryImpl get() {
    return newInstance(remoteDataSourceProvider.get(), localDataSourceProvider.get(), userPreferencesProvider.get());
  }

  public static UserRepositoryImpl_Factory create(
      Provider<RemoteDataSource> remoteDataSourceProvider,
      Provider<LocalDataSource> localDataSourceProvider,
      Provider<UserPreferences> userPreferencesProvider) {
    return new UserRepositoryImpl_Factory(remoteDataSourceProvider, localDataSourceProvider, userPreferencesProvider);
  }

  public static UserRepositoryImpl newInstance(RemoteDataSource remoteDataSource,
      LocalDataSource localDataSource, UserPreferences userPreferences) {
    return new UserRepositoryImpl(remoteDataSource, localDataSource, userPreferences);
  }
}
