package com.example.gaitapplication

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import android.widget.VideoView
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.storage.FirebaseStorage
import org.json.JSONObject
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

class AnalyzeVideoActivity : AppCompatActivity() {

    var uploadToolbar: Toolbar? = null
    private val CAMERA_PERMISSION_CODE = 100
    private val VIDEO_RECORD_CODE = 101
    private var videoView: VideoView? = null;
    private var videoPath: Uri? = null
    var analyzeBtn: Button? = null
    var recordBtn: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_analyze_video)

        videoView = findViewById(R.id.videoView)
        videoView!!.setVideoURI(videoPath)
        analyzeBtn = findViewById(R.id.analyzeVideobtn)
        recordBtn = findViewById(R.id.recordVideoButton)

        if (cameraCheck()) {
            cameraPermision()
        } else {
            Log.i("VIDEO", "Camera is not detected")
        }
    }

    private fun cameraCheck(): Boolean {
        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
            return true;
        }
        return false;
    }

    private fun cameraPermision() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_DENIED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION_CODE
            )
        }
    }

    fun analyzeVideoRecordBtnClicked(view: View) {
        val intent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
        startActivityForResult(intent, VIDEO_RECORD_CODE)
    }

    private fun getfiletype(videouri: Uri): String? {
        val r = contentResolver
        // get the file type ,in this case its mp4
        val mimeTypeMap = MimeTypeMap.getSingleton()
        return mimeTypeMap.getExtensionFromMimeType(r.getType(videouri))
    }

    fun analyzeVideoBtnClicked(view: View) {
        if (videoPath != null) {
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
            val currentDate = sdf.format(Date())
            val videoID = "Video_ID_" + currentDate + UUID.randomUUID().toString()
            // save the selected video in Firebase storage
            val reference = FirebaseStorage.getInstance()
                .getReference("VideoUploads/" + videoID + "." + getfiletype(videoPath!!))
            reference.putFile(videoPath!!).addOnSuccessListener { taskSnapshot ->
                val uriTask = taskSnapshot.storage.downloadUrl
                while (!uriTask.isSuccessful);

                // get the link of vimage
                val downloadUri = uriTask.result.toString()
                Toast.makeText(this, "The URI is $downloadUri", Toast.LENGTH_SHORT).show()

                newDownloadTask(
                    findViewById(R.id.PredictiontextView),
                    findViewById(R.id.modePredtextView)
                ).execute(downloadUri)

            }.addOnFailureListener { e -> // Error, Image not uploaded
                //progressDialog!!.dismiss()
                Toast.makeText(this, "Failed " + e.message, Toast.LENGTH_SHORT).show()
            }.addOnProgressListener { taskSnapshot ->
                // Progress Listener for loading
                // percentage on the dialog box
                // show the progress bar
                //val progress = 100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount
                //progressDialog!!.setMessage("Uploaded " + progress.toInt() + "%")
            }
        } else {
            Toast.makeText(this, "No video recorded!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == VIDEO_RECORD_CODE) {
            if (resultCode == RESULT_OK) {
                videoPath = data!!.data
                Log.i("videoPathURI", videoPath.toString())
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

    class newDownloadTask(PredictiontextView: TextView, modelDetTV: TextView) :
        AsyncTask<String?, Void?, String?>() {
        val predTV: TextView = PredictiontextView
        val modTv: TextView = modelDetTV
        override fun doInBackground(vararg params: String?): String? {

            var result: String? = ""
            val url: URL
            var urlConnection: HttpURLConnection? = null
            return try {
                Log.i("TESTTEST", "In here1 Params: $params")
                url =
                    URL("http://ec2-43-204-63-184.ap-south-1.compute.amazonaws.com:8000/predict_SEI")
                urlConnection = url.openConnection() as HttpURLConnection
                urlConnection.setRequestMethod("POST")
                urlConnection.setRequestProperty("Content-Type", "application/json; utf-8")
                urlConnection.setRequestProperty("Accept", "application/json")
                urlConnection.setDoOutput(true)
                val jsonPost: JSONObject = JSONObject()
                jsonPost.put("video_url", params[0])
                val jsonInputString = jsonPost.toString()
                urlConnection.getOutputStream().use { os ->
                    val input: ByteArray = jsonInputString.toByteArray()
                    os.write(input, 0, input.size)
                }


                val `in`: InputStream = urlConnection.getInputStream()
                val reader = InputStreamReader(`in`)
                var data: Int = reader.read()
                while (data != -1) {
                    val current = data.toChar()
                    result += current
                    data = reader.read()
                    Log.i("TESTEST", "In here1 param${params[0]}")
                    Log.i("TESTEST", "In here1 data $data")
                    Log.i("TESTEST", "In here1 result $result")
                }

                result
            } catch (e: Exception) {
                Log.i("TESTEST", "In here2")
                //Toast.makeText(this, "Did not work! 3", Toast.LENGTH_SHORT).show()
                null
            }
        }
    }
}

