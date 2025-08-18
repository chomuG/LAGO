package com.lago.app.data.local;

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
public final class LocalDataSource_Factory implements Factory<LocalDataSource> {
  @Override
  public LocalDataSource get() {
    return newInstance();
  }

  public static LocalDataSource_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static LocalDataSource newInstance() {
    return new LocalDataSource();
  }

  private static final class InstanceHolder {
    static final LocalDataSource_Factory INSTANCE = new LocalDataSource_Factory();
  }
}
