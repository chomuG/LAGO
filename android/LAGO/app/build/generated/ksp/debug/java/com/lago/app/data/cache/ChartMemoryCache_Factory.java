package com.lago.app.data.cache;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
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
public final class ChartMemoryCache_Factory implements Factory<ChartMemoryCache> {
  @Override
  public ChartMemoryCache get() {
    return newInstance();
  }

  public static ChartMemoryCache_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static ChartMemoryCache newInstance() {
    return new ChartMemoryCache();
  }

  private static final class InstanceHolder {
    static final ChartMemoryCache_Factory INSTANCE = new ChartMemoryCache_Factory();
  }
}
