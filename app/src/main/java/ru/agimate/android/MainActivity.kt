package ru.agimate.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import org.koin.android.ext.android.inject
import ru.agimate.android.service.TriggerServiceManager
import ru.agimate.android.ui.components.BottomNavigationBar
import ru.agimate.android.ui.components.ServiceStatusBanner
import ru.agimate.android.ui.navigation.NavGraph
import ru.agimate.android.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private val triggerServiceManager: TriggerServiceManager by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                AgimateApp(triggerServiceManager)
            }
        }
    }
}

@Composable
fun AgimateApp(
    triggerServiceManager: TriggerServiceManager
) {
    val navController = rememberNavController()
    val isServiceRunning by triggerServiceManager.isServiceRunning.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            BottomNavigationBar(navController = navController)
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            ServiceStatusBanner(isRunning = isServiceRunning)
            NavGraph(
                navController = navController,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
