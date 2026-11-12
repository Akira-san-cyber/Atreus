package com.iistable.atreus
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.material3.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.iistable.atreus.ui.*
import com.iistable.atreus.ui.theme.AtreusTheme
import androidx.fragment.app.FragmentActivity

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        com.cactus.CactusContextInitializer.initialize(this)
        val app = application as AtreusApp
        setContent { 
            AtreusTheme { 
                Surface {
                    val vm: ChatViewModel = viewModel(factory = ChatViewModel.Factory(applicationContext, app.engine, app.timetable))
                    ChatScreen(vm = vm) 
                } 
            } 
        }
    }
}
