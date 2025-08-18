package com.lago.app.data.local.prefs;

import android.content.Context;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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
public final class PatternAnalysisPreferences_Factory implements Factory<PatternAnalysisPreferences> {
  private final Provider<Context> contextProvider;

  public PatternAnalysisPreferences_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public PatternAnalysisPreferences get() {
    return newInstance(contextProvider.get());
  }

  public static PatternAnalysisPreferences_Factory create(Provider<Context> contextProvider) {
    return new PatternAnalysisPreferences_Factory(contextProvider);
  }

  public static PatternAnalysisPreferences newInstance(Context context) {
    return new PatternAnalysisPreferences(context);
  }
}
