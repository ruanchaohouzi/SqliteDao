轻量级的数据库ORM框架

采取注解和反射的机制实现，代码比较轻量级，如果需要实现复杂功能，可以在此基础上改建。

一、使用方式

由于没有发布到Moven仓库，可以直接拷贝sqlitedao工程下的代码直接使用，也就几个类。

1、新建数据库对象并添加注解


```
@DbTable("user")
public class User {

    @Id(columns = "_id")
    private Long id;
    @DbColumns(columns ="age",isNull = false)
    private int age = -1;
    @DbColumns(columns ="sex",isNull = false)
    private String sex;
    @DbColumns(columns ="name",isNull = false)
    private String name;
    @DbColumns(columns ="icon",isNull = true)
    private byte[] icon;
}
```

（1）其中@DbTable("user")注解表示数据库表的名字

（2）@Id(columns = "_id")表示主键，默认自动增长，可以设置autoincrement不需要自动增长，
主要类型必须是包装类Long类型，为了解决long类型默认值为0的问题。

（3）@DbColumns(columns ="sex",isNull = false)表示数据库表的列名，默认可以为null,
可以通过参数isNull控制是否为空。

2、创建数据库和表对象

```
mUserDao = BaseDaoFactory.getInstance().getBaseDao("rc.db", User.class);
```


3、实现增删改查方法（这里只是列举了部分简单操作，复杂的操作详见后面的数据库接口）

```
增加操作

public void insertDb(View view) {
        User user = new User();
        user.setAge(Integer.valueOf(mAge.getText().toString()));
        user.setName(mName.getText().toString());
        user.setSex(mSex.getText().toString());
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.icon);
        user.setIcon(bitmapToByteArr(bitmap));
        Long insert = mUserDao.insert(user);
    }

删除操作
public void deleteDb(View view) throws Exception {
        User user = new User();
        user.setId(2l);
        long delete = mUserDao.delete(user);
    }

更新操作
public void update(View view) throws Exception {
        User user = new User();
        user.setId(1l);
        user.setSex("男");
        user.setAge(28);
        user.setName("ruanchao");
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.icon);
        user.setIcon(bitmapToByteArr(bitmap));
        long update = mUserDao.update(user);
    }

查询操作
 public void query(View view) {
        User user = mUserDao.queryById(User.class, 2L);
    }
```

二、数据库框架详细说明

1、注解类(annotation包下)

目前主要定义了三个注解（主键、表名、列名），以后可以根据需要扩展注解，例如加入主外键等。

```
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DbColumns {
    String columns();
    boolean isNull() default true;
}

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface DbTable {
    String value();
}

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Id {
    String columns();
    boolean autoincrement() default true;
}
```

2、数据库接口类（IBaseDao）

主要定义了目前数据库操作的增删改查接口

```
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
```

3、数据库操作具体实现类（BaseDao）

 主要在类BaseDao中，由于代码比较多，不做详细介绍。主要通过注解和方式的方式实现。

4、数据库操作工厂类（BaseDaoFactory)

```
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
```

工厂类是一个单例模式，主要创建数据库和初始化表，可以指定创建数据库的位置。



