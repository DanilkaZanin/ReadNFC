package com.example.readnfcjetpackcompose.viewmodel

import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.util.Log
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class NfcViewModel : ViewModel() {

    private val _statusText = MutableStateFlow("Нажмите кнопку 'Начать поиск'")
    val statusText: StateFlow<String> = _statusText.asStateFlow()

    private val _isScanningStatus = MutableStateFlow(false)
    val isScanningStatus: StateFlow<Boolean> = _isScanningStatus.asStateFlow()

    fun handleNfcIntent(intent: Intent?) {
        if (intent == null || !_isScanningStatus.value) return

        if (NfcAdapter.ACTION_TAG_DISCOVERED == intent.action ||
            NfcAdapter.ACTION_TECH_DISCOVERED == intent.action) {

            val tag: Tag? = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG)

            if (tag != null) {
                val uidBytes = tag.id
                val techListString = tag.techList.joinToString(", ")
                val uidHex = bytesToHexString(uidBytes)

                Log.d("NFC_READ", "Карта прочитана в ViewModel. UID: $uidHex")
                stopScanning()

                val message = "Карта прочитана!\nUID: $uidHex, \nсписок технологий: $techListString"
                _statusText.value = message
            }
        }
    }

    fun startScanning() {
        _isScanningStatus.value = true
        _statusText.value = "Готов к чтению! Поднесите карту..."
    }

    fun stopScanning() {
        _isScanningStatus.value = false
        _statusText.value = "Поиск остановлен. Нажмите 'Начать поиск'."
    }

    fun setNfcUnavailable() {
        _statusText.value = "Это устройство не поддерживает NFC."
        _isScanningStatus.value = false
    }

    private fun bytesToHexString(src: ByteArray): String {
        return src.joinToString("") { "%02X".format(it) }
    }
}