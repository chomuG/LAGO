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
public final class LogoutUseCase_Factory implements Factory<LogoutUseCase> {
  private final Provider<UserRepository> userRepositoryProvider;

  public LogoutUseCase_Factory(Provider<UserRepository> userRepositoryProvider) {
    this.userRepositoryProvider = userRepositoryProvider;
  }

  @Override
  public LogoutUseCase get() {
    return newInstance(userRepositoryProvider.get());
  }

  public static LogoutUseCase_Factory create(Provider<UserRepository> userRepositoryProvider) {
    return new LogoutUseCase_Factory(userRepositoryProvider);
  }

  public static LogoutUseCase newInstance(UserRepository userRepository) {
    return new LogoutUseCase(userRepository);
  }
}
