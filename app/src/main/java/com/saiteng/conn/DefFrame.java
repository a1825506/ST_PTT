package com.saiteng.conn;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.saiteng.stptt.Config;
import com.saiteng.stptt.MyPTTApplication;
import com.saiteng.stptt.Utils;
import com.saiteng.user.ChannelInfo;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;


/**
 * Created by Moore on 2017/7/31.
 * 该类创建各种命令帧
 */

public class DefFrame {


     private static boolean Call=false;
    /**
     *通知帧命令
     */
    public static void createNotifty(final TcpConnect tcpConnect) {
        final byte[] order = new byte[20];
        byte[] notifty = {0x68,0x00,0x0e,0x68,0x00,0x00,0x00,0x10,0x00,0x00,0x00,0x00};
        //将用户ID复制到notifty数组中
        System.arraycopy(Config.USERID, 0, notifty, 8, 4);
        System.arraycopy(notifty, 0, order, 0, notifty.length);
        System.arraycopy( Config.LOCAL_IPADDRESS, 0, order, notifty.length, Config.LOCAL_IPADDRESS.length);
        System.arraycopy(Config.LOCAL_PORT, 0, order, notifty.length+Config.LOCAL_IPADDRESS.length, Config.LOCAL_PORT.length);
        int sum=0;
        for(int i=4;i<18;i++){
            sum+=order[i];
        }
        order[18]=(byte)sum;
        order[19]=0x16;
        Log.e("DefFrame","发送通知帧");
        sendOrder(tcpConnect,order);
    }

    /**
     *数据链打通帧,该帧主要为了打通终端与服务端之间的数据链通道,用UDP发送
     */
    public static  void  createopen() {
        final byte[] order = new byte[16];
        byte[] notifty = {0x68,0x00,0x0a,0x68,0x00,0x00,0x00,0x11};
        System.arraycopy(notifty, 0, order, 0, notifty.length);
        System.arraycopy( Config.LOCAL_IPADDRESS, 0, order, notifty.length, Config.LOCAL_IPADDRESS.length);
        System.arraycopy(Config.LOCAL_PORT, 0, order, notifty.length+Config.LOCAL_IPADDRESS.length, Config.LOCAL_PORT.length);
        int sum=0;
        for(int i=4;i<14;i++){
            sum+=order[i];
        }
        order[14]=(byte)sum;
        order[15]=0x16;
        new Thread(new Runnable() {
            @Override
            public void run() {
                MyPTTApplication.getInstance().getAudioSender().sendData(order,order.length);
            }
        }).start();
    }


    /**
     * 登出命令
     */
    public static void logout(final TcpConnect tcpConnect) {
        final byte[] order = new byte[14];
        byte[] notifty = {0x68,0x00,0x08,0x68,0x00,0x00,0x00,0x02,0x00,0x00,0x00,0x00};
        System.arraycopy(Utils.int2Bytes(MyPTTApplication.getInstance().getSharedTools().getShareInt(Config.CurrentUserID,0),4), 0, notifty, 8, 4);
        System.arraycopy(notifty, 0, order, 0, notifty.length);
        int sum=0;
        for(int i=4;i<12;i++){
            sum+=order[i];
        }
        order[12]=(byte)sum;
        order[13]=0x16;
        sendOrder(tcpConnect,order);
    }


    /**
     *切换编组命令
     */
    public static  void sendChannelSwitchOrder(final TcpConnect tcpConnect) {
        byte[] srcChannelid = Utils.int2Bytes(MyPTTApplication.getInstance().getSharedTools().getShareInt(Config.CurrentChannelID,0),4);//当前频道ID

        byte[] desChannelid =  Utils.int2Bytes(Config.switchChannelID,4);//需要切换的频道ID

        byte[] userid = Utils.int2Bytes(MyPTTApplication.getInstance().getSharedTools().getShareInt(Config.CurrentUserID,0),4);//当前用户ID

        final byte[] head = {0x68,0x00,0x10,0x68,0x00,0x00,0x00,0x39,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00};
        System.arraycopy(srcChannelid, 0, head, 8, 4);
        System.arraycopy(desChannelid, 0, head, 12, 4);
        System.arraycopy(userid, 0, head, 16, 4);
        int sum=0;
        for(int i=4;i<20;i++){
            sum+=head[i];
        }
        head[20]=(byte)sum;
        head[21]=0x16;

        sendOrder(tcpConnect,head);
    }

    /**
     *接听临时会话命令
     */
    public static void cretatejointempchannel(final boolean result,final TcpConnect tcpConnect) {
        final byte[] head ={0x68,0x00,0x09,0x68,0x00,0x00,0x00,0x3A,0x00,0x00,0x00,0x00,0x00,0x00,0x00};
        byte[] tempChannelID = Utils.int2Bytes(Config.tempChannelID,4);
        System.arraycopy(tempChannelID,0,head,8,4);
        byte[] byte_rec = {0x01};//接听
        byte[] byte_ref = {0x00};//拒绝
        if(result){
            System.arraycopy(byte_rec,0,head,12,1);
        }else
            System.arraycopy(byte_ref,0,head,12,1);
        int sum=0;
        for(int i=4;i<13;i++){
            sum+=head[i];
        }
        head[13]=(byte)sum;
        head[14]=0x16;
        sendOrder(tcpConnect,head);
        Log.i("-------","发送接听命令帧");//
    }


    /**
     *申请频道过程，持续发
     */
    public static void Call(final boolean isCall,final TcpConnect tcpConnect) {
        Call=isCall;
        final byte[] order = new byte[19];
        byte[] notifty = {0x68,0x00,0x0d,0x68,0x00,0x00,0x00,(byte)0x91};
        byte[] userid =Utils.int2Bytes(MyPTTApplication.getInstance().getSharedTools().getShareInt(Config.CurrentUserID,0),4);
        byte[] channelid = Utils.int2Bytes(MyPTTApplication.getInstance().getSharedTools().getShareInt(Config.CurrentChannelID,0),4);
        byte[] flag={1};
        System.arraycopy(notifty, 0, order, 0, notifty.length);
        System.arraycopy(userid, 0, order, notifty.length, 4);
        System.arraycopy(channelid, 0, order, notifty.length+4, 4);
        System.arraycopy(flag, 0, order, notifty.length+8, 1);

        int sum=0;
        for(int i=4;i<17;i++){
            sum+=order[i];
        }
        order[17]=(byte)sum;
        order[18]=0x16;

        new Thread(new Runnable() {
            @Override
            public void run() {
                while(Call){
                    tcpConnect.sendOrder(order,order.length);
                    Log.e("发送申请命令",order[7]+"");
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            }
        }).start();
    }


    /**
     *申请结束
     */
    public static void  CallEnd(final boolean isCall,final TcpConnect tcpConnect){
        Call=isCall;
        final byte[] order = new byte[19];
        byte[] notifty = {0x68,0x00,0x0d,0x68,0x00,0x00,0x00,(byte)0x91};
        byte[] userid =Utils.int2Bytes(MyPTTApplication.getInstance().getSharedTools().getShareInt(Config.CurrentUserID,0),4);
        byte[] channelid = Utils.int2Bytes(MyPTTApplication.getInstance().getSharedTools().getShareInt(Config.CurrentChannelID,0),4);
        byte[] flag={0};
        System.arraycopy(notifty, 0, order, 0, notifty.length);
        System.arraycopy(userid, 0, order, notifty.length, 4);
        System.arraycopy(channelid, 0, order, notifty.length+4, 4);
        System.arraycopy(flag, 0, order, notifty.length+8, 1);

        int sum=0;
        for(int i=4;i<17;i++){
            sum+=order[i];
        }
        order[17]=(byte)sum;
        order[18]=0x16;

        sendOrder(tcpConnect,order);
    }

    /**
     *上报当前频道的ID
     */
    public static void uploadChannelid(final TcpConnect tcpConnect) {
        int channelID=0;
        String json= MyPTTApplication.getInstance().getSharedTools().getShareObject("channel",null);
        if (json != null) {
            Gson gson = new Gson();
            Type type = new TypeToken<List<ChannelInfo>>() {
            }.getType();
            List<ChannelInfo> list_channel = new ArrayList<ChannelInfo>();
            list_channel = gson.fromJson(json, type);
            for(int i = 0; i< list_channel.size(); i++){
                if(list_channel.get(i).getChannelname().equals(MyPTTApplication.getInstance().getSharedTools().getShareString(Config.CurrentChannel,"default"))){
                    //获取当前的频道ID。
                    channelID= list_channel.get(i).getChannelid();
                }

            }
        }
        final byte[] head={0x68,0x00,0x08,0x68,0x00,0x00,0x00,0x08,0x00,0x00,0x00,0x08,0x00,0x16};
        byte[] byte_channelid= Utils.int2Bytes(channelID,4);
        System.arraycopy(byte_channelid,0,head,8,4);
        int sum=0;
        for(int i=4;i<12;i++){
            sum+=head[i];
        }
        head[12]=(byte)sum;
        sendOrder(tcpConnect,head);
    }
       //创建临时会话  group_name  user3:user1    group_list  user3-user1
    public static void tempTalkOrder(final String channel_name,final String channel_list,final TcpConnect tcpConnect){
        byte[] head ={0x68,0x00,0x00,0x68,0x00,0x00,0x00,0x30};
        byte[] group_name = channel_name.getBytes();
        byte[] group_list = channel_list.getBytes();
        byte[] byte_channel_name = new byte[64];//编组名为固定64字节，暂时定为谁发起的就叫什么。
        System.arraycopy(group_list, 0, byte_channel_name, 0, group_list.length);//将帧头部分复制到数据帧数组中
        byte[] channel_type = {0x00,0x00,0x00,0x00};//编组类型只能是临时编组
        byte[] isbans = {0x00,0x00,0x00,0x00};//是否被禁用
        byte[] discrip = new byte[128];//128字节的描述部分
        int group_name_length = group_name.length;//用户名列表长度
        byte[] byte_group_name_length =Utils.int2Bytes(group_name_length,4);//用户名列表长度
        int datalength = 24+64+128+2+group_name.length;//对照命令格式

        final byte[] order = new byte[datalength];//整个命令帧的长度为用户数据区长度16+group_name.length+channel_name.length+6定义帧长度
        byte[] data_length = Utils.int2Bytes(datalength-6,2);//用户数据区长度
        int l = Utils.bytes2Int(data_length,0,2);
        System.arraycopy(data_length, 0, head, 1, 2);
        System.arraycopy(head, 0, order, 0, head.length);//将帧头部分复制到数据帧数组中
        System.arraycopy(byte_channel_name,0,order,head.length,64);//将编组名复制到数据帧数组中
        System.arraycopy(channel_type, 0, order,head.length+64,4);//将编组类型复制到数据帧数组中
        System.arraycopy(byte_group_name_length, 0, order,head.length+68,4);//将用户名列表长度复制到数据帧数组中
        System.arraycopy(group_name, 0, order,head.length+72,group_name.length);//将用户名复制到数据帧数组中
        System.arraycopy(channel_type, 0, order, head.length+72+group_name.length,4);//编组类型复制到数据帧数组中
        System.arraycopy(isbans, 0, order, head.length+76+group_name.length,4);
        System.arraycopy(discrip, 0, order, head.length+80+group_name.length,4);

        int sum=0;
        for(int i=4;i<datalength-2;i++){
            sum+=order[i];
        }
        order[datalength-2]=(byte)sum;
        order[datalength-1]=0x16;
        sendOrder(tcpConnect,order);

    }
   /**
    *发送心跳包
    */
    public static void heartBeat(final TcpConnect tcpConnect){
        final byte[] head ={0x68,0x00,0x04,0x68,0x00,0x00,0x00,0x20,0x20,0x16};
        sendOrder(tcpConnect,head);
    }

    /**
     *用户退出当前临时会话
     */
    public static void ExitTempChannel(final TcpConnect tcpConnect){
        final byte[] head ={0x68,0x00,0x00,0x68,0x00,0x00,0x00,0x37};
        byte[] tempChannelId =Utils.int2Bytes(Config.choosetempChannelID,4);
        byte[] username = MyPTTApplication.getInstance().getSharedTools().getShareString(Config.CurrentUser,null).getBytes();
        byte[] datalength = Utils.int2Bytes(username.length,2);
        final byte[] order = new byte[14+username.length];
        System.arraycopy(datalength, 0, head, 1, 2);
        System.arraycopy(head, 0, order, 0, 8);
        System.arraycopy(tempChannelId, 0, order, 8, 4);
        System.arraycopy(username, 0, order, 12, username.length);
        int sum=0;
        for(int i=4;i<order.length-2;i++){
            sum+=order[i];
        }
        order[order.length-2]=(byte)sum;
        order[head.length-1]=0x16;
        sendOrder(tcpConnect,order);
    }


    /**
     *上报终端信息，聊天时的连接需要
     */
    public static void UploadInfo(final FileTcpConnect fileTcpConnect){
        final byte[] head ={0x68,0x00,0x04,0x68,0x00,0x00,0x00,(byte)0x94,0x00,0x00,0x00,0x00,0x00,0x16};
        byte[] userid =Utils.int2Bytes(MyPTTApplication.getInstance().getSharedTools().getShareInt(Config.CurrentUserID,0),4);
        System.arraycopy(userid, 0, head, 8, 4);
        int int_userid = Utils.bytes2Int(userid, 0, 4);
        Log.e("Userid",""+Integer.toHexString(int_userid));
        int sum=0;
        for(int i=4;i<head.length-2;i++){
            sum+=head[i];
        }
        head[head.length-2]=(byte)sum;
        new Thread(new Runnable() {
            @Override
            public void run() {
                NettyClient.getInstance().sendMsgToServer(head, new ChannelFutureListener() {    //3
                    @Override
                    public void operationComplete(ChannelFuture future) {
                        if (future.isSuccess()) {                //4
                           // Log.d("Write heartbeat successful");

                        } else {
                           // Timber.e("Write heartbeat error");
                           // WriteLogUtil.writeLogByThread("heartbeat error");
                        }
                    }
                });
   //             MyPTTApplication.getInstance().getFileTcpConnect().sendFileOrder(head,head.length);
            }
        }).start();
    }
    /**
     *上报聊天信息，
     */
    public static void sendChatInfo(int type,int messageId,long message_length,byte[] data){
         byte[] head ={(byte)0xEF,(byte)0xEF,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,(byte)0xEF,(byte)0xEF};
         final byte[] order = new byte[head.length+data.length+2];
         byte[] byte_datalength =Utils.int2Bytes(data.length,2) ;
         byte[] byte_messageid = Utils.int2Bytes(messageId,4);
         byte[] byte_messagelength = new byte[8];
         byte_messagelength =Utils.long2Byte(byte_messagelength,message_length);
         byte[] byte_messagetype = Utils.int2Bytes(type,1);
         byte[] userid =Utils.int2Bytes(MyPTTApplication.getInstance().getSharedTools().getShareInt(Config.CurrentUserID,0),4);
         byte[] channelid = Utils.int2Bytes(MyPTTApplication.getInstance().getSharedTools().getShareInt(Config.CurrentChannelID,0),4);
        System.arraycopy(byte_datalength, 0, head, 2, 2);
        System.arraycopy(byte_messageid, 0, head, 4, 4);
        System.arraycopy(byte_messagelength, 4, head, 8, 4);
        System.arraycopy(byte_messagetype, 0, head, 12, 1);
        System.arraycopy(userid, 0, head, 13, 4);
        System.arraycopy(channelid, 0, head, 17, 4);
        System.arraycopy(head, 0, order, 0, head.length);
        System.arraycopy(data, 0, order, head.length, data.length);
        int sum=0;
        for(int i=4;i<order.length-2;i++){
            sum+=order[i];
        }
        order[order.length-2]=(byte)sum;
        order[order.length-1]=0x36;
        new Thread(new Runnable() {
            @Override
            public void run() {
                NettyClient.getInstance().sendMsgToServer(order, new ChannelFutureListener() {    //3
                    @Override
                    public void operationComplete(ChannelFuture future) {
                        if (future.isSuccess()) {                //4
                            Log.e("DefFrame","发送成功"+order.length);

                        } else {
                            Log.e("DefFrame","发送失败");
                        }
                    }
                });
             //   MyPTTApplication.getInstance().getFileTcpConnect().sendFileOrder(order,order.length);
            }
        }).start();
    }

    public static void sendOrder(final TcpConnect tcpConnect,final byte[] order){
        new Thread(new Runnable() {
            @Override
            public void run() {
                tcpConnect.sendOrder(order,order.length);
            }
        }).start();
    }
}
