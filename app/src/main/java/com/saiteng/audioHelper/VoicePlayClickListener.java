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
package com.saiteng.audioHelper;

import java.io.File;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.Toast;

import com.saiteng.fragment.ChatFragment;
import com.saiteng.stptt.Config;
import com.saiteng.stptt.MainActivity;
import com.saiteng.stptt.MainPTTActivity;
import com.saiteng.stptt.R;
import com.saiteng.user.MessageInfo;

public class VoicePlayClickListener implements View.OnClickListener {
    String TAG = "VoicePlayClickListener";
	MessageInfo message;
	ImageView voiceIconView;

	private AnimationDrawable voiceAnimation = null;
	MediaPlayer mediaPlayer = null;
	ImageView iv_read_status;
	Activity activity;
	private BaseAdapter adapter;

	public static boolean isPlaying = false;
	public static VoicePlayClickListener currentPlayListener = null;

	/**
	 * 
	 * @param message
	 * @param v
	 * @param iv_read_status
	 */
	public VoicePlayClickListener(MessageInfo message, ImageView v, ImageView iv_read_status, BaseAdapter adapter
			 ) {
		this.message = message;
		this.iv_read_status = iv_read_status;
		this.adapter = adapter;
		voiceIconView = v;
		this.activity = MainPTTActivity.getActivity();
	}

	public void stopPlayVoice() {
		voiceAnimation.stop();
		if (message.message_Direct == ChatFragment.MESSAGE_RECE) {
			voiceIconView.setImageResource(R.mipmap.chatfrom_voice_playing);
		} else {
			voiceIconView.setImageResource(R.mipmap.chatto_voice_playing);
		}
		// stop play voice
		if (mediaPlayer != null) {
			mediaPlayer.stop();
			mediaPlayer.release();
		}
		isPlaying = false;
		ChatFragment.playMsgId = null;
		adapter.notifyDataSetChanged();
	}

	public void playVoice(String filePath) {
		if (!(new File(filePath).exists())) {
			return;
		}
		AudioManager audioManager = (AudioManager) activity.getSystemService(Context.AUDIO_SERVICE);

		mediaPlayer = new MediaPlayer();
	 {
			audioManager.setSpeakerphoneOn(false);// 关闭扬声器
			// 把声音设定成Earpiece（听筒）出来，设定为正在通话中
			audioManager.setMode(AudioManager.MODE_IN_CALL);
			mediaPlayer.setAudioStreamType(AudioManager.STREAM_VOICE_CALL);
		}
		try {
			mediaPlayer.setDataSource(filePath);
			mediaPlayer.prepare();
			mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

				@Override
				public void onCompletion(MediaPlayer mp) {
					// TODO Auto-generated method stub
					mediaPlayer.release();
					mediaPlayer = null;
					stopPlayVoice(); // stop animation
				}

			});
			isPlaying = true;
			currentPlayListener = this;
			mediaPlayer.start();
			showAnimation();


		} catch (Exception e) {
			if(Config.DEBUG){
				Log.e(TAG,e.toString());

			}
		}
	}

	// show the voice playing animation
	private void showAnimation() {
		 {
			voiceIconView.setImageResource(R.drawable.voice_to_icon);
		}
		voiceAnimation = (AnimationDrawable) voiceIconView.getDrawable();
		voiceAnimation.start();
	}

	@Override
	public void onClick(View v) {
		if (isPlaying) {
			currentPlayListener.stopPlayVoice();
		}

	//	if (message.message_Direct == ChatFragment.MESSAGE_SEND) {
			// for sent msg, we will try to play the voice file directly
			playVoice(message.getPath());
		//} else {
//			if (message.status == EMMessage.Status.SUCCESS) {
//				File file = new File(voiceBody.getLocalUrl());
//				if (file.exists() && file.isFile())
//					playVoice(voiceBody.getLocalUrl());
//				else
//					System.err.println("file not exist");
//
//			} else if (message.status == EMMessage.Status.INPROGRESS) {
//				Toast.makeText(activity, "正在下载语音，稍后点击", Toast.LENGTH_SHORT).show();
//			} else if (message.status == EMMessage.Status.FAIL) {
//				Toast.makeText(activity, "正在下载语音，稍后点击", Toast.LENGTH_SHORT).show();
//				new AsyncTask<Void, Void, Void>() {
//
//					@Override
//					protected Void doInBackground(Void... params) {
//						EMChatManager.getInstance().asyncFetchMessage(message);
//						return null;
//					}
//
//					@Override
//					protected void onPostExecute(Void result) {
//						super.onPostExecute(result);
//						adapter.notifyDataSetChanged();
//					}
//
//				}.execute();
//
//			}

	//	}
	}
}