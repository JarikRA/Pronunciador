package com.jackanota.pronunciador

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast

class MainActivity : AppCompatActivity() {

    private lateinit var etText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        etText = findViewById(R.id.et_text)
    }

    fun next(view: View){
        val text = etText.text.toString().trim()

        if(text.isEmpty()){
            Toast.makeText(this, "Primero ingresa el texto", Toast.LENGTH_SHORT).show()
            return
        }

        val intent = Intent(this, EvaluatorActivity::class.java)
        intent.putExtra("text",text)
        startActivity(intent)
    }
}