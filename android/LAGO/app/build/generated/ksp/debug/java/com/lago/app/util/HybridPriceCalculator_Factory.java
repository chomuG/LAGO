package com.lago.app.util;

import com.lago.app.data.service.InitialPriceService;
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
public final class HybridPriceCalculator_Factory implements Factory<HybridPriceCalculator> {
  private final Provider<InitialPriceService> initialPriceServiceProvider;

  public HybridPriceCalculator_Factory(Provider<InitialPriceService> initialPriceServiceProvider) {
    this.initialPriceServiceProvider = initialPriceServiceProvider;
  }

  @Override
  public HybridPriceCalculator get() {
    return newInstance(initialPriceServiceProvider.get());
  }

  public static HybridPriceCalculator_Factory create(
      Provider<InitialPriceService> initialPriceServiceProvider) {
    return new HybridPriceCalculator_Factory(initialPriceServiceProvider);
  }

  public static HybridPriceCalculator newInstance(InitialPriceService initialPriceService) {
    return new HybridPriceCalculator(initialPriceService);
  }
}
