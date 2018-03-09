package com.saiteng.stptt;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.saiteng.audioHelper.AudioSender;
import com.saiteng.audioHelper.ChatAudioSender;
import com.saiteng.conn.DefFrame;
import com.saiteng.conn.NettyClient;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;

/**
 * Created by Moore on 2017/9/7.
 */

public class PathUtil {
    private static String TAG="PathUtil";
    private static final String PATH = Environment
            .getExternalStorageDirectory().getPath();
    private static final String FilePath = "/PTT";
    public static final String voiceChatPathName = "/voicechat/";
    public static final String imagePathName = "/image/";
    public static final String voicePathName = "/voice/";
    public static final String filePathName = "/file/";
    public static final String videoPathName = "/video/";
    public static final String meetingPathName = "/meeting/";
    public static final String beforeencoder="/beforeencoder.pcm";
    public static final String afterdecoder="/afterdecoder.pcm";

    private File voicePath = null;
    private File imagePath = null;
    private File filePath = null;
    private File videoPath = null;


    private static PathUtil instance = null;

    private PathUtil() {
    }

    public static PathUtil getInstance() {
        if(instance == null) {
            instance = new PathUtil();
        }

        return instance;
    }

    public static void readFile(int messagetype,int messageId,long message_length,String filePath){
        FileInputStream fileInputStream = null;
        File file =new File(filePath);
        Log.e(TAG,"文件的大小："+ file.length());
        ChatAudioSender sender = new ChatAudioSender();
        sender.setParam(messagetype,messageId,message_length);
        sender.startSending();
        try {
            fileInputStream = new FileInputStream(filePath);
            final byte[] rawbyte = new byte[Config.ORDERLENGTH];
            int size;
            int totalsize=0;
            while ((size = fileInputStream.read(rawbyte, 0, Config.ORDERLENGTH)) != -1) {
                //每次读取998个字节。添加头尾后是1K的数据进行处理后发送，
                 sender.addData(rawbyte,size);
                 totalsize= totalsize+size;
            }
            fileInputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static FileOutputStream fos = null;
    public static void writeFile(String filePath,byte[] data){
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                fos = new FileOutputStream(file);// 建立一个可存取字节的文件
            }
            fos.write(data);
            if(Config.DEBUG){
                Log.e(TAG,"写入数据"+data.length);
            }

        } catch (Exception e) {
            if(Config.DEBUG){
                Log.e(TAG,"写入数据异常："+e.toString());
            }

        }
    }
    static FileOutputStream fos1 = null;
    public static void writeBeforePCMFile(byte[] data){

        try {
            File file = new File(PATH+FilePath+beforeencoder);
            if (!file.exists()) {
                fos1 = new FileOutputStream(file);// 建立一个可存取字节的文件
            }
            fos1.write(data);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    static FileOutputStream fos2 = null;
    public static void writeAfterPCMFile(byte[] data){

        try {
            File file = new File(PATH+FilePath+afterdecoder);
            if (!file.exists()) {
                fos2 = new FileOutputStream(file);// 建立一个可存取字节的文件
            }
            fos2.write(data);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void initDirs() {
        this.voicePath = generateVoicePath(PATH, FilePath, voiceChatPathName);
        if(!this.voicePath.exists()) {
            this.voicePath.mkdirs();
        }

        this.imagePath = generateVoicePath(PATH, FilePath, imagePathName);
        if(!this.imagePath.exists()) {
            this.imagePath.mkdirs();
        }

        this.filePath = generateVoicePath(PATH, FilePath, filePathName);
        if(!this.filePath.exists()) {
            this.filePath.mkdirs();
        }

        this.videoPath = generateVoicePath(PATH, FilePath, videoPathName);
        if(!this.videoPath.exists()) {
            this.videoPath.mkdirs();
        }
    }

    private static File generateVoicePath(String var0, String var1, String var2) {
     return new File(var0+var1+var2);
    }

    public File getVoicePath() {
        return this.voicePath;
    }

    public static String getVoiceChatPathName(){return PATH+FilePath+voiceChatPathName;}

    public static String getVideoChatPathName(){return PATH+FilePath+videoPathName;}

    public static String getImageChatPathName(){return PATH+FilePath+imagePathName;}
}
