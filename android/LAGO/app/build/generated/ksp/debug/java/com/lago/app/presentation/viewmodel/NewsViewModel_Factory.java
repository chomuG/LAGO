package com.lago.app.presentation.viewmodel;

import com.lago.app.data.local.prefs.UserPreferences;
import com.lago.app.domain.usecase.GetInterestNewsUseCase;
import com.lago.app.domain.usecase.GetNewsUseCase;
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
public final class NewsViewModel_Factory implements Factory<NewsViewModel> {
  private final Provider<GetNewsUseCase> getNewsUseCaseProvider;

  private final Provider<GetInterestNewsUseCase> getInterestNewsUseCaseProvider;

  private final Provider<UserPreferences> userPreferencesProvider;

  public NewsViewModel_Factory(Provider<GetNewsUseCase> getNewsUseCaseProvider,
      Provider<GetInterestNewsUseCase> getInterestNewsUseCaseProvider,
      Provider<UserPreferences> userPreferencesProvider) {
    this.getNewsUseCaseProvider = getNewsUseCaseProvider;
    this.getInterestNewsUseCaseProvider = getInterestNewsUseCaseProvider;
    this.userPreferencesProvider = userPreferencesProvider;
  }

  @Override
  public NewsViewModel get() {
    return newInstance(getNewsUseCaseProvider.get(), getInterestNewsUseCaseProvider.get(), userPreferencesProvider.get());
  }

  public static NewsViewModel_Factory create(Provider<GetNewsUseCase> getNewsUseCaseProvider,
      Provider<GetInterestNewsUseCase> getInterestNewsUseCaseProvider,
      Provider<UserPreferences> userPreferencesProvider) {
    return new NewsViewModel_Factory(getNewsUseCaseProvider, getInterestNewsUseCaseProvider, userPreferencesProvider);
  }

  public static NewsViewModel newInstance(GetNewsUseCase getNewsUseCase,
      GetInterestNewsUseCase getInterestNewsUseCase, UserPreferences userPreferences) {
    return new NewsViewModel(getNewsUseCase, getInterestNewsUseCase, userPreferences);
  }
}
