package com.lago.app.di;

import com.lago.app.data.remote.api.HistoryChallengeApiService;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
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
public final class NetworkModule_ProvideHistoryChallengeApiServiceFactory implements Factory<HistoryChallengeApiService> {
  private final Provider<Retrofit> retrofitProvider;

  public NetworkModule_ProvideHistoryChallengeApiServiceFactory(
      Provider<Retrofit> retrofitProvider) {
    this.retrofitProvider = retrofitProvider;
  }

  @Override
  public HistoryChallengeApiService get() {
    return provideHistoryChallengeApiService(retrofitProvider.get());
  }

  public static NetworkModule_ProvideHistoryChallengeApiServiceFactory create(
      Provider<Retrofit> retrofitProvider) {
    return new NetworkModule_ProvideHistoryChallengeApiServiceFactory(retrofitProvider);
  }

  public static HistoryChallengeApiService provideHistoryChallengeApiService(Retrofit retrofit) {
    return Preconditions.checkNotNullFromProvides(NetworkModule.INSTANCE.provideHistoryChallengeApiService(retrofit));
  }
}
