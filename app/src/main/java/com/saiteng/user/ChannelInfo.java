
package com.saiteng.user;

import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;

/**
 * Created by Moore on 2017/8/8.
 * 封装频道消息类型
 * 包括频道名，频道别名，频道ID
 * Serializable:做序列化。为了保存在内存中的各种对象的状态（也就是实例变量，不是方法），并且可以把保存的对象状态再读出来
 */

public class ChannelInfo implements Serializable {

    private String channelname;
    private String channelalias;
    private int channelid;
    private int channelidtype;

    public void setChannelname(String channelname){
           this.channelname = channelname;
    }
    public String getChannelname(){
        return channelname;
    }

    public void setChannelalias(String channelalias){
        this.channelalias = channelalias;
    }
    public String getChannelalias(){
        return channelalias;
    }

    public void setChannelid(int channelid){
        this.channelid = channelid;
    }
    public int getChannelid(){
        return channelid;
    }

    public void setChannelidtype(int channelidtype){
        this.channelidtype = channelidtype;
    }
    public int getChannelidtype(){
        return channelidtype;
    }


}
