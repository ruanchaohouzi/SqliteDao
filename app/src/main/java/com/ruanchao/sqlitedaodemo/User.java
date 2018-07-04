package com.ruanchao.sqlitedaodemo;

import com.ruanchao.sqlitedao.annotation.DbColumns;
import com.ruanchao.sqlitedao.annotation.DbTable;
import com.ruanchao.sqlitedao.annotation.Id;

/**
 * Created by ruanchao on 2018/6/29.
 */

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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public byte[] getIcon() {
        return icon;
    }

    public void setIcon(byte[] icon) {
        this.icon = icon;
    }
}
