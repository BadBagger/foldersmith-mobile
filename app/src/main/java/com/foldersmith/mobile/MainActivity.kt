package com.foldersmith.mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.foldersmith.mobile.ui.FolderSmithApp
import com.foldersmith.mobile.ui.theme.FolderSmithTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val repository = (application as FolderSmithApplication).repository
        setContent {
            FolderSmithTheme {
                FolderSmithApp(repository = repository)
            }
        }
    }
}
