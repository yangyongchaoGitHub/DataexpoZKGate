package com.dataexpo.dataexpozkgate.comm;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.dataexpo.dataexpozkgate.model.Code;
import com.dataexpo.dataexpozkgate.model.PassRecord;

import java.util.ArrayList;
import java.util.Date;

public class DBUtils {
    private final String TAG = DBUtils.class.getSimpleName();
    private final String dbname = "gzcz";
    private final String dbnamePath = "/gzcz.db";
    private SQLiteDatabase db;

    private static class HolderClass {
        private static final DBUtils instance = new DBUtils();
    }

    /**
     * 单例模式
     */
    public static DBUtils getInstance() {
        return HolderClass.instance;
    }

    /**
     * 创建数据表
     * @param contenxt 上下文对象
     */
    public void create(Context contenxt) {
        String path = contenxt.getCacheDir().getPath() + dbnamePath;
        Log.i(TAG, "path========="+ path);
        db = SQLiteDatabase.openOrCreateDatabase(path, null);
        String sql = "create table if not exists " + dbname +
                "(id integer primary key autoincrement," +
                "name nchar(64)," +
                "printtime nchar(64)," +
                "company nchar(64), " +
                "role nchar(32), " +
                "code nchar(32), " +
                "expoid nchar(24))";
        db.execSQL(sql);//创建表
    }

    /**
     * 添加数据
     * bsid 添加的数据ID
     * name 添加数据名称
     */
    public long insertData(String name, String printtime, String company, String role, String code, String expoid) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("name", name);
        contentValues.put("printtime", printtime);
        contentValues.put("company", company);
        contentValues.put("role", role);
        contentValues.put("code", code);
        contentValues.put("expoid", expoid);
        long dataSize = db.insert(dbname, null, contentValues);
        Log.i(TAG, "insertData====" + name + " == " + printtime);
        return dataSize;
    }

    /**
     * 查询数据
     * 返回List
     */
    public ArrayList<PassRecord> listAll() {
        ArrayList<PassRecord> list = new ArrayList<>();
        Cursor cursor = db.query(dbname, null, null, null, null, null, null);
        while (cursor.moveToNext()) {
            String name = cursor.getString(cursor.getColumnIndex("name"));
            String printtime = cursor.getString(cursor.getColumnIndex("printtime"));
            String company = cursor.getString(cursor.getColumnIndex("company"));
            String role = cursor.getString(cursor.getColumnIndex("role"));
            String code = cursor.getString(cursor.getColumnIndex("code"));
            String expoid = cursor.getString(cursor.getColumnIndex("expoid"));
            list.add(new PassRecord(name, printtime, company, role, code, expoid));
//            Log.i(TAG, "name: " + name + " printtime: " + printtime + " company: " + company +
//                    " role: " + role + " code: " + code + " expoid: " + expoid);
        }
        cursor.close();

        return list;
    }

    public int countToDay() {
        int result = 0;
        String printtime = Utils.formatTime(new Date().getTime(), "yyyy-MM-dd");
        //tv_welcome.setText(date);

        Cursor cursor = db.query(dbname, new String[]{"printtime"}, "printtime like ? ",
                new String[]{"%" + printtime + "%"}, null, null, null);
        while (cursor.moveToNext()) {
            result++;
        }
        cursor.close();

        return result;
    }

    /**
     * 根据ID删除数据
     * id 删除id
     */
    public int delData(int id) {
        Log.e(TAG, "id==============" + id);
        int inde = db.delete(dbname, "id = ?", new String[]{String.valueOf(id)});
        Log.e(TAG, "删除了==============" + inde);
        return inde;
    }

    /**
     * 根据
     *
     */
    public int delDataAll() {
        int inde = db.delete(dbname,null,null);
        Log.e("--Main--", "删除了==============" + inde);
        return inde;
    }

    /**
     * 根据ID修改数据
     * id 修改条码的id
     * bsid 修改的ID
     * name 修改的数据库
     */
    public int modifyData(int id, int bsid, String name) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("name", name);
        contentValues.put("bsid", id);
        int index = db.update(dbname, contentValues, "id = ?", new String[]{String.valueOf(id)});
        Log.e("--Main--", "修改了===============" + index);
        return index;
    }

    /**
     * 查询code单个数据
     * @param code
     * @return
     */
    public boolean selectisData(String code) {
        //查询数据库
        Cursor cursor = db.query(dbname, null, "eucode = ?", new String[]{code}, null, null, null);
        while (cursor.moveToNext()) {
            return true;
        }
        return false;
    }

    public int count(String code) {
        int result = 0;
        Cursor cursor = db.query(dbname, null, "code = ?", new String[]{code}, null, null, null);
        while (cursor.moveToNext()) {
            result++;
        }
        cursor.close();

        return result;
    }
}