package com.lago.app.data.local.prefs;

import android.content.SharedPreferences;
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
public final class UserPreferences_Factory implements Factory<UserPreferences> {
  private final Provider<SharedPreferences> sharedPreferencesProvider;

  public UserPreferences_Factory(Provider<SharedPreferences> sharedPreferencesProvider) {
    this.sharedPreferencesProvider = sharedPreferencesProvider;
  }

  @Override
  public UserPreferences get() {
    return newInstance(sharedPreferencesProvider.get());
  }

  public static UserPreferences_Factory create(
      Provider<SharedPreferences> sharedPreferencesProvider) {
    return new UserPreferences_Factory(sharedPreferencesProvider);
  }

  public static UserPreferences newInstance(SharedPreferences sharedPreferences) {
    return new UserPreferences(sharedPreferences);
  }
}
