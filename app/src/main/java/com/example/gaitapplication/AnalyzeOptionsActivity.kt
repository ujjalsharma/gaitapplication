package com.example.gaitapplication

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast

class AnalyzeOptionsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_analyze_options)
    }

    fun cnnImgBtnClicked(view: View) {
        val intent = Intent(this, AnalyzeSEIActivity::class.java)
        startActivity(intent)

    }
    fun cnnvidBtnClicked(view: View) {
        val intent = Intent(this, AnalyzeVideoActivity::class.java)
        startActivity(intent)

    }
    fun lstmVidBtnClicked(view: View) {
        Toast.makeText(this, "Developement in progress!", Toast.LENGTH_SHORT).show()

    }
}