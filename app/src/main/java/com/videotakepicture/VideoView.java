package com.videotakepicture;

import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by Moore on 2018-01-26.
 */

public class VideoView implements SurfaceHolder.Callback{

    private SurfaceHolder mSurfaceHolder;

    private MediaRecorder mediaRecorder;

    private int mCurrentCameraId = 1; // 1 代表前置摄像头 0 代表后置摄像头

    private Camera mCamera;

    private Camera.Parameters localParameters;

    private int nMaxZoomValue;

    private int[] isZoomSupportOfCamera = new int[2];

    private RecordView mrecordView;

    private int nCurZoomValue;

    private boolean isRecording;

    @SuppressWarnings("deprecation")
    public void init(SurfaceView view) {

        mSurfaceHolder = view.getHolder();

        mSurfaceHolder.addCallback(this);

        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }


    public boolean isRecording() {
        return isRecording;
    }

    public int getmCurrentCameraId() {
        return mCurrentCameraId;
    }

    public void setmCurrentCameraId(int mCurrentCameraId) {
        this.mCurrentCameraId = mCurrentCameraId;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

        mSurfaceHolder = holder;
        new Thread() {
            @Override
            public void run() {
                startPreview();
            }
        }.start();
    }



    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        mSurfaceHolder = holder;
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

        stopRecord();
        mSurfaceHolder = null;
    }

    public void setRecordView(RecordView recordView){
        mrecordView = recordView;
    }


    /**
     * 开始预览画面
     */
    @SuppressWarnings("deprecation")
    public void startPreview() {
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
        if (mCamera == null) {
            mCamera = Camera.open(mCurrentCameraId);
            try {
                mCamera.setPreviewDisplay(mSurfaceHolder);
            } catch (IOException e) {
                e.printStackTrace();
            }
            localParameters = this.mCamera.getParameters();
            localParameters.setPreviewFrameRate(5); // 设置每秒显示4帧
            // localParameters.setPictureSize(width, height); // 设置保存的图片尺寸
            localParameters.setJpegQuality(80); // 设置照片质量
            // 设置照片格式
            localParameters.setPictureFormat(PixelFormat.JPEG);

            if (localParameters.isZoomSupported()) {
                nMaxZoomValue = localParameters.getMaxZoom(); // 获取最大的焦距

                nCurZoomValue = localParameters.getZoom(); // 获取当前的焦距
                isZoomSupportOfCamera[mCurrentCameraId] = 1;
            }
            mCamera.startPreview();
            if (this.mCurrentCameraId == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                mCamera.setDisplayOrientation(270);
            } else {
                mCamera.setDisplayOrientation(90);
            }
        }
    }


    /**
     * 开始录制视频
     * @return
     */
    @SuppressWarnings("deprecation")
    public boolean startRecord() {

        mediaRecorder = new MediaRecorder(); //录制视频类
        mediaRecorder.reset();
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
        if (mCamera == null) {
            mCamera = Camera.open(mCurrentCameraId);
            // Log.d("geek", mCurrentCameraId+"   s");
            localParameters = this.mCamera.getParameters();
            localParameters.setPreviewFrameRate(5); // 设置每秒显示4帧
            // localParameters.setPictureSize(width, height); // 设置保存的图片尺寸
            localParameters.setJpegQuality(80); // 设置照片质量
            // 设置照片格式
            localParameters.setPictureFormat(PixelFormat.JPEG);

            if (localParameters.isZoomSupported()) {
                nMaxZoomValue = localParameters.getMaxZoom(); // 获取最大的焦距
                mrecordView.setMaxChangeZomm(nMaxZoomValue);
                nCurZoomValue = localParameters.getZoom(); // 获取当前的焦距
                isZoomSupportOfCamera[mCurrentCameraId] = 1;
            }
            if (this.mCurrentCameraId == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                mCamera.setDisplayOrientation(270);
            } else {
                mCamera.setDisplayOrientation(90);
            }

        }
        mCamera.unlock();
        mediaRecorder.setCamera(mCamera);
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

        CamcorderProfile localCamcorderProfile = null;
        if (this.mCurrentCameraId == Camera.CameraInfo.CAMERA_FACING_FRONT)
            localCamcorderProfile = CamcorderProfile.get(this.mCurrentCameraId,
                    CamcorderProfile.QUALITY_HIGH);
        else
            localCamcorderProfile = CamcorderProfile
                    .get(CamcorderProfile.QUALITY_1080P);
        localCamcorderProfile.duration = 86400;
        mediaRecorder.setProfile(localCamcorderProfile);

        mediaRecorder.setPreviewDisplay(mSurfaceHolder.getSurface());
        mediaRecorder.setOutputFile(VideoUtils.generateFileName());



        if (this.mCurrentCameraId == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            //mCamera.setDisplayOrientation(270);
            mediaRecorder.setOrientationHint(0);
        } else {
            //mCamera.setDisplayOrientation(0);
            mediaRecorder.setOrientationHint(0);
        }
        try {
            mediaRecorder.prepare();
            mediaRecorder.start();
            isRecording = true;
            mrecordView.setDuration(0);
            mrecordView.getHandler().sendEmptyMessage(1);


        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 停止录制视频
     * @return
     */
    public boolean stopRecord() {
        try {
            if (mediaRecorder != null) {

                mrecordView.getHandler().removeMessages(1);
                mrecordView.getHandler().sendEmptyMessage(4);
                mediaRecorder.stop();
                mediaRecorder.reset();
                mediaRecorder.release();
                mediaRecorder = null;
            }
        } catch (Exception e) {
            return false;
        }

        isRecording = false;
        return true;
    }

    /**
     * 拉远焦距
     */
    public void changerZoom(int progress) {
        if (isZoomSupportOfCamera[mCurrentCameraId] == 1 && mCamera != null) {
            if (progress >= this.nMaxZoomValue)
                return;
            localParameters = this.mCamera.getParameters();

            this.localParameters.isZoomSupported();
            this.nCurZoomValue = progress;
            nCurZoomValue = nCurZoomValue >= nMaxZoomValue ? nMaxZoomValue
                    : progress;
            this.localParameters.setZoom(nCurZoomValue);
            this.mCamera.setParameters(this.localParameters);

        }
    }


    Camera.PictureCallback pictureCallback = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {


            mCamera.cancelAutoFocus(); //这一句很关键
            //恢复对焦模式
            //localParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            //localParameters.setFocusAreas(null);
            localParameters.setPreviewFrameRate(25); // 设置每秒显示4帧
            // localParameters.setPictureSize(width, height); // 设置保存的图片尺寸
            localParameters.setJpegQuality(80); // 设置照片质量
            // 设置照片格式
            localParameters.setPictureFormat(PixelFormat.JPEG);

            if (localParameters.isZoomSupported()) {
                nMaxZoomValue = localParameters.getMaxZoom(); // 获取最大的焦距

                nCurZoomValue = localParameters.getZoom(); // 获取当前的焦距
                isZoomSupportOfCamera[mCurrentCameraId] = 1;
            }
            mCamera.setParameters(localParameters);
            //
            mCamera.startPreview();



            File file = new File(VideoUtils.filePath(),
                    VideoUtils.getDate(System.currentTimeMillis()) + ".rar");
            try {
                FileOutputStream outputStream = new FileOutputStream(file);
                outputStream.write(data); // 写入sd卡中
                outputStream.close(); // 关闭输出流


            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };

    /**
     * 照相的方法，回调函数
     */
    public void takePicture() {
        mCamera.takePicture(null, null, pictureCallback);
    }
}
