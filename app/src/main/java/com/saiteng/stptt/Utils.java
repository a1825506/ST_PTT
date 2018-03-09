package com.saiteng.stptt;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.hardware.Camera;
import android.media.AudioRecord;
import android.os.Build;
import android.os.Environment;
import android.os.StrictMode;
import android.os.Vibrator;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;

/**
 * Created by Moore on 2017/7/5.
 */

public class Utils {

    private Utils() {
    };


    public static void PlayVibrator(Context context){
        Vibrator vibrator = (Vibrator) context
                .getSystemService(Context.VIBRATOR_SERVICE);
        long[] pattern = { 100, 400 };
        vibrator.vibrate(25);
    }

    public static byte[] getBooleanArray(byte b) {
        byte[] array = new byte[8];
        for (int i = 7; i >= 0; i--) {
            array[i] = (byte)(b & 1);
            b = (byte) (b >> 1);
        }
        return array;
    }

    /**
     * 二进制字符串转byte
     */
    public static byte decodeBinaryString(String byteStr) {
        int re, len;
        if (null == byteStr) {
            return 0;
        }
        len = byteStr.length();
        if (len != 4 && len != 8) {
            return 0;
        }
        if (len == 8) {// 8 bit处理
            if (byteStr.charAt(0) == '0') {// 正数
                re = Integer.parseInt(byteStr, 2);
            } else {// 负数
                re = Integer.parseInt(byteStr, 2) - 256;
            }
        } else {// 4 bit处理
            re = Integer.parseInt(byteStr, 2);
        }
        return (byte) re;
    }



    /**
     * 将int数值转换为占四个字节的byte数组，本方法适用于(高位在前，低位在后)的顺序。  和bytesToInt2（）配套使用
     */
    public static byte[] longToBytes2(long value)
    {
        byte[] src = new byte[4];
        src[0] = (byte) ((value>>24) & 0xFF);
        src[1] = (byte) ((value>>16)& 0xFF);
        src[2] = (byte) ((value>>8)&0xFF);
        src[3] = (byte) (value & 0xFF);
        return src;
    }

    //long转byte[]
    public static byte[] long2Byte(byte[] bb, long x) {
        bb[ 0] = (byte) (x >> 56);
        bb[ 1] = (byte) (x >> 48);
        bb[ 2] = (byte) (x >> 40);
        bb[ 3] = (byte) (x >> 32);
        bb[ 4] = (byte) (x >> 24);
        bb[ 5] = (byte) (x >> 16);
        bb[ 6] = (byte) (x >> 8);
        bb[ 7] = (byte) (x >> 0);
        return bb;
    }


    //byte[]转为long
    public static long getLong(byte[] bb) {
        return ((((long) bb[ 0] & 0xff) << 56)
                | (((long) bb[ 1] & 0xff) << 48)
                | (((long) bb[ 2] & 0xff) << 40)
                | (((long) bb[ 3] & 0xff) << 32)
                | (((long) bb[ 4] & 0xff) << 24)
                | (((long) bb[ 5] & 0xff) << 16)
                | (((long) bb[ 6] & 0xff) << 8) | (((long) bb[ 7] & 0xff) << 0));
    }

    //将int转换为byte[]
    public static byte[] int2Bytes(int value, int len) {
        byte[] b = new byte[len];
        for (int i = 0; i < len; i++) {
            b[len - i - 1] = (byte)((value >> 8 * i) & 0xff);
        }
        return b;
    }

    //byte[]转换为int
    public static int bytes2Int(byte[] b, int start, int len) {
        int sum = 0;
        int end = start + len;
        for (int i = start; i < end; i++) {
            int n = ((int)b[i]) & 0xff;
            n <<= (--len) * 8;
            sum += n;
        }
        return sum;
    }


    public static String unintbyte2long(byte[] res) {
        int firstByte = 0;
        int secondByte = 0;
        int thirdByte = 0;
        int fourthByte = 0;
        int index = 0;
        firstByte = (0x000000FF & ((int) res[index]));
        secondByte = (0x000000FF & ((int) res[index + 1]));
        thirdByte = (0x000000FF & ((int) res[index + 2]));
        fourthByte = (0x000000FF & ((int) res[index + 3]));
//      index = index + 4;
//      long ip  =((long) (firstByte << 24 | secondByte << 16 | thirdByte << 8 | fourthByte)) & 0xFFFFFFFFL;
        return  firstByte+"."+secondByte+"."+thirdByte+"."+fourthByte;

    }



    /**将ip转换为无符号整型*/
    public static long ipToLong(String strIp) {
        String[]ip = strIp.split("\\.");
        return (Long.parseLong(ip[0]) << 24) + (Long.parseLong(ip[1]) << 16) + (Long.parseLong(ip[2]) << 8) + Long.parseLong(ip[3]);
    }



    //获取本地IP函数
    public static long getLocalIPAddress()
    {
        String hostIp = "127.0.0.1";
        try {
            Enumeration nis = NetworkInterface.getNetworkInterfaces();
            InetAddress ia = null;
            while (nis.hasMoreElements()) {
                NetworkInterface ni = (NetworkInterface) nis.nextElement();
                Enumeration<InetAddress> ias = ni.getInetAddresses();
                while (ias.hasMoreElements()) {
                    ia = ias.nextElement();
                    if (ia instanceof Inet6Address) {
                        continue;// skip ipv6
                    }
                    String ip = ia.getHostAddress();
                    if (!"127.0.0.1".equals(ip)) {
                        hostIp = ia.getHostAddress();
                        break;
                    }
                }
            }
        } catch (SocketException e) {
            Log.i("yao", "SocketException");
            e.printStackTrace();
        }
        return ipToLong(hostIp);
    }


    @SuppressLint("NewApi")
    public static void enableStrictMode() {
        if(Utils.hasGingerbread())
        {
            StrictMode.ThreadPolicy.Builder threadPolicyBuilder =
                    new StrictMode.ThreadPolicy.Builder()
                            .detectAll()
                            .penaltyLog();
            StrictMode.VmPolicy.Builder vmPolicyBuilder =
                    new StrictMode.VmPolicy.Builder()
                            .detectAll()
                            .penaltyLog();

            if (Utils.hasGingerbread()) {
                threadPolicyBuilder.penaltyFlashScreen();
                vmPolicyBuilder
                        .setClassInstanceLimit(ImageGridActivity.class, 1);
            }
            StrictMode.setThreadPolicy(threadPolicyBuilder.build());
            StrictMode.setVmPolicy(vmPolicyBuilder.build());
        }





    }

    public static boolean hasFroyo() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO;

    }

    public static boolean hasGingerbread() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD;
    }

    public static boolean hasHoneycomb() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
    }

    public static boolean hasHoneycombMR1() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1;
    }

    public static boolean hasJellyBean() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN;
    }

    public static boolean hasKitKat() {
        return Build.VERSION.SDK_INT >= 19;
    }

    public static List<Camera.Size> getResolutionList(Camera camera)
    {
        Camera.Parameters parameters = camera.getParameters();
        List<Camera.Size> previewSizes = parameters.getSupportedPreviewSizes();
        return previewSizes;
    }

    public static class ResolutionComparator implements Comparator<Camera.Size> {

        @Override
        public int compare(Camera.Size lhs, Camera.Size rhs) {
            if(lhs.height!=rhs.height)
                return lhs.height-rhs.height;
            else
                return lhs.width-rhs.width;
        }

    }

    /**
     * @说明 主要是为解析静态数据包，将一个字节数组转换为short数组
     * @param b
     */
    public static short[] byteArray2ShortArray(byte[] b) {
        int len = b.length / 2;
        int index = 0;
        short[] re = new short[len];
        byte[] buf = new byte[2];
        for (int i = 0; i < b.length;) {
            buf[0] = b[i];
            buf[1] = b[i + 1];
            short st = byteToShort(buf);
            re[index] = st;
            index++;
            i += 2;
        }
        return re;
    }


    /**
     * @说明 主要是为解析静态数据包，将一个short数组反转为字节数组
     * @param b
     */
    public static byte[] shortArray2ByteArray(short[] b) {
        byte[] rebt = new byte[b.length * 2];
        int index = 0;
        for (int i = 0; i < b.length; i++) {
            short st = b[i];
            byte[] bt = shortToByte(st);
            rebt[index] = bt[0];
            rebt[index + 1] = bt[1];
            index += 2;
        }
        return rebt;
    }


    /**
     * @功能 短整型与字节的转换
     * @return 两位的字节数组
     */
    public static byte[] shortToByte(short number) {
        int temp = number;
        byte[] b = new byte[2];
        for (int i = 0; i < b.length; i++) {
            b[i] = new Integer(temp & 0xff).byteValue();// 将最低位保存在最低位
            temp = temp >> 8; // 向右移8位
        }
        return b;
    }



    /**
     * @功能 字节的转换与短整型
     * @return 短整型
     */
    public static short byteToShort(byte[] b) {
        short s = 0;
        short s0 = (short) (b[0] & 0xff);// 最低位
        short s1 = (short) (b[1] & 0xff);
        s1 <<= 8;
        s = (short) (s0 | s1);
        return s;
    }



}
