package com.lago.app.data.local.dao;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.lago.app.data.local.Converters;
import com.lago.app.data.local.entity.CachedChartData;
import com.lago.app.data.local.entity.CachedStockInfo;
import com.lago.app.domain.entity.CandlestickData;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Integer;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class ChartCacheDao_Impl implements ChartCacheDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<CachedChartData> __insertionAdapterOfCachedChartData;

  private final Converters __converters = new Converters();

  private final EntityInsertionAdapter<CachedStockInfo> __insertionAdapterOfCachedStockInfo;

  private final SharedSQLiteStatement __preparedStmtOfDeleteExpiredChartData;

  private final SharedSQLiteStatement __preparedStmtOfDeleteExpiredStockInfo;

  private final SharedSQLiteStatement __preparedStmtOfDeleteOldChartData;

  private final SharedSQLiteStatement __preparedStmtOfDeleteOldStockInfo;

  public ChartCacheDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfCachedChartData = new EntityInsertionAdapter<CachedChartData>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `cached_chart_data` (`id`,`stockCode`,`timeFrame`,`data`,`lastUpdated`,`expiryTime`) VALUES (?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final CachedChartData entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getStockCode());
        statement.bindString(3, entity.getTimeFrame());
        final String _tmp = __converters.fromCandlestickDataList(entity.getData());
        statement.bindString(4, _tmp);
        statement.bindLong(5, entity.getLastUpdated());
        statement.bindLong(6, entity.getExpiryTime());
      }
    };
    this.__insertionAdapterOfCachedStockInfo = new EntityInsertionAdapter<CachedStockInfo>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `cached_stock_info` (`stockCode`,`name`,`currentPrice`,`priceChange`,`priceChangePercent`,`previousDay`,`lastUpdated`,`expiryTime`) VALUES (?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final CachedStockInfo entity) {
        statement.bindString(1, entity.getStockCode());
        statement.bindString(2, entity.getName());
        statement.bindDouble(3, entity.getCurrentPrice());
        statement.bindDouble(4, entity.getPriceChange());
        statement.bindDouble(5, entity.getPriceChangePercent());
        if (entity.getPreviousDay() == null) {
          statement.bindNull(6);
        } else {
          statement.bindLong(6, entity.getPreviousDay());
        }
        statement.bindLong(7, entity.getLastUpdated());
        statement.bindLong(8, entity.getExpiryTime());
      }
    };
    this.__preparedStmtOfDeleteExpiredChartData = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM cached_chart_data WHERE expiryTime <= ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteExpiredStockInfo = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM cached_stock_info WHERE expiryTime <= ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteOldChartData = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM cached_chart_data WHERE lastUpdated < ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteOldStockInfo = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM cached_stock_info WHERE lastUpdated < ?";
        return _query;
      }
    };
  }

  @Override
  public Object insertChartData(final CachedChartData cachedChartData,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfCachedChartData.insert(cachedChartData);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertStockInfo(final CachedStockInfo cachedStockInfo,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfCachedStockInfo.insert(cachedStockInfo);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteExpiredChartData(final long currentTime,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteExpiredChartData.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, currentTime);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteExpiredChartData.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteExpiredStockInfo(final long currentTime,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteExpiredStockInfo.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, currentTime);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteExpiredStockInfo.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteOldChartData(final long cutoffTime,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteOldChartData.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, cutoffTime);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteOldChartData.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteOldStockInfo(final long cutoffTime,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteOldStockInfo.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, cutoffTime);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteOldStockInfo.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object getCachedChartData(final String id, final long currentTime,
      final Continuation<? super CachedChartData> $completion) {
    final String _sql = "SELECT * FROM cached_chart_data WHERE id = ? AND expiryTime > ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindString(_argIndex, id);
    _argIndex = 2;
    _statement.bindLong(_argIndex, currentTime);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<CachedChartData>() {
      @Override
      @Nullable
      public CachedChartData call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfStockCode = CursorUtil.getColumnIndexOrThrow(_cursor, "stockCode");
          final int _cursorIndexOfTimeFrame = CursorUtil.getColumnIndexOrThrow(_cursor, "timeFrame");
          final int _cursorIndexOfData = CursorUtil.getColumnIndexOrThrow(_cursor, "data");
          final int _cursorIndexOfLastUpdated = CursorUtil.getColumnIndexOrThrow(_cursor, "lastUpdated");
          final int _cursorIndexOfExpiryTime = CursorUtil.getColumnIndexOrThrow(_cursor, "expiryTime");
          final CachedChartData _result;
          if (_cursor.moveToFirst()) {
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpStockCode;
            _tmpStockCode = _cursor.getString(_cursorIndexOfStockCode);
            final String _tmpTimeFrame;
            _tmpTimeFrame = _cursor.getString(_cursorIndexOfTimeFrame);
            final List<CandlestickData> _tmpData;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfData);
            _tmpData = __converters.toCandlestickDataList(_tmp);
            final long _tmpLastUpdated;
            _tmpLastUpdated = _cursor.getLong(_cursorIndexOfLastUpdated);
            final long _tmpExpiryTime;
            _tmpExpiryTime = _cursor.getLong(_cursorIndexOfExpiryTime);
            _result = new CachedChartData(_tmpId,_tmpStockCode,_tmpTimeFrame,_tmpData,_tmpLastUpdated,_tmpExpiryTime);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getCachedStockInfo(final String stockCode, final long currentTime,
      final Continuation<? super CachedStockInfo> $completion) {
    final String _sql = "SELECT * FROM cached_stock_info WHERE stockCode = ? AND expiryTime > ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindString(_argIndex, stockCode);
    _argIndex = 2;
    _statement.bindLong(_argIndex, currentTime);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<CachedStockInfo>() {
      @Override
      @Nullable
      public CachedStockInfo call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfStockCode = CursorUtil.getColumnIndexOrThrow(_cursor, "stockCode");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfCurrentPrice = CursorUtil.getColumnIndexOrThrow(_cursor, "currentPrice");
          final int _cursorIndexOfPriceChange = CursorUtil.getColumnIndexOrThrow(_cursor, "priceChange");
          final int _cursorIndexOfPriceChangePercent = CursorUtil.getColumnIndexOrThrow(_cursor, "priceChangePercent");
          final int _cursorIndexOfPreviousDay = CursorUtil.getColumnIndexOrThrow(_cursor, "previousDay");
          final int _cursorIndexOfLastUpdated = CursorUtil.getColumnIndexOrThrow(_cursor, "lastUpdated");
          final int _cursorIndexOfExpiryTime = CursorUtil.getColumnIndexOrThrow(_cursor, "expiryTime");
          final CachedStockInfo _result;
          if (_cursor.moveToFirst()) {
            final String _tmpStockCode;
            _tmpStockCode = _cursor.getString(_cursorIndexOfStockCode);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final float _tmpCurrentPrice;
            _tmpCurrentPrice = _cursor.getFloat(_cursorIndexOfCurrentPrice);
            final float _tmpPriceChange;
            _tmpPriceChange = _cursor.getFloat(_cursorIndexOfPriceChange);
            final float _tmpPriceChangePercent;
            _tmpPriceChangePercent = _cursor.getFloat(_cursorIndexOfPriceChangePercent);
            final Integer _tmpPreviousDay;
            if (_cursor.isNull(_cursorIndexOfPreviousDay)) {
              _tmpPreviousDay = null;
            } else {
              _tmpPreviousDay = _cursor.getInt(_cursorIndexOfPreviousDay);
            }
            final long _tmpLastUpdated;
            _tmpLastUpdated = _cursor.getLong(_cursorIndexOfLastUpdated);
            final long _tmpExpiryTime;
            _tmpExpiryTime = _cursor.getLong(_cursorIndexOfExpiryTime);
            _result = new CachedStockInfo(_tmpStockCode,_tmpName,_tmpCurrentPrice,_tmpPriceChange,_tmpPriceChangePercent,_tmpPreviousDay,_tmpLastUpdated,_tmpExpiryTime);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<CachedStockInfo> observeCachedStockInfo(final String stockCode) {
    final String _sql = "SELECT * FROM cached_stock_info WHERE stockCode = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, stockCode);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"cached_stock_info"}, new Callable<CachedStockInfo>() {
      @Override
      @Nullable
      public CachedStockInfo call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfStockCode = CursorUtil.getColumnIndexOrThrow(_cursor, "stockCode");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfCurrentPrice = CursorUtil.getColumnIndexOrThrow(_cursor, "currentPrice");
          final int _cursorIndexOfPriceChange = CursorUtil.getColumnIndexOrThrow(_cursor, "priceChange");
          final int _cursorIndexOfPriceChangePercent = CursorUtil.getColumnIndexOrThrow(_cursor, "priceChangePercent");
          final int _cursorIndexOfPreviousDay = CursorUtil.getColumnIndexOrThrow(_cursor, "previousDay");
          final int _cursorIndexOfLastUpdated = CursorUtil.getColumnIndexOrThrow(_cursor, "lastUpdated");
          final int _cursorIndexOfExpiryTime = CursorUtil.getColumnIndexOrThrow(_cursor, "expiryTime");
          final CachedStockInfo _result;
          if (_cursor.moveToFirst()) {
            final String _tmpStockCode;
            _tmpStockCode = _cursor.getString(_cursorIndexOfStockCode);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final float _tmpCurrentPrice;
            _tmpCurrentPrice = _cursor.getFloat(_cursorIndexOfCurrentPrice);
            final float _tmpPriceChange;
            _tmpPriceChange = _cursor.getFloat(_cursorIndexOfPriceChange);
            final float _tmpPriceChangePercent;
            _tmpPriceChangePercent = _cursor.getFloat(_cursorIndexOfPriceChangePercent);
            final Integer _tmpPreviousDay;
            if (_cursor.isNull(_cursorIndexOfPreviousDay)) {
              _tmpPreviousDay = null;
            } else {
              _tmpPreviousDay = _cursor.getInt(_cursorIndexOfPreviousDay);
            }
            final long _tmpLastUpdated;
            _tmpLastUpdated = _cursor.getLong(_cursorIndexOfLastUpdated);
            final long _tmpExpiryTime;
            _tmpExpiryTime = _cursor.getLong(_cursorIndexOfExpiryTime);
            _result = new CachedStockInfo(_tmpStockCode,_tmpName,_tmpCurrentPrice,_tmpPriceChange,_tmpPriceChangePercent,_tmpPreviousDay,_tmpLastUpdated,_tmpExpiryTime);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
