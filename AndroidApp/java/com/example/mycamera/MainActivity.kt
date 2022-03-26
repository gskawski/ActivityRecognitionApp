package com.example.mycamera

import android.content.Context
import android.hardware.Camera
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.widget.Button
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


    }

    private val PERMISSIONS = Array<String>(1) {android.Manifest.permission.CAMERA}

    private val REQUEST_PERMISSIONS = 34

    private val PERMISSIONS_COUNT = 1;

    private fun arePermissionsDenied(): Boolean {
        for(i in 0 until PERMISSIONS_COUNT) {
            if(ContextCompat.checkSelfPermission(this, PERMISSIONS[i]) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                return true
            }
        }
        return false
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if(requestCode == REQUEST_PERMISSIONS && grantResults.isNotEmpty()) {
            if(arePermissionsDenied()) {
                val am = getSystemService(Context.ACTIVITY_SERVICE) as android.app.ActivityManager
                am.clearApplicationUserData()
                recreate()
            } else {
                onResume()
            }
        }
    }


    private var isCameraInitialized = false

    private lateinit var myCamera: android.hardware.Camera

    private lateinit var myHolder: SurfaceHolder

    private lateinit var mPreview: CameraPreview

    private lateinit var preview: FrameLayout

    override fun onResume() {
        super.onResume()
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M && arePermissionsDenied()) {
            requestPermissions(PERMISSIONS, REQUEST_PERMISSIONS)
            return
        }
        if(!isCameraInitialized) {
            myCamera = Camera.open(0)
            mPreview = CameraPreview(this, myCamera)
            preview = findViewById(R.id.camera_preview)
            preview.addView(mPreview)

            val captureButton: Button = findViewById(R.id.button_capture)
            captureButton.setOnClickListener {
                // get an image from the camera
                myCamera?.takePicture(null, null, mPicture)
            }

        }
    }

    class CameraPreview(
            context: Context,
            private val mCamera: Camera
    ) : SurfaceView(context), SurfaceHolder.Callback {

        private val mHolder: SurfaceHolder = holder.apply {
            // Install a SurfaceHolder.Callback so we get notified when the
            // underlying surface is created and destroyed.
            addCallback(this@CameraPreview)
            // deprecated setting, but required on Android versions prior to 3.0
            setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS)
        }

        override fun surfaceCreated(holder: SurfaceHolder) {
            // The Surface has been created, now tell the camera where to draw the preview.
            mCamera.apply {
                try {
                    setPreviewDisplay(holder)
                    startPreview()
                } catch (e: IOException) {
                    //val TAG = "Error setting camera preview: ${e.message}"
                    Log.d(this::class.java.simpleName, "Error setting camera preview: ${e.message}")
                }
            }
        }

        override fun surfaceDestroyed(holder: SurfaceHolder) {
            // empty. Take care of releasing the Camera preview in your activity.
        }

        override fun surfaceChanged(holder: SurfaceHolder, format: Int, w: Int, h: Int) {
            // If your preview can change or rotate, take care of those events here.
            // Make sure to stop the preview before resizing or reformatting it.
            if (mHolder.surface == null) {
                // preview surface does not exist
                return
            }

            // stop preview before making changes
            try {
                mCamera.stopPreview()
            } catch (e: Exception) {
                // ignore: tried to stop a non-existent preview
            }

            // set preview size and make any resize, rotate or
            // reformatting changes here

            // start preview with new settings
            mCamera.apply {
                try {
                    setPreviewDisplay(mHolder)
                    startPreview()
                } catch (e: Exception) {
                    Log.d(this::class.java.simpleName, "Error starting camera preview: ${e.message}")
                }
            }
        }
    }

    private val mPicture = Camera.PictureCallback { data, _ ->
        val pictureFile: File = getOutputMediaFile(MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE) ?: run {
            Log.d(this::class.java.simpleName, ("Error creating media file, check storage permissions"))
            return@PictureCallback
        }

        try {
            val fos = FileOutputStream(pictureFile)
            fos.write(data)
            fos.close()
        } catch (e: FileNotFoundException) {
            Log.d(this::class.java.simpleName, "File not found: ${e.message}")
        } catch (e: IOException) {
            Log.d(this::class.java.simpleName, "Error accessing file: ${e.message}")
        }
    }



    val MEDIA_TYPE_IMAGE = 1
    val MEDIA_TYPE_VIDEO = 2

    /** Create a file Uri for saving an image or video */
    private fun getOutputMediaFileUri(type: Int): Uri {
        return Uri.fromFile(getOutputMediaFile(type))
    }

    /** Create a File for saving an image or video */
    private fun getOutputMediaFile(type: Int): File? {
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        val mediaStorageDir = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                "MyCameraApp"
        )
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        mediaStorageDir.apply {
            if (!exists()) {
                if (!mkdirs()) {
                    Log.d("MyCameraApp", "failed to create directory")
                    return null
                }
            }
        }

        // Create a media file name
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        return when (type) {
            MEDIA_TYPE_IMAGE -> {
                File("${mediaStorageDir.path}${File.separator}IMG_$timeStamp.jpg")
            }
            MEDIA_TYPE_VIDEO -> {
                File("${mediaStorageDir.path}${File.separator}VID_$timeStamp.mp4")
            }
            else -> null
        }
    }


//    class CameraActivity : Activity() {
//
//        private var mCamera: Camera? = null
//        private var mPreview: CameraPreview? = null
//
//        override fun onCreate(savedInstanceState: Bundle?) {
//            super.onCreate(savedInstanceState)
//            setContentView(R.layout.activity_main)
//
//            // Create an instance of Camera
//            mCamera = getCameraInstance()
//
//            mPreview = mCamera?.let {
//                // Create our Preview view
//                CameraPreview(this, it)
//            }
//
//            // Set the Preview view as the content of our activity.
//            mPreview?.also {
//                val preview: FrameLayout = findViewById(R.id.camera_preview)
//                preview.addView(it)
//            }
//        }
//
//        /** A safe way to get an instance of the Camera object. */
//        fun getCameraInstance(): Camera? {
//            return try {
//                Camera.open() // attempt to get a Camera instance
//            } catch (e: Exception) {
//                // Camera is not available (in use or does not exist)
//                null // returns null if camera is unavailable
//            }
//        }
//    }

//    private class CameraPreview : SurfaceView, SurfaceHolder.Callback {
//        private lateinit var mHolder : SurfaceHolder
//        private lateinit var myCamera : Camera
//
//        private fun CameraPreview(context: Context, camera: Camera) {
//            super(context)
//            myCamera = camera
//            mHolder = holder
//            mHolder.addCallback(this)
//            mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS)
//        }
//
//        public fun surfaceCreated(holder: SurfaceHolder) {
//            myHolder = holder
//            try {
//                myCamera.setPreviewDisplay(holder)
//                myCamera.startPreview()
//            } catch (IOException e) {
//                e.printStackTrace()
//            }
//        }
//
//        public fun surfaceDestroyed()
//    }
}