package com.saiteng.audioHelper;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import android.util.Log;

import com.ione.opustool.OpusJni;
import com.saiteng.stptt.Config;
import com.saiteng.stptt.MyPTTApplication;
import com.saiteng.stptt.PathUtil;
import com.saiteng.stptt.Utils;


public class AudioDecoder implements Runnable {

    String LOG = "AudioDecoder";
    private static AudioDecoder decoder;

    private static final int MAX_BUFFER_SIZE = 2048;

    private short[] decodedData;// data of decoded
    private boolean isDecoding = false;
    public static List<AudioData2> dataList = null;

    public static AudioDecoder getInstance() {
        if (decoder == null) {
            decoder = new AudioDecoder();
        }
        return decoder;
    }

    private AudioDecoder() {
        this.dataList = Collections
                .synchronizedList(new LinkedList<AudioData2>());
    }

    /*
     * add Data to be decoded
     * @ data:the data recieved from server
     * @ size:data size
     */
    public void addData(byte[] data, int size) {
        //解码之前先讲数据用户id和当前频道筛选出来

        AudioData2 adata = new AudioData2();
        adata.setSize(size);
        byte[] userid = new byte[4];
        System.arraycopy(data, 0, userid, 0, 4);

        byte[] channelid = new byte[4];
        System.arraycopy(data, 4, channelid, 0, 4);
        int channel_id = Utils.bytes2Int(channelid,0,4);

        byte[] datatype = new byte[4];
        System.arraycopy(data, 8, channelid, 0, 4);
        int data_type = Utils.bytes2Int(datatype,0,4);


        Log.e(LOG,"channel_id="+Integer.toHexString(channel_id));
        Log.e(LOG,"channelid="+Integer.toHexString(MyPTTApplication.getInstance().getSharedTools().getShareInt(Config.CurrentChannelID,0)));
        if(channel_id== MyPTTApplication.getInstance().getSharedTools().getShareInt(Config.CurrentChannelID,0)){
            Log.e(LOG,"channelid="+size);
            if(size>12){
                byte[] tempData = new byte[size-12];
                System.arraycopy(data, 12, tempData, 0, size-12);
                adata.setRealData(tempData);
                adata.setSize(size-12);
                if(data_type==0){
                    dataList.add(adata);
                    Log.e(LOG,"接收到音频数据");
                }else{
                    Log.e(LOG,"接收到视频数据");
                }

            }

        }
    }

    /*
     * start decode AMR data
     */
    public void startDecoding() {
        System.out.println(LOG + "开始解码");
        if (isDecoding) {
            return;
        }
        new Thread(this).start();
    }

    public void run() {
        // start player first
        AudioPlayer player = AudioPlayer.getInstance();
        player.startPlaying();
        //
        this.isDecoding = true;

        Log.d(LOG, LOG + "initialized decoder");
        int decodeSize = 0;
        while (isDecoding) {
            while (dataList.size() > 0) {
                AudioData2 encodedData = dataList.remove(0);
                Log.e(LOG, " 解码前的长度 " + encodedData.getSize());
                decodedData = new short[160];
                byte[] data = encodedData.getRealData();
                decodeSize = OpusJni.getInstance().Opusdecode(data,decodedData,encodedData.getSize());
                Log.e(LOG, " 解码后的长度 " + decodeSize);
                if (decodeSize > 0) {
                    // add decoded audio to player
                    player.addData(decodedData, decodeSize);
                }
            }
        }
        System.out.println(LOG + "stop decoder");
        // stop playback audio
        player.stopPlaying();
    }

    public void stopDecoding() {
        this.isDecoding = false;
    }
}