package com.ruanchao.sqlitedao.database;

import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;

import java.io.File;

/**
 * Created by ruanchao on 2018/7/3.
 */

public class BaseDaoFactory {

    private static volatile BaseDaoFactory mInstabce = null;

    private BaseDaoFactory(){}

    public static BaseDaoFactory getInstance(){
        if (mInstabce == null){
            synchronized (BaseDaoFactory.class){
                if (mInstabce == null){
                    mInstabce = new BaseDaoFactory();
                }
            }
        }
        return mInstabce;
    }

    public<T> IBaseDao<T> getBaseDao(String dbName, Class<T> entity) throws Exception {
        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator  + dbName;
        return getBaseDao(path, dbName, entity);
    }

    public<T> IBaseDao<T> getBaseDao(String path,String dbName, Class<T> entity) throws Exception {
        SQLiteDatabase sqLiteDatabase = SQLiteDatabase.openOrCreateDatabase(path, null);
        BaseDao<T> mBaseDao = null;
        mBaseDao = BaseDao.class.newInstance();
        mBaseDao.init(sqLiteDatabase,entity);
        return mBaseDao;
    }
}
