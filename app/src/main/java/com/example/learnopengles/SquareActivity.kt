package com.example.learnopengles

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.learnopengles.databinding.ActivityHellotriangleBinding
import com.example.learnopengles.databinding.ActivitySquareBinding

class SquareActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        with(ActivitySquareBinding.inflate(layoutInflater)) {
            setContentView(root)
        }
    }
}