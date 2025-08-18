package com.lago.app.data.repository;

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
public final class TransactionRepositoryImpl_Factory implements Factory<TransactionRepositoryImpl> {
  private final Provider<RemoteDataSource> remoteDataSourceProvider;

  public TransactionRepositoryImpl_Factory(Provider<RemoteDataSource> remoteDataSourceProvider) {
    this.remoteDataSourceProvider = remoteDataSourceProvider;
  }

  @Override
  public TransactionRepositoryImpl get() {
    return newInstance(remoteDataSourceProvider.get());
  }

  public static TransactionRepositoryImpl_Factory create(
      Provider<RemoteDataSource> remoteDataSourceProvider) {
    return new TransactionRepositoryImpl_Factory(remoteDataSourceProvider);
  }

  public static TransactionRepositoryImpl newInstance(RemoteDataSource remoteDataSource) {
    return new TransactionRepositoryImpl(remoteDataSource);
  }
}
