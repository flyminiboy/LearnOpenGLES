package com.example.learnopengles

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.learnopengles.databinding.ActivityMainBinding
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {

    val mainHandler = Handler(Looper.getMainLooper())
    private fun postWaitForIdle() {
        mainHandler.post {
            logE("站的等你三千年")
            waitForIdle()
        }
    }

    private fun waitForIdle() {
        Looper.myQueue().addIdleHandler {
            logE("我草没事了，干会啥呢")
            postWaitForIdle()
            false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        with(ActivityMainBinding.inflate(layoutInflater)){
            setContentView(root)

//            waitForIdle()



            mainHellotriangle.setOnClickListener {
                startActivity(Intent(this@MainActivity, HellotriangleActivity::class.java))
            }
            mainHellotexture.setOnClickListener {
                startActivity(Intent(this@MainActivity, TextureActivity::class.java))
            }
            mainHellosquare.setOnClickListener {
                startActivity(Intent(this@MainActivity, SquareActivity::class.java))
            }
            mainHellocamera.setOnClickListener {
                startActivity(Intent(this@MainActivity, CameraActivity::class.java))
            }

        }
    }
}