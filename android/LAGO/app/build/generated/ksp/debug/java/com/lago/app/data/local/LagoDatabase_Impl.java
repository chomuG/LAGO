package com.lago.app.data.local;

import androidx.annotation.NonNull;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomDatabase;
import androidx.room.RoomOpenHelper;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import com.lago.app.data.local.dao.ChartCacheDao;
import com.lago.app.data.local.dao.ChartCacheDao_Impl;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class LagoDatabase_Impl extends LagoDatabase {
  private volatile UserDao _userDao;

  private volatile ChartCacheDao _chartCacheDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(2) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `users` (`id` TEXT NOT NULL, `email` TEXT NOT NULL, `name` TEXT NOT NULL, `profileImageUrl` TEXT, `phoneNumber` TEXT, `dateOfBirth` TEXT, `investmentLevel` TEXT NOT NULL, `preferredRiskLevel` TEXT NOT NULL, `totalInvestment` REAL NOT NULL, `totalReturn` REAL NOT NULL, `isVerified` INTEGER NOT NULL, `createdAt` TEXT NOT NULL, `updatedAt` TEXT NOT NULL, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `cached_chart_data` (`id` TEXT NOT NULL, `stockCode` TEXT NOT NULL, `timeFrame` TEXT NOT NULL, `data` TEXT NOT NULL, `lastUpdated` INTEGER NOT NULL, `expiryTime` INTEGER NOT NULL, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `cached_stock_info` (`stockCode` TEXT NOT NULL, `name` TEXT NOT NULL, `currentPrice` REAL NOT NULL, `priceChange` REAL NOT NULL, `priceChangePercent` REAL NOT NULL, `previousDay` INTEGER, `lastUpdated` INTEGER NOT NULL, `expiryTime` INTEGER NOT NULL, PRIMARY KEY(`stockCode`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, 'a56d9a320fe5e5782fcb18daa3e2bfec')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `users`");
        db.execSQL("DROP TABLE IF EXISTS `cached_chart_data`");
        db.execSQL("DROP TABLE IF EXISTS `cached_stock_info`");
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onDestructiveMigration(db);
          }
        }
      }

      @Override
      public void onCreate(@NonNull final SupportSQLiteDatabase db) {
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onCreate(db);
          }
        }
      }

      @Override
      public void onOpen(@NonNull final SupportSQLiteDatabase db) {
        mDatabase = db;
        internalInitInvalidationTracker(db);
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onOpen(db);
          }
        }
      }

      @Override
      public void onPreMigrate(@NonNull final SupportSQLiteDatabase db) {
        DBUtil.dropFtsSyncTriggers(db);
      }

      @Override
      public void onPostMigrate(@NonNull final SupportSQLiteDatabase db) {
      }

      @Override
      @NonNull
      public RoomOpenHelper.ValidationResult onValidateSchema(
          @NonNull final SupportSQLiteDatabase db) {
        final HashMap<String, TableInfo.Column> _columnsUsers = new HashMap<String, TableInfo.Column>(13);
        _columnsUsers.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUsers.put("email", new TableInfo.Column("email", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUsers.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUsers.put("profileImageUrl", new TableInfo.Column("profileImageUrl", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUsers.put("phoneNumber", new TableInfo.Column("phoneNumber", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUsers.put("dateOfBirth", new TableInfo.Column("dateOfBirth", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUsers.put("investmentLevel", new TableInfo.Column("investmentLevel", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUsers.put("preferredRiskLevel", new TableInfo.Column("preferredRiskLevel", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUsers.put("totalInvestment", new TableInfo.Column("totalInvestment", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUsers.put("totalReturn", new TableInfo.Column("totalReturn", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUsers.put("isVerified", new TableInfo.Column("isVerified", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUsers.put("createdAt", new TableInfo.Column("createdAt", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUsers.put("updatedAt", new TableInfo.Column("updatedAt", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysUsers = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesUsers = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoUsers = new TableInfo("users", _columnsUsers, _foreignKeysUsers, _indicesUsers);
        final TableInfo _existingUsers = TableInfo.read(db, "users");
        if (!_infoUsers.equals(_existingUsers)) {
          return new RoomOpenHelper.ValidationResult(false, "users(com.lago.app.domain.entity.User).\n"
                  + " Expected:\n" + _infoUsers + "\n"
                  + " Found:\n" + _existingUsers);
        }
        final HashMap<String, TableInfo.Column> _columnsCachedChartData = new HashMap<String, TableInfo.Column>(6);
        _columnsCachedChartData.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedChartData.put("stockCode", new TableInfo.Column("stockCode", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedChartData.put("timeFrame", new TableInfo.Column("timeFrame", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedChartData.put("data", new TableInfo.Column("data", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedChartData.put("lastUpdated", new TableInfo.Column("lastUpdated", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedChartData.put("expiryTime", new TableInfo.Column("expiryTime", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysCachedChartData = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesCachedChartData = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoCachedChartData = new TableInfo("cached_chart_data", _columnsCachedChartData, _foreignKeysCachedChartData, _indicesCachedChartData);
        final TableInfo _existingCachedChartData = TableInfo.read(db, "cached_chart_data");
        if (!_infoCachedChartData.equals(_existingCachedChartData)) {
          return new RoomOpenHelper.ValidationResult(false, "cached_chart_data(com.lago.app.data.local.entity.CachedChartData).\n"
                  + " Expected:\n" + _infoCachedChartData + "\n"
                  + " Found:\n" + _existingCachedChartData);
        }
        final HashMap<String, TableInfo.Column> _columnsCachedStockInfo = new HashMap<String, TableInfo.Column>(8);
        _columnsCachedStockInfo.put("stockCode", new TableInfo.Column("stockCode", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedStockInfo.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedStockInfo.put("currentPrice", new TableInfo.Column("currentPrice", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedStockInfo.put("priceChange", new TableInfo.Column("priceChange", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedStockInfo.put("priceChangePercent", new TableInfo.Column("priceChangePercent", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedStockInfo.put("previousDay", new TableInfo.Column("previousDay", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedStockInfo.put("lastUpdated", new TableInfo.Column("lastUpdated", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedStockInfo.put("expiryTime", new TableInfo.Column("expiryTime", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysCachedStockInfo = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesCachedStockInfo = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoCachedStockInfo = new TableInfo("cached_stock_info", _columnsCachedStockInfo, _foreignKeysCachedStockInfo, _indicesCachedStockInfo);
        final TableInfo _existingCachedStockInfo = TableInfo.read(db, "cached_stock_info");
        if (!_infoCachedStockInfo.equals(_existingCachedStockInfo)) {
          return new RoomOpenHelper.ValidationResult(false, "cached_stock_info(com.lago.app.data.local.entity.CachedStockInfo).\n"
                  + " Expected:\n" + _infoCachedStockInfo + "\n"
                  + " Found:\n" + _existingCachedStockInfo);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "a56d9a320fe5e5782fcb18daa3e2bfec", "f68164b4f7e16589465c0a4ec9d784e2");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "users","cached_chart_data","cached_stock_info");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    try {
      super.beginTransaction();
      _db.execSQL("DELETE FROM `users`");
      _db.execSQL("DELETE FROM `cached_chart_data`");
      _db.execSQL("DELETE FROM `cached_stock_info`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  @NonNull
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final HashMap<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(UserDao.class, UserDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(ChartCacheDao.class, ChartCacheDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  @NonNull
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final HashSet<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
    return _autoMigrationSpecsSet;
  }

  @Override
  @NonNull
  public List<Migration> getAutoMigrations(
      @NonNull final Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecs) {
    final List<Migration> _autoMigrations = new ArrayList<Migration>();
    return _autoMigrations;
  }

  @Override
  public UserDao userDao() {
    if (_userDao != null) {
      return _userDao;
    } else {
      synchronized(this) {
        if(_userDao == null) {
          _userDao = new UserDao_Impl(this);
        }
        return _userDao;
      }
    }
  }

  @Override
  public ChartCacheDao chartCacheDao() {
    if (_chartCacheDao != null) {
      return _chartCacheDao;
    } else {
      synchronized(this) {
        if(_chartCacheDao == null) {
          _chartCacheDao = new ChartCacheDao_Impl(this);
        }
        return _chartCacheDao;
      }
    }
  }
}
