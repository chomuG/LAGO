package com.lago.app.data.local;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.lago.app.domain.entity.InvestmentLevel;
import com.lago.app.domain.entity.RiskLevel;
import com.lago.app.domain.entity.User;
import java.lang.Class;
import java.lang.Exception;
import java.lang.IllegalArgumentException;
import java.lang.IllegalStateException;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class UserDao_Impl implements UserDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<User> __insertionAdapterOfUser;

  private final Converters __converters = new Converters();

  public UserDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfUser = new EntityInsertionAdapter<User>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `users` (`id`,`email`,`name`,`profileImageUrl`,`phoneNumber`,`dateOfBirth`,`investmentLevel`,`preferredRiskLevel`,`totalInvestment`,`totalReturn`,`isVerified`,`createdAt`,`updatedAt`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final User entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getEmail());
        statement.bindString(3, entity.getName());
        if (entity.getProfileImageUrl() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getProfileImageUrl());
        }
        if (entity.getPhoneNumber() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getPhoneNumber());
        }
        if (entity.getDateOfBirth() == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.getDateOfBirth());
        }
        statement.bindString(7, __InvestmentLevel_enumToString(entity.getInvestmentLevel()));
        statement.bindString(8, __RiskLevel_enumToString(entity.getPreferredRiskLevel()));
        statement.bindDouble(9, entity.getTotalInvestment());
        statement.bindDouble(10, entity.getTotalReturn());
        final int _tmp = entity.isVerified() ? 1 : 0;
        statement.bindLong(11, _tmp);
        final String _tmp_1 = __converters.fromDate(entity.getCreatedAt());
        if (_tmp_1 == null) {
          statement.bindNull(12);
        } else {
          statement.bindString(12, _tmp_1);
        }
        final String _tmp_2 = __converters.fromDate(entity.getUpdatedAt());
        if (_tmp_2 == null) {
          statement.bindNull(13);
        } else {
          statement.bindString(13, _tmp_2);
        }
      }
    };
  }

  @Override
  public Object insertUser(final User user, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfUser.insert(user);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object getUserById(final String id, final Continuation<? super User> $completion) {
    final String _sql = "SELECT * FROM users WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<User>() {
      @Override
      @Nullable
      public User call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfEmail = CursorUtil.getColumnIndexOrThrow(_cursor, "email");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfProfileImageUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "profileImageUrl");
          final int _cursorIndexOfPhoneNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "phoneNumber");
          final int _cursorIndexOfDateOfBirth = CursorUtil.getColumnIndexOrThrow(_cursor, "dateOfBirth");
          final int _cursorIndexOfInvestmentLevel = CursorUtil.getColumnIndexOrThrow(_cursor, "investmentLevel");
          final int _cursorIndexOfPreferredRiskLevel = CursorUtil.getColumnIndexOrThrow(_cursor, "preferredRiskLevel");
          final int _cursorIndexOfTotalInvestment = CursorUtil.getColumnIndexOrThrow(_cursor, "totalInvestment");
          final int _cursorIndexOfTotalReturn = CursorUtil.getColumnIndexOrThrow(_cursor, "totalReturn");
          final int _cursorIndexOfIsVerified = CursorUtil.getColumnIndexOrThrow(_cursor, "isVerified");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final User _result;
          if (_cursor.moveToFirst()) {
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpEmail;
            _tmpEmail = _cursor.getString(_cursorIndexOfEmail);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpProfileImageUrl;
            if (_cursor.isNull(_cursorIndexOfProfileImageUrl)) {
              _tmpProfileImageUrl = null;
            } else {
              _tmpProfileImageUrl = _cursor.getString(_cursorIndexOfProfileImageUrl);
            }
            final String _tmpPhoneNumber;
            if (_cursor.isNull(_cursorIndexOfPhoneNumber)) {
              _tmpPhoneNumber = null;
            } else {
              _tmpPhoneNumber = _cursor.getString(_cursorIndexOfPhoneNumber);
            }
            final String _tmpDateOfBirth;
            if (_cursor.isNull(_cursorIndexOfDateOfBirth)) {
              _tmpDateOfBirth = null;
            } else {
              _tmpDateOfBirth = _cursor.getString(_cursorIndexOfDateOfBirth);
            }
            final InvestmentLevel _tmpInvestmentLevel;
            _tmpInvestmentLevel = __InvestmentLevel_stringToEnum(_cursor.getString(_cursorIndexOfInvestmentLevel));
            final RiskLevel _tmpPreferredRiskLevel;
            _tmpPreferredRiskLevel = __RiskLevel_stringToEnum(_cursor.getString(_cursorIndexOfPreferredRiskLevel));
            final double _tmpTotalInvestment;
            _tmpTotalInvestment = _cursor.getDouble(_cursorIndexOfTotalInvestment);
            final double _tmpTotalReturn;
            _tmpTotalReturn = _cursor.getDouble(_cursorIndexOfTotalReturn);
            final boolean _tmpIsVerified;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsVerified);
            _tmpIsVerified = _tmp != 0;
            final Date _tmpCreatedAt;
            final String _tmp_1;
            if (_cursor.isNull(_cursorIndexOfCreatedAt)) {
              _tmp_1 = null;
            } else {
              _tmp_1 = _cursor.getString(_cursorIndexOfCreatedAt);
            }
            final Date _tmp_2 = __converters.toDate(_tmp_1);
            if (_tmp_2 == null) {
              throw new IllegalStateException("Expected NON-NULL 'java.util.Date', but it was NULL.");
            } else {
              _tmpCreatedAt = _tmp_2;
            }
            final Date _tmpUpdatedAt;
            final String _tmp_3;
            if (_cursor.isNull(_cursorIndexOfUpdatedAt)) {
              _tmp_3 = null;
            } else {
              _tmp_3 = _cursor.getString(_cursorIndexOfUpdatedAt);
            }
            final Date _tmp_4 = __converters.toDate(_tmp_3);
            if (_tmp_4 == null) {
              throw new IllegalStateException("Expected NON-NULL 'java.util.Date', but it was NULL.");
            } else {
              _tmpUpdatedAt = _tmp_4;
            }
            _result = new User(_tmpId,_tmpEmail,_tmpName,_tmpProfileImageUrl,_tmpPhoneNumber,_tmpDateOfBirth,_tmpInvestmentLevel,_tmpPreferredRiskLevel,_tmpTotalInvestment,_tmpTotalReturn,_tmpIsVerified,_tmpCreatedAt,_tmpUpdatedAt);
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

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }

  private String __InvestmentLevel_enumToString(@NonNull final InvestmentLevel _value) {
    switch (_value) {
      case BEGINNER: return "BEGINNER";
      case INTERMEDIATE: return "INTERMEDIATE";
      case ADVANCED: return "ADVANCED";
      case EXPERT: return "EXPERT";
      default: throw new IllegalArgumentException("Can't convert enum to string, unknown enum value: " + _value);
    }
  }

  private String __RiskLevel_enumToString(@NonNull final RiskLevel _value) {
    switch (_value) {
      case VERY_LOW: return "VERY_LOW";
      case LOW: return "LOW";
      case MEDIUM: return "MEDIUM";
      case HIGH: return "HIGH";
      case VERY_HIGH: return "VERY_HIGH";
      default: throw new IllegalArgumentException("Can't convert enum to string, unknown enum value: " + _value);
    }
  }

  private InvestmentLevel __InvestmentLevel_stringToEnum(@NonNull final String _value) {
    switch (_value) {
      case "BEGINNER": return InvestmentLevel.BEGINNER;
      case "INTERMEDIATE": return InvestmentLevel.INTERMEDIATE;
      case "ADVANCED": return InvestmentLevel.ADVANCED;
      case "EXPERT": return InvestmentLevel.EXPERT;
      default: throw new IllegalArgumentException("Can't convert value to enum, unknown value: " + _value);
    }
  }

  private RiskLevel __RiskLevel_stringToEnum(@NonNull final String _value) {
    switch (_value) {
      case "VERY_LOW": return RiskLevel.VERY_LOW;
      case "LOW": return RiskLevel.LOW;
      case "MEDIUM": return RiskLevel.MEDIUM;
      case "HIGH": return RiskLevel.HIGH;
      case "VERY_HIGH": return RiskLevel.VERY_HIGH;
      default: throw new IllegalArgumentException("Can't convert value to enum, unknown value: " + _value);
    }
  }
}
