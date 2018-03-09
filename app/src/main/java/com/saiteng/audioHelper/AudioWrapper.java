package com.saiteng.audioHelper;


import com.saiteng.rtp.RtpSenderWrapper;
import com.saiteng.stptt.Config;
import com.saiteng.stptt.MyPTTApplication;

public class AudioWrapper {

	private AudioRecorder audioRecorder;

	private AudioReceiver audioReceiver;

	private AudioSender audioSender;

    private AudioSender sender ;



	private static AudioWrapper instanceAudioWrapper;

	private AudioWrapper() {

	}

	public static AudioWrapper getInstance() {
		if (null == instanceAudioWrapper) {
			instanceAudioWrapper = new AudioWrapper();
		}
		return instanceAudioWrapper;
	}

	public void startRecord() {
		if (null == audioRecorder) {
			audioRecorder = new AudioRecorder();
		}
		audioRecorder.startRecording();
	}

	public void startRTPSender(){

		if(audioSender==null){
			audioSender = new AudioSender();
		}
		audioSender.startSending();

	}

	public void startListen() {
		if (null == audioReceiver) {
			audioReceiver = new AudioReceiver();
		}
		audioReceiver.startRecieving();


	}



	public void startSend(){
		if (null == sender) {
			sender = new AudioSender();
		}
		sender.startSending();
	}

	public AudioSender getSender(){
		return sender;
	}

	public void stopRecord() {
		if (audioRecorder != null)
			audioRecorder.stopRecording();
	}

}
