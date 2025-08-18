package com.lago.app.di;

import com.lago.app.data.remote.api.ChartApiService;
import com.lago.app.data.service.InitialPriceService;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
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
public final class NetworkModule_ProvideInitialPriceServiceFactory implements Factory<InitialPriceService> {
  private final Provider<ChartApiService> chartApiServiceProvider;

  public NetworkModule_ProvideInitialPriceServiceFactory(
      Provider<ChartApiService> chartApiServiceProvider) {
    this.chartApiServiceProvider = chartApiServiceProvider;
  }

  @Override
  public InitialPriceService get() {
    return provideInitialPriceService(chartApiServiceProvider.get());
  }

  public static NetworkModule_ProvideInitialPriceServiceFactory create(
      Provider<ChartApiService> chartApiServiceProvider) {
    return new NetworkModule_ProvideInitialPriceServiceFactory(chartApiServiceProvider);
  }

  public static InitialPriceService provideInitialPriceService(ChartApiService chartApiService) {
    return Preconditions.checkNotNullFromProvides(NetworkModule.INSTANCE.provideInitialPriceService(chartApiService));
  }
}
