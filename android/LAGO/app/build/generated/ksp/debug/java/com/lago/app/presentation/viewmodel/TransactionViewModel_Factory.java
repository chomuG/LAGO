package com.lago.app.presentation.viewmodel;

import com.lago.app.domain.usecase.GetTransactionsUseCase;
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
public final class TransactionViewModel_Factory implements Factory<TransactionViewModel> {
  private final Provider<GetTransactionsUseCase> getTransactionsUseCaseProvider;

  public TransactionViewModel_Factory(
      Provider<GetTransactionsUseCase> getTransactionsUseCaseProvider) {
    this.getTransactionsUseCaseProvider = getTransactionsUseCaseProvider;
  }

  @Override
  public TransactionViewModel get() {
    return newInstance(getTransactionsUseCaseProvider.get());
  }

  public static TransactionViewModel_Factory create(
      Provider<GetTransactionsUseCase> getTransactionsUseCaseProvider) {
    return new TransactionViewModel_Factory(getTransactionsUseCaseProvider);
  }

  public static TransactionViewModel newInstance(GetTransactionsUseCase getTransactionsUseCase) {
    return new TransactionViewModel(getTransactionsUseCase);
  }
}
