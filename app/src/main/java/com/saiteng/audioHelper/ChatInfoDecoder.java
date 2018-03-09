package com.saiteng.audioHelper;

import android.content.Intent;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.saiteng.fragment.ChatFragment;
import com.saiteng.stptt.Config;
import com.saiteng.stptt.MyPTTApplication;
import com.saiteng.stptt.PathUtil;
import com.saiteng.stptt.Utils;
import com.saiteng.user.ChannelInfo;
import com.saiteng.user.MessageInfo;
import com.saiteng.user.UserInfo;

import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by Moore on 2017/9/14.
 * 聊天信息解析类
 */

public class ChatInfoDecoder implements Runnable{
    String TAG = "AudioDecoder";
    private static ChatInfoDecoder decoder;
    private boolean isDecoding = false;
    private List<AudioData2> dataList = null;
    private Map<Integer,List<MessageInfo>> map_info = new HashMap<Integer,List<MessageInfo>>();
    private MessageInfo messageInfo;
    private  int type;
    private String sendchannel =null;
    private String sendnamer=null;
    private  byte[] realdata;
    private String filePath=null;
    private String pathname= null;
    private String context=null;
    private int messageType=0;
    int defchannelid = MyPTTApplication.getInstance().getSharedTools().getShareInt(Config.CurrentChannelID,0);
    String defchannel = MyPTTApplication.getInstance().getSharedTools().getShareString(Config.CurrentChannel,"default");
    String defuser = MyPTTApplication.getInstance().getSharedTools().getShareString(Config.CurrentUser,"user1");
    int defuserid = MyPTTApplication.getInstance().getSharedTools().getShareInt(Config.CurrentUserID,0);
    String usergroup = null;
    public static ChatInfoDecoder getInstance() {
        if (decoder == null) {
            decoder = new ChatInfoDecoder();
        }
        return decoder;
    }
    private ChatInfoDecoder() {
        this.dataList = Collections.synchronizedList(new LinkedList<AudioData2>());

    }
    /**
     *接收到聊天数据就添加到队列进行处理,
     * 这里要判断一段数据的完整性和数据的类型。
     */
    int all_datalength =0;
    int totalsize = 0;
    byte[] byteBuffer = null;
    byte[] headBuffer = null;
    boolean isdealing=false;
    public void addData(byte[] data, int size) {

        if(!isdealing){
                if(data[0]==(byte)0xEF&&data[23]==(byte)0xEF){
                    //完整数据包开头。
                    isdealing=true;
                    byte[] alll_datalength = new byte[4];
                    System.arraycopy(data, 8, alll_datalength, 0,4);
                    int  all_len = Utils.bytes2Int(alll_datalength,0,4);
                    int head_len = all_len/Config.ORDERLENGTH+1;
                    all_datalength = head_len*26+all_len;
                    byteBuffer =new byte[all_datalength];
                    System.arraycopy(data, 0, byteBuffer, 0,size);
                    Log.e(TAG,"解析接收到的数据总长度："+all_datalength);
                    messageInfo = new MessageInfo();

                    byte[] datatype = new byte[1];
                    System.arraycopy(data, 12, datatype, 0,1);
                    type =  Utils.bytes2Int(datatype,0,1);
                    //所属频道
                    byte[] byte_Orderid = new byte[4];
                    System.arraycopy(data, 4, byte_Orderid, 0,4);
                    int orderid = Utils.bytes2Int(byte_Orderid,0,4);
                    pathname = orderid+"";
                    //所属频道
                    byte[] byte_channelid = new byte[4];
                    System.arraycopy(data, 17, byte_channelid, 0,4);
                    int channelid = Utils.bytes2Int(byte_channelid,0,4);

                    //发送者ID
                    byte[] byte_userid = new byte[4];
                    System.arraycopy(data, 13, byte_userid, 0,4);
                    int userid = Utils.bytes2Int(byte_userid,0,4);

                    //发送频道
                    String json1=MyPTTApplication.getInstance().getSharedTools().getShareObject("channel",null);
                    if (json1 != null) {
                        Gson gson = new Gson();
                        Type type2 = new TypeToken<List<ChannelInfo>>() {}.getType();
                        List<ChannelInfo> list_channel = new ArrayList<ChannelInfo>();
                        list_channel = gson.fromJson(json1, type2);
                        for (int i = 0; i < list_channel.size(); i++) {
                            ChannelInfo channelinfo = list_channel.get(i);
                            if(channelinfo.getChannelid()==channelid){
                                sendchannel  = channelinfo.getChannelname();
                            }
                        }
                    }

                    //发送者
                    String json=MyPTTApplication.getInstance().getSharedTools().getShareObject("user",null);
                    if (json != null)
                    {
                        Gson gson = new Gson();
                        Type type1 = new TypeToken<List<UserInfo>>(){}.getType();
                        List<UserInfo> list_user = new ArrayList<UserInfo>();
                        list_user = gson.fromJson(json, type1);
                        for(int i=0;i< list_user.size();i++) {
                            UserInfo userinfo = list_user.get(i);
                            if(defuserid==userinfo.getUserid()){
                                //当前用户所属的频道有哪些
                                 usergroup = userinfo.getGroup().toString();
                            }
                            if(userinfo.getUserid()==userid){
                                sendnamer = userinfo.getUsername();
                            }
                        }
                    }

                    if(type==0){
                    //音频文件，保存为本地文件
                       filePath = PathUtil.getVoiceChatPathName()+pathname+".amr";
                       context=null;
                       messageType = ChatFragment.MESSAGE_AUDIO_TYPE;
                       if(Config.DEBUG){
                         Log.e(TAG,"音频信息");
                       }
                    }else if(type==1){
                        //视频文件保存为本地文件，在本地数据库增加记录，再更新聊天界面
                        filePath = PathUtil.getVideoChatPathName()+pathname+".mp4";
                        context=null;
                        messageType = ChatFragment.MESSAGE_VIDEO_TYPE;
                        if(Config.DEBUG){
                            Log.e(TAG,"视频信息");
                        }
                    }else if(type==2){
                    //图片保存为本地文件，在本地数据库增加记录，再更新聊天界面
                        filePath = PathUtil.getImageChatPathName()+pathname+".jpg";
                        context=null;
                        messageType = ChatFragment.MESSAGE_PIC_TYPE;
                        if(Config.DEBUG){
                            Log.e(TAG,"图片信息");
                        }
                    }else if(type==3){
                        filePath = null;

                        messageType = ChatFragment.MESSAGE_TXT_TYPE;
                        if(Config.DEBUG){
                            Log.e(TAG,"文本信息");
                        }
                    }else if(type==4){
                        filePath = null;

                        messageType = ChatFragment.MESSAGE_GPS_TYPE;
                        if(Config.DEBUG){
                            Log.e(TAG,"PPS信息");
                        }
                    }
                    messageInfo.setMessage_Type(messageType);
                    messageInfo.setMessage_senderID(sendnamer);
                    messageInfo.setMessage_receiverID(sendchannel);
                    messageInfo.setMessageData(realdata);
                    messageInfo.setMessage_length(0);
                    messageInfo.setMessageContext(context);
                    messageInfo.setMessage_Direct(ChatFragment.MESSAGE_RECE);
                    messageInfo.setPath(filePath);
                }
        }else{
            System.arraycopy(data, 0, byteBuffer, totalsize,size);
        }

        totalsize=totalsize+size;
        Log.e(TAG,"接收到数据："+totalsize);
        if(totalsize>=all_datalength){
            Log.e(TAG,"接收到数据完成："+totalsize);
            totalsize=0;
            isdealing=false;
            if(byteBuffer!=null){
                int arr_aize = byteBuffer.length;
                Log.e(TAG,"数组长度："+arr_aize);
            }


            dealTempData(byteBuffer);
        }

    }

    public  void dealTempData(byte[] data){
        //data为完整的所有数据，拆分为一帧数据进行解析
        AudioData2 encodedData = null;
        int count = data.length/(Config.ORDERLENGTH+26);
        byte[] tempdata =null;
        byte[] tempdata2=null;
        for(int i=0;i<=count;i++){
            if(i==count){
                tempdata = new byte[data.length-((Config.ORDERLENGTH+26)*i)];
                System.arraycopy(data, (Config.ORDERLENGTH+26)*i, tempdata, 0,data.length-((Config.ORDERLENGTH+26)*i));
            }else{
                tempdata = new byte[Config.ORDERLENGTH+26];
                System.arraycopy(data, (Config.ORDERLENGTH+26)*i, tempdata, 0,Config.ORDERLENGTH+26);
            }
            tempdata2= new byte[tempdata.length-26];
            System.arraycopy(tempdata, 24, tempdata2,0,tempdata.length-26);
            encodedData = new AudioData2();
            encodedData.setSize(tempdata.length-26);
            encodedData.setRealData(tempdata2);
            dataList.add(encodedData);
        }
    }

    /**
    * 开始解析数据
    */
    public void startDecoding() {
        if(Config.DEBUG){
            System.out.println(TAG + "开始解码");
        }
        if (isDecoding) {
            return;
        }
        new Thread(this).start();
    }

    @Override
    public void run() {
        this.isDecoding = true;
        int totalsize=0;
        while (isDecoding) {
            while (dataList.size() > 0) {

                AudioData2 encodedData = dataList.remove(0);

                totalsize = totalsize+encodedData.getSize();
                if(usergroup.contains(sendchannel)&&!sendnamer.equals(defuser)){
                    if(encodedData.getSize()<Config.ORDERLENGTH){
                        //最后一帧数据
                        messageInfo.setMessage_length(totalsize);
                        if(type==3||type==4){
                            context=new String(encodedData.getRealData());
                            messageInfo.setMessageContext(context);
                        }else{
                            PathUtil.writeFile(messageInfo.getPath(),encodedData.getRealData());
                        }

                        MyPTTApplication.getInstance().getDBManager().insertData(messageInfo);
                        Intent intent = new Intent(Config.BOARDCAST_UPDATECHAT);
                        MyPTTApplication.getInstance().getLocalBroadcastManager().sendBroadcast(intent);
                        totalsize=0;
                    }else
                        PathUtil.writeFile(messageInfo.getPath(),encodedData.getRealData());
                }
//                if(usergroup.contains(sendchannel)){
//                    if(encodedData.getSize()<Config.ORDERLENGTH){
//                        //最后一帧数据
//                        messageInfo.setMessage_length(totalsize);
//                        if(type==3||type==4){
//                            context=new String(encodedData.getRealData());
//                            messageInfo.setMessageContext(context);
//                        }else{
//                            PathUtil.writeFile(messageInfo.getPath(),encodedData.getRealData());
//                        }
//                        Log.e(TAG,"解析最后一帧数据："+(encodedData.getSize()));
//                        MyPTTApplication.getInstance().getDBManager().insertData(messageInfo);
//                        totalsize=0;
//                    }else
//                        PathUtil.writeFile(messageInfo.getPath(),encodedData.getRealData());
//                }
            }
        }
    }
}
