package com.saiteng.conn;

import android.content.Intent;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.saiteng.audioHelper.AudioDecoder;
import com.saiteng.audioHelper.ChatInfoDecoder;
import com.saiteng.fragment.ChatFragment;
import com.saiteng.stptt.Config;
import com.saiteng.stptt.MyPTTApplication;
import com.saiteng.stptt.PathUtil;
import com.saiteng.stptt.Utils;
import com.saiteng.user.ChannelInfo;
import com.saiteng.user.MessageInfo;
import com.saiteng.user.UserInfo;
import com.saiteng.videoHelper.AsyncTask;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Moore on 2017/9/12.
 *  开一条连接来传输聊天信息
 *
 */

public class FileTcpConnect extends Thread {
    private Socket socket_client = null;
    private DataOutputStream oWritter = null;
    private DataInputStream oReader = null;
    StringBuilder msg = new StringBuilder();
    private String TAG = "FileTcpConnect";
    private MessageInfo messageInfo;
    private int ALL_DataLength=0;
    private List<Integer> orderid_list = new ArrayList<Integer>();
    private boolean istimer=false;//定时器是否在运行
    private boolean isRecData=false;//是否在接收一段数据

    @Override
    public void run() {
        super.run();
        try {
            socket_client = new Socket(Config.SERVER_IP, Config.SERVER_FilePORT);
            oWritter = new DataOutputStream(socket_client.getOutputStream());
          // 获取Socket对象的输出流，并且在外边包一层DataOutputStream管道，方便输出数
            oReader = new DataInputStream(socket_client.getInputStream());
            byte[] l_aryBuf = new byte[Config.ORDERLENGTH+26];
            byte[] alldata = new byte[1402*10];
            int len = 0;
            int count=0;//用来统计一条数据被接受的次数（数据长度如果大于1024个字节（缓冲区等于1024） 则会被分成多次接收）
            byte[] data=null;

            int totallen =0;
            while ((len=oReader.read(l_aryBuf)) != -1) {
                totallen = totallen+len;
                if(Config.DEBUG){
                   //  Log.e(TAG,"======接收到消息======："+len);

                }

            }
        }catch (IOException e) {
            e.toString();
        }
    }

    /*发送信息的函数*/
    public  void sendFileOrder(byte[] order,int len) {
        try {
            if(oWritter!=null){
                oWritter.write(order,0,len);
                oWritter.flush();
                if(Config.DEBUG){
                    Log.e(TAG,"======发送消息======："+len);
                }
            }else
                Log.e(TAG,"======发送消息失败======："+len);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*关闭socket函数，即断开连接*/
    public void close() {
        try {
            if(oWritter!=null){
                oWritter.close();
                oWritter=null;
            }
            if(oReader!=null){
                oReader.close();
                oReader=null;
            }
            if(socket_client!=null){
                socket_client.close();
                socket_client=null;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
