package com.lago.app.presentation.viewmodel;

import com.lago.app.data.local.prefs.UserPreferences;
import com.lago.app.domain.usecase.GetDailyQuizUseCase;
import com.lago.app.domain.usecase.SolveDailyQuizUseCase;
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
public final class DailyQuizViewModel_Factory implements Factory<DailyQuizViewModel> {
  private final Provider<GetDailyQuizUseCase> getDailyQuizUseCaseProvider;

  private final Provider<SolveDailyQuizUseCase> solveDailyQuizUseCaseProvider;

  private final Provider<UserPreferences> userPreferencesProvider;

  public DailyQuizViewModel_Factory(Provider<GetDailyQuizUseCase> getDailyQuizUseCaseProvider,
      Provider<SolveDailyQuizUseCase> solveDailyQuizUseCaseProvider,
      Provider<UserPreferences> userPreferencesProvider) {
    this.getDailyQuizUseCaseProvider = getDailyQuizUseCaseProvider;
    this.solveDailyQuizUseCaseProvider = solveDailyQuizUseCaseProvider;
    this.userPreferencesProvider = userPreferencesProvider;
  }

  @Override
  public DailyQuizViewModel get() {
    return newInstance(getDailyQuizUseCaseProvider.get(), solveDailyQuizUseCaseProvider.get(), userPreferencesProvider.get());
  }

  public static DailyQuizViewModel_Factory create(
      Provider<GetDailyQuizUseCase> getDailyQuizUseCaseProvider,
      Provider<SolveDailyQuizUseCase> solveDailyQuizUseCaseProvider,
      Provider<UserPreferences> userPreferencesProvider) {
    return new DailyQuizViewModel_Factory(getDailyQuizUseCaseProvider, solveDailyQuizUseCaseProvider, userPreferencesProvider);
  }

  public static DailyQuizViewModel newInstance(GetDailyQuizUseCase getDailyQuizUseCase,
      SolveDailyQuizUseCase solveDailyQuizUseCase, UserPreferences userPreferences) {
    return new DailyQuizViewModel(getDailyQuizUseCase, solveDailyQuizUseCase, userPreferences);
  }
}
