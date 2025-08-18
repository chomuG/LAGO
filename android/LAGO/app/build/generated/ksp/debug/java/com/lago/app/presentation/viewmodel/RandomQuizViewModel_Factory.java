package com.lago.app.presentation.viewmodel;

import com.lago.app.data.local.prefs.UserPreferences;
import com.lago.app.domain.usecase.GetRandomQuizUseCase;
import com.lago.app.domain.usecase.SolveRandomQuizUseCase;
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
public final class RandomQuizViewModel_Factory implements Factory<RandomQuizViewModel> {
  private final Provider<GetRandomQuizUseCase> getRandomQuizUseCaseProvider;

  private final Provider<SolveRandomQuizUseCase> solveRandomQuizUseCaseProvider;

  private final Provider<UserPreferences> userPreferencesProvider;

  public RandomQuizViewModel_Factory(Provider<GetRandomQuizUseCase> getRandomQuizUseCaseProvider,
      Provider<SolveRandomQuizUseCase> solveRandomQuizUseCaseProvider,
      Provider<UserPreferences> userPreferencesProvider) {
    this.getRandomQuizUseCaseProvider = getRandomQuizUseCaseProvider;
    this.solveRandomQuizUseCaseProvider = solveRandomQuizUseCaseProvider;
    this.userPreferencesProvider = userPreferencesProvider;
  }

  @Override
  public RandomQuizViewModel get() {
    return newInstance(getRandomQuizUseCaseProvider.get(), solveRandomQuizUseCaseProvider.get(), userPreferencesProvider.get());
  }

  public static RandomQuizViewModel_Factory create(
      Provider<GetRandomQuizUseCase> getRandomQuizUseCaseProvider,
      Provider<SolveRandomQuizUseCase> solveRandomQuizUseCaseProvider,
      Provider<UserPreferences> userPreferencesProvider) {
    return new RandomQuizViewModel_Factory(getRandomQuizUseCaseProvider, solveRandomQuizUseCaseProvider, userPreferencesProvider);
  }

  public static RandomQuizViewModel newInstance(GetRandomQuizUseCase getRandomQuizUseCase,
      SolveRandomQuizUseCase solveRandomQuizUseCase, UserPreferences userPreferences) {
    return new RandomQuizViewModel(getRandomQuizUseCase, solveRandomQuizUseCase, userPreferences);
  }
}
