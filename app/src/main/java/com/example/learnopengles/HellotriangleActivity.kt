package com.example.learnopengles

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.learnopengles.databinding.ActivityHellotriangleBinding

class HellotriangleActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        with(ActivityHellotriangleBinding.inflate(layoutInflater)) {
            setContentView(root)
        }
    }
}