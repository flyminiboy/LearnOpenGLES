package com.example.learnopengles

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.learnopengles.databinding.ActivityHellotriangleBinding
import com.example.learnopengles.databinding.ActivityMatrixBinding

class MatrixActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        with(ActivityMatrixBinding.inflate(layoutInflater)) {
            setContentView(root)
        }
    }
}