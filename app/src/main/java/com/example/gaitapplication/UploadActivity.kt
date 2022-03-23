package com.example.gaitapplication

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class UploadActivity : AppCompatActivity() {
    var uploadToolbar: Toolbar? = null
    private val CAMERA_PERMISSION_CODE = 100
    private val VIDEO_RECORD_CODE = 101
    var videoView: VideoView? = null;
    var videoPath: Uri? = null
    var uploadBtn: Button? = null
    var recordBtn: Button? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload)


        videoView = findViewById(R.id.videoView)
        videoView!!.setVideoURI(videoPath)
        uploadBtn = findViewById(R.id.uploadBtn)
        recordBtn = findViewById(R.id.recordBtn)

        if (cameraCheck()) {
            cameraPermision()
        } else {
            Log.i("VIDEO", "Camera is not detected")
        }


    }

    fun uploadBtnClicked(view: View) {
        if (videoPath !=null) {
            val intent = Intent(this, UploadDetailsActivity::class.java)
            intent.putExtra("videoPath", videoPath.toString())
            videoPath = null
            startActivity(intent)
        } else {
            Toast.makeText(this, "No video recorded!", Toast.LENGTH_SHORT).show()
        }

    }

    private fun cameraCheck(): Boolean {
        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
            return true;
        }
        return false;
    }

    private fun cameraPermision() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_CODE)
        }
    }

    fun recordBtnClicked(view: View) {
        val intent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
        startActivityForResult(intent, VIDEO_RECORD_CODE)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == VIDEO_RECORD_CODE) {
            if (resultCode == RESULT_OK) {
                videoPath = data!!.data
                videoView!!.setVideoURI(videoPath)
                videoView!!.start()
                Log.i("VIDEO", "video is recorded at path $videoPath")
            } else if (resultCode == RESULT_CANCELED) {
                Log.i("VIDEO", "video is recorded is cancelled ")
            }
            run { Log.i("VIDEO", "video is recorded has error ") }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        startActivity(intent)
    }
}