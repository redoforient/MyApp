package com.ips.mycamera;

import android.hardware.Camera;
import android.util.Log;

/**
 * Created by linc on 15-5-11.
 */
public class CameraUtils {
    private static final String TAG = "CameraUtils";

    /**
     * A safe way to get an instance of the Camera object.
     */
    //CameraInfo.CAMERA_FACING_BACK
    //Camera.getCameraInfo()
    public static Camera getCameraInstance(int cameraId) {
        Camera c = null;
        try {
            c = Camera.open(cameraId); // attempt to get a Camera instance
        } catch (Exception e) {
            // Camera is not available (in use or does not exist)
            Log.e(TAG, "get camera failed." + e.getMessage());
        }
        return c; // returns null if camera is unavailable
    }
}
