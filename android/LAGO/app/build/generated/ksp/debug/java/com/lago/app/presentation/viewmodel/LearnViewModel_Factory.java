package com.lago.app.presentation.viewmodel;

import com.lago.app.data.local.prefs.UserPreferences;
import com.lago.app.domain.usecase.GetDailyQuizStreakUseCase;
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
public final class LearnViewModel_Factory implements Factory<LearnViewModel> {
  private final Provider<GetDailyQuizStreakUseCase> getDailyQuizStreakUseCaseProvider;

  private final Provider<UserPreferences> userPreferencesProvider;

  public LearnViewModel_Factory(
      Provider<GetDailyQuizStreakUseCase> getDailyQuizStreakUseCaseProvider,
      Provider<UserPreferences> userPreferencesProvider) {
    this.getDailyQuizStreakUseCaseProvider = getDailyQuizStreakUseCaseProvider;
    this.userPreferencesProvider = userPreferencesProvider;
  }

  @Override
  public LearnViewModel get() {
    return newInstance(getDailyQuizStreakUseCaseProvider.get(), userPreferencesProvider.get());
  }

  public static LearnViewModel_Factory create(
      Provider<GetDailyQuizStreakUseCase> getDailyQuizStreakUseCaseProvider,
      Provider<UserPreferences> userPreferencesProvider) {
    return new LearnViewModel_Factory(getDailyQuizStreakUseCaseProvider, userPreferencesProvider);
  }

  public static LearnViewModel newInstance(GetDailyQuizStreakUseCase getDailyQuizStreakUseCase,
      UserPreferences userPreferences) {
    return new LearnViewModel(getDailyQuizStreakUseCase, userPreferences);
  }
}
