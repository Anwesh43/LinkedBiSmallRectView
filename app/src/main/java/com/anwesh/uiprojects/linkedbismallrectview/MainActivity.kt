package com.anwesh.uiprojects.linkedbismallrectview

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.anwesh.uiprojects.bismallrectlineview.BiSmallRectView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        BiSmallRectView.create(this)
    }
}
