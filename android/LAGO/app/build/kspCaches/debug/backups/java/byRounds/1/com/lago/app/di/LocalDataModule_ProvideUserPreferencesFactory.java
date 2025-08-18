package com.lago.app.di;

import android.content.SharedPreferences;
import com.lago.app.data.local.prefs.UserPreferences;
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
public final class LocalDataModule_ProvideUserPreferencesFactory implements Factory<UserPreferences> {
  private final Provider<SharedPreferences> sharedPreferencesProvider;

  public LocalDataModule_ProvideUserPreferencesFactory(
      Provider<SharedPreferences> sharedPreferencesProvider) {
    this.sharedPreferencesProvider = sharedPreferencesProvider;
  }

  @Override
  public UserPreferences get() {
    return provideUserPreferences(sharedPreferencesProvider.get());
  }

  public static LocalDataModule_ProvideUserPreferencesFactory create(
      Provider<SharedPreferences> sharedPreferencesProvider) {
    return new LocalDataModule_ProvideUserPreferencesFactory(sharedPreferencesProvider);
  }

  public static UserPreferences provideUserPreferences(SharedPreferences sharedPreferences) {
    return Preconditions.checkNotNullFromProvides(LocalDataModule.INSTANCE.provideUserPreferences(sharedPreferences));
  }
}
