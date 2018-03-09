package com.videotakepicture;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.app.Activity;
import android.content.Context;
import android.os.storage.StorageManager;
import android.util.Log;

public class StorageList {
	private Activity mActivity;
	private StorageManager mStorageManager;
	private Method mMethodGetPaths;
	private Method mMethodGetPathsState;
	private String TAG = "StorageList";

	public StorageList(Activity activity) {
		mActivity = activity;
		if (mActivity != null) {
			mStorageManager = (StorageManager) mActivity
					.getSystemService(Context.STORAGE_SERVICE);
			try {
				mMethodGetPaths = mStorageManager.getClass().getMethod(
						"getVolumePaths");
				// ͨ���������ʵ��mStorageManager��getClass()��ȡStorageManager���Ӧ��Class����
				// getMethod("getVolumePaths")����StorageManager���Ӧ��Class�����getVolumePaths���������ﲻ������
				// getDeclaredMethod()----���Բ���ԭ�����ĵ���Ȩ��
				// mMethodGetPathsState=mStorageManager.getClass().
				// getMethod("getVolumeState",String.class);//String.class�β��б�
			} catch (NoSuchMethodException ex) {
				ex.printStackTrace();
			}
		}
	}

	public String[] getVolumnPaths() {
		String[] paths = null;
		try {
			paths = (String[]) mMethodGetPaths.invoke(mStorageManager);// ���ø÷���
			Log.d(TAG, "Storage'paths[0]:" + paths[0]);
			Log.d(TAG, "Storage'paths[1]:" + paths[1]);
		} catch (IllegalArgumentException ex) {
			ex.printStackTrace();
		} catch (IllegalAccessException ex) {
			ex.printStackTrace();
		} catch (InvocationTargetException ex) {
			ex.printStackTrace();
		}
		return paths;
	}

	public String getVolumeState(String mountPoint) {
		// mountPoint�ǹ��ص���Storage'paths[1]:/mnt/extSdCard����/mnt/extSdCard/
		// ��ͬ�ֻ���Ӵ洢�����ֲ�һ����/mnt/sdcard
		String status = null;
		try {
			status = (String) mMethodGetPathsState.invoke(mStorageManager,
					mountPoint);
			// ���ø÷�����mStorageManager��������mountPoint��ʵ����
		} catch (IllegalArgumentException ex) {
			ex.printStackTrace();
		} catch (IllegalAccessException ex) {
			ex.printStackTrace();
		} catch (InvocationTargetException ex) {
			ex.printStackTrace();
		}
		Log.d(TAG, "VolumnState:" + status);
		return status;
	}
}
