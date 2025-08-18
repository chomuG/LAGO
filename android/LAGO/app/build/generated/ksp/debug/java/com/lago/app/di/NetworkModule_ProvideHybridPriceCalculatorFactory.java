package com.lago.app.di;

import com.lago.app.data.service.InitialPriceService;
import com.lago.app.util.HybridPriceCalculator;
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
public final class NetworkModule_ProvideHybridPriceCalculatorFactory implements Factory<HybridPriceCalculator> {
  private final Provider<InitialPriceService> initialPriceServiceProvider;

  public NetworkModule_ProvideHybridPriceCalculatorFactory(
      Provider<InitialPriceService> initialPriceServiceProvider) {
    this.initialPriceServiceProvider = initialPriceServiceProvider;
  }

  @Override
  public HybridPriceCalculator get() {
    return provideHybridPriceCalculator(initialPriceServiceProvider.get());
  }

  public static NetworkModule_ProvideHybridPriceCalculatorFactory create(
      Provider<InitialPriceService> initialPriceServiceProvider) {
    return new NetworkModule_ProvideHybridPriceCalculatorFactory(initialPriceServiceProvider);
  }

  public static HybridPriceCalculator provideHybridPriceCalculator(
      InitialPriceService initialPriceService) {
    return Preconditions.checkNotNullFromProvides(NetworkModule.INSTANCE.provideHybridPriceCalculator(initialPriceService));
  }
}
