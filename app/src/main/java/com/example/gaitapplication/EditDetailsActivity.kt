package com.example.gaitapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class EditDetailsActivity : AppCompatActivity() {
    var editprofileToolbar: Toolbar? = null
    var nametv: EditText? = null
    var agetv: EditText? = null
    var weighttv: EditText? = null
    var heightv: EditText? = null
    var emailtv: EditText? = null
    val mAuth = FirebaseAuth.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_details)
        editprofileToolbar = findViewById(R.id.editprofileToolbar)
        setSupportActionBar(editprofileToolbar)

        nametv = findViewById(R.id.nametv)
        agetv = findViewById(R.id.agetv)
        weighttv = findViewById(R.id.weighttv)
        heightv = findViewById(R.id.heighttv)
        emailtv = findViewById(R.id.emailtv)
    }

    override fun onStart() {
        super.onStart()
        emailtv?.setText(mAuth.currentUser?.email.toString())
        FirebaseDatabase.getInstance().getReference().child("users").child(mAuth.currentUser?.uid.toString())
            .addValueEventListener(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {}

                override fun onDataChange(snapshot: DataSnapshot) {
                    nametv?.setText(snapshot.child("name").value.toString())
                    agetv?.setText(snapshot.child("age").value.toString())
                    heightv?.setText(snapshot.child("height_in_cm").value.toString())
                    weighttv?.setText(snapshot.child("weight_in_kg").value.toString())

                }

            })
    }

    fun saveBtnClicked(view: View) {
        FirebaseDatabase.getInstance().getReference().child("users")
            .child(mAuth.currentUser?.uid.toString()).child("name").setValue(nametv?.text.toString())
        FirebaseDatabase.getInstance().getReference().child("users")
            .child(mAuth.currentUser?.uid.toString()).child("age").setValue(agetv?.text.toString())
        FirebaseDatabase.getInstance().getReference().child("users")
            .child(mAuth.currentUser?.uid.toString()).child("height_in_cm").setValue(heightv?.text.toString())
        FirebaseDatabase.getInstance().getReference().child("users")
            .child(mAuth.currentUser?.uid.toString()).child("weight_in_kg").setValue(weighttv?.text.toString())
        onBackPressed()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, MyProfileActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        startActivity(intent)
    }
}