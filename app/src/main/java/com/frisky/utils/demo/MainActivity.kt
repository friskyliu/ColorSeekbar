package com.frisky.utils.demo

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.frisky.utils.ColorSeekBar

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val csb = findViewById<ColorSeekBar>(R.id.color_seekbar)
        findViewById<View>(R.id.btn_val).setOnClickListener {
            csb.selectedColor = Color.RED
        }
        findViewById<View>(R.id.btn_seeds).setOnClickListener {
            csb.colorSeeds = intArrayOf(
                Color.parseColor("#000000"),
                Color.parseColor("#FF5252"),
                Color.parseColor("#FFEB3B"),
                Color.parseColor("#00C853"),
                Color.parseColor("#00B0FF"),
                Color.parseColor("#D500F9"),
                Color.parseColor("#8D6E63"),
            )
        }
        val textview = findViewById<TextView>(R.id.tv_color)

        csb.listener = object : ColorSeekBar.OnColorChangeListener {
            override fun onColorSeekBarDown(color: Int) {
                textview.text = "onColorSeekBarDown"
                textview.setTextColor(color)
            }

            override fun onColorChanged(color: Int) {
                textview.text = "onColorChanged"
                textview.setTextColor(color)
            }

            override fun onColorSeekBarUp(color: Int) {
                textview.text = "onColorSeekBarUp"
                textview.setTextColor(color)
            }
        }
    }
}