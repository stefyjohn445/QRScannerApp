package com.example.qrscannerapplication
// BluetoothManager.kt
import android.Manifest
import android.app.Activity
import android.bluetooth.*
import android.content.Context
import android.content.pm.PackageManager
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.core.app.ActivityCompat
import java.util.*

interface BluetoothPermissionCallback {
    fun onRequestPermissionsResult(requestCode: Int, grantResults: IntArray)
}
class BluetoothManager(private val context: Context, private val permissionCallback: BluetoothPermissionCallback) {

    private val handler: Handler = Handler(Looper.getMainLooper())
    private var bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private var bluetoothGatt: BluetoothGatt? = null
    private val BLUETOOTH_PERMISSION_REQUEST_CODE = 100
    private var deviceAddress: String? = null // Store the device address
    fun connectToDevice(deviceAddress: String) {
        this.deviceAddress = deviceAddress // Store the device address
        val device = bluetoothAdapter?.getRemoteDevice(deviceAddress)
        device?.let {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    context as Activity,  // Make sure your context is an instance of Activity
                    arrayOf(Manifest.permission.BLUETOOTH_CONNECT),
                    BLUETOOTH_PERMISSION_REQUEST_CODE
                )
            } else {
                // Bluetooth permission is already granted, proceed with connection
//                bluetoothGatt = device.connectGatt(context, false, gattCallback)
                connectGatt(device)
            }
        }
    }

    // Override onRequestPermissionsResult in your Activity or Fragment
    fun onRequestPermissionsResult(
        requestCode: Int,
        grantResults: IntArray
    ) {
        when (requestCode) {
            BLUETOOTH_PERMISSION_REQUEST_CODE -> {
                val granted = grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
//                permissionCallback.onRequestPermissionsResult(granted)
                permissionCallback.onRequestPermissionsResult(
                    BLUETOOTH_PERMISSION_REQUEST_CODE,
                    if (granted) intArrayOf(PackageManager.PERMISSION_GRANTED)
                    else intArrayOf(PackageManager.PERMISSION_DENIED)
                )

                if (granted) {

                    // Bluetooth permission granted, proceed with connection
                    deviceAddress?.let { address ->
                        val device = bluetoothAdapter?.getRemoteDevice(address)
                        device?.let {
                            connectGatt(device)
                        }
                    }
                } else {
                    // Bluetooth permission denied, handle accordingly (e.g., show a message)
                    Toast.makeText(context, "Bluetooth permission denied", Toast.LENGTH_SHORT)
                        .show()
                }

            }
            // Handle other permission requests if needed
        }
    }
    private fun connectGatt(device: BluetoothDevice) {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        bluetoothGatt = device.connectGatt(context, false, gattCallback)
    }


    fun sendQRCodeText(qrCodeText: String) {
        bluetoothGatt?.let { gatt ->
            val characteristic = getWritableCharacteristic()
            characteristic?.value = qrCodeText.toByteArray()
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }
            gatt.writeCharacteristic(characteristic)
        }
    }

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                if (ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.BLUETOOTH_CONNECT
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return
                }
                gatt.discoverServices()
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            // Handle service discovery if needed
        }
    }

    private fun getWritableCharacteristic(): BluetoothGattCharacteristic? {
        val serviceUUID = UUID.fromString("your_service_uuid")
        val characteristicUUID = UUID.fromString("your_characteristic_uuid")

        val service = bluetoothGatt?.getService(serviceUUID)
        return service?.getCharacteristic(characteristicUUID)
    }
}
