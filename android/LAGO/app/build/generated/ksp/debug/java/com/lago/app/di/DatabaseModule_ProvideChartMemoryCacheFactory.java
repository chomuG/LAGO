package com.lago.app.di;

import com.lago.app.data.cache.ChartMemoryCache;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
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
public final class DatabaseModule_ProvideChartMemoryCacheFactory implements Factory<ChartMemoryCache> {
  @Override
  public ChartMemoryCache get() {
    return provideChartMemoryCache();
  }

  public static DatabaseModule_ProvideChartMemoryCacheFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static ChartMemoryCache provideChartMemoryCache() {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideChartMemoryCache());
  }

  private static final class InstanceHolder {
    static final DatabaseModule_ProvideChartMemoryCacheFactory INSTANCE = new DatabaseModule_ProvideChartMemoryCacheFactory();
  }
}
