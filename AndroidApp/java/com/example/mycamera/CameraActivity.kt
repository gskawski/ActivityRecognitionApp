package com.example.mycamera

import android.app.Activity
import android.hardware.Camera
import android.os.Bundle
import android.widget.FrameLayout

class CameraActivity : Activity() {

    private var mCamera: Camera? = null
    private var mPreview: MainActivity.CameraPreview? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Create an instance of Camera
        mCamera = getCameraInstance()

        mPreview = mCamera?.let {
            // Create our Preview view
            MainActivity.CameraPreview(this, it)
        }

        // Set the Preview view as the content of our activity.
        mPreview?.also {
            val preview: FrameLayout = findViewById(R.id.camera_preview)
            preview.addView(it)
        }
    }

    /** A safe way to get an instance of the Camera object. */
    fun getCameraInstance(): Camera? {
        return try {
            Camera.open() // attempt to get a Camera instance
        } catch (e: Exception) {
            // Camera is not available (in use or does not exist)
            null // returns null if camera is unavailable
        }
    }

    override fun onPause() {
        super.onPause()
        //releaseMediaRecorder() // if you are using MediaRecorder, release it first
        releaseCamera() // release the camera immediately on pause event
    }

    private fun releaseCamera() {
        mCamera?.release() // release the camera for other applications
        mCamera = null
    }
}