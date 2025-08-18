package com.lago.app.presentation.viewmodel;

import com.lago.app.data.local.prefs.UserPreferences;
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
public final class OrderHistoryViewModel_Factory implements Factory<OrderHistoryViewModel> {
  private final Provider<TransactionRepository> transactionRepositoryProvider;

  private final Provider<UserPreferences> userPreferencesProvider;

  public OrderHistoryViewModel_Factory(
      Provider<TransactionRepository> transactionRepositoryProvider,
      Provider<UserPreferences> userPreferencesProvider) {
    this.transactionRepositoryProvider = transactionRepositoryProvider;
    this.userPreferencesProvider = userPreferencesProvider;
  }

  @Override
  public OrderHistoryViewModel get() {
    return newInstance(transactionRepositoryProvider.get(), userPreferencesProvider.get());
  }

  public static OrderHistoryViewModel_Factory create(
      Provider<TransactionRepository> transactionRepositoryProvider,
      Provider<UserPreferences> userPreferencesProvider) {
    return new OrderHistoryViewModel_Factory(transactionRepositoryProvider, userPreferencesProvider);
  }

  public static OrderHistoryViewModel newInstance(TransactionRepository transactionRepository,
      UserPreferences userPreferences) {
    return new OrderHistoryViewModel(transactionRepository, userPreferences);
  }
}
