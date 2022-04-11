package com.example.gaitapplication

import android.content.Intent
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.storage.FirebaseStorage
import org.json.JSONObject
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*


class AnalyzeSEIActivity : AppCompatActivity() {

    lateinit var imageView: ImageView
    lateinit var selectImgbutton: Button
    private val pickImage = 100
    private var imageUri: Uri? = null
    var predTV: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_analyze_seiactivity)
        predTV = findViewById(R.id.PredictiontextView)
        imageView = findViewById(R.id.imageView)
        selectImgbutton = findViewById(R.id.selectImgButton)
        selectImgbutton.setOnClickListener {
            val gallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
            startActivityForResult(gallery, pickImage)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == pickImage) {
            imageUri = data?.data
            imageView.setImageURI(imageUri)
        }
    }

    private fun getfiletype(videouri: Uri): String? {
        val r = contentResolver
        // get the file type ,in this case its mp4
        val mimeTypeMap = MimeTypeMap.getSingleton()
        return mimeTypeMap.getExtensionFromMimeType(r.getType(videouri))
    }

    fun analyzeSeiBtnClicked(view: View) {
        if (imageUri==null) {
            Toast.makeText(this, "Please select an SEI Image!", Toast.LENGTH_SHORT).show()
        } else {

            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
            val currentDate = sdf.format(Date())
            val videoID = "Video_ID_"+currentDate+ UUID.randomUUID().toString()
            // save the selected video in Firebase storage
            val reference = FirebaseStorage.getInstance()
                .getReference("VideoUploads/" + videoID + "." + getfiletype(imageUri!!))
            reference.putFile(imageUri!!).addOnSuccessListener { taskSnapshot ->
                val uriTask = taskSnapshot.storage.downloadUrl
                while (!uriTask.isSuccessful);
                // get the link of vimage
                val downloadUri = uriTask.result.toString()
//                val reference1: DatabaseReference =
//                    FirebaseDatabase.getInstance().getReference("Video_Uploads")
//
//
//                val uploadMap: Map<String, String> = mapOf(
//                    "name" to nameEd!!.text.toString(),
//                    "userID" to mAuth.currentUser?.uid.toString(),
//                    "videoID" to videoID,
//                    "timestamp" to currentDate,
//                    "videolink" to downloadUri,
//                    "age" to ageEd!!.text.toString(),
//                    "height_in_cm" to heightEd!!.text.toString(),
//                    "weight_in_kg" to weightEd!!.text.toString(),
//                    "pathologies" to pathEd!!.text.toString()
//                )
//                reference1.child(videoID).setValue(uploadMap)
                // Video uploaded successfully
                // Dismiss dialog
                //progressDialog!!.dismiss()
                newDownloadTask(findViewById(R.id.PredictiontextView), findViewById(R.id.modePredtextView)).execute(downloadUri)
                Toast.makeText(this, "Video Uploaded!!", Toast.LENGTH_SHORT).show()
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

        }


    }


    class newDownloadTask(PredictiontextView: TextView, modelDetTV: TextView) : AsyncTask<String?, Void?, String?>() {
        val predTV: TextView = PredictiontextView
        val modTv: TextView = modelDetTV
        override fun doInBackground(vararg params: String?): String? {
            var result: String? = ""
            val url: URL
            var urlConnection: HttpURLConnection? = null
            return try {
                url = URL("http://ec2-65-2-38-209.ap-south-1.compute.amazonaws.com:8000/predict_SEI")
                urlConnection = url.openConnection() as HttpURLConnection
                urlConnection.setRequestMethod("POST")
                urlConnection.setRequestProperty("Content-Type", "application/json; utf-8")
                urlConnection.setRequestProperty("Accept", "application/json")
                urlConnection.setDoOutput(true)
                val jsonPost: JSONObject = JSONObject()
                jsonPost.put("image_url", params[0])
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
                }
                result
            } catch (e: Exception) {
                //Toast.makeText(this, "Did not work! 3", Toast.LENGTH_SHORT).show()
                null
            }
        }


        override fun onPostExecute(s: String?) {
            super.onPostExecute(s)
            try {
                val jsonObject = JSONObject(s)
                val prediction = jsonObject.getString("prediction")
                predTV.setText(prediction)
//                var model_details = "Probabilities generated by model for each pathology class."
//                model_details +=  "\nDiplegic: "+jsonObject.getString("Diplegic")
//                model_details +=  "\nHemiplegic: "+jsonObject.getString("Hemiplegic")
//                model_details +=  "\nNeuropathic: "+jsonObject.getString("Neuropathic")
//                model_details +=  "\nNormal: "+jsonObject.getString("Normal")
//                model_details +=  "\nParkinson: "+jsonObject.getString("Parkinson")
                var model_details = ""
                val percString = jsonObject.getString(prediction)
                var perc = percString.toFloat()
                perc = (perc * 100.00).toFloat();
                val percInt = perc.toInt()
                model_details += "We are "+percInt.toString()+"% certain that the prediction is true"
                modTv.setText(model_details)

            } catch (e: Exception) {
                //
            }
        }
    }



    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, AnalyzeOptionsActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        startActivity(intent)
    }
}