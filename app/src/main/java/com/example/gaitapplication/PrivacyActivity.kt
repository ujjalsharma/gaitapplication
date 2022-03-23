package com.example.gaitapplication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.widget.Toolbar

class PrivacyActivity : AppCompatActivity() {

    var privacyToolbar: Toolbar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_privacy)
        privacyToolbar = findViewById(R.id.privacyToolbar)
        setSupportActionBar(privacyToolbar)
    }
}