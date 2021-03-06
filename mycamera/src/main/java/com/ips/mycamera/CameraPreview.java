package com.ips.mycamera;

/**
 * Created by linc on 15-5-11.
 */

import android.content.Context;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;

/**
 * @desc A basic Camera preview class
 * @author zhangll
 * create at 2019/1/18 20:06
 */
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
    private final static String TAG = "CameraPreview";
    private SurfaceHolder mHolder;
    private Camera mCamera;
    private Camera.Parameters parameters = null;

    public CameraPreview(Context context, Camera camera) {
        super(context);
        mCamera = camera;

        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = getHolder();
        mHolder.addCallback(this);
        // deprecated setting, but required on Android versions prior to 3.0
        //SURFACE_TYPE_PUSH_BUFFERS
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // The Surface has been created, now tell the camera where to draw the preview.
        try {
            parameters = mCamera.getParameters();
            int orientation = this.getResources().getConfiguration().orientation;
            if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                parameters.set("orientation", "portrait");
                parameters.setRotation(90);
                mCamera.setDisplayOrientation(90);
            } else {
                // This is an undocumented although widely known feature
                parameters.set("orientation", "landscape");
                parameters.setRotation(0);
                // For Android 2.2 and above
                mCamera.setDisplayOrientation(0);
                // Uncomment for Android 2.0 and above
            }
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
        } catch (IOException e) {
            Log.d(TAG, "Error setting camera preview: " + e.getMessage());
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // empty. Take care of releasing the Camera preview in your activity.
        //this.getHolder().removeCallback(this);
        //mCamera.stopPreview();
        //mCamera.release();
        //mCamera = null;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        // If your preview can change or rotate, take care of those events here.
        // Make sure to stop the preview before resizing or reformatting it.

        if (mHolder.getSurface() == null) {
            // preview surface does not exist
            return;
        }

        // stop preview before making changes
        try {
            mCamera.stopPreview();
        } catch (Exception e) {
            // ignore: tried to stop a non-existent preview
        }

        // set preview size and make any resize, rotate or
        // reformatting changes here

        // start preview with new settings
        try {
            // set preview size and make any resize,
            //parameters.setPreviewSize(width, height);

            // rotate or reformatting changes here


            if (null != mCamera) {
                mCamera.setPreviewDisplay(mHolder);
                mCamera.startPreview();
            }
        } catch (Exception e) {
            Log.d(TAG, "Error starting camera preview: " + e.getMessage());
        }
    }
}