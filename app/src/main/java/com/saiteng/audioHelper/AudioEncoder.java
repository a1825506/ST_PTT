package com.saiteng.audioHelper;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import android.os.Environment;
import android.util.Log;
import com.ione.opustool.OpusJni;
import com.saiteng.stptt.Config;
import com.saiteng.stptt.MyPTTApplication;
import com.saiteng.stptt.PathUtil;
import com.saiteng.stptt.Utils;

public class AudioEncoder implements Runnable {
    String LOG = "AudioEncoder";

    private static AudioEncoder encoder;
    private boolean isEncoding = false;

    private List<AudioData> dataList = null;

    public static AudioEncoder getInstance() {
        if (encoder == null) {
            encoder = new AudioEncoder();
        }
        return encoder;
    }

    private AudioEncoder() {

        dataList = Collections.synchronizedList(new LinkedList<AudioData>());
    }

    public void addData(short[] data, int size) {

        AudioData rawData = new AudioData();
        rawData.setSize(size);
        short[] tempData = new short[size];
        System.arraycopy(data, 0, tempData, 0, size);
        rawData.setRealData(tempData);
        dataList.add(rawData);
    }

    /*
     * start encoding
     */
    public void startEncoding() {
        System.out.println(LOG + "start encode thread");
        if (isEncoding) {
            Log.e(LOG, "encoder has been started  !!!");
            return;
        }
        new Thread(this).start();
    }

    /*
     * end encoding
     */
    public void stopEncoding() {
        this.isEncoding = false;
    }

    public void run() {
        // start sender before encoder
        AudioSender sender =  AudioSender.getInstance();
        sender.startSending();
        isEncoding = true;
        int encodeSize=0;
        byte[] encodedData;
        while (isEncoding) {
            if (dataList.size() == 0) {
                try {
                    Thread.sleep(20);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                continue;
            }
            if (isEncoding) {
                AudioData rawData = dataList.remove(0);
                encodedData = new byte[OpusJni.getInstance().OpusgetFrameSize()];
                encodeSize = OpusJni.getInstance().Opusencode(rawData.getRealData(),0,encodedData,rawData.getSize());
                if (encodeSize > 0) {
                    //给编码后的数据加上RTP头和自定义头。
                    byte[] data = addHead(encodedData);
                    Log.e("编码后的数据长度",""+data.length);
                    sender.addData(data, data.length);
                }
            }
        }
        System.out.println(LOG + "end encoding");
        sender.stopSending();
    }
   /**
    * 添加RTP12字节头和自定义头12字节
    *
    */
    private byte[] addHead(byte[] data) {

        byte[] newdata = new byte[data.length+12];

        //自定义头包括4字节用户ID，4字节群组ID,4字节预留
        byte[] head = new byte[12];

        int id =  MyPTTApplication.getInstance().getSharedTools().getShareInt(Config.CurrentUserID,0);

        byte[] byte_id = Utils.int2Bytes(id,4);

        int channelid = MyPTTApplication.getInstance().getSharedTools().getShareInt(Config.CurrentChannelID,0);

        byte[] channel_id = Utils.int2Bytes(channelid,4);

        System.arraycopy(byte_id,0,head,0,4);

        System.arraycopy(channel_id,0,head,4,4);

        System.arraycopy(head,0,newdata,0,12);

        System.arraycopy(data,0,newdata,12,data.length);

        return newdata;

    }

}