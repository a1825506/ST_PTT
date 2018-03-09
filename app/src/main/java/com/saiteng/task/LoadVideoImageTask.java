package com.saiteng.task;

import java.io.File;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.saiteng.picHelper.ImageCache;
import com.saiteng.picHelper.ImageUtils;
import com.saiteng.stptt.ShowVideoActivity;


public class LoadVideoImageTask extends AsyncTask<Object, Void, Bitmap> {

	private ImageView iv = null;
	String thumbnailPath = null;
	String thumbnailUrl = null;
	Activity activity;
	BaseAdapter adapter;

	@Override
	protected Bitmap doInBackground(Object... params) {
		thumbnailPath = (String) params[0];
		iv = (ImageView) params[1];
		activity=(Activity)params[2];
		if (new File(thumbnailPath).exists()) {
			return ImageUtils.getVideoThumbnail(thumbnailPath, 120, 120, MediaStore.Video.Thumbnails.MINI_KIND);
		} else {
			return null;
		}
	}

	@Override
	protected void onPostExecute(Bitmap result) {
		super.onPostExecute(result);
		if (result != null) {
			iv.setImageBitmap(result);
			ImageCache.getInstance().put(thumbnailPath, result);
			iv.setClickable(true);
			iv.setTag(thumbnailPath);
			iv.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					if (thumbnailPath != null) {
						Intent intent = new Intent(activity,
								ShowVideoActivity.class);
						intent.putExtra("localpath",thumbnailPath);

						activity.startActivity(intent);

					}
				}
			});

		} else {


		}
	}

}
