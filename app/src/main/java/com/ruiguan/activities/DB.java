package com.ruiguan.activities;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import static com.ruiguan.activities.MenuActivity.input_data;
public class DB extends SQLiteOpenHelper {

    public DB(Context context) {
        super(context, "overSpeed.db", null, 1);
    }
    //创建table
    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "CREATE TABLE " + "overSpeedSave" + " (" + "id"+
                " INTEGER primary key autoincrement, " + "date" + " text, "+ "company" +" text, "+"number"+" text, "+"ratedSpeed"+" text, "+"disMax"+" text, "+"SpeedMax"+" text," +
                "AMax" +" text,"+ "AMin" +" text);";
        db.execSQL(sql);
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String sql = "DROP TABLE IF EXISTS " + "overSpeedSave";
        db.execSQL(sql);
        onCreate(db);
    }

    public Cursor select() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db
                .query("overSpeedSave", null, null, null, null, null, null);
        return cursor;
    }
    //增加操作
    public long insert(String str_date)
    {
        String str_company=input_data.getCom();
        String str_number=input_data.getNumber();
        String str_ratedSpeed=input_data.getRatedspeed();
        String str_disMax=input_data.getRatedspeed();
        String str_SpeedMax=input_data.getRatedspeed();
        String str_AMax=input_data.getRatedspeed();
        String str_AMin=input_data.getRatedspeed();

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("date", str_date);
        cv.put("company", str_company);
        cv.put("number", str_number);
        cv.put("ratedSpeed", str_ratedSpeed);
        cv.put("disMax",str_disMax);
        cv.put("SpeedMax",str_SpeedMax);
        cv.put("AMax",str_AMax);
        cv.put("AMin",str_AMin);

        long row = db.insert("overSpeedSave", null, cv);
        return row;
    }
    //删除操作
    public void delete(int id)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        String where = "id" + " = ?";
        String[] whereValue ={ Integer.toString(id) };
        db.delete("overSpeedSave", where, whereValue);
    }
    //修改操作
    public void update(int id)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        String where = "id" + " = ?";
        String[] whereValue = { Integer.toString(id) };

        ContentValues cv = new ContentValues();
        cv.put("date", "20190629");
        cv.put("company", "大连机电工程有限公司");
        cv.put("number", "RT240");
        cv.put("ratedSpeed","2.5");
        cv.put("overspeed", "2.89");
        db.update("overSpeedSave", cv, where, whereValue);
    }

    private class InputData {
    }
}
