package com.lago.app.domain.usecase;

import com.lago.app.domain.repository.StudyRepository;
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
public final class SolveRandomQuizUseCase_Factory implements Factory<SolveRandomQuizUseCase> {
  private final Provider<StudyRepository> repositoryProvider;

  public SolveRandomQuizUseCase_Factory(Provider<StudyRepository> repositoryProvider) {
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public SolveRandomQuizUseCase get() {
    return newInstance(repositoryProvider.get());
  }

  public static SolveRandomQuizUseCase_Factory create(
      Provider<StudyRepository> repositoryProvider) {
    return new SolveRandomQuizUseCase_Factory(repositoryProvider);
  }

  public static SolveRandomQuizUseCase newInstance(StudyRepository repository) {
    return new SolveRandomQuizUseCase(repository);
  }
}
