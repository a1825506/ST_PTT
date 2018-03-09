package com.saiteng.Service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.ione.opustool.OpusJni;
import com.saiteng.audioHelper.AudioWrapper;
import com.saiteng.audioHelper.ChatInfoDecoder;
import com.saiteng.conn.DefFrame;
import com.saiteng.conn.FileTcpConnect;
import com.saiteng.conn.NettyClient;
import com.saiteng.conn.NettyListener;
import com.saiteng.conn.TcpConnect;
import com.saiteng.dialog.CommonDialog;
import com.saiteng.stptt.Config;
import com.saiteng.stptt.MyPTTApplication;
import com.saiteng.stptt.R;
import java.io.IOException;

import io.netty.buffer.ByteBuf;

/**
 * Created by Moore on 2017/7/31.
 * 在service中启动发送心跳包的线程，心跳包必须在软件运行过程中一直发送
 */

public class MainPTTService extends Service implements NettyListener {
    private String TAG="MainPTTService";
    KeepAliveSendThread keepAliveThread;
    KeepAliveReceiveThread keepAliveReceiveThread;
    KeepCallThread keepCallThread;//呼叫持续20s
    private int Delay=30;//心跳包允许的最大延时
    private MyPTTApplication myPTTApplication;
    private long lasttime;
    private AudioWrapper audioWrapper = AudioWrapper.getInstance();
    private TemptalkReceiver temptalkReceiver;
    private IntentFilter intentFilter;
    private MediaPlayer mpRec,mpCall;//mediaPlayer对象
    private AssetFileDescriptor file;
    private CommonDialog commonDialogRec,commonDialogCall;
    private  boolean call=true;
    private String channel_name="";
    private String channel_list="";
    private TcpConnect tcpConnect;
    private FileTcpConnect fileTcpConnect;
    private boolean isTempCreate=false;
    private boolean isCall=false;//频道申请过程标识。
    private ChatInfoDecoder decoder;
    @Override
    public void onCreate() {
        //先执行
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //如果多次context.startService(service)会多次调用。
        keepAliveThread = new KeepAliveSendThread();
        keepAliveReceiveThread=new KeepAliveReceiveThread();
        myPTTApplication = MyPTTApplication.getInstance();
        myPTTApplication.setAudioWrapper(audioWrapper);
        intentFilter = new IntentFilter();
        temptalkReceiver = new TemptalkReceiver();
        intentFilter.addAction(Config.BOARDCAST_TEMPTALK);
        intentFilter.addAction(Config.BOARDCAST_STOPSERVER);
        intentFilter.addAction(Config.BOARDCAST_TEMPTALK_RECE);
        intentFilter.addAction(Config.BOARDCAST_STARTUDP);
        intentFilter.addAction(Config.BOARDCAST_LOGIN_QUE);
        intentFilter.addAction(Config.BOARDCAST_SWITCHCHANNEL_QUE);
        intentFilter.addAction(Config.BOARDCAST_SWITCHCHANNEL_SUCCESS);
        intentFilter.addAction(Config.BOARDCAST_SENDNOTIFY_QUE);
        intentFilter.addAction(Config.BOARDCAST_TEMPTALKSUCCESS);//创建临时会话成功
        intentFilter.addAction(Config.BOARDCAST_TEMPTALKFAILURE);//
        intentFilter.addAction(Config.BOARDCAST_CALL);
        intentFilter.addAction(Config.BOARDCAST_CALL_END);
        intentFilter.addAction(Config.BOARDCAST_TEMPTALKCLOSE);//创建临时会话失败
        intentFilter.addAction(Config.BOARDCAST_CREATECONN);
        intentFilter.addAction(Config. BOARDCAST_CONNECT_SUCCESS);
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");//监听网络
        intentFilter.addAction(Config.BOARDCAST_EXITTEMPCHANNEL);
        intentFilter.addAction(Config.BOARDCAST_RENAMETEMPCHANNEL);
        intentFilter.addAction(Config.BOARDCAST_UPLOADCHANNEL);
        if(myPTTApplication.getLocalBroadcastManager()!=null){
            myPTTApplication.getLocalBroadcastManager().registerReceiver(temptalkReceiver, intentFilter);
        }
        NettyClient.getInstance().setListener(this);
        OpusJni.getInstance().Opusopen(Config.AUDIORECORD_SAMOLERATE,8);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onMessageResponse(ByteBuf byteBuf) {
        byte[] bytes = byteBuf.array();
     //   Log.e(TAG,"接收到数据"+byteBuf.readableBytes());

         decoder.addData(bytes,byteBuf.readableBytes());
    }

    @Override
    public void onServiceStatusConnectChanged(int statusCode) {
        if (statusCode == NettyListener.STATUS_CONNECT_SUCCESS) {
            Log.e(TAG,"连接成功");
        } else {
            Log.e(TAG,"连接失败");
        }

    }


    //临时会话呼叫持续20s。20秒没有接听则停止呼叫
    public class KeepCallThread extends Thread{
        int i=0;
        public void run(){
            while(call){
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if(i<20){
                    i++;
                }else{
                    mhandler.sendEmptyMessage(Config.TEMPTALK_CLOSE_TIMEOUT);
                    call=false;
                    isTempCreate=false;
                }
            }
        }
    };

   //发送心跳包的线程
    public class KeepAliveSendThread extends Thread {
        public void run(){
            while(true){
                try {
                    DefFrame.heartBeat(tcpConnect);
                    lasttime= System.currentTimeMillis();
                    Config.RECEIVE_TIME=false;
                    Thread.sleep(Config.KEEPALIVE_FREAM_TIME);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    /**
     * 超过30秒没有收到心跳包则认为连接断开
     * 连接断开后后台服务关闭，程序停止运行
     * 返回到重新登录界面。
     * */
    public class KeepAliveReceiveThread extends Thread{
        public void run(){
            int count=0;
            while(true){
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if(Config.RECEIVE_TIME){
                    count=0;
                }else
                    count++;
                if(count>30){
                    //一分钟没收到来自服务器的心跳包则认为连接断开。
                    stopService();
                }
            }
        }
    }

    private Handler mhandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case Config.CREATECONN:
                    createConn();
                    break;
                case Config.TEMPTALK:
                    //发送创建命令
                    isTempCreate=true;
                    DefFrame.tempTalkOrder(channel_name,channel_list,tcpConnect);
                    break;
                case Config.TEMPTALK_SUCCESS:
                    //服务器返回创建成功，显示临时会话窗口
                     createTalkDialog();
                    call=true;
                    keepCallThread = new KeepCallThread();
                    keepCallThread.start();
                    playCallsound(R.raw.call);
                    break;
                case Config.TEMPTALK_FAILURE:
                    isTempCreate=false;
                    if(commonDialogCall!=null){   commonDialogCall.closeDialog();}
                    if(mpCall!=null){ mpCall.stop();}
                    call=false;
                    Toast.makeText(getApplicationContext(),"创建临时会话失败",Toast.LENGTH_SHORT).show();
                    break;
                case Config.TEMPTALK_CLOSE:
                    //有成员接听，发起者则响应关闭呼叫窗口，如果是来时发起者自己的接听则不响应。
                        isTempCreate=false;
                        if(commonDialogCall!=null){   commonDialogCall.closeDialog();}
                        if(mpCall!=null){ mpCall.stop();}
                        call=false;
                    break;
                 case Config.TEMPTALK_CLOSE_TIMEOUT:
                     if(commonDialogCall!=null){   commonDialogCall.closeDialog();}
                     if(mpCall!=null){ mpCall.stop();}
                     call=false;
                    break;
                case Config.TEMPTALKRECE:
                    //客户端接收到被加入临时会话的消息。
                    if(!isTempCreate){
                        //如果是创建者，则不响应。
                        createTalkReceDialog();
                        call=true;
                        keepCallThread = new KeepCallThread();
                        keepCallThread.start();
                        playRecsound(R.raw.rece);
                    }else{
                        DefFrame.cretatejointempchannel(true,tcpConnect);
                    }
                    break;
                case Config.STARTUDP:
                    audioWrapper.startListen();
                    //audioWrapper.startSend();
                  //  MyPTTApplication.getInstance().setAudioSender(audioWrapper.getSender());
                    break;
                case Config.STOPSERVER:
                    stopService();
                    break;
                case Config.LOGIN_QUE:
                    DefFrame.logout(tcpConnect);
                    break;
                case Config.SWITCHCHANNEL_QUE:
                    DefFrame.sendChannelSwitchOrder(tcpConnect);
                    break;
                case Config.SENDNOTIFY_QUE:
                    DefFrame.createNotifty(tcpConnect);
                    break;
                case Config.SWITCHCHANNEL_SUCCESS:
                    //切换编组成功,根据切换后的编组ID获得切换后的编组名称。
                    myPTTApplication.getSharedTools().setShareString(Config.CurrentChannel,Config.switchChannel);
                    Intent intent = new Intent(Config.BOARDCAST_SWITCHCHANNEL);
                    myPTTApplication.getLocalBroadcastManager().sendBroadcast(intent);
                    break;
                case Config.CONNECT_SUCCESS:
                    //建立连接成功，开启 发送心跳和心跳检测线程
                    keepAliveThread.start();
                    keepAliveReceiveThread.start();
                    break;
                case Config.UPLOADCHANNEL:
                    DefFrame.uploadChannelid(tcpConnect);

                    DefFrame.UploadInfo(fileTcpConnect);
                    // 在接收前，要先启动解析器
                    decoder = ChatInfoDecoder.getInstance();
                    decoder.startDecoding();
                    break;
                case Config.CALL_BEGIN:
                    isCall=true;
                    DefFrame.Call(isCall,tcpConnect);
                    break;
                case Config.CALL_END:
                    isCall=false;
                    DefFrame.CallEnd(isCall,tcpConnect);
                    break;
                case Config.RENAMETEMPCHANNEL:
                    break;
                case Config.EXITTEMPCHANNEL:
                    DefFrame.ExitTempChannel(tcpConnect);
                    break;
            }
        }
    };
    /**
     *创建连接
     */
    private void createConn() {

        tcpConnect = new TcpConnect();
        tcpConnect.execute();

        if (!NettyClient.getInstance().getConnectStatus()) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    NettyClient.getInstance().connect();//连接服务器
                }
            }).start();
        }





        MyPTTApplication.getInstance().setTcpConnect(tcpConnect);

      //  MyPTTApplication.getInstance().setFileTcpConnect(fileTcpConnect);
    }

    private void stopService() {
        if(tcpConnect!=null){
            tcpConnect.close();
        }

       NettyClient.getInstance().setReconnectNum(0);
        NettyClient.getInstance().disconnect();

        if(fileTcpConnect!=null){
            fileTcpConnect.close();
        }
        OpusJni.getInstance().Opusclose();
        //清空本地关于userh和channel的记录

        stopSelf();
        MyPTTApplication.getInstance().exitAll();
    }


    /**
     * 播放音频资源
     */
    private void playCallsound(int rec) {
        mpCall = new MediaPlayer();
        file = getResources().openRawResourceFd(rec);
        try {
            mpCall.setDataSource(file.getFileDescriptor(), file.getStartOffset(),
                    file.getLength());
            mpCall.prepare();
            file.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mpCall.setVolume(0.3f, 0.3f);
        mpCall.setLooping(true);
        mpCall.start();
    }

    /**
     * 播放音频资源
     */
    private void  playRecsound(int rec) {
        mpRec = new MediaPlayer();
        file = getResources().openRawResourceFd(rec);
        try {
            mpRec.setDataSource(file.getFileDescriptor(), file.getStartOffset(),
                    file.getLength());
            mpRec.prepare();
            file.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mpRec.setVolume(0.3f, 0.3f);
        mpRec.setLooping(true);
        mpRec.start();
    }

    class TemptalkReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            if(intent.getAction()==Config.BOARDCAST_CREATECONN){
                 //创建连接的通知
                mhandler.sendEmptyMessage(Config.CREATECONN);
            }else if(intent.getAction()==Config.BOARDCAST_TEMPTALK){
                //创建临时会话窗口的广播
                channel_list=intent.getStringExtra("tempGrouplist");
                channel_name =intent.getStringExtra("tempchannelname");
                mhandler.sendEmptyMessage(Config.TEMPTALK);
            }else if(intent.getAction()==Config.BOARDCAST_TEMPTALKSUCCESS){
                //创建临时会话成功
                mhandler.sendEmptyMessage(Config.TEMPTALK_SUCCESS);
            }else if(intent.getAction()==Config.BOARDCAST_TEMPTALK_RECE){
                mhandler.sendEmptyMessage(Config.TEMPTALKRECE);
            }else if(intent.getAction()==Config.BOARDCAST_TEMPTALKFAILURE){
                //创建临时会话失败
                mhandler.sendEmptyMessage(Config.TEMPTALK_FAILURE);
            }else if(intent.getAction()==Config.BOARDCAST_TEMPTALKCLOSE){
                //临时会话呼叫窗体的到响应，关闭窗体
                mhandler.sendEmptyMessage(Config.TEMPTALK_CLOSE);
            }else if(intent.getAction()==Config.BOARDCAST_STARTUDP){
                mhandler.sendEmptyMessage(Config.STARTUDP);
            }else if(intent.getAction()==Config.BOARDCAST_STOPSERVER){
                mhandler.sendEmptyMessage(Config.STOPSERVER);
            }else if(intent.getAction()=="android.net.conn.CONNECTIVITY_CHANGE"){
                mhandler.sendEmptyMessage(Config.STOPSERVER);
            }else if(intent.getAction()==Config.BOARDCAST_LOGIN_QUE){
                mhandler.sendEmptyMessage(Config.LOGIN_QUE);
            }else if(intent.getAction()==Config.BOARDCAST_SWITCHCHANNEL_QUE){
                mhandler.sendEmptyMessage(Config.SWITCHCHANNEL_QUE);
            }else if(intent.getAction()==Config.BOARDCAST_SENDNOTIFY_QUE){
                mhandler.sendEmptyMessage(Config.SENDNOTIFY_QUE);
            }else if(intent.getAction()==Config.BOARDCAST_SWITCHCHANNEL_SUCCESS){
                //接收到切换频道成功的消息
                mhandler.sendEmptyMessage(Config.SWITCHCHANNEL_SUCCESS);
            }else if(intent.getAction()==Config.BOARDCAST_CONNECT_SUCCESS){
                mhandler.sendEmptyMessage(Config.CONNECT_SUCCESS);
            }else if(intent.getAction()==Config.BOARDCAST_UPLOADCHANNEL){
                //上报当前的频道ID（每次重新登录时需要）
                mhandler.sendEmptyMessage(Config.UPLOADCHANNEL);
            }else if(intent.getAction()==Config.BOARDCAST_CALL){
                  //频道申请 发送通知
                mhandler.sendEmptyMessage(Config.CALL_BEGIN);
            }else if(intent.getAction()==Config.BOARDCAST_CALL_END){
                  //频道申请结束，发送结束通知
                mhandler.sendEmptyMessage(Config.CALL_END);
            }else if(intent.getAction()==Config.BOARDCAST_RENAMETEMPCHANNEL){
                //重命名临时会话
                mhandler.sendEmptyMessage(Config.RENAMETEMPCHANNEL);
            }else if(intent.getAction()==Config.BOARDCAST_EXITTEMPCHANNEL){
                //退出临时会话
                mhandler.sendEmptyMessage(Config.EXITTEMPCHANNEL);
            }
        }
    }
     /**
      *创建发送临时会话的窗体
      */
    private boolean createTalkDialog() {
        commonDialogCall =new CommonDialog(this,"请稍后","正在呼叫"+channel_name,null,"取消");
        commonDialogCall.setOnDiaLogListener(new CommonDialog.OnDialogListener() {
            @Override
            public void dialogPositiveListener(View customView, DialogInterface dialogInterface, int which) {
                mhandler.sendEmptyMessage(Config.TEMPTALKRECE_POSITIVE);
            }
            @Override
            public void dialogNegativeListener(View customView, DialogInterface dialogInterface, int which) {
                //点击取消按钮
                mpCall.stop();
                call=false;
            }
        });
        commonDialogCall.showDialog();
        return true;
    }

    /**
     *创建接收临时会话的窗体
     */
    private void createTalkReceDialog() {
        commonDialogRec =new CommonDialog(this,"请稍后",Config.temp_username,"接听","挂断");
        commonDialogRec.setOnDiaLogListener(new CommonDialog.OnDialogListener() {
            @Override
            public void dialogPositiveListener(View customView, DialogInterface dialogInterface, int which) {
                   //点击接听。则发送同意加入临时会话的命名给服务器
                Config.switchChannel =  Config.temp_username;
                Config.switchChannelID=Config.tempChannelID;
                Intent intent = new Intent(Config.BOARDCAST_SWITCHCHANNEL_QUE);
                MyPTTApplication.getInstance().getLocalBroadcastManager().sendBroadcast(intent);
                mpRec.stop();
                call=false;
                DefFrame.cretatejointempchannel(true,tcpConnect);

            }

            @Override
            public void dialogNegativeListener(View customView, DialogInterface dialogInterface, int which) {
                ////点击挂断。则发送不同意加入临时会话的命名给服务器
                mpRec.stop();
                call=false;
                DefFrame.cretatejointempchannel(false,tcpConnect);
            }
        });
        commonDialogRec.showDialog();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        MyPTTApplication.getInstance().getLocalBroadcastManager().unregisterReceiver(temptalkReceiver);
        stopService();
    }
}
