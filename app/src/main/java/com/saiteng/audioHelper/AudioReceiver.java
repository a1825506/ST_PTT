package com.saiteng.audioHelper;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

import android.util.Log;

import com.saiteng.stptt.Config;
import com.saiteng.stptt.MyPTTApplication;

public class AudioReceiver implements Runnable {
    String LOG = "AudioReceiver";
    int port = Config.LOCAL_RECEVED_STREAM_PORT;// 接收的端口
    DatagramSocket socket;
    DatagramPacket packet;
    boolean isRunning = false;

    private byte[] packetBuf = new byte[80000];
    byte[] h264Data = new byte[80000];

    /*
     * 开始接收数据
     */
    public void startRecieving() {
        if (socket == null) {
            try {
                socket = new DatagramSocket(port);
                MyPTTApplication.getInstance().setUdpsocket(socket);

            } catch (SocketException e) {
                e.getMessage();
            }
        }
        new Thread(this).start();
    }

    /*
     * 停止接收数据
     */
    public void stopRecieving() {
        isRunning = false;
    }

    /*
     * 释放资源
     */
    private void release() {
        if (packet != null) {
            packet = null;
        }
        if (socket != null) {
            socket.close();
            socket = null;
        }
    }

    public void run() {
        // 在接收前，要先启动解码器
        AudioDecoder decoder = AudioDecoder.getInstance();
        decoder.startDecoding();
//        VideoDecoder decoder = VideoDecoder.getInstance();
//        decoder.startDecoding();
        isRunning = true;
        int h264Length = 0;
        try {
            while (isRunning) {
                packet = new DatagramPacket(packetBuf, packetBuf.length);
               socket.receive(packet);
                byte[] rtpData = packet.getData();
                Log.e("====收到一个包======",""+rtpData.length);
                if (rtpData != null ) {
                    if (rtpData[0] == -128 && rtpData[1] == 96) {
                        Log.e(LOG, "run:xxx");
                        int l1 = (rtpData[12] << 24) & 0xff000000;
                        int l2 = (rtpData[13] << 16) & 0x00ff0000;
                        int l3 = (rtpData[14] << 8) & 0x0000ff00;
                        int l4 = rtpData[15] & 0x000000FF;
                        h264Length = l1 + l2 + l3 + l4;
                        Log.e(LOG, "run: h264Length=" + h264Length);
                        System.arraycopy(rtpData, 16, h264Data, 0, h264Length);
                        Log.e(LOG, "run:h264Data[0]=" + h264Data[0] + "," + h264Data[1] + "," + h264Data[2] + "," + h264Data[3]
                                + "," + h264Data[4] + "," + h264Data[5] + "," + h264Data[6] + "," + h264Data[7]
                                + "," + h264Data[8] + "," + h264Data[9] + "," + h264Data[10]
                                + "," + h264Data[11] + "," + h264Data[12] + "," + h264Data[13]
                                + "," + h264Data[14] + "," + h264Data[15] + "," + h264Data[16]
                                + "," + h264Data[17] + "," + h264Data[18] + "," + h264Data[19]
                                + "," + h264Data[20] + "," + h264Data[21] + "," + h264Data[22]);//打印sps、pps
                       // offerDecoder(h264Data, h264Data.length);
                        Log.e(LOG, "run: 视频数据"+h264Data.length);
                    }else{
                        Log.e(LOG, "run: 音频数据"+rtpData.length);
                    }
                }

                // 每接收一个UDP包，就交给解码器，等待解码
//                if(packet.getLength()!=16){
//                    decoder.addData(rtpData, rtpData.length);
//                }
            }

        } catch (IOException e) {
            Log.e(LOG, "RECIEVE ERROR!");
        }
        // 接收完成，停止解码器，释放资源
        decoder.stopDecoding();
        release();
        Log.e(LOG, "stop recieving");
    }
}
