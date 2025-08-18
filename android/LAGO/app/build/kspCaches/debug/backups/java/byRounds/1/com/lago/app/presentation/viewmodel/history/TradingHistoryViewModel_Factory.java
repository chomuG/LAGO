package com.lago.app.presentation.viewmodel.history;

import com.lago.app.domain.repository.MockTradeRepository;
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
public final class TradingHistoryViewModel_Factory implements Factory<TradingHistoryViewModel> {
  private final Provider<MockTradeRepository> mockTradeRepositoryProvider;

  public TradingHistoryViewModel_Factory(
      Provider<MockTradeRepository> mockTradeRepositoryProvider) {
    this.mockTradeRepositoryProvider = mockTradeRepositoryProvider;
  }

  @Override
  public TradingHistoryViewModel get() {
    return newInstance(mockTradeRepositoryProvider.get());
  }

  public static TradingHistoryViewModel_Factory create(
      Provider<MockTradeRepository> mockTradeRepositoryProvider) {
    return new TradingHistoryViewModel_Factory(mockTradeRepositoryProvider);
  }

  public static TradingHistoryViewModel newInstance(MockTradeRepository mockTradeRepository) {
    return new TradingHistoryViewModel(mockTradeRepository);
  }
}
