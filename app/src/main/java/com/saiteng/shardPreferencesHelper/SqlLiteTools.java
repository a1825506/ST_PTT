package com.saiteng.shardPreferencesHelper;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.saiteng.user.MessageInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Moore on 2017/8/11.
 */

public class SqlLiteTools extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "STPTT.db";
    private static final int DATABASE_VERSION = 1;
    public SqlLiteTools(Context context){
        //CursorFactory设置为null,使用默认值
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }



    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS messageinfo (_id INTEGER PRIMARY KEY AUTOINCREMENT, _type ,_direct ,_userid , _channelid ,_messagepath ,_length ,_context ,_sendtype)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("ALTER TABLE person ADD COLUMN other STRING");
    }
}
