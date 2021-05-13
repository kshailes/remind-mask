package com.example.coronasafety

import android.os.Bundle
import android.os.PersistableBundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatCheckBox

class ListActivity: AppCompatActivity()  {

    private lateinit var actionTextView: AppCompatCheckBox
    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
        setContentView(R.layout.activity_list)
        actionTextView = findViewById(R.id.tv_action)

        val type = intent?.extras?.get("flag") ?: 0

        if(type == 1) {
            actionTextView.setText("Wash Your hands")
        }
        else if(type == 2) {
            actionTextView.setText("Take Your MASK")
        }
        else {
            actionTextView.setText("Do Something bro")
        }

    }
}