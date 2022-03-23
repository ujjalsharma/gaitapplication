package com.example.gaitapplication

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import java.text.SimpleDateFormat
import java.util.*

class UploadDetailsActivity : AppCompatActivity() {
    var uploadDetailsToolbar: Toolbar? = null
    var videoPath: Uri? = null
    var uploadBtn: Button? = null
    var progressDialog: ProgressDialog? = null
    val mAuth = FirebaseAuth.getInstance()
    var nameEd: EditText? = null
    var ageEd: EditText? = null
    var weightEd: EditText? = null
    var heightEd: EditText? = null
    var pathEd: EditText? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload_details)
        uploadDetailsToolbar = findViewById(R.id.uploaddetailsToolbar)
        setSupportActionBar(uploadDetailsToolbar)
        videoPath = Uri.parse(intent.getStringExtra("videoPath"))
        uploadBtn = findViewById(R.id.uploadButton)
        nameEd = findViewById(R.id.nameTv)
        ageEd = findViewById(R.id.ageTv)
        weightEd = findViewById(R.id.weightTv)
        heightEd = findViewById(R.id.heightTv)
        pathEd = findViewById(R.id.pathTv)
    }

    override fun onStart() {
        super.onStart()
        FirebaseDatabase.getInstance().getReference().child("users").child(mAuth.currentUser?.uid.toString())
            .addValueEventListener(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {}

                override fun onDataChange(snapshot: DataSnapshot) {
                    nameEd?.setText(snapshot.child("name").value.toString())
                    ageEd?.setText(snapshot.child("age").value.toString())
                    heightEd?.setText(snapshot.child("height_in_cm").value.toString())
                    weightEd?.setText(snapshot.child("weight_in_kg").value.toString())

                }

            })
    }

    fun uploadButtonClicked(view: View) {

        // Code for showing progressDialog while uploading
        progressDialog = ProgressDialog(this)
        progressDialog!!.setTitle("Uploading...")
        progressDialog!!.show()
        uploadvideo()
    }

    private fun getfiletype(videouri: Uri): String? {
        val r = contentResolver
        // get the file type ,in this case its mp4
        val mimeTypeMap = MimeTypeMap.getSingleton()
        return mimeTypeMap.getExtensionFromMimeType(r.getType(videouri))
    }

    private fun uploadvideo() {
        if (videoPath != null) {
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
            val currentDate = sdf.format(Date())
            val videoID = "Video_ID_"+currentDate+UUID.randomUUID().toString()
            // save the selected video in Firebase storage
            val reference = FirebaseStorage.getInstance()
                .getReference("VideoUploads/" + videoID + "." + getfiletype(videoPath!!))
            reference.putFile(videoPath!!).addOnSuccessListener { taskSnapshot ->
                val uriTask = taskSnapshot.storage.downloadUrl
                while (!uriTask.isSuccessful);
                // get the link of video
                val downloadUri = uriTask.result.toString()
                val reference1: DatabaseReference =
                    FirebaseDatabase.getInstance().getReference("Video_Uploads")


                val uploadMap: Map<String, String> = mapOf(
                    "name" to nameEd!!.text.toString(),
                    "userID" to mAuth.currentUser?.uid.toString(),
                    "videoID" to videoID,
                    "timestamp" to currentDate,
                    "videolink" to downloadUri,
                    "age" to ageEd!!.text.toString(),
                    "height_in_cm" to heightEd!!.text.toString(),
                    "weight_in_kg" to weightEd!!.text.toString(),
                    "pathologies" to pathEd!!.text.toString()
                )
                reference1.child(videoID).setValue(uploadMap)
                // Video uploaded successfully
                // Dismiss dialog
                progressDialog!!.dismiss()
                Toast.makeText(this, "Video Uploaded!!", Toast.LENGTH_SHORT).show()
                onBackPressed()
            }.addOnFailureListener { e -> // Error, Image not uploaded
                progressDialog!!.dismiss()
                Toast.makeText(this, "Failed " + e.message, Toast.LENGTH_SHORT).show()
            }.addOnProgressListener { taskSnapshot ->
                // Progress Listener for loading
                // percentage on the dialog box
                // show the progress bar
                val progress = 100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount
                progressDialog!!.setMessage("Uploaded " + progress.toInt() + "%")
            }
        }
    }


    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, UploadActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        startActivity(intent)
    }
}