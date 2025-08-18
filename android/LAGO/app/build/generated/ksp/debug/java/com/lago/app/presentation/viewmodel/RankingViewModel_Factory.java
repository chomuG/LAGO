package com.lago.app.presentation.viewmodel;

import com.lago.app.data.local.prefs.UserPreferences;
import com.lago.app.domain.repository.RankingRepository;
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
public final class RankingViewModel_Factory implements Factory<RankingViewModel> {
  private final Provider<RankingRepository> rankingRepositoryProvider;

  private final Provider<UserPreferences> userPreferencesProvider;

  public RankingViewModel_Factory(Provider<RankingRepository> rankingRepositoryProvider,
      Provider<UserPreferences> userPreferencesProvider) {
    this.rankingRepositoryProvider = rankingRepositoryProvider;
    this.userPreferencesProvider = userPreferencesProvider;
  }

  @Override
  public RankingViewModel get() {
    return newInstance(rankingRepositoryProvider.get(), userPreferencesProvider.get());
  }

  public static RankingViewModel_Factory create(
      Provider<RankingRepository> rankingRepositoryProvider,
      Provider<UserPreferences> userPreferencesProvider) {
    return new RankingViewModel_Factory(rankingRepositoryProvider, userPreferencesProvider);
  }

  public static RankingViewModel newInstance(RankingRepository rankingRepository,
      UserPreferences userPreferences) {
    return new RankingViewModel(rankingRepository, userPreferences);
  }
}
