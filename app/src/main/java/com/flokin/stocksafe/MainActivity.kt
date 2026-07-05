package com.flokin.stocksafe

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize
import com.flokin.stocksafe.ui.theme.StockSafeTheme
import com.flokin.stocksafe.util.SecurityUtils
import com.flokin.stocksafe.navigation.AppNavGraph

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ================================================
        // FLAG_SECURE: Mencegah screenshot dan screen recording
        // Data stok dan harga adalah informasi sensitif bisnis
        // ================================================
        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )

        enableEdgeToEdge()

        setContent {
            StockSafeTheme {

                // ================================================
                // ROOT DETECTION: Periksa keamanan device
                // ================================================
                val isRooted        = remember { SecurityUtils.isDeviceRooted() }
                var userAcknowledged by remember { mutableStateOf(!isRooted) }

                if (!userAcknowledged) {
                    AlertDialog(
                        // Tidak bisa di-dismiss dengan klik luar dialog
                        onDismissRequest = {},
                        title = { Text("⚠️ Peringatan Keamanan") },
                        text  = {
                            Text(
                                "Device ini terdeteksi telah di-root atau " +
                                "dimodifikasi.\n\n" +
                                "Data stok dan informasi bisnis sensitif " +
                                "tidak dapat dijamin keamanannya di device " +
                                "yang di-root.\n\n" +
                                "Sangat disarankan untuk tidak menggunakan " +
                                "aplikasi ini di device yang di-root."
                            )
                        },
                        confirmButton = {
                            TextButton(
                                onClick = { userAcknowledged = true },
                                colors  = ButtonDefaults.textButtonColors(
                                    contentColor = MaterialTheme.colorScheme.error
                                )
                            ) {
                                Text("Lanjutkan (Risiko Sendiri)")
                            }
                        },
                        dismissButton = {
                            Button(onClick = { finish() }) {
                                Text("Keluar dari Aplikasi")
                            }
                        }
                    )
                } else {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color    = MaterialTheme.colorScheme.background
                    ) {
                        // AppNavGraph akan ditambahkan di Bagian berikutnya
                        AppNavGraph()
                    }
                }
            }
        }
    }
}