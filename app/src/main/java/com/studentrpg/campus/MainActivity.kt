package com.studentrpg.campus

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.studentrpg.campus.ui.CampusRoute
import com.studentrpg.campus.ui.theme.CampusTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CampusTheme { CampusRoute() }
        }
    }
}
