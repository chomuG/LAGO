package com.lago.app.data.scheduler;

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
public final class SmartUpdateScheduler_Factory implements Factory<SmartUpdateScheduler> {
  @Override
  public SmartUpdateScheduler get() {
    return newInstance();
  }

  public static SmartUpdateScheduler_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static SmartUpdateScheduler newInstance() {
    return new SmartUpdateScheduler();
  }

  private static final class InstanceHolder {
    static final SmartUpdateScheduler_Factory INSTANCE = new SmartUpdateScheduler_Factory();
  }
}
