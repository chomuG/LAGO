package com.lago.app.domain.usecase;

import com.lago.app.domain.repository.UserRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata("javax.inject.Singleton")
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
public final class CheckLoginStatusUseCase_Factory implements Factory<CheckLoginStatusUseCase> {
  private final Provider<UserRepository> userRepositoryProvider;

  public CheckLoginStatusUseCase_Factory(Provider<UserRepository> userRepositoryProvider) {
    this.userRepositoryProvider = userRepositoryProvider;
  }

  @Override
  public CheckLoginStatusUseCase get() {
    return newInstance(userRepositoryProvider.get());
  }

  public static CheckLoginStatusUseCase_Factory create(
      Provider<UserRepository> userRepositoryProvider) {
    return new CheckLoginStatusUseCase_Factory(userRepositoryProvider);
  }

  public static CheckLoginStatusUseCase newInstance(UserRepository userRepository) {
    return new CheckLoginStatusUseCase(userRepository);
  }
}
