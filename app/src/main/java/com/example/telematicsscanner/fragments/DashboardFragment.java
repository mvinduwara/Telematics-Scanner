
package com.example.telematicsscanner.fragments;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.telematicsscanner.R;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

public class DashboardFragment extends Fragment {

    private TextView tvStatus, tvRpm, tvTemp;
    private Button btnConnect;

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket bluetoothSocket;
    private InputStream inputStream;
    private OutputStream outputStream;

    private static final int PERMISSION_REQUEST_CODE = 100;
    // This is the standard UUID for Serial Port Profile (SPP) devices like OBD-II scanners
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        tvStatus = view.findViewById(R.id.tv_connection_status);
        tvRpm = view.findViewById(R.id.tv_rpm);
        tvTemp = view.findViewById(R.id.tv_temp);
        btnConnect = view.findViewById(R.id.btn_connect_obd);

        BluetoothManager bluetoothManager = (BluetoothManager) requireContext().getSystemService(Context.BLUETOOTH_SERVICE);
        if (bluetoothManager != null) {
            bluetoothAdapter = bluetoothManager.getAdapter();
        }

        btnConnect.setOnClickListener(v -> {
            if (bluetoothAdapter == null) {
                Toast.makeText(getContext(), "Device does not support Bluetooth", Toast.LENGTH_SHORT).show();
                return;
            }
            checkPermissionsAndConnect();
        });

        return view;
    }

    private void checkPermissionsAndConnect() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(requireActivity(),
                        new String[]{Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN},
                        PERMISSION_REQUEST_CODE);
                return;
            }
        } else {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(requireActivity(),
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        PERMISSION_REQUEST_CODE);
                return;
            }
        }
        startBluetoothSetup();
    }

    private void startBluetoothSetup() {
        if (!bluetoothAdapter.isEnabled()) {
            Toast.makeText(getContext(), "Please turn on Bluetooth first!", Toast.LENGTH_LONG).show();
            return;
        }

        tvStatus.setText("Scanning Paired Devices...");
        tvStatus.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));

        // Get all devices currently paired to the phone
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return;
        }

        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        BluetoothDevice obdDevice = null;

        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                // OBD-II scanners usually have "OBD" in their name.
                if (device.getName() != null && device.getName().toUpperCase().contains("OBD")) {
                    obdDevice = device;
                    break;
                }
            }
        }

        if (obdDevice == null) {
            Toast.makeText(getContext(), "No paired OBD-II scanner found. Please pair it in Android Bluetooth settings.", Toast.LENGTH_LONG).show();
            tvStatus.setText("Disconnected");
            tvStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            return;
        }

        // If we found it, try to connect!
        connectToDevice(obdDevice);
    }

    private void connectToDevice(BluetoothDevice device) {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return;
        }

        tvStatus.setText("Connecting to " + device.getName() + "...");

        // Network and Bluetooth connections MUST be done on a background thread in Android
        new Thread(() -> {
            try {
                // Create the socket
                bluetoothSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
                bluetoothAdapter.cancelDiscovery(); // Cancelling discovery speeds up the connection

                // Attempt to connect to the hardware
                bluetoothSocket.connect();

                // If successful, grab the data streams
                inputStream = bluetoothSocket.getInputStream();
                outputStream = bluetoothSocket.getOutputStream();

                // Update the UI on the main thread
                requireActivity().runOnUiThread(() -> {
                    tvStatus.setText("Connected to ECU");
                    tvStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                    Toast.makeText(getContext(), "Hardware Linked Successfully!", Toast.LENGTH_SHORT).show();
                });

            } catch (IOException e) {
                // If the connection fails
                requireActivity().runOnUiThread(() -> {
                    tvStatus.setText("Connection Failed");
                    tvStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                    Toast.makeText(getContext(), "Ensure car is ON and scanner is plugged in.", Toast.LENGTH_LONG).show();
                });
                try {
                    if (bluetoothSocket != null) bluetoothSocket.close();
                } catch (IOException closeException) {
                    // Ignore
                }
            }
        }).start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Always close the socket when closing the app to prevent battery drain
        try {
            if (bluetoothSocket != null) bluetoothSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}