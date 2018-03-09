package com.videotakepicture;

import java.io.File;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Environment;
import android.os.StatFs;
import android.os.Vibrator;
import android.util.Log;
import android.widget.Toast;

public class VideoUtils {

	private static String[] paths;
	private static final String PATH = "/route";
	private static String ExtSDDir = "/storage/extSdCard";
	private static final String IntSDDir = Environment
			.getExternalStorageDirectory().getPath();
	private static final String filePath = "/route/routep";
	public static Activity mActivity;

	public VideoUtils(Activity mActivity) {
		VideoUtils.mActivity = mActivity;
		StorageList mStorageList = new StorageList(mActivity);
		paths = mStorageList.getVolumnPaths();
		ExtSDDir = paths[1];
	}

	// ¼��ʱ�����ʾ
	public static void createDirectory2Store(Context context) {
		File ExtSDPath = new File(ExtSDDir + PATH);
		File IntSDPath = new File(IntSDDir + PATH);
		Log.d("geek", IntSDPath.toString()+" ����");
		Log.d("geek", ExtSDPath.toString() + "����");
		if (!ExtSDPath.exists()) {
			boolean success = (ExtSDPath.mkdir());
			Log.d("geek", success+"");
			if (!success) {///storage/emulated/0/MPU
				if (!IntSDPath.exists()) {
					IntSDPath.mkdir();
				}
			}
		}
	}

	public static void createFilePath(Context context) {
		File ExtSDPath = new File(ExtSDDir + filePath);
		File IntSDPath = new File(IntSDDir + filePath);
		if (!ExtSDPath.exists()) {
			boolean success = (ExtSDPath.mkdir());
			if (!success) {
				if (!IntSDPath.exists()) {
					IntSDPath.mkdir();
				}
			}
		}
	}

	public static int pathIsExist() {
		File ExtSDPath = new File(ExtSDDir + filePath);
		File IntSDPath = new File(IntSDDir + filePath);
		if (ExtSDPath.exists()) {
			return 0;
		} else {
			return 1;
		}
	}

	public static String filePath() {
		File ExtSDPath = new File(ExtSDDir + filePath);
		File IntSDPath = new File(IntSDDir + filePath);
		if (ExtSDPath.exists()) {
			return ExtSDPath.getPath();
		} else {
			return IntSDPath.getPath();
		}
	}

	public static void deleteFiles() {

		File file1 = new File(ExtSDDir + PATH);
		if (file1.exists()) {
			File[] files = file1.listFiles();
			for (int i = 0; i < files.length; i++) {
				if (files[i].exists()) {
					files[i].delete();
				}
			}
			file1.delete();
		}

		File file2 = new File(IntSDDir + PATH);
		if (file2.exists()) {
			File[] files = file2.listFiles();
			for (int i = 0; i < files.length; i++) {
				if (files[i].exists()) {
					files[i].delete();
				}
			}
			file2.delete();
		}
		File file3 = new File(ExtSDDir + filePath);
		if (file3.exists()) {
			File[] files = file3.listFiles();
			for (int i = 0; i < files.length; i++) {
				if (files[i].exists()) {
					files[i].delete();
				}
			}
			file3.delete();
		}

		File file4 = new File(IntSDDir + filePath);
		if (file4.exists()) {
			File[] files = file4.listFiles();
			for (int i = 0; i < files.length; i++) {
				if (files[i].exists()) {
					files[i].delete();
				}
			}
			file4.delete();
		}
		
		
	}

	public static String generateFileName() {
		SimpleDateFormat simpleDateFormate = new SimpleDateFormat(
				"yyyyMMdd_HHmmss");

		File ExtSD = new File(ExtSDDir + PATH);
		if (ExtSD.exists()) {
			return ExtSDDir + PATH + "/" + simpleDateFormate.format(new Date())
					+ ".rar";

		} else {

			return IntSDDir + PATH + "/" + simpleDateFormate.format(new Date())
					+ ".rar";
		}

	}

	public static void showDialog(final Context context, String message) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle("提示").setMessage(message)
				.setPositiveButton("确定", new OnClickListener() {

					public void onClick(DialogInterface dialog, int which) {
						((Activity) context).finish();
						System.exit(0);
					}
				}).show();

	}

	public static String time2String(int duration) {
		int minDuration = duration / 60;
		int secDuration = duration % 60;
		return "00:" + (minDuration >= 10 ? minDuration : "0" + minDuration)
				+ ":" + (secDuration >= 10 ? secDuration : "0" + secDuration);
	}

	// ��1��
	public static void vibrateOnce(Context context) {
		Vibrator vibrator = (Vibrator) context
				.getSystemService(Context.VIBRATOR_SERVICE);
		long[] pattern = { 50, 50 };
		vibrator.vibrate(pattern, -1);
	}

	// ������
	public static void vibrateTwice(Context context) {
		Vibrator vibrator = (Vibrator) context
				.getSystemService(Context.VIBRATOR_SERVICE);
		long[] pattern = { 100, 50, 100, 50 };
		vibrator.vibrate(pattern, -1);
	}

	/**
	 * ������
	 * 
	 * @param context
	 */
	public static void vibrateThrice(Context context) {
		Vibrator vibrator = (Vibrator) context
				.getSystemService(Context.VIBRATOR_SERVICE);
		long[] pattern = { 100, 600, 100, 600, 100, 600 };
		vibrator.vibrate(pattern, -1);
	}

	/**
	 * ��ʱ�����ʽ�� HH:mm:ss ---24Сʱ�Ƶ� hh:mm:ss ---12Сʱ�Ƶ�
	 * 
	 * @param timestamp
	 * @return
	 */
	public static String getDate(long timestamp) {
		String date = new SimpleDateFormat("yyyyMMdd_HHmmss")
				.format(new Date(timestamp));
		return date;
	}
	
	/**
	 * ��ȡ�ֻ����õ�sdcard���ܴ�С
	 * @return
	 */
	// ��ȡ�ֻ����õ��ڴ�ռ� ���� ��λ M
	public static double getMemorySize() {
		File path = Environment.getDataDirectory();
		StatFs stat = new StatFs(path.getPath());
		long blockSize = stat.getBlockSize();
		long availableBlocks = stat.getAvailableBlocks();
		float number = (availableBlocks * blockSize) * 1.0f / (1024 * 1024 * 1024);
		BigDecimal b = new BigDecimal(number);
		double size = b.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
		return size;
	}
	
	/**
	 * ��ȡ�ֻ�����sdcard�Ŀ��ÿռ�
	 * @return
	 */
	 public static double getTotalInternalMemorySize() {
	        File path = Environment.getDataDirectory();
	        StatFs stat = new StatFs(path.getPath());
	        long blockSize = stat.getBlockSize();
	        long totalBlocks = stat.getBlockCount();
	        float number = (totalBlocks * blockSize) * 1.0f / (1024 * 1024 * 1024);
	        BigDecimal b = new BigDecimal(number);
			double size = b.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
	        return size;
	    }
	
	
	// ��ȡsdcard���õ��ڴ�ռ� ���� ��λ G
	public static double getSDSize() {
		String state = Environment.getExternalStorageState();
		// SD��������
		if (!state.equals(Environment.MEDIA_MOUNTED)) {
			return -1;
		}
		
		File path = new File(ExtSDDir);
		StatFs stat = new StatFs(path.getPath());
		long blockSize = stat.getBlockSize();
		long availableBlocks = stat.getAvailableBlocks();
		
		float number = (availableBlocks * blockSize) * 1.0f / (1024 * 1024 * 1024);
		BigDecimal b = new BigDecimal(number);
		double size = b.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
		
		return size;

	}
	
	//��ȡsdcard���ܴ�С
	public static double getAllSize() {

		String state = Environment.getExternalStorageState();
		// SD��������
		if (!state.equals(Environment.MEDIA_MOUNTED)) {
			return -1;
		}

		File path = new File(ExtSDDir);
		StatFs stat = new StatFs(path.getPath());

		long blockSize = stat.getBlockSize();

		long availableBlocks = stat.getBlockCount();
		
		float number = (availableBlocks * blockSize)* 1.0f / (1024 * 1024 * 1024);
		
		BigDecimal b = new BigDecimal(number);
		double size = b.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
		
		return size;

	}
	
	/**
	 * �ж��Ƿ��������sdcard �õ����ÿռ��С��
	 * @return
	 */
	public static double getAvailableSizeData(){
		File ExtSD = new File(ExtSDDir + PATH);
		if(ExtSD.exists()){
			return getSDSize();
		}else{
			return getMemorySize();
		}
	} 
	
	/**
	 * �ж��Ƿ��������sdcard �õ��ܿռ��С
	 * @return
	 */
	public static double getTotalSizeData(){
		File ExtSD = new File(ExtSDDir + PATH);
		if(ExtSD.exists()){
			return getAllSize(); 
		}else{
			return getTotalInternalMemorySize();
		}
	}

}