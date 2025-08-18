package com.lago.app.domain.usecase;

import com.lago.app.domain.repository.TransactionRepository;
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
public final class GetTransactionsUseCase_Factory implements Factory<GetTransactionsUseCase> {
  private final Provider<TransactionRepository> transactionRepositoryProvider;

  public GetTransactionsUseCase_Factory(
      Provider<TransactionRepository> transactionRepositoryProvider) {
    this.transactionRepositoryProvider = transactionRepositoryProvider;
  }

  @Override
  public GetTransactionsUseCase get() {
    return newInstance(transactionRepositoryProvider.get());
  }

  public static GetTransactionsUseCase_Factory create(
      Provider<TransactionRepository> transactionRepositoryProvider) {
    return new GetTransactionsUseCase_Factory(transactionRepositoryProvider);
  }

  public static GetTransactionsUseCase newInstance(TransactionRepository transactionRepository) {
    return new GetTransactionsUseCase(transactionRepository);
  }
}
