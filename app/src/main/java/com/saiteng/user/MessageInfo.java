package com.saiteng.user;

import android.util.Log;

import com.saiteng.conn.DefFrame;
import com.saiteng.fragment.ChatFragment;
import com.saiteng.stptt.Config;
import com.saiteng.stptt.MyPTTApplication;
import com.saiteng.stptt.PathUtil;
import com.saiteng.stptt.Utils;

import java.util.Random;

/**
 * Created by Moore on 2017/8/11.
 * 封装聊天的多媒体消息
 * 包括消息类型（文本，位置，图片，视频，语音等）
 * 发送者（用户ID）
 * 接受者（频道ID）
 * 消息内容
 */

public class MessageInfo {
    public int message_Type;//信息类型
    public String message_sender;//信息发送者
    public String message_receiver;//信息发送的频道
    public String messageContext;//信息内容（为文本数据是需要）
    public String path="/PPT";//信息在本地保存的路径。
    public int message_Direct;//信息是接收的还是发送的
    public int sendstatus;//信息发送状态，发送中，发送成功，发送失败
    public long message_length;//信息内容长
    public int messageId;//发送一条信息的ID，同一文件的ID相同，在分包发送时需要该ID来标记一条完整的信息。
    public byte[] messageData;


    public void setMessageData(byte[] messageData){
        this.messageData = messageData;
    }
    public byte[] getMessageData(){
        return messageData;
    }

    public void setMessage_Type(int type){
        this.message_Type = type;
    }
    public int getMessage_Type(){
        return message_Type;
    }

     public void setMessage_senderID(String senderID){
         this.message_sender = senderID;
     }
    public String getMessage_senderID(){
        return message_sender;
    }

    public void setMessage_receiverID(String receiverID){
        this.message_receiver = receiverID;
    }
    public String getMessage_receiverID(){
        return message_receiver;
    }

    public void setMessageContext(String context_byte){
        this.messageContext = context_byte;
    }
    public String getMessageContext(){
        return messageContext;
    }

    public void setPath(String path){
        this.path = path;
    }

    public String getPath(){
        return path;
    }

    public void setMessage_Direct(int message_Direct){
        this.message_Direct = message_Direct;
    }

    public int getMessage_Direct(){
        return message_Direct;
    }

    public void setSendstatus(int sendstatus){
        this.sendstatus = sendstatus;
    }

    public int getSendstatus(){
        return sendstatus;
    }

    public void setMessage_length(long message_length){this.message_length = message_length;
    }
    public long getMessage_length(){return message_length;};



    public void sendMessage(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                messageId = new Random().nextInt();
                if(message_Type== ChatFragment.MESSAGE_TXT_TYPE){
                    //发送实时文本信息。
                    if(messageContext.getBytes().length<=1376){
                        //用户数据长度小于1376个字节，超过这个长度要进行分包发送
                        DefFrame.sendChatInfo(message_Type,messageId,message_length,messageContext.getBytes());
                    }
                }else if(message_Type== ChatFragment.MESSAGE_GPS_TYPE){
                    DefFrame.sendChatInfo(message_Type,messageId,message_length,messageContext.getBytes());
                }else if(message_Type==ChatFragment.MESSAGE_AUDIO_TYPE){
                    PathUtil.readFile(message_Type,messageId,message_length,path);
                }else if(message_Type==ChatFragment.MESSAGE_PIC_TYPE){
                    PathUtil.readFile(message_Type,messageId,message_length,path);
                }else if(message_Type==ChatFragment.MESSAGE_VIDEO_TYPE){
                    PathUtil.readFile(message_Type,messageId,message_length,path);
                }
            }
        }).start();

    }
}
