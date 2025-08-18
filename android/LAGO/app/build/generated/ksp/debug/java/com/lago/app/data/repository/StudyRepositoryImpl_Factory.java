package com.lago.app.data.repository;

import com.lago.app.data.remote.StudyApiService;
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
public final class StudyRepositoryImpl_Factory implements Factory<StudyRepositoryImpl> {
  private final Provider<StudyApiService> studyApiServiceProvider;

  public StudyRepositoryImpl_Factory(Provider<StudyApiService> studyApiServiceProvider) {
    this.studyApiServiceProvider = studyApiServiceProvider;
  }

  @Override
  public StudyRepositoryImpl get() {
    return newInstance(studyApiServiceProvider.get());
  }

  public static StudyRepositoryImpl_Factory create(
      Provider<StudyApiService> studyApiServiceProvider) {
    return new StudyRepositoryImpl_Factory(studyApiServiceProvider);
  }

  public static StudyRepositoryImpl newInstance(StudyApiService studyApiService) {
    return new StudyRepositoryImpl(studyApiService);
  }
}
