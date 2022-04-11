package com.example.gaitapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MainActivity : AppCompatActivity() {
    var logoutBtn: Button? = null
    var homepageToolbar: Toolbar? = null
    val mAuth = FirebaseAuth.getInstance()
    var hiNameT: TextView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        logoutBtn = findViewById(R.id.button2)
        homepageToolbar = findViewById(R.id.homepageToolbar)
        setSupportActionBar(homepageToolbar)
        hiNameT = findViewById(R.id.hiTv)
    }

    override fun onStart() {
        super.onStart()

        FirebaseDatabase.getInstance().getReference().child("users")
            .addValueEventListener(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {}

                override fun onDataChange(snapshot: DataSnapshot) {
                   if (!snapshot.child(mAuth.currentUser?.uid.toString()).exists()) {
                       val userMap: Map<String, String> = mapOf(
                           "name" to "",
                           "userID" to mAuth.currentUser?.uid.toString(),
                           "emailID" to mAuth.currentUser?.email.toString(),
                           "age" to "",
                           "height_in_cm" to "",
                           "weight_in_kg" to ""
                       )

                       FirebaseDatabase.getInstance().getReference().child("users")
                           .child(mAuth.currentUser?.uid.toString())
                           .setValue(userMap)

                   } else {
                       val hiName = snapshot.child(mAuth.currentUser?.uid.toString()).child("name").value.toString()
                       hiNameT?.text = "Hi "+hiName
                   }

                }

            })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.home_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId==R.id.logout) {
            logoutClicked()
        } else if (item.itemId==R.id.profile) {
            val intent = Intent(this, MyProfileActivity::class.java)
            startActivity(intent)
        } else if (item.itemId==R.id.privacy_policy) {
            val intent = Intent(this, PrivacyActivity::class.java)
            startActivity(intent)
        }
        return super.onOptionsItemSelected(item)
    }

    fun logoutClicked() {
        AuthUI.getInstance().signOut(this)
            .addOnSuccessListener {
                val intent = Intent(this,LoginActivity::class.java)
                startActivity(intent)
                this.finish()
                Toast.makeText(this, "Successfully Log Out", Toast.LENGTH_SHORT).show()
            }
    }

    fun uploadClicked(view: View) {
        val intent = Intent(this, UploadActivity::class.java)
        startActivity(intent)
    }
    fun analyzeClicked(view: View) {
        val intent = Intent(this, AnalyzeOptionsActivity::class.java)
        startActivity(intent)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finishAffinity()
    }


}