package com.lago.app.presentation.viewmodel;

import com.lago.app.data.local.prefs.UserPreferences;
import com.lago.app.domain.usecase.GetTermsUseCase;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
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
public final class WordbookViewModel_Factory implements Factory<WordbookViewModel> {
  private final Provider<GetTermsUseCase> getTermsUseCaseProvider;

  private final Provider<UserPreferences> userPreferencesProvider;

  public WordbookViewModel_Factory(Provider<GetTermsUseCase> getTermsUseCaseProvider,
      Provider<UserPreferences> userPreferencesProvider) {
    this.getTermsUseCaseProvider = getTermsUseCaseProvider;
    this.userPreferencesProvider = userPreferencesProvider;
  }

  @Override
  public WordbookViewModel get() {
    return newInstance(getTermsUseCaseProvider.get(), userPreferencesProvider.get());
  }

  public static WordbookViewModel_Factory create(Provider<GetTermsUseCase> getTermsUseCaseProvider,
      Provider<UserPreferences> userPreferencesProvider) {
    return new WordbookViewModel_Factory(getTermsUseCaseProvider, userPreferencesProvider);
  }

  public static WordbookViewModel newInstance(GetTermsUseCase getTermsUseCase,
      UserPreferences userPreferences) {
    return new WordbookViewModel(getTermsUseCase, userPreferences);
  }
}
