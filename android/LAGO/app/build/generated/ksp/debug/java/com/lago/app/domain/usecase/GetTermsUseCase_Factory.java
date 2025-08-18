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
public final class GetTermsUseCase_Factory implements Factory<GetTermsUseCase> {
  private final Provider<StudyRepository> studyRepositoryProvider;

  public GetTermsUseCase_Factory(Provider<StudyRepository> studyRepositoryProvider) {
    this.studyRepositoryProvider = studyRepositoryProvider;
  }

  @Override
  public GetTermsUseCase get() {
    return newInstance(studyRepositoryProvider.get());
  }

  public static GetTermsUseCase_Factory create(Provider<StudyRepository> studyRepositoryProvider) {
    return new GetTermsUseCase_Factory(studyRepositoryProvider);
  }

  public static GetTermsUseCase newInstance(StudyRepository studyRepository) {
    return new GetTermsUseCase(studyRepository);
  }
}
