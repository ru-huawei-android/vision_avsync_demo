package com.kkostrov.avsyncissuedemo

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.kkostrov.avsyncissuedemo.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    private lateinit var startPlayer: View.OnClickListener

    private val viewBinding by lazy(LazyThreadSafetyMode.NONE) {
        ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)

        startPlayer = View.OnClickListener {
            val myIntent = Intent(this, PlayerActivity::class.java)
            myIntent.putExtra("alternative", it.id == R.id.custom_player_button)
            this.startActivity(myIntent)
        }

        viewBinding.customPlayerButton.setOnClickListener(startPlayer)
        viewBinding.standardPlayerButton.setOnClickListener(startPlayer)
    }
}