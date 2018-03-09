package com.saiteng.shardPreferencesHelper;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.saiteng.user.MessageInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Moore on 2017/8/11.
 */

public class DBManager {
    SQLiteDatabase db;
    private SqlLiteTools helper;
    private List<MessageInfo> data = new ArrayList<MessageInfo>();

    public DBManager(Context context) {
        helper = new SqlLiteTools(context);
        //因为getWritableDatabase内部调用了mContext.openOrCreateDatabase(mName, 0, mFactory);
        //获取可写数据库
        db = helper.getWritableDatabase();
    }

    public void insertData(MessageInfo messageInfo){

        db.execSQL("INSERT INTO messageinfo VALUES (NULL,?,?,?,?,?,?,?,?)",new Object[]{messageInfo.message_Type,messageInfo.message_Direct, messageInfo.message_sender,messageInfo.message_receiver,messageInfo.path,messageInfo.message_length,messageInfo.messageContext,messageInfo.sendstatus});
    }

    public List<MessageInfo> selectData(String  user, String channel){

        MessageInfo messageInfo = null;

        Cursor c = db.rawQuery("select * from messageinfo ",null);

        while(c.moveToNext()){


            String userid=c.getString(c.getColumnIndex("_userid"));
            String channelid=c.getString(c.getColumnIndex("_channelid"));
            if(channel.equals(channelid)){
                messageInfo = new MessageInfo();
                String str0=c.getString(c.getColumnIndex("_type"));
                String str1=c.getString(c.getColumnIndex("_messagepath"));
                String str2=c.getString(c.getColumnIndex("_direct"));
                String str3=c.getString(c.getColumnIndex("_length"));
                String str4=c.getString(c.getColumnIndex("_context"));
                String str5=c.getString(c.getColumnIndex("_sendtype"));

                messageInfo.setMessage_Type(Integer.parseInt(str0));

                messageInfo.setPath(str1);
                messageInfo.setMessage_senderID(userid);

                messageInfo.setMessage_Direct(Integer.parseInt(str2));

                messageInfo.setMessage_length(Integer.parseInt(str3));

                messageInfo.setMessageContext(str4);

                messageInfo.setSendstatus(Integer.parseInt(str5));

                data.add(messageInfo);
            }

        }
        return data;
    }
}
