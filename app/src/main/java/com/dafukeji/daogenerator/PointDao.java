package com.dafukeji.daogenerator;

import java.util.List;
import java.util.ArrayList;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import de.greenrobot.dao.AbstractDao;
import de.greenrobot.dao.Property;
import de.greenrobot.dao.internal.SqlUtils;
import de.greenrobot.dao.internal.DaoConfig;
import de.greenrobot.dao.query.Query;
import de.greenrobot.dao.query.QueryBuilder;

import com.dafukeji.daogenerator.Point;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table "POINT".
*/
public class PointDao extends AbstractDao<Point, Long> {

    public static final String TABLENAME = "POINT";

    /**
     * Properties of entity Point.<br/>
     * Can be used for QueryBuilder and for referencing column names.
    */
    public static class Properties {
        public final static Property CurrentTime = new Property(0, Long.class, "currentTime", false, "CURRENT_TIME");
        public final static Property Id = new Property(1, Long.class, "id", true, "_id");
        public final static Property Temperature = new Property(2, Float.class, "temperature", false, "TEMPERATURE");
        public final static Property CureId = new Property(3, Long.class, "cureId", false, "CURE_ID");
    };

    private DaoSession daoSession;

    private Query<Point> cure_PointsQuery;

    public PointDao(DaoConfig config) {
        super(config);
    }
    
    public PointDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
        this.daoSession = daoSession;
    }

    /** Creates the underlying database table. */
    public static void createTable(SQLiteDatabase db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"POINT\" (" + //
                "\"CURRENT_TIME\" INTEGER," + // 0: currentTime
                "\"_id\" INTEGER PRIMARY KEY AUTOINCREMENT ," + // 1: id
                "\"TEMPERATURE\" REAL," + // 2: temperature
                "\"CURE_ID\" INTEGER);"); // 3: cureId
    }

    /** Drops the underlying database table. */
    public static void dropTable(SQLiteDatabase db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"POINT\"";
        db.execSQL(sql);
    }

    /** @inheritdoc */
    @Override
    protected void bindValues(SQLiteStatement stmt, Point entity) {
        stmt.clearBindings();
 
        Long currentTime = entity.getCurrentTime();
        if (currentTime != null) {
            stmt.bindLong(1, currentTime);
        }
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(2, id);
        }
 
        Float temperature = entity.getTemperature();
        if (temperature != null) {
            stmt.bindDouble(3, temperature);
        }
 
        Long cureId = entity.getCureId();
        if (cureId != null) {
            stmt.bindLong(4, cureId);
        }
    }

    @Override
    protected void attachEntity(Point entity) {
        super.attachEntity(entity);
        entity.__setDaoSession(daoSession);
    }

    /** @inheritdoc */
    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 1) ? null : cursor.getLong(offset + 1);
    }    

    /** @inheritdoc */
    @Override
    public Point readEntity(Cursor cursor, int offset) {
        Point entity = new Point( //
            cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0), // currentTime
            cursor.isNull(offset + 1) ? null : cursor.getLong(offset + 1), // id
            cursor.isNull(offset + 2) ? null : cursor.getFloat(offset + 2), // temperature
            cursor.isNull(offset + 3) ? null : cursor.getLong(offset + 3) // cureId
        );
        return entity;
    }
     
    /** @inheritdoc */
    @Override
    public void readEntity(Cursor cursor, Point entity, int offset) {
        entity.setCurrentTime(cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0));
        entity.setId(cursor.isNull(offset + 1) ? null : cursor.getLong(offset + 1));
        entity.setTemperature(cursor.isNull(offset + 2) ? null : cursor.getFloat(offset + 2));
        entity.setCureId(cursor.isNull(offset + 3) ? null : cursor.getLong(offset + 3));
     }
    
    /** @inheritdoc */
    @Override
    protected Long updateKeyAfterInsert(Point entity, long rowId) {
        entity.setId(rowId);
        return rowId;
    }
    
    /** @inheritdoc */
    @Override
    public Long getKey(Point entity) {
        if(entity != null) {
            return entity.getId();
        } else {
            return null;
        }
    }

    /** @inheritdoc */
    @Override    
    protected boolean isEntityUpdateable() {
        return true;
    }
    
    /** Internal query to resolve the "points" to-many relationship of Cure. */
    public List<Point> _queryCure_Points(Long cureId) {
        synchronized (this) {
            if (cure_PointsQuery == null) {
                QueryBuilder<Point> queryBuilder = queryBuilder();
                queryBuilder.where(Properties.CureId.eq(null));
                cure_PointsQuery = queryBuilder.build();
            }
        }
        Query<Point> query = cure_PointsQuery.forCurrentThread();
        query.setParameter(0, cureId);
        return query.list();
    }

    private String selectDeep;

    protected String getSelectDeep() {
        if (selectDeep == null) {
            StringBuilder builder = new StringBuilder("SELECT ");
            SqlUtils.appendColumns(builder, "T", getAllColumns());
            builder.append(',');
            SqlUtils.appendColumns(builder, "T0", daoSession.getCureDao().getAllColumns());
            builder.append(" FROM POINT T");
            builder.append(" LEFT JOIN CURE T0 ON T.\"CURE_ID\"=T0.\"_id\"");
            builder.append(' ');
            selectDeep = builder.toString();
        }
        return selectDeep;
    }
    
    protected Point loadCurrentDeep(Cursor cursor, boolean lock) {
        Point entity = loadCurrent(cursor, 0, lock);
        int offset = getAllColumns().length;

        Cure cure = loadCurrentOther(daoSession.getCureDao(), cursor, offset);
        entity.setCure(cure);

        return entity;    
    }

    public Point loadDeep(Long key) {
        assertSinglePk();
        if (key == null) {
            return null;
        }

        StringBuilder builder = new StringBuilder(getSelectDeep());
        builder.append("WHERE ");
        SqlUtils.appendColumnsEqValue(builder, "T", getPkColumns());
        String sql = builder.toString();
        
        String[] keyArray = new String[] { key.toString() };
        Cursor cursor = db.rawQuery(sql, keyArray);
        
        try {
            boolean available = cursor.moveToFirst();
            if (!available) {
                return null;
            } else if (!cursor.isLast()) {
                throw new IllegalStateException("Expected unique result, but count was " + cursor.getCount());
            }
            return loadCurrentDeep(cursor, true);
        } finally {
            cursor.close();
        }
    }
    
    /** Reads all available rows from the given cursor and returns a list of new ImageTO objects. */
    public List<Point> loadAllDeepFromCursor(Cursor cursor) {
        int count = cursor.getCount();
        List<Point> list = new ArrayList<Point>(count);
        
        if (cursor.moveToFirst()) {
            if (identityScope != null) {
                identityScope.lock();
                identityScope.reserveRoom(count);
            }
            try {
                do {
                    list.add(loadCurrentDeep(cursor, false));
                } while (cursor.moveToNext());
            } finally {
                if (identityScope != null) {
                    identityScope.unlock();
                }
            }
        }
        return list;
    }
    
    protected List<Point> loadDeepAllAndCloseCursor(Cursor cursor) {
        try {
            return loadAllDeepFromCursor(cursor);
        } finally {
            cursor.close();
        }
    }
    

    /** A raw-style query where you can pass any WHERE clause and arguments. */
    public List<Point> queryDeep(String where, String... selectionArg) {
        Cursor cursor = db.rawQuery(getSelectDeep() + where, selectionArg);
        return loadDeepAllAndCloseCursor(cursor);
    }
 
}
