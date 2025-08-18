package com.lago.app.presentation.ui.chart.state;

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
public final class ChartStateManager_Factory implements Factory<ChartStateManager> {
  @Override
  public ChartStateManager get() {
    return newInstance();
  }

  public static ChartStateManager_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static ChartStateManager newInstance() {
    return new ChartStateManager();
  }

  private static final class InstanceHolder {
    static final ChartStateManager_Factory INSTANCE = new ChartStateManager_Factory();
  }
}
