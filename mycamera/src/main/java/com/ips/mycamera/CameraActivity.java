package com.ips.mycamera;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * linc 2015.5.11
 * the detail of use Camera
 * 1.Detect and Access Camera
 * 2.Create a Preview Class
 * 3.Build a Preview Layout
 * 4.Setup Listeners for Capture
 * 5.Capture and Save Files
 * 6.Release the Camera
 */
public class CameraActivity extends ActionBarActivity {
    private static final String TAG = "CameraActivity";
    private Camera mCamera;
    private CameraPreview mPreview;
    private File mPhotoFile;
    private ImageView ivBitMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        ivBitMap = (ImageView) findViewById(R.id.iv_bitmap);
        openCamera();
    }

    private void openCamera() {
        if (checkCameraHardware(this)) {

            // Create an instance of Camera
            mCamera = CameraUtils.getCameraInstance(Camera.CameraInfo.CAMERA_FACING_FRONT);

            // Create our Preview view and set it as the content of our activity.
            mPreview = new CameraPreview(this, mCamera);
            FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
            preview.addView(mPreview);
        }
    }

    /**
     * Check if this device has a camera
     */
    private boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            Log.d(TAG, "the number of camera is " + Camera.getNumberOfCameras());
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }

    public void onCaptureClicked(View view) {
        mCamera.takePicture(shutterCallback, rawCallback, jpegCallback);
        //mCamera.takePicture(shutterCallback, rawCallback, jpegCallback2);
    }


    //call an empty shutter callback and then it plays the default sound.
    Camera.ShutterCallback shutterCallback = new Camera.ShutterCallback() {
        public void onShutter() {
            Log.d(TAG, "onShutter'd");
        }
    };


    Camera.PictureCallback rawCallback = new Camera.PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera) {
            Log.d(TAG, "onPictureTaken - raw");
        }
    };


    private Camera.PictureCallback jpegCallback = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            Log.d(TAG, "onPictureTaken");
            mPhotoFile = ImageFileUtils.getOutputMediaFile();
            if (mPhotoFile == null) {
                Log.d(TAG, "Error creating media file, check storage permissions: ");
                return;
            }

            //转化为BitMap
            Bitmap bitmapPicture = BitmapFactory.decodeByteArray(data, 0, data.length);
            Bitmap rotateBitmap = ImageFileUtils.rotateBitmap(bitmapPicture, -90);//将图片逆时针旋转90度
            ivBitMap.setImageBitmap(rotateBitmap);

            try {
                FileOutputStream fos = new FileOutputStream(mPhotoFile);
                fos.write(data);
                fos.close();
                Log.d(TAG, "save picture success");
                //notify
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    Intent mediaScanIntent = new Intent(
                            Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                    Uri contentUri = Uri.fromFile(mPhotoFile); //out is your output file
                    mediaScanIntent.setData(contentUri);
                    CameraActivity.this.sendBroadcast(mediaScanIntent);
                } else {
                    File fileDir = Environment.getExternalStorageDirectory();
                    Uri fileUri = Uri.parse("file://" + fileDir);
                    Intent mediaMounted = new Intent(Intent.ACTION_MEDIA_MOUNTED, fileUri);
                    sendBroadcast(mediaMounted);
                }
            } catch (FileNotFoundException e) {
                Log.d(TAG, "File not found: " + e.getMessage());
            } catch (IOException e) {
                Log.d(TAG, "Error accessing file: " + e.getMessage());
            }

            //拍照后允许继续进行preview与拍照
            mCamera.startPreview();
        }
    };


    Camera.PictureCallback jpegCallback2 = new Camera.PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera) {

            FileOutputStream outStream = null;
            if (data == null) {
                Log.e(TAG, "DATA IS NULL");
            }
            try {
                Log.e(TAG, "DIR:" + Environment.getExternalStorageDirectory().getAbsolutePath());
                Log.e(TAG, "SIZE:" + Integer.toString(data.length));
                //outStream = getApplicationContext().openFileOutput("PhotoZhangll.jpg", Context.MODE_WORLD_READABLE);
                outStream = new FileOutputStream(ImageFileUtils.getOutputMediaFile());
                outStream.write(data);
                outStream.flush();
                outStream.close();
                Log.d(TAG, "onPictureTaken - wrote bytes:" + data.length);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Log.d(TAG, "onPictureTaken - jpeg");
        }
    };


    @Override
    protected void onPause() {
        super.onPause();
        releaseCamera();// release the camera immediately on pause event
    }


    private void releaseCamera() {
        if (mCamera != null) {
            mCamera.release();// release the camera for other applications
            mCamera = null;
        }
    }

}
