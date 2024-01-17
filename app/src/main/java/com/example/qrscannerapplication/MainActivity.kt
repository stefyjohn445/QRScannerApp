package com.example.qrscannerapplication

import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.Manifest
import android.content.Intent
import android.widget.TextView
import android.widget.Toast
import com.google.zxing.Result
import me.dm7.barcodescanner.zxing.ZXingScannerView
import java.util.regex.Pattern

class MainActivity : AppCompatActivity(), ZXingScannerView.ResultHandler,  BluetoothPermissionCallback {

    private val CAMERA_PERMISSION_REQUEST = 100
    private lateinit var scannerView: ZXingScannerView
    private lateinit var scannerText: TextView
    private lateinit var bluetoothManager: BluetoothManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bluetoothManager = BluetoothManager(this, this)

//        scannerView = ZXingScannerView(this)
        scannerView = findViewById(R.id.scannerView)
        val editTextSSID = findViewById<EditText>(R.id.editTextSSID)
        val editTextPassword = findViewById<EditText>(R.id.editTextPassword)
        val btnLog = findViewById<Button>(R.id.btnLog)
        val btnConfigure = findViewById<Button>(R.id.btnConfigure)
        scannerText = findViewById<TextView>(R.id.ScannerText)

        btnLog.setOnClickListener {
            // Handle LOG button click
            // Add your logic here
            val intent = Intent(this, LogActivity::class.java)
            startActivity(intent)
        }

        btnConfigure.setOnClickListener {
            // Handle Configure button click
            // Add your logic here
            val intent = Intent(this, ConfigureActivity::class.java)
            startActivity(intent)
        }

// Retrieve values
        val ssid = editTextSSID.text.toString()
        val password = editTextPassword.text.toString()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.CAMERA), 1)
            }
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 2)
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (checkSelfPermission(Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.BLUETOOTH), 3)
            }
            if (checkSelfPermission(Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.BLUETOOTH_ADMIN), 4)
            }
        }

    }

    override fun onResume() {
        super.onResume()
        scannerView.setResultHandler(this)
        scannerView.startCamera()
    }

    override fun onPause() {
        super.onPause()
        scannerView.stopCamera()
    }

    override fun handleResult(result: Result?) {
        result?.let {
//            val ssid = extractSSIDFromQRCode(result.text)
            val ssid = result.text
            if (ssid.isNotEmpty()) {
                // Do something with the SSID
                // You might want to display it in a TextView or perform further actions
                // For now, let's print it to log

//                val extractedSSID = extractSSIDFromQRCode(ssid)

                // Display the extracted SSID in editTextSSID
                val editTextSSID = findViewById<EditText>(R.id.editTextSSID)
                val editTextPassword = findViewById<EditText>(R.id.editTextPassword)
//                editTextSSID.setText(extractedSSID)
//                editTextSSID.setText(extractedSSID)

                Toast.makeText(this, "SSID: $ssid", Toast.LENGTH_SHORT).show()
                println("SSID from QR Code: $ssid")
                scannerText.text = ssid
                editTextPassword.setText("copernicus")

                // Connect to BLE device and send QR code text
//                bluetoothManager.connectToDevice("your_ble_device_address")
                bluetoothManager.sendQRCodeText(ssid)

            } else {
                println("SSID empty")
                // Handle empty SSID
            }

            // Resume scanning
            scannerView.resumeCameraPreview(this)
        }
    }

    private fun extractSSIDFromQRCode(qrCodeText: String): String {
        // Use regex to find the pattern after the last '/'
//        val regex = Regex("/([^/]+)\$")
//        val matchResult = regex.find(qrCodeText)
//
//        // Extract the matched group or return an empty string if not found
//        return matchResult?.groupValues?.getOrNull(1) ?: ""

//        val pattern = Pattern.compile("/([A-Z]+)\\d+/\\d+[A-Z]$")
//        val matcher = pattern.matcher(qrCodeText)

        val secondLastSlashIndex = qrCodeText.lastIndexOf("/", qrCodeText.lastIndexOf("/") - 1)
        val targetString = qrCodeText.substring(secondLastSlashIndex - 5, secondLastSlashIndex)

        println(targetString)
        return targetString
//        return if (matcher.find()) {
//            matcher.group(1)
//        } else {
//            "pmkg"
//        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, grantResults: IntArray) {
        bluetoothManager.onRequestPermissionsResult(requestCode, grantResults)
    }
}