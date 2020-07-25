package com.sumiya.routeme.scenes.map

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.sumiya.routeme.R

interface MapActivityProtocol {

}

class MapActivity : AppCompatActivity(), MapActivityProtocol {

    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
}
