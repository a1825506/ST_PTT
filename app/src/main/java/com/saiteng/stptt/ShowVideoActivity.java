package com.saiteng.stptt;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;


/**
/**
 * 展示视频内容
 * 
 * @author Administrator
 * 
 */
public class ShowVideoActivity extends BaseActivity{

	private RelativeLayout loadingLayout;
	private ProgressBar progressBar;
	private String localFilePath;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.showvideo_activity);
		loadingLayout = (RelativeLayout) findViewById(R.id.loading_layout);
		progressBar = (ProgressBar) findViewById(R.id.progressBar);
		localFilePath = getIntent().getStringExtra("localpath");
//		String remotepath = getIntent().getStringExtra("remotepath");
//		String secret = getIntent().getStringExtra("secret");
		if (localFilePath != null && new File(localFilePath).exists()) {
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setDataAndType(Uri.fromFile(new File(localFilePath)),
					"video/mp4");
			startActivity(intent);
			finish();
		}

	}
	
	public String getLocalFilePath(String remoteUrl){
		String localPath;
		if (remoteUrl.contains("/")) {
			localPath = PathUtil.getInstance().getVideoChatPathName()
					+ "/" + remoteUrl.substring(remoteUrl.lastIndexOf("/") + 1)
					+ ".mp4";
		} else {
			localPath = PathUtil.getInstance().getVideoChatPathName()
					+ "/" + remoteUrl + ".mp4";
		}
		return localPath;
	}
	
	/**
	 * 播放本地视频
	 * @param localPath 视频路径
	 */
	private void showLocalVideo(String localPath){
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setDataAndType(Uri.fromFile(new File(localPath)),
				"video/mp4");
		startActivity(intent);
		finish();
	}
	
	
	

	/**
	 * 下载视频文件
	 */
	private void downloadVideo(final String remoteUrl,
			final Map<String, String> header) {

		if (TextUtils.isEmpty(localFilePath)) {
			localFilePath = getLocalFilePath(remoteUrl);
		}
		if (new File(localFilePath).exists()) {
			showLocalVideo(localFilePath);
			return;
		}
		loadingLayout.setVisibility(View.VISIBLE);


	}

	@Override
	public void onBackPressed() {
		finish();
	}
 

}
