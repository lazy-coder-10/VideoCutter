package com.videocutter

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.videocutter.photoeditor.EditImageActivity
import com.videocutter.videodub_addsound.VideoDubRecord_Activity_new

class SelectActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_type)

        findViewById<Button>(R.id.idIvImage).setOnClickListener {
            startActivity(Intent(this,
                    EditImageActivity::class.java))
        }

        findViewById<Button>(R.id.idIvVideo).setOnClickListener {
          startActivity(Intent(this,
                    VideoDubRecord_Activity_new::class.java))
        }
    }

}