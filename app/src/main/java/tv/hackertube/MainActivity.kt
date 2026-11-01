package tv.hackertube

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val library = Library(this)
        setContent {
            HackerTubeTheme { App(library) }
        }
    }
}
