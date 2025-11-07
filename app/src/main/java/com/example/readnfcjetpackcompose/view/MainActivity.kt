package com.example.readnfcjetpackcompose.view

import android.app.PendingIntent
import android.content.Intent
import android.nfc.NfcAdapter
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

import com.example.readnfcjetpackcompose.ui.theme.ReadNFCJetpackComposeTheme
import com.example.readnfcjetpackcompose.viewmodel.NfcViewModel

class MainActivity : ComponentActivity() {

    private val nfcViewModel: NfcViewModel by viewModels()

    private var nfcAdapter: NfcAdapter? = null
    private lateinit var pendingIntent: PendingIntent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        if (nfcAdapter == null) {
            nfcViewModel.setNfcUnavailable()
        }

        val intent = Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE)

        setContent {
            ReadNFCJetpackComposeTheme {
                val isScanning by nfcViewModel.isScanningStatus.collectAsState()

                LaunchedEffect(isScanning) {
                    if (isScanning) {
                        enableNfcForegroundDispatch()
                    } else {
                        disableNfcForegroundDispatch()
                    }
                }

                NfcStatusScreen(viewModel = nfcViewModel)
            }
        }
        nfcViewModel.handleNfcIntent(intent)
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        Log.d("NFC_DEBUG", "onNewIntent вызван в Activity, передача в VM.")
        nfcViewModel.handleNfcIntent(intent)
    }

    private fun enableNfcForegroundDispatch() {
        if (!isFinishing && !isDestroyed) {
            nfcAdapter?.enableForegroundDispatch(this, pendingIntent, null, null)
            Log.d("NFC_DISPATCH", "Foreground Dispatch ENABLED")
        }
    }

    private fun disableNfcForegroundDispatch() {
        if (!isFinishing && !isDestroyed) {
            nfcAdapter?.disableForegroundDispatch(this)
            Log.d("NFC_DISPATCH", "Foreground Dispatch DISABLED")
        }
    }
}

@Composable
fun NfcStatusScreen(viewModel: NfcViewModel) {
    val status by viewModel.statusText.collectAsState()
    val isScanning by viewModel.isScanningStatus.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = status,
        )

        Button(onClick = {
            if (isScanning) {
                viewModel.stopScanning()
            } else {
                viewModel.startScanning()
            }
        }) {
            Text(text = if (isScanning) "Сбросить поиск" else "Начать поиск NFC")
        }
    }
}