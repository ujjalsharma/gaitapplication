package com.example.gaitapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.internal.InternalTokenProvider

class MyProfileActivity : AppCompatActivity() {

    var profileToolbar: Toolbar? = null
    var nametv: TextView? = null
    var agetv: TextView? = null
    var weighttv: TextView? = null
    var heightv: TextView? = null
    var emailtv: TextView? = null
    val mAuth = FirebaseAuth.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_profile)
        profileToolbar = findViewById(R.id.myprofileToolbar)
        setSupportActionBar(profileToolbar)
        nametv = findViewById(R.id.nametv)
        agetv = findViewById(R.id.agetv)
        weighttv = findViewById(R.id.weighttv)
        heightv = findViewById(R.id.heighttv)
        emailtv = findViewById(R.id.emailtv)
    }

    override fun onStart() {
        super.onStart()
        emailtv?.text = mAuth.currentUser?.email.toString()
        FirebaseDatabase.getInstance().getReference().child("users").child(mAuth.currentUser?.uid.toString())
            .addValueEventListener(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {}

                override fun onDataChange(snapshot: DataSnapshot) {
                    nametv?.text = snapshot.child("name").value.toString()
                    agetv?.text = snapshot.child("age").value.toString()
                    heightv?.text = snapshot.child("height_in_cm").value.toString()
                    weighttv?.text = snapshot.child("weight_in_kg").value.toString()

                }

            })
    }

    fun editBtnClicked(view: View) {
        val intent = Intent(this, EditDetailsActivity::class.java)
        startActivity(intent)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        startActivity(intent)
    }
}