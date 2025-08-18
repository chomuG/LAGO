package com.lago.app.data.remote;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import retrofit2.Retrofit;

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
public final class RemoteDataSource_Factory implements Factory<RemoteDataSource> {
  private final Provider<Retrofit> retrofitProvider;

  public RemoteDataSource_Factory(Provider<Retrofit> retrofitProvider) {
    this.retrofitProvider = retrofitProvider;
  }

  @Override
  public RemoteDataSource get() {
    return newInstance(retrofitProvider.get());
  }

  public static RemoteDataSource_Factory create(Provider<Retrofit> retrofitProvider) {
    return new RemoteDataSource_Factory(retrofitProvider);
  }

  public static RemoteDataSource newInstance(Retrofit retrofit) {
    return new RemoteDataSource(retrofit);
  }
}
