package com.ruanchao.sqlitedao.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.util.Log;

import com.ruanchao.sqlitedao.annotation.DbColumns;
import com.ruanchao.sqlitedao.annotation.DbTable;
import com.ruanchao.sqlitedao.annotation.Id;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by ruanchao on 2018/6/29.
 */

public class BaseDao<T> implements IBaseDao<T> {

    private SQLiteDatabase mSQLiteDatabase;
    private boolean isInit = false;
    private String mDbTableName;
    private static final String TAG = BaseDao.class.getSimpleName();
    //主要是为了缓存数据库的列名和对象的字段映射关系
    private HashMap<String,Field> mColumnCacheMap = new HashMap<>();
    private String primaryKey;

    public synchronized boolean init(SQLiteDatabase sQLiteDatabase, Class<T> entity) throws Exception {
        if (isInit){
            return true;
        }
        mSQLiteDatabase = sQLiteDatabase;
        if (!mSQLiteDatabase.isOpen()){
            return false;
        }
        if (!isAutoCreateTable(entity)){
            return false;
        }
        initColmunCacheMap(entity);
        isInit = true;
        return true;
    }

    private void initColmunCacheMap(Class<T> entity) {

        //必须根据实际数据库中的列进行映射，反射数据库实际失败，对象的注解和数据库的列不一致的问题
        String sql = "select * from " + mDbTableName + " limit 1,0";
        Cursor cursor = null;
        try{
            cursor = mSQLiteDatabase.rawQuery(sql, null);
            String[] columnNames = cursor.getColumnNames();
            Field[] declaredFields = entity.getDeclaredFields();
            for (String  columnName:columnNames){
                Field columnField = null;
                for (Field declaredField:declaredFields){
                    DbColumns dbColumnsAnnot = declaredField.getAnnotation(DbColumns.class);
                    Id idAnnot = declaredField.getAnnotation(Id.class);
                    if ((dbColumnsAnnot!=null && dbColumnsAnnot.columns().equals(columnName))
                            ||(idAnnot != null && idAnnot.columns().equals(columnName))){
                        columnField = declaredField;
                        break;
                    }
                }
                if (columnField != null){
                    mColumnCacheMap.put(columnName, columnField);
                }
            }
        }catch (Exception e){

        }finally {
            if (cursor != null){
                cursor.close();
                cursor = null;
            }
        }
    }

    private boolean isAutoCreateTable(Class<T> entity) throws Exception {
        DbTable dbTableAnnot = entity.getAnnotation(DbTable.class);
        if (dbTableAnnot == null){
            return false;
        }
        mDbTableName = dbTableAnnot.value();
        StringBuilder sb = new StringBuilder();
        sb.append("CREATE TABLE IF NOT EXISTS ");
        sb.append(mDbTableName + "(");
        Field[] declaredFields = entity.getDeclaredFields();
        for (int i = 0; i<declaredFields.length; i++){
            //拼接数据库主键
            Id idAnnot = declaredFields[i].getAnnotation(Id.class);
            if (idAnnot != null){
                sb.append(idAnnot.columns() + " INTEGER PRIMARY KEY");
                primaryKey = idAnnot.columns();
                if (idAnnot.autoincrement()){
                    sb.append(" AUTOINCREMENT ,");
                }
            }
            //拼接数据库字段
            DbColumns dbColumnsAnnot = declaredFields[i].getAnnotation(DbColumns.class);
            if (dbColumnsAnnot != null){
                String columnsName = dbColumnsAnnot.columns();

                Class<?> type = declaredFields[i].getType();
                String columnsType = getColumnsType(type);
                if (columnsType == null){
                    //不支持的数据库类型
                    continue;
                }
                sb.append(columnsName + " " + columnsType);
                boolean isNull = dbColumnsAnnot.isNull();
                if (!isNull) {
                    sb.append("  NOT NULL");
                }
                sb.append(",");
            }
        }
        if (sb.toString().charAt(sb.length() - 1) == ','){
            sb.deleteCharAt(sb.length() - 1);
        }
        sb.append(" )");
        Log.i(TAG,"create table sql:" + sb.toString());
        mSQLiteDatabase.execSQL(sb.toString());
        return true;
    }

    private String getColumnsType(Class<?> type) {
        String columnsType = null;
        if (type == String.class){
            columnsType = " TEXT ";
        }else if (type == Double.class){
            columnsType = " DOUBLE ";
        }else if (type == int.class){
            columnsType = " INT ";
        }else if (type == Long.class){
            columnsType = " BIGINT ";
        }else if (type == byte[].class){
            columnsType =" BLOB ";
        }
        return columnsType;
    }

    @Override
    public long insert(T entity) {

        ContentValues contentValues = null;
        try {
            contentValues = getContentValues(entity);
            return mSQLiteDatabase.insert(mDbTableName, null,contentValues);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return -1;
    }

    @Override
    public long delete(T t) throws Exception{
        Object primaryKeyValue = getPrimaryKeyValue(t);
        String whereClause = primaryKey + "=?";
        String[] whereArgs = new String[]{String.valueOf((long)primaryKeyValue)};
        return delete(whereClause,whereArgs);
    }

    @Override
    public long delete(String whereClause, String[] whereArgs) {
        return mSQLiteDatabase.delete(mDbTableName,whereClause, whereArgs);
    }

    @Override
    public long update(T t) throws Exception {
        Object primaryKeyValue = getPrimaryKeyValue(t);
        //为了保证更新必须含有主键
        if (primaryKeyValue != null) {
            ContentValues values = getContentValues(t);
            String whereClause = primaryKey + "=?";
            String[] whereArgs = new String[]{String.valueOf((long)primaryKeyValue)};
            return update(values, whereClause, whereArgs);
        }
        return -1;
    }

    @Override
    public long update(ContentValues values, String whereClause, String[] whereArgs) {
        return mSQLiteDatabase.update(mDbTableName,values,whereClause,whereArgs);
    }

    @Override
    public T queryById(Class<T> t, Long id) {
        String selection = primaryKey + "= ?";
        String[] selectionArgs = new String[]{id.toString()};
        List<T> list = query(t, selection, selectionArgs, null, null, null, null);
        if (list != null && list.size() >0){
            return list.get(0);
        }
        return null;
    }

    @Override
    public List<T> queryAll(Class<T> entity) {
        return query(entity,null,null,null,null,null,null);
    }

    @Override
    public List<T> query(Class<T> entity, String selection, String[] selectionArgs, String groupBy, String having, String orderBy, String limit) {

        try {
            String[] column = (String[]) mColumnCacheMap.keySet().toArray(new String[0]);
            Cursor cursor = mSQLiteDatabase.query(mDbTableName, column, selection, selectionArgs, groupBy, having, orderBy, limit);
            List<T> list = new ArrayList<>();
            while (cursor.moveToNext()) {
                Iterator<Map.Entry<String, Field>> entryIterator = mColumnCacheMap.entrySet().iterator();
                Object t = entity.newInstance();
                while (entryIterator.hasNext()) {
                    Map.Entry<String, Field> entry = entryIterator.next();

                    Field field = entry.getValue();
                    field.setAccessible(true);
                    String columnName = entry.getKey();
                    Class<?> type = field.getType();
                    if (type == String.class) {
                        field.set(t, cursor.getString(cursor.getColumnIndex(columnName)));
                    } else if (type == Double.class) {
                        field.set(t, cursor.getDouble(cursor.getColumnIndex(columnName)));
                    } else if (type == int.class) {
                        field.set(t, cursor.getInt(cursor.getColumnIndex(columnName)));
                    } else if (type == Long.class) {
                        field.set(t, cursor.getLong(cursor.getColumnIndex(columnName)));
                    } else if (type == byte[].class) {
                        field.set(t, cursor.getBlob(cursor.getColumnIndex(columnName)));
                    }
                }
                if (t != null) {
                    list.add((T) t);
                }

            }
            return list;
        } catch (Exception e) {

            Log.e("BaseDao" , e.getMessage());
        }
        return null;
    }

    @NonNull
    private Object getPrimaryKeyValue(T t) throws Exception {
        Field primaryKeyField = mColumnCacheMap.get(primaryKey);
        Object primaryKeyValue = primaryKeyField.get(t);
        if (primaryKeyValue == null){
            throw new Exception("primaryKeyValue can not null");
        }
        return primaryKeyValue;
    }

    private ContentValues getContentValues(T entity) throws IllegalAccessException {
        ContentValues contentValues = new ContentValues();
        Iterator<Map.Entry<String, Field>> iterator = mColumnCacheMap.entrySet().iterator();
        while (iterator.hasNext()){
            Map.Entry<String, Field> entry = iterator.next();
            String column = entry.getKey();
            Field field = entry.getValue();
            field.setAccessible(true);
            Object fieldValue = field.get(entity);
            if (fieldValue == null){
                continue;
            }
            Class<?> type = field.getType();
            if (type == String.class){
                contentValues.put(column, String.valueOf(fieldValue));
            }else if (type == Double.class){
                contentValues.put(column, (double)fieldValue);
            }else if (type == int.class){
                contentValues.put(column, (int)fieldValue);
            }else if (type == Long.class){
                contentValues.put(column, (long)fieldValue);
            }else if (type == byte[].class){
                contentValues.put(column, (byte[]) fieldValue);
            }
        }

        return contentValues;
    }
}
