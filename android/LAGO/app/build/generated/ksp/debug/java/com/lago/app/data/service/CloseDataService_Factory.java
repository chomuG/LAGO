package com.lago.app.data.service;

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
public final class CloseDataService_Factory implements Factory<CloseDataService> {
  private final Provider<ChartApiService> chartApiServiceProvider;

  public CloseDataService_Factory(Provider<ChartApiService> chartApiServiceProvider) {
    this.chartApiServiceProvider = chartApiServiceProvider;
  }

  @Override
  public CloseDataService get() {
    return newInstance(chartApiServiceProvider.get());
  }

  public static CloseDataService_Factory create(Provider<ChartApiService> chartApiServiceProvider) {
    return new CloseDataService_Factory(chartApiServiceProvider);
  }

  public static CloseDataService newInstance(ChartApiService chartApiService) {
    return new CloseDataService(chartApiService);
  }
}
