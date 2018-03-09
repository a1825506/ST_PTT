package com.saiteng.stptt;

import android.app.Activity;
import android.app.Application;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;

import com.saiteng.audioHelper.AudioSender;
import com.saiteng.audioHelper.AudioWrapper;
import com.saiteng.conn.FileTcpConnect;
import com.saiteng.conn.TcpConnect;
import com.saiteng.shardPreferencesHelper.DBManager;
import com.saiteng.shardPreferencesHelper.SharedTools;

import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Moore on 2017/7/26.
 */

public class MyPTTApplication extends Application{
    private Handler pttHandler;
    private boolean openudp=false;
    private static MyPTTApplication instance;
    private AudioWrapper audioWrapper;
    private AudioSender audioSender;
    private LocalBroadcastManager localBroadcastManager;
    private SharedTools sharedTools;
    private DBManager dbManager;
    private List<Activity> list_activity = new ArrayList<Activity>();
    private DatagramSocket udpsocket;
    private TcpConnect tcpConnect;
    private  FileTcpConnect fileTcpConnect;
    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }

    public void addActivity(Activity activity){
        list_activity.add(activity);
    }


    //关闭每一个list内的activity
    public void exitAll() {
        try {
            for (Activity activity:list_activity) {
                if (activity != null)
                    activity.finish();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            System.exit(0);
        }
    }



    public static MyPTTApplication getInstance() {
        return instance;

    }
    public void setPttHandler(Handler mhandler){
        this.pttHandler = mhandler;
    }


    public boolean getOpenudp(){
        return openudp;
    }

    public void setAudioWrapper( AudioWrapper audioWrapper ){
        this.audioWrapper = audioWrapper;
    }

    public AudioWrapper getAudioWrapper(){
        return audioWrapper;
    }

    public void setUdpsocket( DatagramSocket udpsocket ){
        this.udpsocket = udpsocket;
    }

    public DatagramSocket getUdpsocket(){
        return udpsocket;
    }


    public void setAudioSender(AudioSender audioSender){this.audioSender = audioSender;}

    public AudioSender getAudioSender(){
        return audioSender;
    }

    public void setLocalBroadcastManager(LocalBroadcastManager localBroadcastManager){this.localBroadcastManager = localBroadcastManager;}

    public LocalBroadcastManager getLocalBroadcastManager(){return localBroadcastManager;}

    public void setSharedTools(SharedTools sharedTools){this.sharedTools = sharedTools;}

    public SharedTools getSharedTools(){return sharedTools;}

    public void setDBManager(DBManager dbManager){this.dbManager = dbManager;}

    public DBManager getDBManager(){return dbManager;}

    public void setTcpConnect(TcpConnect tcpConnect){
          this.tcpConnect = tcpConnect;
    }

    public TcpConnect getTcpConnect(){
        return tcpConnect;
    }


    public void setFileTcpConnect(FileTcpConnect fileTcpConnect){
        this.fileTcpConnect = fileTcpConnect;
    }

    public FileTcpConnect getFileTcpConnect(){
        return fileTcpConnect;
    }

}
