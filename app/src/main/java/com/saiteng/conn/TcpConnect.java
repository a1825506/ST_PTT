package com.saiteng.conn;

import android.content.Intent;
import android.util.Log;
import com.saiteng.stptt.Config;
import com.saiteng.stptt.MainPTTActivity;
import com.saiteng.stptt.MyPTTApplication;
import com.saiteng.stptt.Utils;
import com.saiteng.user.ChannelInfo;
import com.saiteng.user.UserInfo;
import com.saiteng.videoHelper.AsyncTask;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Moore on 2017/8/16.
 */

public class TcpConnect extends AsyncTask {
    private  Socket socket_client = null;
    private  DataOutputStream oWritter = null;
    private  DataInputStream oReader = null;
    StringBuilder msg = new StringBuilder();
    private String TAG = "STPTT_TcpConnect";

    private int     localPort;
    private long    localIP;
    private UserInfo class_userinfo;
    private ChannelInfo class_channelInfo;
    private Intent intent;

    public TcpConnect(){
        localIP = Utils.getLocalIPAddress();
        Config.LOCAL_IPADDRESS=Utils.longToBytes2(localIP);
        Config.LOCAL_PORT = Utils.int2Bytes(Config.LOCAL_RECEVED_STREAM_PORT,2);
        byte[] port = {-49,111};
        int p=Utils.bytes2Int(port,0,2);
    }
    @Override
    protected Object doInBackground(Object[] params) {
        try {
            socket_client = new Socket(Config.SERVER_IP,Config.SERVER_PORT);
            oWritter = new DataOutputStream(socket_client.getOutputStream()); // 获取Socket对象的输出流，并且在外边包一层DataOutputStream管道，方便输出数
            oReader = new DataInputStream(socket_client.getInputStream());
            intent = new Intent(Config.BOARDCAST_CONNECT_SUCCESS);
            MyPTTApplication.getInstance().getLocalBroadcastManager().sendBroadcast(intent);
            byte[] l_aryBuf = new byte[1024];
            byte[] data=null;
            int count=0;//用来统计一条数据被接受的次数（数据长度如果大于1024个字节（缓冲区等于1024） 则会被分成多次接收）
            int len = 0;
            while ((len=oReader.read(l_aryBuf)) != -1) {
                Log.e(TAG,"标志位："+l_aryBuf[7]);
                data=new byte[1024*(count+1)];
                if(len==1024){
                    count++;
                    System.arraycopy(l_aryBuf, 0, data, 1024*count, len);
                    continue;
                }else{
                    System.arraycopy(l_aryBuf, 0, data, 1024*count, len);
                    checkData(data);
                }
                l_aryBuf=null;
                l_aryBuf=new byte[1024];
            }
            close();
        } catch (IOException e) {
            intent = new Intent(Config.BOARDCAST_CONNECT_ERROR);
            MyPTTApplication.getInstance().getLocalBroadcastManager().sendBroadcast(intent);
        }
        return null;
    }

    /**
     * 检验数据的合法性。根据校验和来验证。
     */
    private void checkData(byte[] receviedata){
        //接收前必须要保证数据的完整性。
        byte[] length = new byte[2];
        List<Integer> index = new ArrayList<Integer>();
        for(int i=0;i<receviedata.length;i++){
            if(receviedata[i]==104&&receviedata[i+3]==104){
                System.arraycopy(receviedata, i+1, length, 0,2);
                int int_length = Utils.bytes2Int(length,0,2);
                if(receviedata[i+4+int_length+1]==22){
                    //判断接收到的数据中包括多少条命令。由{68,00,00,68，，，，16}确定，在去验证数据的正确性
                    index.add(i);
                }
            }
        }
        //验证命令数据的正确性，由校验和验证
        for(int i=0;i<index.size();i++){
            int data_length = 0;
            if(i==index.size()-1){
                byte[] byte_data_length = new byte[2];
                System.arraycopy(receviedata, index.get(i)+1, byte_data_length, 0,2);
                data_length = Utils.bytes2Int(byte_data_length,0,2)+6;
            }else{
                data_length = index.get(i+1)-index.get(i);
            }
            byte[] order  = new byte[data_length];
            System.arraycopy(receviedata, index.get(i), order, 0,data_length);
            int sum=0;
            for (int j=4;j<data_length-2;j++){
                sum+=order[j];
            }
            byte src=(byte)sum;
            if(src==order[data_length-2]){
                //如果校验和相等，则处理命令
                dealData(order);
            }else
                Log.e(TAG,"收到数据格式有误，请检查数据的合法性"+receviedata[7]);

        }
    }


    /*发送信息的函数*/
    public  void sendOrder(byte[] order,int len) {
        try {
            if(oWritter!=null){
                oWritter.write(order,0,len);
                oWritter.flush();
            }else{
                intent = new Intent(Config.BOARDCAST_CONNECT_ERROR);
                MyPTTApplication.getInstance().getLocalBroadcastManager().sendBroadcast(intent);
            }

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

    private void dealData(byte[] receviedata) {

            if (receviedata[7] == 1) {
                Log.e(TAG,"标志位："+receviedata[7]+"。登录返回信息");
                //登录返回信息。
                if (receviedata[8] == 0) {
                    intent = new Intent(Config.BOARDCAST_LOGIN_UNREGISTER);
                } else if (receviedata[8] == 1) {
                    //登录成功，获取保存用户ID
                    intent = new Intent(Config.BOARDCAST_LOGIN_SUCCESS);
                    System.arraycopy(receviedata, 9, Config.USERID, 0, 4);
                } else if (receviedata[8] == 2) {
                    intent = new Intent(Config.BOARDCAST_LOGIN_FORBIDDEN);
                } else if (receviedata[8] == 3) {
                    intent = new Intent(Config.BOARDCAST_LOGIN_FAILED);
                } else
                    intent = new Intent(Config.BOARDCAST_LOGIN_OUT);
                MyPTTApplication.getInstance().getLocalBroadcastManager().sendBroadcast(intent);
                intent = null;
            } else if (receviedata[7] == 2) {
                //用户登出
                Log.e(TAG,"标志位："+receviedata[7]+"。用户登出返回信息");
                intent = new Intent(Config.BOARDCAST_STOPSERVER);
                MyPTTApplication.getInstance().getLocalBroadcastManager().sendBroadcast(intent);
            }else if(receviedata[7] == 8){
                //用户上报当前频道ID后的收到响应。
                Log.e(TAG,"标志位："+receviedata[7]+"。上报当前频道返回信息");
            } else if (receviedata[7] == 16) {
                //接收到通知帧返回消息,包括UDP连接的ip和端口
                Log.e(TAG,"标志位："+receviedata[7]+"。通知帧返回信息");
                MainPTTActivity.connect = true;
                byte[] destip = new byte[4];
                byte[] destport = new byte[2];
                System.arraycopy(receviedata, 8, destip, 0, 4);
                System.arraycopy(receviedata, 12, destport, 0, 2);
                Config.SERVER_DESTPORT = Utils.bytes2Int(destport, 0, 2);
              //  Config.SERVER_DESTIP = Utils.unintbyte2long(destip);
                Log.e("TAG","UDP port"+Config.SERVER_DESTPORT);
               intent = new Intent(Config.BOARDCAST_STARTUDP);
                MyPTTApplication.getInstance().getLocalBroadcastManager().sendBroadcast(intent);


            } else if (receviedata[7] == 32) {
                // 接收到心跳包，更新接收到心跳的的时间戳。
                Log.e(TAG,"标志位："+receviedata[7]+"。心跳包返回信息");
                Config.RECEIVE_TIME = true;
            } else if (receviedata[7] == 4) {
                Log.e(TAG,"标志位："+receviedata[7]+"。所有用户信息");
                //服务器下发所有用户信息。需保存所有用户的信息到本地。
                byte[] usernum = new byte[4];//记录用户个数
                System.arraycopy(receviedata, 8, usernum, 0, 4);
                int user_num = Utils.bytes2Int(usernum, 0, 4);
                List<Integer> index_list = new ArrayList();
                List<Integer> index_list1 = new ArrayList();
                byte[] userinfo = new byte[receviedata.length-18];//去掉头尾的用户信息数据
                System.arraycopy(receviedata, 12, userinfo, 0, receviedata.length-18);
                int temp=0;
                for(int i=0;i<user_num;i++){
                    class_userinfo = new UserInfo();
                    //找 用户名 后的第一个 "-" 分割符下标
                        for(int k=temp;k<userinfo.length;k++){
                            if(userinfo[k]==45){
                                index_list.add(k);
                                break;
                            }
                        }

                        byte[] byte_username=new byte[index_list.get(0)-temp-1];
                        System.arraycopy(userinfo, temp+1, byte_username, 0, index_list.get(0)-temp-1);
                        String str_username = new String(byte_username);//用户名按字符串处理
                        System.out.println("用户名："+str_username);
                        class_userinfo.setUsername(str_username);

                        byte[] byte_userid = new byte[4];
                        System.arraycopy(userinfo, temp+byte_username.length+2, byte_userid, 0, 4);
                        int int_userid = Utils.bytes2Int(byte_userid, 0, 4);
                        System.out.println("用户ID："+Integer.toHexString(int_userid));
                        class_userinfo.setUserid(int_userid);

                        byte[] byte_userbans = new byte[4];
                        System.arraycopy(userinfo, temp+byte_username.length+7, byte_userbans, 0, 4);
                        int int_userbans = Utils.bytes2Int(byte_userbans, 0, 4);
                        System.out.println("用户是否被禁用"+int_userbans);
                        class_userinfo.setIsforbidden(int_userbans);

                        byte[] byte_useronline = new byte[4];
                        System.arraycopy(userinfo, temp+byte_username.length+12, byte_useronline, 0, 4);
                        int int_userlogin = Utils.bytes2Int(byte_useronline, 0, 4);
                        System.out.println("用户是否登录："+int_userlogin + "");
                        class_userinfo.setOnline(int_userlogin);

                        byte[] byte_groupnum = new byte[4];
                        System.arraycopy(userinfo, temp+byte_username.length+17, byte_groupnum, 0, 4);
                        int int_groupnum = Utils.bytes2Int(byte_groupnum, 0, 4);
                        System.out.println("用户所属组个数："+int_groupnum + "");

                        // 找到所属组名后的第一个“#”分割符号
                        for(int j=temp+byte_username.length+22;j<userinfo.length;j++){
                            if(userinfo[j]==35){
                                index_list1.add(j);
                                break;
                            }
                        }
                        if(index_list1.size()==0){
                            //最后一组数据，没有分割符
                            byte[] byte_groupname = new byte[userinfo.length-(temp+byte_username.length+22)];
                            System.arraycopy(userinfo, temp+byte_username.length+22, byte_groupname, 0, userinfo.length-(temp+byte_username.length+22));
                            String str_groupnum = new String(byte_groupname);
                            System.out.println("用户所属群组："+str_groupnum);
                            class_userinfo.setGroup(str_groupnum);

                        }else{
                            byte[] byte_groupname = new byte[index_list1.get(0)-(temp+byte_username.length+22)];
                            System.arraycopy(userinfo, temp+byte_username.length+22, byte_groupname, 0, index_list1.get(0)-(temp+byte_username.length+22));
                            String str_groupnum = new String(byte_groupname);
                            System.out.println("用户所属群组："+str_groupnum);
                            class_userinfo.setGroup(str_groupnum);
                            temp=index_list1.get(0);

                        }
                    index_list.clear();
                    index_list1.clear();
                    Config.userInfoList.add(class_userinfo);
                }
                //保存所有用户信息
                MyPTTApplication.getInstance().getSharedTools().setShareObject("user",Config.userInfoList);
            } else if(receviedata[7] == 55){
                Log.e(TAG,"标志位："+receviedata[7]+"。用户退出临时频道信息");
            }else if (receviedata[7] == 56) {
                Log.e(TAG,"标志位："+receviedata[7]+"。所有频道信息");
                //服务器下发所有频道信息.需保存所有频道信息到本地
                byte[] groupnum = new byte[4];//记录群组个数
                System.arraycopy(receviedata, 8, groupnum, 0, 4);
                int group_num = Utils.bytes2Int(groupnum, 0, 4);
                byte[] groupinfo = new byte[receviedata.length-14];//去掉头尾的组信息数据
                System.arraycopy(receviedata, 12, groupinfo, 0, receviedata.length-14);
                //#test1:123456:0#test2:123456:0#test3:123456:1#user1:user3:123456:1
                int temp=0;
                List<Integer> index_list = new ArrayList();
                List<Integer> index_list1 = new ArrayList();
                for(int i=0;i<group_num;i++){
                    class_channelInfo = new ChannelInfo();
                    //遍历组名后的第一个:分割符。组名规定为字母 数字 下划线
                    for(int k=temp;k<groupinfo.length;k++){
                        if(groupinfo[k]==58){
                            index_list.add(k);
                            break;
                        }
                    }
                    byte[] byte_groupname = new byte[index_list.get(0)-temp-1];
                    System.arraycopy(groupinfo, temp+1, byte_groupname, 0, index_list.get(0)-temp-1);
                    String str_groupname = new String(byte_groupname);
                    System.out.println("频道名："+str_groupname);
                    class_channelInfo.setChannelname(str_groupname);

                    byte[] byte_groupid = new byte[4];
                    System.arraycopy(groupinfo, temp+byte_groupname.length+2, byte_groupid, 0, 4);
                    int int_groupid = Utils.bytes2Int(byte_groupid, 0, 4);
                    System.out.println("频道ID："+Integer.toHexString(int_groupid));//
                    class_channelInfo.setChannelid(int_groupid);

                    byte[] byte_grouptype = new byte[4];
                    System.arraycopy(groupinfo, temp+byte_groupname.length+7, byte_grouptype, 0, 4);
                    int int_type = Utils.bytes2Int(byte_grouptype,0,4);
                    System.out.println("频道类型："+int_type);//
                    class_channelInfo.setChannelidtype(int_type);

                    // 找到频道类型后的第一个“#”分割符号
                    for(int j=temp+byte_groupname.length+7;j<groupinfo.length;j++){
                        if(groupinfo[j]==35){
                            index_list1.add(j);
                            break;
                        }
                    }
                    if(index_list1.size()!=0){
                        temp=index_list1.get(0);
                    }
                    index_list.clear();
                    index_list1.clear();
                    Config.channelInfoList.add(class_channelInfo);
                }
                intent = new Intent(Config.BOARDCAST_UPLOADCHANNEL);//上报当前的的频道ID
                MyPTTApplication.getInstance().getLocalBroadcastManager().sendBroadcast(intent);
                MyPTTApplication.getInstance().getSharedTools().setShareObject("channel",Config.channelInfoList);
            } else if (receviedata[7] == 57) {
                //收到切换编组消息
                Log.e(TAG,"标志位："+receviedata[7]+"。切换频道返回信息");
                byte[] result = new byte[1];
                byte[] channel_id = new byte[4];
                System.arraycopy(receviedata, 8, result, 0,1);
                System.arraycopy(receviedata, 9, channel_id, 0,4);
                int int_result = Utils.bytes2Int(result,0,1);
                int int_channel_id = Utils.bytes2Int(channel_id,0,4);
                System.out.println("返回编组ID："+Integer.toHexString(int_channel_id));
                if(int_result==1){
                   //切换成功
                    MyPTTApplication.getInstance().getSharedTools().setShareInt(Config.CurrentChannelID,int_channel_id);
                    intent = new Intent(Config.BOARDCAST_SWITCHCHANNEL_SUCCESS);
                }else{
                    //切换失败
                    intent = new Intent(Config.BOARDCAST_SWITCHCHANNEL_FAILED);
                }
                MyPTTApplication.getInstance().getLocalBroadcastManager().sendBroadcast(intent);
            } else if (receviedata[7] == 48) {
                //收到创建临时频道是否成功的返回信息。
                Log.e(TAG,"标志位："+receviedata[7]+"。创建临时频道信息");
                if (receviedata[8] == 0) {
                    //创建失败
                    intent = new Intent(Config.BOARDCAST_TEMPTALKCLOSE);
                    MyPTTApplication.getInstance().getLocalBroadcastManager().sendBroadcast(intent);
                } else if (receviedata[8] == 1) {
                    //创建成功，发送创建成功广播
                    intent = new Intent(Config.BOARDCAST_TEMPTALKSUCCESS);
                    MyPTTApplication.getInstance().getLocalBroadcastManager().sendBroadcast(intent);
                }
            } else if (receviedata[7] == 58) {
                Log.e(TAG,"标志位："+receviedata[7]+"。临时频道信息");
                class_channelInfo = new ChannelInfo();
                //服务器下发临时频道信息。接收到这个消息，手机弹出接听或挂断窗口。
                byte[] byte_tempChannelID = new byte[4];
                System.arraycopy(receviedata, 8, byte_tempChannelID, 0, 4);
                //临时编组的ID，需要在手机客户端保存。每次切换频道的时候需要将该ID告诉服务器。
                Config.tempChannelID = Utils.bytes2Int(byte_tempChannelID, 0, 4);
                System.out.println("临时频道ID："+Integer.toHexString( Config.tempChannelID));//
                class_channelInfo.setChannelid(Config.tempChannelID);
                //获取用户名列表。按字符串处理
                byte[] byte_username = new byte[64];
                System.arraycopy(receviedata, 12, byte_username, 0, 64);

                Config.temp_username = new String(byte_username);
                class_channelInfo.setChannelname(Config.temp_username);
                class_channelInfo.setChannelidtype(0);
                Config.channelInfoList.add(class_channelInfo);
                MyPTTApplication.getInstance().getSharedTools().setShareObject("channel",Config.channelInfoList);
                //刷新临时会话列表。弹出接听 挂断弹框
                intent = new Intent(Config.BOARDCAST_TEMPTALK_RECE);
                MyPTTApplication.getInstance().getLocalBroadcastManager().sendBroadcast(intent);
            } else if (receviedata[7] == 7) {
                Log.e(TAG,"标志位："+receviedata[7]+"。接听临时频道信息");
                //服务器下发设备接听或挂断临时会话的信息，只要有用户接听，发起端就停止呼叫，进入对讲状态，切换到当前临时频道频道
                byte[] byte_recTemp = new byte[4];
                System.arraycopy(receviedata, 16, byte_recTemp, 0, 4);
                int int_recTemp = Utils.bytes2Int(byte_recTemp, 0, 4);
                byte[] userid = new byte[4];
                System.arraycopy(receviedata, 12, userid, 0, 4);
                int int_userid = Utils.bytes2Int(userid, 0, 4);
                if (int_recTemp == 1) {
                    //发起端关闭呼叫界面
                    if(MyPTTApplication.getInstance().getSharedTools().getShareInt(Config.CurrentUserID,0)!=int_userid){
                        intent = new Intent(Config.BOARDCAST_TEMPTALKCLOSE);
                        MyPTTApplication.getInstance().getLocalBroadcastManager().sendBroadcast(intent);
                        //发送切换编组的命令
                        Config.switchChannelID=Config.tempChannelID;
                        Config.switchChannel =  Config.temp_username;
                        Intent intent = new Intent(Config.BOARDCAST_SWITCHCHANNEL_QUE);
                        MyPTTApplication.getInstance().getLocalBroadcastManager().sendBroadcast(intent);
                    }


                }
            }else if(receviedata[7] == -111){
              //  Log.e(TAG,"标志位："+receviedata[7]+"。通道申请响应");
                if(receviedata[8] == 0){
                    //通道申请失败不可以发送语音对讲
                    intent = new Intent(Config.BOARDCAST_CALL_FAILED);
                }else if(receviedata[8] == 1){
                    //通道申请成功才可以发送语音对讲
                    intent = new Intent(Config.BOARDCAST_CALL_SUCCESS);
                }
                MyPTTApplication.getInstance().getLocalBroadcastManager().sendBroadcast(intent);
            }
        intent=null;
    }
}
