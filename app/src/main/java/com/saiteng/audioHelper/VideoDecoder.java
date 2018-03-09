package com.saiteng.audioHelper;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.util.Log;
import android.view.Surface;

import com.saiteng.stptt.Config;
import com.saiteng.stptt.MyPTTApplication;
import com.saiteng.stptt.Utils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Moore on 2018-03-08.
 */

public class VideoDecoder implements Runnable{

    public static List<AudioData2> dataList = null;

    public static VideoDecoder videoDecoder;

    public String LOG = "VideoDecoder";

    private boolean isDecoding = false;

    private int Video_Width = 640;
    private int Video_Height = 480;
    private int FrameRate = 15;
    long timeoutUs = 10000;
    private Boolean isUsePpsAndSps = false;

    private MediaCodec mCodec;

    private Surface mSurface;

    private  MediaCodec.BufferInfo info;

    public static VideoDecoder getInstance() {
        if (videoDecoder == null) {
           Log.e("VideoDecoder","VideoDecoder 未初始化");
        }
        return videoDecoder;
    }

    public VideoDecoder(Surface surface){

        this.mSurface = surface;

        this.dataList = Collections
                .synchronizedList(new LinkedList<AudioData2>());

        info = new MediaCodec.BufferInfo();

        try
        {
            //通过多媒体格式名创建一个可用的解码器
            mCodec = MediaCodec.createDecoderByType("video/avc");
        } catch (IOException e) {
            e.printStackTrace();
        }
        //初始化编码器
        final MediaFormat mediaformat = MediaFormat.createVideoFormat("video/avc", Video_Width, Video_Height);
        //获取h264中的pps及sps数据
        if (isUsePpsAndSps) {
            byte[] header_sps = {0, 0, 0, 1, 103, 66, 0, 42, (byte) 149, (byte) 168, 30, 0, (byte) 137, (byte) 249, 102, (byte) 224, 32, 32, 32, 64};
            byte[] header_pps = {0, 0, 0, 1, 104, (byte) 206, 60, (byte) 128, 0, 0, 0, 1, 6, (byte) 229, 1, (byte) 151, (byte) 128};
            mediaformat.setByteBuffer("csd-0", ByteBuffer.wrap(header_sps));
            mediaformat.setByteBuffer("csd-1", ByteBuffer.wrap(header_pps));
        }
        //设置帧率
        mediaformat.setInteger(MediaFormat.KEY_FRAME_RATE, FrameRate);

        mCodec.configure(mediaformat, mSurface, null, 0);

    }

    public void startDecoding() {

        if (isDecoding) {
            return;
        }

        new Thread(this).start();
    }

    public void stopDecoding() {
        this.isDecoding = false;
    }

    public void addData(byte[] data, int size) {
        AudioData2 adata = new AudioData2();
        adata.setSize(size);
        byte[] userid = new byte[4];
        System.arraycopy(data, 0, userid, 0, 4);
        byte[] channelid = new byte[4];
        System.arraycopy(data, 4, channelid, 0, 4);
        int channel_id = Utils.bytes2Int(channelid,0,4);
        Log.e(LOG,"channel_id="+Integer.toHexString(channel_id));
        Log.e(LOG,"channelid="+Integer.toHexString(MyPTTApplication.getInstance().getSharedTools().getShareInt(Config.CurrentChannelID,0)));
        if(channel_id== MyPTTApplication.getInstance().getSharedTools().getShareInt(Config.CurrentChannelID,0)){
            Log.e(LOG,"channelid="+size);
            if(size>12){
                byte[] tempData = new byte[size-12];
                System.arraycopy(data, 12, tempData, 0, size-12);
                adata.setRealData(tempData);
                adata.setSize(size-12);
                dataList.add(adata);
            }

        }
    }


    @Override
    public void run() {

        this.isDecoding = true;

        while (!isDecoding) {

            while (dataList.size() > 0) {
                AudioData2 encodedData = dataList.remove(0);
                int inIndex = mCodec.dequeueInputBuffer(timeoutUs);
                if (inIndex >= 0) {
                    ByteBuffer byteBuffer = mCodec.getInputBuffers()[inIndex];
                    byteBuffer.clear();
                    byteBuffer.put(encodedData.getRealData(), 0, encodedData.getSize());
                    byteBuffer.limit(encodedData.getSize());
                    mCodec.queueInputBuffer(inIndex, 0, encodedData.getSize(), 0, MediaCodec.BUFFER_FLAG_SYNC_FRAME);
                }
                int outIndex = mCodec.dequeueOutputBuffer(info, timeoutUs);
                Log.d(LOG, "video decoding .....");
                while (outIndex >= 0) {
//                        ByteBuffer buffer = decoder.getOutputBuffer(outIndex);
                    mCodec.releaseOutputBuffer(outIndex, true);
                    outIndex = mCodec.dequeueOutputBuffer(info, timeoutUs);//再次获取数据，如果没有数据输出则outIndex=-1 循环结束
                }
            }
        }
    }


}
