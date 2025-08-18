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
public final class FavoriteCache_Factory implements Factory<FavoriteCache> {
  @Override
  public FavoriteCache get() {
    return newInstance();
  }

  public static FavoriteCache_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static FavoriteCache newInstance() {
    return new FavoriteCache();
  }

  private static final class InstanceHolder {
    static final FavoriteCache_Factory INSTANCE = new FavoriteCache_Factory();
  }
}
