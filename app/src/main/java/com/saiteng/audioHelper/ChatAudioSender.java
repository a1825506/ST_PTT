package com.saiteng.audioHelper;

import android.util.Log;

import com.saiteng.conn.NettyClient;
import com.saiteng.stptt.Config;
import com.saiteng.stptt.MyPTTApplication;
import com.saiteng.stptt.Utils;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;


/**
 * Created by Moore on 2017/9/14.
 */

public class ChatAudioSender implements Runnable{
    String TAG = "ChatAudioSender ";
    private boolean isSendering = false;
    private List<AudioData2> dataList;
    private  int messagetype, messageId;
    private long message_length;
    byte[] head ={(byte)0xEF,(byte)0xEF,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,(byte)0xEF,(byte)0xEF};
    private int totalaSize=0;

    public ChatAudioSender(){
        dataList = Collections.synchronizedList(new LinkedList<AudioData2>());
    }

    public void setParam(int messagetype1,int messageId1,long message_length1){
        this.messagetype = messagetype1;
        this.messageId = messageId1;
        this.message_length = message_length1;

        byte[] byte_messageid = Utils.int2Bytes(messageId,4);
        byte[] byte_messagelength = new byte[8];
        byte_messagelength =Utils.long2Byte(byte_messagelength,message_length);

        byte[] byte_messagetype = Utils.int2Bytes(messagetype,1);
        byte[] userid =Utils.int2Bytes(MyPTTApplication.getInstance().getSharedTools().getShareInt(Config.CurrentUserID,0),4);
        byte[] channelid = Utils.int2Bytes(MyPTTApplication.getInstance().getSharedTools().getShareInt(Config.CurrentChannelID,0),4);
        byte[] byte_datalength = Utils.int2Bytes(Config.ORDERLENGTH,2) ;
        System.arraycopy(byte_datalength, 0, head, 2, 2);
        System.arraycopy(byte_messageid, 0, head, 4, 4);
        System.arraycopy(byte_messagelength, 4, head, 8, 4);
        System.arraycopy(byte_messagetype, 0, head, 12, 1);
        System.arraycopy(userid, 0, head, 13, 4);
        System.arraycopy(channelid, 0, head, 17, 4);
    }

    public void addData(byte[] data, int size) {
        byte[] tempData=null;
        int lastsize = size%Config.ORDERLENGTH;//模运算，判断是不是最后一次读取数据
        AudioData2 encodedData = new AudioData2();
        if(lastsize!=0){
                    // //最后一次读取，读取到文件结尾
            tempData = new byte[head.length+lastsize+2];
            byte[] byte_datalength = Utils.int2Bytes(lastsize,2) ;
            System.arraycopy(byte_datalength, 0, head, 2, 2);
            System.arraycopy(head, 0, tempData, 0, head.length);
            System.arraycopy(data, 0, tempData, head.length, lastsize);

        }else{
            tempData = new byte[head.length+Config.ORDERLENGTH+2];
            System.arraycopy(head, 0, tempData, 0, head.length);
            System.arraycopy(data, 0, tempData, head.length, Config.ORDERLENGTH);
        }
        int sum=0;
        //校验和，需要时必须加上
        for(int i=24;i<tempData.length-2;i++){
            sum+=tempData[i];
        }
        tempData[tempData.length-2]=(byte)sum;
        tempData[tempData.length-1]=0x36;
        encodedData.setRealData(tempData);
        encodedData.setSize(tempData.length);
        dataList.add(encodedData);
    }

    /*
    * start sending data
    */
    public void startSending() {
        new Thread(this).start();
    }

    /*
     * stop sending data
     */
    public void stopSending() {
        this.isSendering = false;
    }


    @Override
    public void run() {
        this.isSendering = true;
        if(Config.DEBUG){
            System.out.println(TAG + "start....");
        }
      //  byte[] alldata = new byte[1024*Config.FRAMECOUNT-1] ;
        int count=0;
        int totalsize=0;
        while (isSendering) {
            if (dataList.size() > 0) {
                final AudioData2 encodedData = dataList.remove(0);
                byte[] all_data = null ;
                count++;
                totalsize = totalsize+encodedData.getSize();
              //  Log.e(TAG,"发送数据量："+totalsize);

                    NettyClient.getInstance().sendMsgToServer(encodedData.getRealData(), new ChannelFutureListener() {    //3
                        @Override
                        public void operationComplete(ChannelFuture future) {
                            if (future.isSuccess()) {
                                if(encodedData.getSize()<99826){
                                    Log.e("DefFrame","发送成功");
                                }else{
                                    Log.e("DefFrame","发送中。。。"+encodedData.getSize());
                                }
                            } else {
                                Log.e("DefFrame","发送失败");
                            }
                        }
                    });




//                if(encodedData.getSize()<1024){
//                    isSendering=false;
//                    if( count<Config.FRAMECOUNT){
//                        all_data = new byte[(count-1)*1024+encodedData.getSize()];
//                        System.arraycopy(alldata, 0, all_data,0,(count-1)*1024);
//                        System.arraycopy(encodedData.getRealData(), 0, all_data,(count-1)*1024,encodedData.getSize());
//                    }else{
//                        all_data = new byte[Config.FRAMECOUNT*1024];
//                        System.arraycopy(alldata, 0, all_data,0,(count-1)*1024);
//                        System.arraycopy(encodedData.getRealData(), 0, all_data,(count-1)*1024,encodedData.getSize());
//                    }
//                }else{
//                    if(count==Config.FRAMECOUNT){
//                        all_data = new byte[Config.FRAMECOUNT*1024];
//                        System.arraycopy(alldata, 0, all_data,0,(count-1)*1024);
//                        System.arraycopy(encodedData.getRealData(), 0, all_data,(count-1)*1024,encodedData.getSize());
//                    }else
//                        System.arraycopy(encodedData.getRealData(), 0, alldata,(count-1)*1024, encodedData.getSize());
//                }
//                if(count==Config.FRAMECOUNT||encodedData.getSize()<1024){
//                    count=0;
//                    try {
//                        NettyClient.getInstance().sendMsgToServer(all_data, new ChannelFutureListener() {    //3
//                            @Override
//                            public void operationComplete(ChannelFuture future) {
//                                if (future.isSuccess()) {
//                                    if(encodedData.getSize()<1024){
//                                        Log.e("DefFrame","发送成功");
//                                    }
//                                } else {
//                                    Log.e("DefFrame","发送失败");
//                                }
//                            }
//                        });
//                        Thread.sleep(Config.SlEEPTIME);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                }
            }
        }

        if(Config.DEBUG) {
            System.out.println(TAG + "stop!!!!");
        }
    }
}
