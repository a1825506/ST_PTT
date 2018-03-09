package com.saiteng.stptt;

import android.media.AudioFormat;
import android.os.Environment;

import com.saiteng.user.ChannelInfo;
import com.saiteng.user.UserInfo;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Moore on 2017/7/24.
 */

public class Config {
    //tcp的ip和端口
    public static String SERVER_IP="192.168.8.155";
    public static int SERVER_PORT=13339;
    public static int SERVER_FilePORT=8560;
    //udp连接的远程端口
    public static int SERVER_DESTPORT=8004;
    public static int LOCAL_RECEVED_STREAM_PORT=11254;
    public static byte [] LOCAL_PORT={};
    public static byte[] LOCAL_IPADDRESS={};
    public static int ORDERLENGTH = 99800;//发送聊天文件的长度，加上头尾字节为1204字节。。
    public static int SlEEPTIME = 50;//发送间隔、单位毫秒。为20时 每帧长度为998+26=1024字节。一秒发送50K。根据带宽调整
    public static int FRAMECOUNT=20; //  一次发送多少多少个数据包。根据发送间隔和带宽调整
    public static  File cameraFile;

    //返回的登录结果
    public static int LOGIN_SUCCESS =0x0001;
    public static int LOGIN_FAILED=0x0002;
    public static int LOGIN_UNREGISTER=0x0003;
    public static int LOGIN_FORBIDDEN=0x0004;
    public static int LOGIN_UNKOWN = 0x0005;
    public static int CONN_ERROR=0x0006;
    public static int START_UPD=0x0007;
    public static int DECODER_HANDLER =0x0008;
    public static final int VISIBLE = 0x0009;
    public static final int INVISIBLE =0x0010;
    public static final int TEMPTALK = 0x0011;

    public static final int TEMPTALK_CLOSE = 0x0012;
    public static final int TEMPTALKRECE = 0x0013;
    public static final int TEMPTALKRECE_POSITIVE = 0x0014;
    public static final int TEMPTALKRECE_CANNEL = 0x0100;
    public static final int SWITCHTOCHAT=0x0101;
    public static final int SWITCHCHANNEL = 0x0102;
    public static final int PTTVISIVLE = 0x0103;
    public static final int PTTINVISIVLE = 0x0104;
    public static final int STARTUDP = 0x0105;
    public static final int STOPSERVER= 0x0106;
    public static final int LOGIN_QUE=0x0107;
    public static final int SWITCHCHANNEL_QUE=0x0108;
    public static final int SENDNOTIFY_QUE=0x0109;
    public static final int TEMPTALK_SUCCESS = 0x0110;
    public static final int SWITCHCHANNEL_SUCCESS=0x0111;
    public static final int CREATECONN=0x0112;
    public static final int CONNECT_SUCCESS=0x0113;
    public static final int UPLOADCHANNEL=0x0114;
    public static final int TEMPTALK_FAILURE=0x0115;
    public static final int TEMPTALK_CLOSE_TIMEOUT=0x0116;
    public static final int CALL_BEGIN=0x0117;
    public static final int CALL_END=0x0118;
    public static final int CALL_SUCCESS=0x0119;
    public static final int CALL_FAILED= 0x0120;
    public static final int RENAMETEMPCHANNEL= 0x0121;
    public static final int EXITTEMPCHANNEL= 0x0122;
    public static final int UPDATE_TEMPCHANNEL=0x0123;
    public static final int UPDATE_CHAT = 0x0124;





    //音视频采集参数
    public static int EASY_SDK_AUDIO_CODEC_AAC=0x15002;
    public static int EASY_SDK_AUDIO_CODEC_G726=0x1100B;
    public static int AUDIORECORD_SAMOLERATE = 8000;
    public static int AUDIORECORD_CHANNL = AudioFormat.CHANNEL_IN_MONO;
    public static int AUDIORECORD_SAMPLEBIT = AudioFormat.ENCODING_PCM_16BIT;
    public static int AudioHandler = -1;//音频解码句柄

    //心跳包发送时间间隔
    public static int KEEPALIVE_FREAM_TIME=20*1000;
    //接收心跳的时间
    public static boolean RECEIVE_TIME = false;

    //本地广播
    public static String BOARDCAST_CREATECONN="com.saiteng.stptt.CREATECONN";//创建连接
    public static String BOARDCAST_DISCONNECT="com.saiteng.stptt.dis_connect";
    public static String BOARDCAST_OPEN = "com.saiteng.stptt.open";
    //登录结果广播
    public static String BOARDCAST_LOGIN_SUCCESS ="com.saiteng.stptt.LOGIN_SUCCESS";
    public static String BOARDCAST_LOGIN_FAILED="com.saiteng.stptt.LOGIN_FAILED";
    public static String BOARDCAST_LOGIN_UNREGISTER="com.saiteng.stptt.LOGIN_UNREGISTER";
    public static String BOARDCAST_LOGIN_FORBIDDEN="com.saiteng.stptt.LOGIN_FORBIDDEN";
    public static String BOARDCAST_LOGIN_UNKOWN="com.saiteng.stptt.LOGIN_UNKNOW";
    public static String BOARDCAST_CONNECT_ERROR="com.saiteng.stptt.CONN_ERROR";
    public static String BOARDCAST_CONNECT_SUCCESS="com.saiteng.stptt.CONNECT_SUCCESS";
    public static String BOARDCAST_LOGIN_OUT="com.saiteng.stptt.LOGIN_OUT";//接收到服务器响应登出操作
    public static String BOARDCAST_VISIBLE = "com.saiteng.stptt.VISIBLE";
    public static String BOARDCAST_INVISIBLE="com.saiteng.stptt.INVISIBLE";
    public static final String  BOARDCAST_TEMPTALK="com.saiteng.stptt.TEMPTALK";//发起临时会话
    public static final String  BOARDCAST_TEMPTALKCLOSE="com.saiteng.stptt.TEMPTALKCLOSE";//临时会话窗口得到效应关闭
    public static final String  BOARDCAST_TEMPTALKFAILURE="com.saiteng.stptt.TEMPTALKFAILURE";//发起临时会话失败

    public static final String  BOARDCAST_TEMPTALKSUCCESS="com.saiteng.stptt.TEMPTALKSUCCESS";//发起临时会话成功

    public static final String  BOARDCAST_TEMPTALK_RECE="com.saiteng.stptt.TEMPTALK_RECE";//接收临时会话
    public static final String  BOARDCAST_SWITCHCHANNEL="com.saiteng.stptt.SWITCHCHANNEL";//切换频道
    public static final String  BOARDCAST_SWITCHCHANNEL_SUCCESS="com.saiteng.stptt.SWITCHCHANNEL_SUCCESS";//切换频道
    public static final String  BOARDCAST_SWITCHCHANNEL_FAILED="com.saiteng.stptt.WITCHCHANNEL_FAILED";//切换频道

    public static final String  BOARDCAST_UPLOADCHANNEL="com.saiteng.stptt.UPLOADCHANNEL";//上报当前频道
    public static final String  BOARDCAST_SWITCHTOCHAT="com.saiteng.stptt.SWITCHTOCHAT";//切换到聊天界面
    public static final String  BOARDCAST_STARTUDP="com.saiteng.stptt.STARTUDP";//获取到服务器的UDPip和端口，启动接收和发送线程
    public static String BOARDCAST_STOPSERVER="com.saiteng.stptt.STOP_SERV";
    public static String BOARDCAST_LOGIN_QUE="com.saiteng.stptt.LOGIN_QUE";//设备发送注销命令
    public static String BOARDCAST_SWITCHCHANNEL_QUE="com.saiteng.stptt.SWITCHCHANNEL_QUE";//设备发送切换编组命令
    public static String BOARDCAST_SENDNOTIFY_QUE="com.saiteng.stptt.SENDNOTIFY_QUE";//设备需要发送通知帧
    public static String BOARDCAST_CALL = "com.saiteng.stptt.BOARDCAST_CALL";//频道申请
    public static String BOARDCAST_CALL_END = "com.saiteng.stptt.BOARDCAST_CALL_END";//频道申请结束
    public static String BOARDCAST_CALL_SUCCESS="com.saiteng.stptt.BOARDCAST_CALL_SUCCESS";//通道申请成功
    public static String BOARDCAST_CALL_FAILED="com.saiteng.stptt.BOARDCAST_CALL_FAILED";//通道申请失败
    public static String BOARDCAST_RENAMETEMPCHANNEL="com.saiteng.stptt.BOARDCAST_RENAMETTEMPCHANNEL";//重命名临时会话
    public static String BOARDCAST_EXITTEMPCHANNEL="com.saiteng.stptt.BOARDCAST_EXITTEMPCHANNEL";//退出临时会话
    public static String BOARDCAST_UPDATECHAT="com.saiteng.stptt.BOARDCAST_UPDATECHAT";//更新聊天界面


    public static final  int DISCONNECT_FROM_SERVER = 0x00100000;


    public static String DCIM= Environment.getExternalStorageDirectory().getPath()+"/DCIM/Camera";


    public static double Latitue,Longitude;

    public static final boolean DEBUG=true;//是否为调试状态


    public static final String NEW_FRIENDS_USERNAME = "item_new_friends";
    public static final String GROUP_USERNAME = "item_groups";
    public static final String MESSAGE_ATTR_IS_VOICE_CALL = "is_voice_call";
    public static final String ACCOUNT_REMOVED = "account_removed";

    public static byte[] USERID =new byte[4];

   //保存用户信息
   public static final List<UserInfo> userInfoList = new ArrayList<UserInfo>();
    //保存频道信息
    public static final List<ChannelInfo> channelInfoList = new ArrayList<ChannelInfo>();

    //保存当前的频道
    public static  String CurrentChannel="CurrentChannel";//当前频道在shardPreferecces中的标识

    public static String CurrentChannelID ="CurrentChannelID";//当前频道ID在shardPreferecces中的标识

    public static String CurrentUserID="CurrentUserID";//当前用户ID在shardPreferecces中的标识

    public static  String CurrentUser = "CurrentUser";//当前用户名在shardPreferecces中的标识

    public static int tempChannelID=0;//临时频道的ID

    public static int choosetempChannelID=0;//选中的临时会话的ID

    public static int switchChannelID=0;//需要切换的频道ID；

    public static String switchChannel=null;//需要切换的频道名称；

    public static String temp_username=null;//临时频道内的用户

    public static int position = -1;

    public static int viewWidth = 600;

    public static int  viewHeight = 700;

    public static final int SERVICE_MESSAGE_EXIT                = 0x100001;

    public static final int SERVICE_MESSAGE_STOP_RECORD         = 0x100004;

    public static final int SERVICE_MESSAGE_CHANGE_CAMERA       = 0x100005;

    public static final int SERVICE_MESSAGE_CHANGE_ZOOM         = 0x100006;

    public static final int SEND_MESSAGE_VISIBILITY_SEEKBAR     = 0x100007;

    public static final int SERVICE_MESSAGE_TAKEPIC              = 0x100008;

    public static final int SERVICE_MESSAGE_START_RECORD         = 0x100009;

    public static final int VOLUME_CHANGED                       = 0x100010;



}
