package com.example.e_homepro;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

public class LightControlActivity extends AppCompatActivity {

    private BluetoothAdapter bluetoothAdapter = null;
    private BluetoothSocket bluetoothSocket = null;
    private OutputStream outputStream = null;

    // MAC Address of your HC-05 Bluetooth module
    private static final String DEVICE_ADDRESS = "00:23:02:35:0E:EB"; // Replace with your module's MAC address
    private static final UUID PORT_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb"); // Serial Port Service ID

    private static final int REQUEST_ENABLE_BLUETOOTH = 1;
    private static final int REQUEST_BLUETOOTH_PERMISSION = 2;

    private boolean connected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_light_control);

        // Initialize Bluetooth adapter
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            // Device doesn't support Bluetooth
            Toast.makeText(this, "Device doesn't support Bluetooth", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Check Bluetooth permissions
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
            // Request Bluetooth permissions
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH}, REQUEST_BLUETOOTH_PERMISSION);
        } else {
            // Bluetooth permissions granted, proceed with connection
            connectToBluetooth();
        }

        // Connect to Bluetooth button
        Button connectButton = findViewById(R.id.connectButton);
        connectButton.setOnClickListener(view -> {
            // Open system Bluetooth settings
            Intent intent = new Intent(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS);
            startActivity(intent);
        });

        Button onButton = findViewById(R.id.turnOnButton);
        onButton.setOnClickListener(view -> sendCommand("1"));
        Button offButton = findViewById(R.id.turnOffButton);
        offButton.setOnClickListener(view -> sendCommand("0"));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_BLUETOOTH_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Bluetooth permissions granted, proceed with connection
                connectToBluetooth();
            } else {
                // Permission denied, show a message and finish the activity
                Toast.makeText(this, "Bluetooth permissions are required for this feature", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    // Method to connect to the HC-05 Bluetooth module
    @SuppressLint("MissingPermission")
    private void connectToBluetooth() {
        // Check if Bluetooth is enabled
        if (!bluetoothAdapter.isEnabled()) {
            // Bluetooth is not enabled, open system settings to enable it
            Intent enableBluetoothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBluetoothIntent, REQUEST_ENABLE_BLUETOOTH);
            return;
        }

        // Get paired devices
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        if (pairedDevices.isEmpty()) {
            // No paired devices found
            Toast.makeText(this, "No paired devices found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Attempt Bluetooth connection in a separate thread
        new ConnectBluetoothTask().execute(pairedDevices);
    }

    // AsyncTask to handle Bluetooth connection
    private class ConnectBluetoothTask extends AsyncTask<Set<BluetoothDevice>, Void, Void> {

        @SuppressLint("MissingPermission")
        @Override
        protected Void doInBackground(Set<BluetoothDevice>... pairedDevices) {
            // Connect to the HC-05 Bluetooth module
            for (BluetoothDevice device : pairedDevices[0]) {
                if (device.getAddress().equals(DEVICE_ADDRESS)) {
                    try {
                        bluetoothSocket = device.createRfcommSocketToServiceRecord(PORT_UUID);
                        bluetoothSocket.connect();
                        connected = true;
                        runOnUiThread(() -> Toast.makeText(LightControlActivity.this, "Connected to HC-05", Toast.LENGTH_SHORT).show());
                    } catch (IOException e) {
                        e.printStackTrace();
                        connected = false;
                        runOnUiThread(() -> Toast.makeText(LightControlActivity.this, "Failed to connect to HC-05", Toast.LENGTH_SHORT).show());
                    }
                    break;
                }
            }
            return null;
        }
    }

    // Method to send command to Arduino
    private void sendCommand(String command) {
        if (connected && bluetoothSocket != null) { // Check if connected and socket is not null
            try {
                outputStream = bluetoothSocket.getOutputStream();
                outputStream.write(command.getBytes());
                // Add a newline character to the end of the command
                outputStream.write("\n".getBytes());
                // Flush the output stream to ensure the command is sent immediately
                outputStream.flush();
                // Optionally, close the output stream after sending the command
                // outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Failed to send command to Arduino", Toast.LENGTH_SHORT).
                        show();
            }
        }
    }
}
