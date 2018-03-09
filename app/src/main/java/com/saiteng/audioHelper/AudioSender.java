package com.saiteng.audioHelper;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import android.os.Environment;
import android.util.Log;

import com.ione.opustool.OpusJni;
import com.saiteng.stptt.Config;
import com.saiteng.stptt.MyPTTApplication;
import com.saiteng.stptt.Utils;


public class AudioSender implements Runnable {
    String LOG = "AudioSender ";
    private boolean isSendering = false;
    private List<AudioData2> dataList;
    private static AudioSender audioSender;
    DatagramSocket socket;
    int port;
    InetAddress ip;
    DatagramPacket dataPacket;

    public static AudioSender getInstance() {
        if (audioSender == null) {
            audioSender = new AudioSender();
        }
        return audioSender;
    }


    public AudioSender() {
        dataList = Collections.synchronizedList(new LinkedList<AudioData2>());
        try {
                ip = InetAddress.getByName(Config.SERVER_IP);
                Log.e(LOG, "服务端地址是 " + ip.toString());
                port = Config.SERVER_DESTPORT;
                socket =  MyPTTApplication.getInstance().getUdpsocket();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        new Thread(this).start();
    }

    public void addData(byte[] data, int size) {
        AudioData2 encodedData = new AudioData2();
        Log.e("编码后发送的数据长度",""+size);
        encodedData.setSize(size);
        byte[] tempData = new byte[size];
        System.arraycopy(data, 0, tempData, 0, size);
        encodedData.setRealData(tempData);
        dataList.add(encodedData);
    }

    /*
     * send data to server
     */
    public void sendData(byte[] data, int size) {
        try {
            dataPacket = new DatagramPacket(data, size, ip, port);
            dataPacket.setData(data);
            Log.e("====发送一个包======", size + "");
            socket.send(dataPacket);
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("====发送失败======", size + "");
        }
    }

    /*
     * start sending data
     */
    public void startSending() {

        this.isSendering = true;
    }

    /*
     * stop sending data
     */
    public void stopSending() {

        this.isSendering = false;
    }


    // run
    public void run() {
        Log.e(LOG,"准备发送....");
        while (true) {
            if (isSendering) {
                if (dataList.size() > 0) {
                    AudioData2 encodedData = dataList.remove(0);
                    if (encodedData.getSize() != 0) {
                        sendData(encodedData.getRealData(), encodedData.getSize());
                         //MyPTTApplication.getInstance().getRtpInitSession().sendData(encodedData.getRealData());
                    }
                }
            }
        }
    }
}