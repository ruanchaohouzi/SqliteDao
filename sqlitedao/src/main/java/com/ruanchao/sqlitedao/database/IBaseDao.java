package com.ruanchao.sqlitedao.database;

import android.content.ContentValues;

import java.util.List;

/**
 * Created by ruanchao on 2018/6/29.
 */

public interface IBaseDao<T> {

    long insert(T t);

    long delete(T t) throws Exception;

    long delete( String whereClause, String[] whereArgs);

    long update(T t) throws Exception;

    long update(ContentValues values, String whereClause, String[] whereArgs);

    List<T> queryAll(Class<T> t);

    T queryById(Class<T> t,Long id);

    List<T> query(Class<T> t, String selection,
                  String[] selectionArgs, String groupBy, String having,
                  String orderBy, String limit);

}
