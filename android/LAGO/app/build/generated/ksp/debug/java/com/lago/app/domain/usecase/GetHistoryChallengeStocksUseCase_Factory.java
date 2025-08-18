package com.lago.app.domain.usecase;

import com.lago.app.domain.repository.HistoryChallengeRepository;
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
public final class GetHistoryChallengeStocksUseCase_Factory implements Factory<GetHistoryChallengeStocksUseCase> {
  private final Provider<HistoryChallengeRepository> repositoryProvider;

  public GetHistoryChallengeStocksUseCase_Factory(
      Provider<HistoryChallengeRepository> repositoryProvider) {
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public GetHistoryChallengeStocksUseCase get() {
    return newInstance(repositoryProvider.get());
  }

  public static GetHistoryChallengeStocksUseCase_Factory create(
      Provider<HistoryChallengeRepository> repositoryProvider) {
    return new GetHistoryChallengeStocksUseCase_Factory(repositoryProvider);
  }

  public static GetHistoryChallengeStocksUseCase newInstance(
      HistoryChallengeRepository repository) {
    return new GetHistoryChallengeStocksUseCase(repository);
  }
}
