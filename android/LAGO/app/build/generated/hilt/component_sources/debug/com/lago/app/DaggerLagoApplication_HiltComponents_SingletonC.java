package com.lago.app;

import android.app.Activity;
import android.app.Service;
import android.content.SharedPreferences;
import android.view.View;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.gson.Gson;
import com.lago.app.data.cache.ChartMemoryCache;
import com.lago.app.data.cache.FavoriteCache;
import com.lago.app.data.cache.RealTimeStockCache;
import com.lago.app.data.local.LagoDatabase;
import com.lago.app.data.local.LocalDataSource;
import com.lago.app.data.local.dao.ChartCacheDao;
import com.lago.app.data.local.prefs.PatternAnalysisPreferences;
import com.lago.app.data.local.prefs.UserPreferences;
import com.lago.app.data.remote.ApiService;
import com.lago.app.data.remote.NewsApiService;
import com.lago.app.data.remote.RemoteDataSource;
import com.lago.app.data.remote.StudyApiService;
import com.lago.app.data.remote.api.AuthApiService;
import com.lago.app.data.remote.api.ChartApiService;
import com.lago.app.data.remote.api.HistoryChallengeApiService;
import com.lago.app.data.remote.api.PortfolioApiService;
import com.lago.app.data.remote.websocket.SmartStockWebSocketService;
import com.lago.app.data.repository.AuthRepositoryImpl;
import com.lago.app.data.repository.ChartRepositoryImpl;
import com.lago.app.data.repository.HistoryChallengeRepositoryImpl;
import com.lago.app.data.repository.MockTradeRepositoryImpl;
import com.lago.app.data.repository.NewsRepositoryImpl;
import com.lago.app.data.repository.PortfolioRepositoryImpl;
import com.lago.app.data.repository.RankingRepositoryImpl;
import com.lago.app.data.repository.StockListRepositoryImpl;
import com.lago.app.data.repository.StudyRepositoryImpl;
import com.lago.app.data.repository.TransactionRepositoryImpl;
import com.lago.app.data.repository.UserRepositoryImpl;
import com.lago.app.data.scheduler.SmartUpdateScheduler;
import com.lago.app.data.service.CloseDataService;
import com.lago.app.data.service.InitialPriceService;
import com.lago.app.di.DatabaseModule_ProvideChartCacheDaoFactory;
import com.lago.app.di.DatabaseModule_ProvideChartMemoryCacheFactory;
import com.lago.app.di.DatabaseModule_ProvideDatabaseFactory;
import com.lago.app.di.LocalDataModule_ProvideSharedPreferencesFactory;
import com.lago.app.di.LocalDataModule_ProvideUserPreferencesFactory;
import com.lago.app.di.NetworkModule_ProvideApiServiceFactory;
import com.lago.app.di.NetworkModule_ProvideAuthApiServiceFactory;
import com.lago.app.di.NetworkModule_ProvideChartApiServiceFactory;
import com.lago.app.di.NetworkModule_ProvideGsonFactory;
import com.lago.app.di.NetworkModule_ProvideHistoryChallengeApiServiceFactory;
import com.lago.app.di.NetworkModule_ProvideHttpLoggingInterceptorFactory;
import com.lago.app.di.NetworkModule_ProvideHybridPriceCalculatorFactory;
import com.lago.app.di.NetworkModule_ProvideInitialPriceServiceFactory;
import com.lago.app.di.NetworkModule_ProvideNewsApiServiceFactory;
import com.lago.app.di.NetworkModule_ProvideOkHttpClientFactory;
import com.lago.app.di.NetworkModule_ProvidePortfolioApiServiceFactory;
import com.lago.app.di.NetworkModule_ProvideRetrofitFactory;
import com.lago.app.di.NetworkModule_ProvideStudyApiServiceFactory;
import com.lago.app.domain.repository.StudyRepository;
import com.lago.app.domain.usecase.AnalyzeChartPatternUseCase;
import com.lago.app.domain.usecase.GetChartPatternsUseCase;
import com.lago.app.domain.usecase.GetDailyQuizStreakUseCase;
import com.lago.app.domain.usecase.GetDailyQuizUseCase;
import com.lago.app.domain.usecase.GetHistoryChallengeNewsDetailUseCase;
import com.lago.app.domain.usecase.GetHistoryChallengeNewsUseCase;
import com.lago.app.domain.usecase.GetInterestNewsUseCase;
import com.lago.app.domain.usecase.GetNewsDetailUseCase;
import com.lago.app.domain.usecase.GetNewsUseCase;
import com.lago.app.domain.usecase.GetRandomQuizUseCase;
import com.lago.app.domain.usecase.GetTermsUseCase;
import com.lago.app.domain.usecase.GetTransactionsUseCase;
import com.lago.app.domain.usecase.SolveDailyQuizUseCase;
import com.lago.app.domain.usecase.SolveRandomQuizUseCase;
import com.lago.app.presentation.viewmodel.DailyQuizViewModel;
import com.lago.app.presentation.viewmodel.DailyQuizViewModel_HiltModules;
import com.lago.app.presentation.viewmodel.DailyQuizViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.lago.app.presentation.viewmodel.DailyQuizViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.lago.app.presentation.viewmodel.HistoryChallengeNewsDetailViewModel;
import com.lago.app.presentation.viewmodel.HistoryChallengeNewsDetailViewModel_HiltModules;
import com.lago.app.presentation.viewmodel.HistoryChallengeNewsDetailViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.lago.app.presentation.viewmodel.HistoryChallengeNewsDetailViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.lago.app.presentation.viewmodel.LearnViewModel;
import com.lago.app.presentation.viewmodel.LearnViewModel_HiltModules;
import com.lago.app.presentation.viewmodel.LearnViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.lago.app.presentation.viewmodel.LearnViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.lago.app.presentation.viewmodel.LoginViewModel;
import com.lago.app.presentation.viewmodel.LoginViewModel_HiltModules;
import com.lago.app.presentation.viewmodel.LoginViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.lago.app.presentation.viewmodel.LoginViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.lago.app.presentation.viewmodel.NewsDetailViewModel;
import com.lago.app.presentation.viewmodel.NewsDetailViewModel_HiltModules;
import com.lago.app.presentation.viewmodel.NewsDetailViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.lago.app.presentation.viewmodel.NewsDetailViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.lago.app.presentation.viewmodel.NewsViewModel;
import com.lago.app.presentation.viewmodel.NewsViewModel_HiltModules;
import com.lago.app.presentation.viewmodel.NewsViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.lago.app.presentation.viewmodel.NewsViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.lago.app.presentation.viewmodel.OrderHistoryViewModel;
import com.lago.app.presentation.viewmodel.OrderHistoryViewModel_HiltModules;
import com.lago.app.presentation.viewmodel.OrderHistoryViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.lago.app.presentation.viewmodel.OrderHistoryViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.lago.app.presentation.viewmodel.PatternStudyViewModel;
import com.lago.app.presentation.viewmodel.PatternStudyViewModel_HiltModules;
import com.lago.app.presentation.viewmodel.PatternStudyViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.lago.app.presentation.viewmodel.PatternStudyViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.lago.app.presentation.viewmodel.RandomQuizViewModel;
import com.lago.app.presentation.viewmodel.RandomQuizViewModel_HiltModules;
import com.lago.app.presentation.viewmodel.RandomQuizViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.lago.app.presentation.viewmodel.RandomQuizViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.lago.app.presentation.viewmodel.RankingViewModel;
import com.lago.app.presentation.viewmodel.RankingViewModel_HiltModules;
import com.lago.app.presentation.viewmodel.RankingViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.lago.app.presentation.viewmodel.RankingViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.lago.app.presentation.viewmodel.TransactionViewModel;
import com.lago.app.presentation.viewmodel.TransactionViewModel_HiltModules;
import com.lago.app.presentation.viewmodel.TransactionViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.lago.app.presentation.viewmodel.TransactionViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.lago.app.presentation.viewmodel.WordbookViewModel;
import com.lago.app.presentation.viewmodel.WordbookViewModel_HiltModules;
import com.lago.app.presentation.viewmodel.WordbookViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.lago.app.presentation.viewmodel.WordbookViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.lago.app.presentation.viewmodel.chart.ChartViewModel;
import com.lago.app.presentation.viewmodel.chart.ChartViewModel_HiltModules;
import com.lago.app.presentation.viewmodel.chart.ChartViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.lago.app.presentation.viewmodel.chart.ChartViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.lago.app.presentation.viewmodel.history.TradingHistoryViewModel;
import com.lago.app.presentation.viewmodel.history.TradingHistoryViewModel_HiltModules;
import com.lago.app.presentation.viewmodel.history.TradingHistoryViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.lago.app.presentation.viewmodel.history.TradingHistoryViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.lago.app.presentation.viewmodel.historychallenge.HistoryChallengeChartViewModel;
import com.lago.app.presentation.viewmodel.historychallenge.HistoryChallengeChartViewModel_HiltModules;
import com.lago.app.presentation.viewmodel.historychallenge.HistoryChallengeChartViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.lago.app.presentation.viewmodel.historychallenge.HistoryChallengeChartViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.lago.app.presentation.viewmodel.home.HomeViewModel;
import com.lago.app.presentation.viewmodel.home.HomeViewModel_HiltModules;
import com.lago.app.presentation.viewmodel.home.HomeViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.lago.app.presentation.viewmodel.home.HomeViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.lago.app.presentation.viewmodel.mypage.BotPortfolioViewModel;
import com.lago.app.presentation.viewmodel.mypage.BotPortfolioViewModel_HiltModules;
import com.lago.app.presentation.viewmodel.mypage.BotPortfolioViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.lago.app.presentation.viewmodel.mypage.BotPortfolioViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.lago.app.presentation.viewmodel.mypage.MyPageViewModel;
import com.lago.app.presentation.viewmodel.mypage.MyPageViewModel_HiltModules;
import com.lago.app.presentation.viewmodel.mypage.MyPageViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.lago.app.presentation.viewmodel.mypage.MyPageViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.lago.app.presentation.viewmodel.portfolio.PortfolioViewModel;
import com.lago.app.presentation.viewmodel.portfolio.PortfolioViewModel_HiltModules;
import com.lago.app.presentation.viewmodel.portfolio.PortfolioViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.lago.app.presentation.viewmodel.portfolio.PortfolioViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.lago.app.presentation.viewmodel.purchase.PurchaseViewModel;
import com.lago.app.presentation.viewmodel.purchase.PurchaseViewModel_HiltModules;
import com.lago.app.presentation.viewmodel.purchase.PurchaseViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.lago.app.presentation.viewmodel.purchase.PurchaseViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.lago.app.presentation.viewmodel.stocklist.StockListViewModel;
import com.lago.app.presentation.viewmodel.stocklist.StockListViewModel_HiltModules;
import com.lago.app.presentation.viewmodel.stocklist.StockListViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.lago.app.presentation.viewmodel.stocklist.StockListViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.lago.app.util.HybridPriceCalculator;
import dagger.hilt.android.ActivityRetainedLifecycle;
import dagger.hilt.android.ViewModelLifecycle;
import dagger.hilt.android.internal.builders.ActivityComponentBuilder;
import dagger.hilt.android.internal.builders.ActivityRetainedComponentBuilder;
import dagger.hilt.android.internal.builders.FragmentComponentBuilder;
import dagger.hilt.android.internal.builders.ServiceComponentBuilder;
import dagger.hilt.android.internal.builders.ViewComponentBuilder;
import dagger.hilt.android.internal.builders.ViewModelComponentBuilder;
import dagger.hilt.android.internal.builders.ViewWithFragmentComponentBuilder;
import dagger.hilt.android.internal.lifecycle.DefaultViewModelFactories;
import dagger.hilt.android.internal.lifecycle.DefaultViewModelFactories_InternalFactoryFactory_Factory;
import dagger.hilt.android.internal.managers.ActivityRetainedComponentManager_LifecycleModule_ProvideActivityRetainedLifecycleFactory;
import dagger.hilt.android.internal.managers.SavedStateHandleHolder;
import dagger.hilt.android.internal.modules.ApplicationContextModule;
import dagger.hilt.android.internal.modules.ApplicationContextModule_ProvideContextFactory;
import dagger.internal.DaggerGenerated;
import dagger.internal.DoubleCheck;
import dagger.internal.LazyClassKeyMap;
import dagger.internal.MapBuilder;
import dagger.internal.Preconditions;
import dagger.internal.Provider;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;

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
public final class DaggerLagoApplication_HiltComponents_SingletonC {
  private DaggerLagoApplication_HiltComponents_SingletonC() {
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {
    private ApplicationContextModule applicationContextModule;

    private Builder() {
    }

    public Builder applicationContextModule(ApplicationContextModule applicationContextModule) {
      this.applicationContextModule = Preconditions.checkNotNull(applicationContextModule);
      return this;
    }

    public LagoApplication_HiltComponents.SingletonC build() {
      Preconditions.checkBuilderRequirement(applicationContextModule, ApplicationContextModule.class);
      return new SingletonCImpl(applicationContextModule);
    }
  }

  private static final class ActivityRetainedCBuilder implements LagoApplication_HiltComponents.ActivityRetainedC.Builder {
    private final SingletonCImpl singletonCImpl;

    private SavedStateHandleHolder savedStateHandleHolder;

    private ActivityRetainedCBuilder(SingletonCImpl singletonCImpl) {
      this.singletonCImpl = singletonCImpl;
    }

    @Override
    public ActivityRetainedCBuilder savedStateHandleHolder(
        SavedStateHandleHolder savedStateHandleHolder) {
      this.savedStateHandleHolder = Preconditions.checkNotNull(savedStateHandleHolder);
      return this;
    }

    @Override
    public LagoApplication_HiltComponents.ActivityRetainedC build() {
      Preconditions.checkBuilderRequirement(savedStateHandleHolder, SavedStateHandleHolder.class);
      return new ActivityRetainedCImpl(singletonCImpl, savedStateHandleHolder);
    }
  }

  private static final class ActivityCBuilder implements LagoApplication_HiltComponents.ActivityC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private Activity activity;

    private ActivityCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
    }

    @Override
    public ActivityCBuilder activity(Activity activity) {
      this.activity = Preconditions.checkNotNull(activity);
      return this;
    }

    @Override
    public LagoApplication_HiltComponents.ActivityC build() {
      Preconditions.checkBuilderRequirement(activity, Activity.class);
      return new ActivityCImpl(singletonCImpl, activityRetainedCImpl, activity);
    }
  }

  private static final class FragmentCBuilder implements LagoApplication_HiltComponents.FragmentC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private Fragment fragment;

    private FragmentCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
    }

    @Override
    public FragmentCBuilder fragment(Fragment fragment) {
      this.fragment = Preconditions.checkNotNull(fragment);
      return this;
    }

    @Override
    public LagoApplication_HiltComponents.FragmentC build() {
      Preconditions.checkBuilderRequirement(fragment, Fragment.class);
      return new FragmentCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, fragment);
    }
  }

  private static final class ViewWithFragmentCBuilder implements LagoApplication_HiltComponents.ViewWithFragmentC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl;

    private View view;

    private ViewWithFragmentCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl,
        FragmentCImpl fragmentCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
      this.fragmentCImpl = fragmentCImpl;
    }

    @Override
    public ViewWithFragmentCBuilder view(View view) {
      this.view = Preconditions.checkNotNull(view);
      return this;
    }

    @Override
    public LagoApplication_HiltComponents.ViewWithFragmentC build() {
      Preconditions.checkBuilderRequirement(view, View.class);
      return new ViewWithFragmentCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, fragmentCImpl, view);
    }
  }

  private static final class ViewCBuilder implements LagoApplication_HiltComponents.ViewC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private View view;

    private ViewCBuilder(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
        ActivityCImpl activityCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
    }

    @Override
    public ViewCBuilder view(View view) {
      this.view = Preconditions.checkNotNull(view);
      return this;
    }

    @Override
    public LagoApplication_HiltComponents.ViewC build() {
      Preconditions.checkBuilderRequirement(view, View.class);
      return new ViewCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, view);
    }
  }

  private static final class ViewModelCBuilder implements LagoApplication_HiltComponents.ViewModelC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private SavedStateHandle savedStateHandle;

    private ViewModelLifecycle viewModelLifecycle;

    private ViewModelCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
    }

    @Override
    public ViewModelCBuilder savedStateHandle(SavedStateHandle handle) {
      this.savedStateHandle = Preconditions.checkNotNull(handle);
      return this;
    }

    @Override
    public ViewModelCBuilder viewModelLifecycle(ViewModelLifecycle viewModelLifecycle) {
      this.viewModelLifecycle = Preconditions.checkNotNull(viewModelLifecycle);
      return this;
    }

    @Override
    public LagoApplication_HiltComponents.ViewModelC build() {
      Preconditions.checkBuilderRequirement(savedStateHandle, SavedStateHandle.class);
      Preconditions.checkBuilderRequirement(viewModelLifecycle, ViewModelLifecycle.class);
      return new ViewModelCImpl(singletonCImpl, activityRetainedCImpl, savedStateHandle, viewModelLifecycle);
    }
  }

  private static final class ServiceCBuilder implements LagoApplication_HiltComponents.ServiceC.Builder {
    private final SingletonCImpl singletonCImpl;

    private Service service;

    private ServiceCBuilder(SingletonCImpl singletonCImpl) {
      this.singletonCImpl = singletonCImpl;
    }

    @Override
    public ServiceCBuilder service(Service service) {
      this.service = Preconditions.checkNotNull(service);
      return this;
    }

    @Override
    public LagoApplication_HiltComponents.ServiceC build() {
      Preconditions.checkBuilderRequirement(service, Service.class);
      return new ServiceCImpl(singletonCImpl, service);
    }
  }

  private static final class ViewWithFragmentCImpl extends LagoApplication_HiltComponents.ViewWithFragmentC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl;

    private final ViewWithFragmentCImpl viewWithFragmentCImpl = this;

    ViewWithFragmentCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl,
        FragmentCImpl fragmentCImpl, View viewParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
      this.fragmentCImpl = fragmentCImpl;


    }
  }

  private static final class FragmentCImpl extends LagoApplication_HiltComponents.FragmentC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl = this;

    FragmentCImpl(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
        ActivityCImpl activityCImpl, Fragment fragmentParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;


    }

    @Override
    public DefaultViewModelFactories.InternalFactoryFactory getHiltInternalFactoryFactory() {
      return activityCImpl.getHiltInternalFactoryFactory();
    }

    @Override
    public ViewWithFragmentComponentBuilder viewWithFragmentComponentBuilder() {
      return new ViewWithFragmentCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl, fragmentCImpl);
    }
  }

  private static final class ViewCImpl extends LagoApplication_HiltComponents.ViewC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final ViewCImpl viewCImpl = this;

    ViewCImpl(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
        ActivityCImpl activityCImpl, View viewParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;


    }
  }

  private static final class ActivityCImpl extends LagoApplication_HiltComponents.ActivityC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl = this;

    ActivityCImpl(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
        Activity activityParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;


    }

    @Override
    public void injectMainActivity(MainActivity mainActivity) {
      injectMainActivity2(mainActivity);
    }

    @Override
    public DefaultViewModelFactories.InternalFactoryFactory getHiltInternalFactoryFactory() {
      return DefaultViewModelFactories_InternalFactoryFactory_Factory.newInstance(getViewModelKeys(), new ViewModelCBuilder(singletonCImpl, activityRetainedCImpl));
    }

    @Override
    public Map<Class<?>, Boolean> getViewModelKeys() {
      return LazyClassKeyMap.<Boolean>of(MapBuilder.<String, Boolean>newMapBuilder(21).put(BotPortfolioViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, BotPortfolioViewModel_HiltModules.KeyModule.provide()).put(ChartViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, ChartViewModel_HiltModules.KeyModule.provide()).put(DailyQuizViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, DailyQuizViewModel_HiltModules.KeyModule.provide()).put(HistoryChallengeChartViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, HistoryChallengeChartViewModel_HiltModules.KeyModule.provide()).put(HistoryChallengeNewsDetailViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, HistoryChallengeNewsDetailViewModel_HiltModules.KeyModule.provide()).put(HomeViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, HomeViewModel_HiltModules.KeyModule.provide()).put(LearnViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, LearnViewModel_HiltModules.KeyModule.provide()).put(LoginViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, LoginViewModel_HiltModules.KeyModule.provide()).put(MyPageViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, MyPageViewModel_HiltModules.KeyModule.provide()).put(NewsDetailViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, NewsDetailViewModel_HiltModules.KeyModule.provide()).put(NewsViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, NewsViewModel_HiltModules.KeyModule.provide()).put(OrderHistoryViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, OrderHistoryViewModel_HiltModules.KeyModule.provide()).put(PatternStudyViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, PatternStudyViewModel_HiltModules.KeyModule.provide()).put(PortfolioViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, PortfolioViewModel_HiltModules.KeyModule.provide()).put(PurchaseViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, PurchaseViewModel_HiltModules.KeyModule.provide()).put(RandomQuizViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, RandomQuizViewModel_HiltModules.KeyModule.provide()).put(RankingViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, RankingViewModel_HiltModules.KeyModule.provide()).put(StockListViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, StockListViewModel_HiltModules.KeyModule.provide()).put(TradingHistoryViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, TradingHistoryViewModel_HiltModules.KeyModule.provide()).put(TransactionViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, TransactionViewModel_HiltModules.KeyModule.provide()).put(WordbookViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, WordbookViewModel_HiltModules.KeyModule.provide()).build());
    }

    @Override
    public ViewModelComponentBuilder getViewModelComponentBuilder() {
      return new ViewModelCBuilder(singletonCImpl, activityRetainedCImpl);
    }

    @Override
    public FragmentComponentBuilder fragmentComponentBuilder() {
      return new FragmentCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl);
    }

    @Override
    public ViewComponentBuilder viewComponentBuilder() {
      return new ViewCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl);
    }

    @CanIgnoreReturnValue
    private MainActivity injectMainActivity2(MainActivity instance) {
      MainActivity_MembersInjector.injectUserPreferences(instance, singletonCImpl.provideUserPreferencesProvider.get());
      return instance;
    }
  }

  private static final class ViewModelCImpl extends LagoApplication_HiltComponents.ViewModelC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ViewModelCImpl viewModelCImpl = this;

    Provider<BotPortfolioViewModel> botPortfolioViewModelProvider;

    Provider<ChartViewModel> chartViewModelProvider;

    Provider<DailyQuizViewModel> dailyQuizViewModelProvider;

    Provider<HistoryChallengeChartViewModel> historyChallengeChartViewModelProvider;

    Provider<HistoryChallengeNewsDetailViewModel> historyChallengeNewsDetailViewModelProvider;

    Provider<HomeViewModel> homeViewModelProvider;

    Provider<LearnViewModel> learnViewModelProvider;

    Provider<LoginViewModel> loginViewModelProvider;

    Provider<MyPageViewModel> myPageViewModelProvider;

    Provider<NewsDetailViewModel> newsDetailViewModelProvider;

    Provider<NewsViewModel> newsViewModelProvider;

    Provider<OrderHistoryViewModel> orderHistoryViewModelProvider;

    Provider<PatternStudyViewModel> patternStudyViewModelProvider;

    Provider<PortfolioViewModel> portfolioViewModelProvider;

    Provider<PurchaseViewModel> purchaseViewModelProvider;

    Provider<RandomQuizViewModel> randomQuizViewModelProvider;

    Provider<RankingViewModel> rankingViewModelProvider;

    Provider<StockListViewModel> stockListViewModelProvider;

    Provider<TradingHistoryViewModel> tradingHistoryViewModelProvider;

    Provider<TransactionViewModel> transactionViewModelProvider;

    Provider<WordbookViewModel> wordbookViewModelProvider;

    ViewModelCImpl(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
        SavedStateHandle savedStateHandleParam, ViewModelLifecycle viewModelLifecycleParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;

      initialize(savedStateHandleParam, viewModelLifecycleParam);

    }

    AnalyzeChartPatternUseCase analyzeChartPatternUseCase() {
      return new AnalyzeChartPatternUseCase(singletonCImpl.chartRepositoryImplProvider.get());
    }

    GetDailyQuizUseCase getDailyQuizUseCase() {
      return new GetDailyQuizUseCase(singletonCImpl.bindStudyRepositoryProvider.get());
    }

    SolveDailyQuizUseCase solveDailyQuizUseCase() {
      return new SolveDailyQuizUseCase(singletonCImpl.bindStudyRepositoryProvider.get());
    }

    GetHistoryChallengeNewsDetailUseCase getHistoryChallengeNewsDetailUseCase() {
      return new GetHistoryChallengeNewsDetailUseCase(singletonCImpl.newsRepositoryImplProvider.get());
    }

    GetDailyQuizStreakUseCase getDailyQuizStreakUseCase() {
      return new GetDailyQuizStreakUseCase(singletonCImpl.bindStudyRepositoryProvider.get());
    }

    GetNewsDetailUseCase getNewsDetailUseCase() {
      return new GetNewsDetailUseCase(singletonCImpl.newsRepositoryImplProvider.get());
    }

    GetNewsUseCase getNewsUseCase() {
      return new GetNewsUseCase(singletonCImpl.newsRepositoryImplProvider.get());
    }

    GetInterestNewsUseCase getInterestNewsUseCase() {
      return new GetInterestNewsUseCase(singletonCImpl.newsRepositoryImplProvider.get());
    }

    GetChartPatternsUseCase getChartPatternsUseCase() {
      return new GetChartPatternsUseCase(singletonCImpl.bindStudyRepositoryProvider.get());
    }

    GetRandomQuizUseCase getRandomQuizUseCase() {
      return new GetRandomQuizUseCase(singletonCImpl.bindStudyRepositoryProvider.get());
    }

    SolveRandomQuizUseCase solveRandomQuizUseCase() {
      return new SolveRandomQuizUseCase(singletonCImpl.bindStudyRepositoryProvider.get());
    }

    GetHistoryChallengeNewsUseCase getHistoryChallengeNewsUseCase() {
      return new GetHistoryChallengeNewsUseCase(singletonCImpl.newsRepositoryImplProvider.get());
    }

    GetTransactionsUseCase getTransactionsUseCase() {
      return new GetTransactionsUseCase(singletonCImpl.transactionRepositoryImplProvider.get());
    }

    GetTermsUseCase getTermsUseCase() {
      return new GetTermsUseCase(singletonCImpl.bindStudyRepositoryProvider.get());
    }

    @SuppressWarnings("unchecked")
    private void initialize(final SavedStateHandle savedStateHandleParam,
        final ViewModelLifecycle viewModelLifecycleParam) {
      this.botPortfolioViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 0);
      this.chartViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 1);
      this.dailyQuizViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 2);
      this.historyChallengeChartViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 3);
      this.historyChallengeNewsDetailViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 4);
      this.homeViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 5);
      this.learnViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 6);
      this.loginViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 7);
      this.myPageViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 8);
      this.newsDetailViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 9);
      this.newsViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 10);
      this.orderHistoryViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 11);
      this.patternStudyViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 12);
      this.portfolioViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 13);
      this.purchaseViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 14);
      this.randomQuizViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 15);
      this.rankingViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 16);
      this.stockListViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 17);
      this.tradingHistoryViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 18);
      this.transactionViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 19);
      this.wordbookViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 20);
    }

    @Override
    public Map<Class<?>, javax.inject.Provider<ViewModel>> getHiltViewModelMap() {
      return LazyClassKeyMap.<javax.inject.Provider<ViewModel>>of(MapBuilder.<String, javax.inject.Provider<ViewModel>>newMapBuilder(21).put(BotPortfolioViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (botPortfolioViewModelProvider))).put(ChartViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (chartViewModelProvider))).put(DailyQuizViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (dailyQuizViewModelProvider))).put(HistoryChallengeChartViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (historyChallengeChartViewModelProvider))).put(HistoryChallengeNewsDetailViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (historyChallengeNewsDetailViewModelProvider))).put(HomeViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (homeViewModelProvider))).put(LearnViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (learnViewModelProvider))).put(LoginViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (loginViewModelProvider))).put(MyPageViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (myPageViewModelProvider))).put(NewsDetailViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (newsDetailViewModelProvider))).put(NewsViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (newsViewModelProvider))).put(OrderHistoryViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (orderHistoryViewModelProvider))).put(PatternStudyViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (patternStudyViewModelProvider))).put(PortfolioViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (portfolioViewModelProvider))).put(PurchaseViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (purchaseViewModelProvider))).put(RandomQuizViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (randomQuizViewModelProvider))).put(RankingViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (rankingViewModelProvider))).put(StockListViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (stockListViewModelProvider))).put(TradingHistoryViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (tradingHistoryViewModelProvider))).put(TransactionViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (transactionViewModelProvider))).put(WordbookViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (wordbookViewModelProvider))).build());
    }

    @Override
    public Map<Class<?>, Object> getHiltViewModelAssistedMap() {
      return Collections.<Class<?>, Object>emptyMap();
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final ActivityRetainedCImpl activityRetainedCImpl;

      private final ViewModelCImpl viewModelCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
          ViewModelCImpl viewModelCImpl, int id) {
        this.singletonCImpl = singletonCImpl;
        this.activityRetainedCImpl = activityRetainedCImpl;
        this.viewModelCImpl = viewModelCImpl;
        this.id = id;
      }

      @SuppressWarnings("unchecked")
      @Override
      public T get() {
        switch (id) {
          case 0: // com.lago.app.presentation.viewmodel.mypage.BotPortfolioViewModel
          return (T) new BotPortfolioViewModel(singletonCImpl.userRepositoryImplProvider.get(), singletonCImpl.realTimeStockCacheProvider.get(), singletonCImpl.smartStockWebSocketServiceProvider.get(), singletonCImpl.provideHybridPriceCalculatorProvider.get());

          case 1: // com.lago.app.presentation.viewmodel.chart.ChartViewModel
          return (T) new ChartViewModel(singletonCImpl.chartRepositoryImplProvider.get(), viewModelCImpl.analyzeChartPatternUseCase(), singletonCImpl.provideUserPreferencesProvider.get(), singletonCImpl.smartStockWebSocketServiceProvider.get(), singletonCImpl.smartUpdateSchedulerProvider.get(), singletonCImpl.provideChartMemoryCacheProvider.get(), singletonCImpl.realTimeStockCacheProvider.get(), singletonCImpl.mockTradeRepositoryImplProvider.get(), singletonCImpl.portfolioRepositoryImplProvider.get(), singletonCImpl.chartCacheDao(), singletonCImpl.favoriteCacheProvider.get(), singletonCImpl.patternAnalysisPreferencesProvider.get());

          case 2: // com.lago.app.presentation.viewmodel.DailyQuizViewModel
          return (T) new DailyQuizViewModel(viewModelCImpl.getDailyQuizUseCase(), viewModelCImpl.solveDailyQuizUseCase(), singletonCImpl.provideUserPreferencesProvider.get());

          case 3: // com.lago.app.presentation.viewmodel.historychallenge.HistoryChallengeChartViewModel
          return (T) new HistoryChallengeChartViewModel(singletonCImpl.chartRepositoryImplProvider.get(), viewModelCImpl.analyzeChartPatternUseCase(), singletonCImpl.provideUserPreferencesProvider.get(), singletonCImpl.realTimeStockCacheProvider.get());

          case 4: // com.lago.app.presentation.viewmodel.HistoryChallengeNewsDetailViewModel
          return (T) new HistoryChallengeNewsDetailViewModel(viewModelCImpl.getHistoryChallengeNewsDetailUseCase());

          case 5: // com.lago.app.presentation.viewmodel.home.HomeViewModel
          return (T) new HomeViewModel(singletonCImpl.userRepositoryImplProvider.get(), singletonCImpl.provideUserPreferencesProvider.get(), singletonCImpl.realTimeStockCacheProvider.get(), singletonCImpl.smartStockWebSocketServiceProvider.get(), singletonCImpl.smartUpdateSchedulerProvider.get(), singletonCImpl.closeDataServiceProvider.get(), singletonCImpl.provideHybridPriceCalculatorProvider.get(), singletonCImpl.provideInitialPriceServiceProvider.get());

          case 6: // com.lago.app.presentation.viewmodel.LearnViewModel
          return (T) new LearnViewModel(viewModelCImpl.getDailyQuizStreakUseCase(), singletonCImpl.provideUserPreferencesProvider.get());

          case 7: // com.lago.app.presentation.viewmodel.LoginViewModel
          return (T) new LoginViewModel(singletonCImpl.authRepositoryImplProvider.get(), singletonCImpl.provideUserPreferencesProvider.get());

          case 8: // com.lago.app.presentation.viewmodel.mypage.MyPageViewModel
          return (T) new MyPageViewModel(singletonCImpl.userRepositoryImplProvider.get(), singletonCImpl.provideUserPreferencesProvider.get(), singletonCImpl.realTimeStockCacheProvider.get(), singletonCImpl.smartStockWebSocketServiceProvider.get(), singletonCImpl.smartUpdateSchedulerProvider.get(), singletonCImpl.provideHybridPriceCalculatorProvider.get());

          case 9: // com.lago.app.presentation.viewmodel.NewsDetailViewModel
          return (T) new NewsDetailViewModel(viewModelCImpl.getNewsDetailUseCase());

          case 10: // com.lago.app.presentation.viewmodel.NewsViewModel
          return (T) new NewsViewModel(viewModelCImpl.getNewsUseCase(), viewModelCImpl.getInterestNewsUseCase(), singletonCImpl.provideUserPreferencesProvider.get());

          case 11: // com.lago.app.presentation.viewmodel.OrderHistoryViewModel
          return (T) new OrderHistoryViewModel(singletonCImpl.transactionRepositoryImplProvider.get(), singletonCImpl.provideUserPreferencesProvider.get());

          case 12: // com.lago.app.presentation.viewmodel.PatternStudyViewModel
          return (T) new PatternStudyViewModel(viewModelCImpl.getChartPatternsUseCase());

          case 13: // com.lago.app.presentation.viewmodel.portfolio.PortfolioViewModel
          return (T) new PortfolioViewModel(singletonCImpl.mockTradeRepositoryImplProvider.get(), singletonCImpl.userRepositoryImplProvider.get(), singletonCImpl.realTimeStockCacheProvider.get(), singletonCImpl.provideHybridPriceCalculatorProvider.get());

          case 14: // com.lago.app.presentation.viewmodel.purchase.PurchaseViewModel
          return (T) new PurchaseViewModel(singletonCImpl.mockTradeRepositoryImplProvider.get(), singletonCImpl.portfolioRepositoryImplProvider.get(), singletonCImpl.provideUserPreferencesProvider.get(), singletonCImpl.provideChartApiServiceProvider.get());

          case 15: // com.lago.app.presentation.viewmodel.RandomQuizViewModel
          return (T) new RandomQuizViewModel(viewModelCImpl.getRandomQuizUseCase(), viewModelCImpl.solveRandomQuizUseCase(), singletonCImpl.provideUserPreferencesProvider.get());

          case 16: // com.lago.app.presentation.viewmodel.RankingViewModel
          return (T) new RankingViewModel(singletonCImpl.rankingRepositoryImplProvider.get(), singletonCImpl.provideUserPreferencesProvider.get());

          case 17: // com.lago.app.presentation.viewmodel.stocklist.StockListViewModel
          return (T) new StockListViewModel(singletonCImpl.stockListRepositoryImplProvider.get(), singletonCImpl.historyChallengeRepositoryImplProvider.get(), singletonCImpl.chartRepositoryImplProvider.get(), singletonCImpl.smartStockWebSocketServiceProvider.get(), singletonCImpl.smartUpdateSchedulerProvider.get(), singletonCImpl.realTimeStockCacheProvider.get(), viewModelCImpl.getHistoryChallengeNewsUseCase(), singletonCImpl.mockTradeRepositoryImplProvider.get(), singletonCImpl.favoriteCacheProvider.get());

          case 18: // com.lago.app.presentation.viewmodel.history.TradingHistoryViewModel
          return (T) new TradingHistoryViewModel(singletonCImpl.mockTradeRepositoryImplProvider.get());

          case 19: // com.lago.app.presentation.viewmodel.TransactionViewModel
          return (T) new TransactionViewModel(viewModelCImpl.getTransactionsUseCase());

          case 20: // com.lago.app.presentation.viewmodel.WordbookViewModel
          return (T) new WordbookViewModel(viewModelCImpl.getTermsUseCase(), singletonCImpl.provideUserPreferencesProvider.get());

          default: throw new AssertionError(id);
        }
      }
    }
  }

  private static final class ActivityRetainedCImpl extends LagoApplication_HiltComponents.ActivityRetainedC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl = this;

    Provider<ActivityRetainedLifecycle> provideActivityRetainedLifecycleProvider;

    ActivityRetainedCImpl(SingletonCImpl singletonCImpl,
        SavedStateHandleHolder savedStateHandleHolderParam) {
      this.singletonCImpl = singletonCImpl;

      initialize(savedStateHandleHolderParam);

    }

    @SuppressWarnings("unchecked")
    private void initialize(final SavedStateHandleHolder savedStateHandleHolderParam) {
      this.provideActivityRetainedLifecycleProvider = DoubleCheck.provider(new SwitchingProvider<ActivityRetainedLifecycle>(singletonCImpl, activityRetainedCImpl, 0));
    }

    @Override
    public ActivityComponentBuilder activityComponentBuilder() {
      return new ActivityCBuilder(singletonCImpl, activityRetainedCImpl);
    }

    @Override
    public ActivityRetainedLifecycle getActivityRetainedLifecycle() {
      return provideActivityRetainedLifecycleProvider.get();
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final ActivityRetainedCImpl activityRetainedCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
          int id) {
        this.singletonCImpl = singletonCImpl;
        this.activityRetainedCImpl = activityRetainedCImpl;
        this.id = id;
      }

      @SuppressWarnings("unchecked")
      @Override
      public T get() {
        switch (id) {
          case 0: // dagger.hilt.android.ActivityRetainedLifecycle
          return (T) ActivityRetainedComponentManager_LifecycleModule_ProvideActivityRetainedLifecycleFactory.provideActivityRetainedLifecycle();

          default: throw new AssertionError(id);
        }
      }
    }
  }

  private static final class ServiceCImpl extends LagoApplication_HiltComponents.ServiceC {
    private final SingletonCImpl singletonCImpl;

    private final ServiceCImpl serviceCImpl = this;

    ServiceCImpl(SingletonCImpl singletonCImpl, Service serviceParam) {
      this.singletonCImpl = singletonCImpl;


    }
  }

  private static final class SingletonCImpl extends LagoApplication_HiltComponents.SingletonC {
    private final ApplicationContextModule applicationContextModule;

    private final SingletonCImpl singletonCImpl = this;

    Provider<SharedPreferences> provideSharedPreferencesProvider;

    Provider<UserPreferences> provideUserPreferencesProvider;

    Provider<HttpLoggingInterceptor> provideHttpLoggingInterceptorProvider;

    Provider<OkHttpClient> provideOkHttpClientProvider;

    Provider<Retrofit> provideRetrofitProvider;

    Provider<RemoteDataSource> remoteDataSourceProvider;

    Provider<LocalDataSource> localDataSourceProvider;

    Provider<UserRepositoryImpl> userRepositoryImplProvider;

    Provider<RealTimeStockCache> realTimeStockCacheProvider;

    Provider<SmartUpdateScheduler> smartUpdateSchedulerProvider;

    Provider<Gson> provideGsonProvider;

    Provider<SmartStockWebSocketService> smartStockWebSocketServiceProvider;

    Provider<ChartApiService> provideChartApiServiceProvider;

    Provider<InitialPriceService> provideInitialPriceServiceProvider;

    Provider<HybridPriceCalculator> provideHybridPriceCalculatorProvider;

    Provider<ChartMemoryCache> provideChartMemoryCacheProvider;

    Provider<ChartRepositoryImpl> chartRepositoryImplProvider;

    Provider<ApiService> provideApiServiceProvider;

    Provider<FavoriteCache> favoriteCacheProvider;

    Provider<MockTradeRepositoryImpl> mockTradeRepositoryImplProvider;

    Provider<PortfolioApiService> providePortfolioApiServiceProvider;

    Provider<PortfolioRepositoryImpl> portfolioRepositoryImplProvider;

    Provider<LagoDatabase> provideDatabaseProvider;

    Provider<PatternAnalysisPreferences> patternAnalysisPreferencesProvider;

    Provider<StudyApiService> provideStudyApiServiceProvider;

    Provider<StudyRepositoryImpl> studyRepositoryImplProvider;

    Provider<StudyRepository> bindStudyRepositoryProvider;

    Provider<NewsApiService> provideNewsApiServiceProvider;

    Provider<NewsRepositoryImpl> newsRepositoryImplProvider;

    Provider<CloseDataService> closeDataServiceProvider;

    Provider<AuthApiService> provideAuthApiServiceProvider;

    Provider<AuthRepositoryImpl> authRepositoryImplProvider;

    Provider<TransactionRepositoryImpl> transactionRepositoryImplProvider;

    Provider<RankingRepositoryImpl> rankingRepositoryImplProvider;

    Provider<StockListRepositoryImpl> stockListRepositoryImplProvider;

    Provider<HistoryChallengeApiService> provideHistoryChallengeApiServiceProvider;

    Provider<HistoryChallengeRepositoryImpl> historyChallengeRepositoryImplProvider;

    SingletonCImpl(ApplicationContextModule applicationContextModuleParam) {
      this.applicationContextModule = applicationContextModuleParam;
      initialize(applicationContextModuleParam);
      initialize2(applicationContextModuleParam);

    }

    ChartCacheDao chartCacheDao() {
      return DatabaseModule_ProvideChartCacheDaoFactory.provideChartCacheDao(provideDatabaseProvider.get());
    }

    @SuppressWarnings("unchecked")
    private void initialize(final ApplicationContextModule applicationContextModuleParam) {
      this.provideSharedPreferencesProvider = DoubleCheck.provider(new SwitchingProvider<SharedPreferences>(singletonCImpl, 1));
      this.provideUserPreferencesProvider = DoubleCheck.provider(new SwitchingProvider<UserPreferences>(singletonCImpl, 0));
      this.provideHttpLoggingInterceptorProvider = DoubleCheck.provider(new SwitchingProvider<HttpLoggingInterceptor>(singletonCImpl, 6));
      this.provideOkHttpClientProvider = DoubleCheck.provider(new SwitchingProvider<OkHttpClient>(singletonCImpl, 5));
      this.provideRetrofitProvider = DoubleCheck.provider(new SwitchingProvider<Retrofit>(singletonCImpl, 4));
      this.remoteDataSourceProvider = DoubleCheck.provider(new SwitchingProvider<RemoteDataSource>(singletonCImpl, 3));
      this.localDataSourceProvider = DoubleCheck.provider(new SwitchingProvider<LocalDataSource>(singletonCImpl, 7));
      this.userRepositoryImplProvider = DoubleCheck.provider(new SwitchingProvider<UserRepositoryImpl>(singletonCImpl, 2));
      this.realTimeStockCacheProvider = DoubleCheck.provider(new SwitchingProvider<RealTimeStockCache>(singletonCImpl, 8));
      this.smartUpdateSchedulerProvider = DoubleCheck.provider(new SwitchingProvider<SmartUpdateScheduler>(singletonCImpl, 10));
      this.provideGsonProvider = DoubleCheck.provider(new SwitchingProvider<Gson>(singletonCImpl, 11));
      this.smartStockWebSocketServiceProvider = DoubleCheck.provider(new SwitchingProvider<SmartStockWebSocketService>(singletonCImpl, 9));
      this.provideChartApiServiceProvider = DoubleCheck.provider(new SwitchingProvider<ChartApiService>(singletonCImpl, 14));
      this.provideInitialPriceServiceProvider = DoubleCheck.provider(new SwitchingProvider<InitialPriceService>(singletonCImpl, 13));
      this.provideHybridPriceCalculatorProvider = DoubleCheck.provider(new SwitchingProvider<HybridPriceCalculator>(singletonCImpl, 12));
      this.provideChartMemoryCacheProvider = DoubleCheck.provider(new SwitchingProvider<ChartMemoryCache>(singletonCImpl, 16));
      this.chartRepositoryImplProvider = DoubleCheck.provider(new SwitchingProvider<ChartRepositoryImpl>(singletonCImpl, 15));
      this.provideApiServiceProvider = DoubleCheck.provider(new SwitchingProvider<ApiService>(singletonCImpl, 18));
      this.favoriteCacheProvider = DoubleCheck.provider(new SwitchingProvider<FavoriteCache>(singletonCImpl, 19));
      this.mockTradeRepositoryImplProvider = DoubleCheck.provider(new SwitchingProvider<MockTradeRepositoryImpl>(singletonCImpl, 17));
      this.providePortfolioApiServiceProvider = DoubleCheck.provider(new SwitchingProvider<PortfolioApiService>(singletonCImpl, 21));
      this.portfolioRepositoryImplProvider = DoubleCheck.provider(new SwitchingProvider<PortfolioRepositoryImpl>(singletonCImpl, 20));
      this.provideDatabaseProvider = DoubleCheck.provider(new SwitchingProvider<LagoDatabase>(singletonCImpl, 22));
      this.patternAnalysisPreferencesProvider = DoubleCheck.provider(new SwitchingProvider<PatternAnalysisPreferences>(singletonCImpl, 23));
      this.provideStudyApiServiceProvider = DoubleCheck.provider(new SwitchingProvider<StudyApiService>(singletonCImpl, 25));
    }

    @SuppressWarnings("unchecked")
    private void initialize2(final ApplicationContextModule applicationContextModuleParam) {
      this.studyRepositoryImplProvider = new SwitchingProvider<>(singletonCImpl, 24);
      this.bindStudyRepositoryProvider = DoubleCheck.provider((Provider) (studyRepositoryImplProvider));
      this.provideNewsApiServiceProvider = DoubleCheck.provider(new SwitchingProvider<NewsApiService>(singletonCImpl, 27));
      this.newsRepositoryImplProvider = DoubleCheck.provider(new SwitchingProvider<NewsRepositoryImpl>(singletonCImpl, 26));
      this.closeDataServiceProvider = DoubleCheck.provider(new SwitchingProvider<CloseDataService>(singletonCImpl, 28));
      this.provideAuthApiServiceProvider = DoubleCheck.provider(new SwitchingProvider<AuthApiService>(singletonCImpl, 30));
      this.authRepositoryImplProvider = DoubleCheck.provider(new SwitchingProvider<AuthRepositoryImpl>(singletonCImpl, 29));
      this.transactionRepositoryImplProvider = DoubleCheck.provider(new SwitchingProvider<TransactionRepositoryImpl>(singletonCImpl, 31));
      this.rankingRepositoryImplProvider = DoubleCheck.provider(new SwitchingProvider<RankingRepositoryImpl>(singletonCImpl, 32));
      this.stockListRepositoryImplProvider = DoubleCheck.provider(new SwitchingProvider<StockListRepositoryImpl>(singletonCImpl, 33));
      this.provideHistoryChallengeApiServiceProvider = DoubleCheck.provider(new SwitchingProvider<HistoryChallengeApiService>(singletonCImpl, 35));
      this.historyChallengeRepositoryImplProvider = DoubleCheck.provider(new SwitchingProvider<HistoryChallengeRepositoryImpl>(singletonCImpl, 34));
    }

    @Override
    public void injectLagoApplication(LagoApplication lagoApplication) {
    }

    @Override
    public Set<Boolean> getDisableFragmentGetContextFix() {
      return Collections.<Boolean>emptySet();
    }

    @Override
    public ActivityRetainedComponentBuilder retainedComponentBuilder() {
      return new ActivityRetainedCBuilder(singletonCImpl);
    }

    @Override
    public ServiceComponentBuilder serviceComponentBuilder() {
      return new ServiceCBuilder(singletonCImpl);
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, int id) {
        this.singletonCImpl = singletonCImpl;
        this.id = id;
      }

      @SuppressWarnings("unchecked")
      @Override
      public T get() {
        switch (id) {
          case 0: // com.lago.app.data.local.prefs.UserPreferences
          return (T) LocalDataModule_ProvideUserPreferencesFactory.provideUserPreferences(singletonCImpl.provideSharedPreferencesProvider.get());

          case 1: // android.content.SharedPreferences
          return (T) LocalDataModule_ProvideSharedPreferencesFactory.provideSharedPreferences(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 2: // com.lago.app.data.repository.UserRepositoryImpl
          return (T) new UserRepositoryImpl(singletonCImpl.remoteDataSourceProvider.get(), singletonCImpl.localDataSourceProvider.get(), singletonCImpl.provideUserPreferencesProvider.get());

          case 3: // com.lago.app.data.remote.RemoteDataSource
          return (T) new RemoteDataSource(singletonCImpl.provideRetrofitProvider.get());

          case 4: // retrofit2.Retrofit
          return (T) NetworkModule_ProvideRetrofitFactory.provideRetrofit(singletonCImpl.provideOkHttpClientProvider.get());

          case 5: // okhttp3.OkHttpClient
          return (T) NetworkModule_ProvideOkHttpClientFactory.provideOkHttpClient(singletonCImpl.provideHttpLoggingInterceptorProvider.get());

          case 6: // okhttp3.logging.HttpLoggingInterceptor
          return (T) NetworkModule_ProvideHttpLoggingInterceptorFactory.provideHttpLoggingInterceptor();

          case 7: // com.lago.app.data.local.LocalDataSource
          return (T) new LocalDataSource();

          case 8: // com.lago.app.data.cache.RealTimeStockCache
          return (T) new RealTimeStockCache();

          case 9: // com.lago.app.data.remote.websocket.SmartStockWebSocketService
          return (T) new SmartStockWebSocketService(singletonCImpl.provideUserPreferencesProvider.get(), singletonCImpl.realTimeStockCacheProvider.get(), singletonCImpl.smartUpdateSchedulerProvider.get(), singletonCImpl.provideGsonProvider.get(), singletonCImpl.remoteDataSourceProvider.get());

          case 10: // com.lago.app.data.scheduler.SmartUpdateScheduler
          return (T) new SmartUpdateScheduler();

          case 11: // com.google.gson.Gson
          return (T) NetworkModule_ProvideGsonFactory.provideGson();

          case 12: // com.lago.app.util.HybridPriceCalculator
          return (T) NetworkModule_ProvideHybridPriceCalculatorFactory.provideHybridPriceCalculator(singletonCImpl.provideInitialPriceServiceProvider.get());

          case 13: // com.lago.app.data.service.InitialPriceService
          return (T) NetworkModule_ProvideInitialPriceServiceFactory.provideInitialPriceService(singletonCImpl.provideChartApiServiceProvider.get());

          case 14: // com.lago.app.data.remote.api.ChartApiService
          return (T) NetworkModule_ProvideChartApiServiceFactory.provideChartApiService(singletonCImpl.provideRetrofitProvider.get());

          case 15: // com.lago.app.data.repository.ChartRepositoryImpl
          return (T) new ChartRepositoryImpl(singletonCImpl.provideChartApiServiceProvider.get(), singletonCImpl.provideUserPreferencesProvider.get(), singletonCImpl.provideChartMemoryCacheProvider.get());

          case 16: // com.lago.app.data.cache.ChartMemoryCache
          return (T) DatabaseModule_ProvideChartMemoryCacheFactory.provideChartMemoryCache();

          case 17: // com.lago.app.data.repository.MockTradeRepositoryImpl
          return (T) new MockTradeRepositoryImpl(singletonCImpl.provideChartApiServiceProvider.get(), singletonCImpl.provideApiServiceProvider.get(), singletonCImpl.provideUserPreferencesProvider.get(), singletonCImpl.favoriteCacheProvider.get());

          case 18: // com.lago.app.data.remote.ApiService
          return (T) NetworkModule_ProvideApiServiceFactory.provideApiService(singletonCImpl.provideRetrofitProvider.get());

          case 19: // com.lago.app.data.cache.FavoriteCache
          return (T) new FavoriteCache();

          case 20: // com.lago.app.data.repository.PortfolioRepositoryImpl
          return (T) new PortfolioRepositoryImpl(singletonCImpl.providePortfolioApiServiceProvider.get());

          case 21: // com.lago.app.data.remote.api.PortfolioApiService
          return (T) NetworkModule_ProvidePortfolioApiServiceFactory.providePortfolioApiService(singletonCImpl.provideRetrofitProvider.get());

          case 22: // com.lago.app.data.local.LagoDatabase
          return (T) DatabaseModule_ProvideDatabaseFactory.provideDatabase(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 23: // com.lago.app.data.local.prefs.PatternAnalysisPreferences
          return (T) new PatternAnalysisPreferences(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 24: // com.lago.app.data.repository.StudyRepositoryImpl
          return (T) new StudyRepositoryImpl(singletonCImpl.provideStudyApiServiceProvider.get());

          case 25: // com.lago.app.data.remote.StudyApiService
          return (T) NetworkModule_ProvideStudyApiServiceFactory.provideStudyApiService(singletonCImpl.provideRetrofitProvider.get());

          case 26: // com.lago.app.data.repository.NewsRepositoryImpl
          return (T) new NewsRepositoryImpl(singletonCImpl.provideNewsApiServiceProvider.get());

          case 27: // com.lago.app.data.remote.NewsApiService
          return (T) NetworkModule_ProvideNewsApiServiceFactory.provideNewsApiService(singletonCImpl.provideRetrofitProvider.get());

          case 28: // com.lago.app.data.service.CloseDataService
          return (T) new CloseDataService(singletonCImpl.provideChartApiServiceProvider.get());

          case 29: // com.lago.app.data.repository.AuthRepositoryImpl
          return (T) new AuthRepositoryImpl(singletonCImpl.provideAuthApiServiceProvider.get());

          case 30: // com.lago.app.data.remote.api.AuthApiService
          return (T) NetworkModule_ProvideAuthApiServiceFactory.provideAuthApiService(singletonCImpl.provideRetrofitProvider.get());

          case 31: // com.lago.app.data.repository.TransactionRepositoryImpl
          return (T) new TransactionRepositoryImpl(singletonCImpl.remoteDataSourceProvider.get());

          case 32: // com.lago.app.data.repository.RankingRepositoryImpl
          return (T) new RankingRepositoryImpl(singletonCImpl.provideApiServiceProvider.get(), singletonCImpl.provideUserPreferencesProvider.get());

          case 33: // com.lago.app.data.repository.StockListRepositoryImpl
          return (T) new StockListRepositoryImpl(singletonCImpl.provideChartApiServiceProvider.get(), singletonCImpl.provideUserPreferencesProvider.get());

          case 34: // com.lago.app.data.repository.HistoryChallengeRepositoryImpl
          return (T) new HistoryChallengeRepositoryImpl(singletonCImpl.provideHistoryChallengeApiServiceProvider.get());

          case 35: // com.lago.app.data.remote.api.HistoryChallengeApiService
          return (T) NetworkModule_ProvideHistoryChallengeApiServiceFactory.provideHistoryChallengeApiService(singletonCImpl.provideRetrofitProvider.get());

          default: throw new AssertionError(id);
        }
      }
    }
  }
}
