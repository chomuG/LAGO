package com.lago.app.di;

import com.lago.app.data.local.LagoDatabase;
import com.lago.app.data.local.dao.ChartCacheDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata
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
public final class DatabaseModule_ProvideChartCacheDaoFactory implements Factory<ChartCacheDao> {
  private final Provider<LagoDatabase> databaseProvider;

  public DatabaseModule_ProvideChartCacheDaoFactory(Provider<LagoDatabase> databaseProvider) {
    this.databaseProvider = databaseProvider;
  }

  @Override
  public ChartCacheDao get() {
    return provideChartCacheDao(databaseProvider.get());
  }

  public static DatabaseModule_ProvideChartCacheDaoFactory create(
      Provider<LagoDatabase> databaseProvider) {
    return new DatabaseModule_ProvideChartCacheDaoFactory(databaseProvider);
  }

  public static ChartCacheDao provideChartCacheDao(LagoDatabase database) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideChartCacheDao(database));
  }
}
