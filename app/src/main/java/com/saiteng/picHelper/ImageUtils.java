/**
 * Copyright (C) 2013-2014 EaseMob Technologies. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.saiteng.picHelper;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;

import com.saiteng.stptt.PathUtil;

import java.io.File;

import static com.saiteng.videoHelper.ImageResizer.calculateInSampleSize;

public class ImageUtils {


//	public static String getThumbnailImagePath(String imagePath) {
//		String path = imagePath.substring(0, imagePath.lastIndexOf("/") + 1);
//		path += "th" + imagePath.substring(imagePath.lastIndexOf("/") + 1, imagePath.length());
//		EMLog.d("msg", "original image path:" + imagePath);
//		EMLog.d("msg", "thum image path:" + path);
//		return path;
//	}
	
	public static String getImagePath(String remoteUrl)
	{
		String imageName= remoteUrl.substring(remoteUrl.lastIndexOf("/") + 1, remoteUrl.length());
		String path =PathUtil.getInstance().getImageChatPathName()+"/"+ imageName;

        return path;
		
	}
	
	
	public static String getThumbnailImagePath(String thumbRemoteUrl) {
		String thumbImageName= thumbRemoteUrl.substring(thumbRemoteUrl.lastIndexOf("/") + 1, thumbRemoteUrl.length());
		String path = PathUtil.getInstance().getImageChatPathName()+ "th"+thumbImageName;

        return path;
    }


	public static Bitmap decodeScaleImage(String filePath, int reqWidth, int reqHeight) {
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(filePath, options);

		// Calculate inSampleSize
		options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

		// Decode bitmap with inSampleSize set
		options.inJustDecodeBounds = false;
		//避免出现内存溢出的情况，进行相应的属性设置。
		options.inPreferredConfig = Bitmap.Config.RGB_565;
		options.inDither = true;
		return BitmapFactory.decodeFile(filePath, options);
	}


	public static Bitmap getVideoThumbnail(String videoPath, int width, int height, int kind)
	{
		Bitmap bitmap = null;

		// 获取视频的缩略图

		bitmap = ThumbnailUtils.createVideoThumbnail(videoPath, kind);

		if(bitmap != null){  //如果视频已损坏或者格式不支持可能返回null

			bitmap =ThumbnailUtils.extractThumbnail(bitmap, width, height, ThumbnailUtils.OPTIONS_RECYCLE_INPUT);

		}

		return bitmap;
	}

	public static int readPictureDegree(String filePath){

		File file  = new File(filePath);

		if(file.exists()){
			return  1;
		}else{
			return  0;
		}



	}
}
