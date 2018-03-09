package com.saiteng.user;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Moore on 2017/8/8.
 * 封装用户消息
 * 包括用户名，用户别名，用户类型，用户所属频道，用户是否在线，用户是否被禁用等信息。
 *
 */

public class UserInfo {

    private String username;
    private String useralias;
    private long userid;
    private String usertype;
    private int isforbidden;
    private String group;
    private int isOnline;

    public void setUsername(String username){
        this.username = username;
    }
    public String getUsername(){
        return username;
    }

    public void setUseralias(String useralias){
        this.useralias = useralias;
    }
    public String getUseralias(){
        return useralias;
    }

    public void setUserid(long userid){
        this.userid = userid;
    }
    public long getUserid(){
        return userid;
    }
    public void setUsertype(String usertype){
        this.usertype = usertype;
    }
    public String getUsertype(){
        return usertype;
    }

    public void setIsforbidden(int isforbidden){
        this.isforbidden = isforbidden;
    }
    public int getIsforbidden(){
        return isforbidden;
    }

    public void setGroup(String group){
        this.group = group;
    }
    public String getGroup(){
        return group;
    }

    public void setOnline(int online){
        this.isOnline=online;
    }

    public int getOnline(){
        return isOnline;
    }

}
