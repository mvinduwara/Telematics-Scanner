// app/src/main/java/com/example/telematicsscanner/fragments/DashboardFragment.java
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
import com.example.telematicsscanner.database.AppDatabase;
import com.example.telematicsscanner.database.TelemetryDao;
import com.example.telematicsscanner.database.TelemetryLog;

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

    private boolean isPolling = false; // Controls our data loop

    private static final int PERMISSION_REQUEST_CODE = 100;
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

        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return;
        }

        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        BluetoothDevice obdDevice = null;

        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                if (device.getName() != null && device.getName().toUpperCase().contains("OBD")) {
                    obdDevice = device;
                    break;
                }
            }
        }

        if (obdDevice == null) {
            Toast.makeText(getContext(), "No paired OBD-II scanner found.", Toast.LENGTH_LONG).show();
            tvStatus.setText("Disconnected");
            return;
        }

        connectToDevice(obdDevice);
    }

    private void connectToDevice(BluetoothDevice device) {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return;
        }

        tvStatus.setText("Connecting to " + device.getName() + "...");

        new Thread(() -> {
            try {
                bluetoothSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
                bluetoothAdapter.cancelDiscovery();
                bluetoothSocket.connect();

                inputStream = bluetoothSocket.getInputStream();
                outputStream = bluetoothSocket.getOutputStream();

                requireActivity().runOnUiThread(() -> {
                    tvStatus.setText("Connected to ECU");
                    tvStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                });

                // Phase 3 Starts Here!
                isPolling = true;
                startTelemetryLoop();

            } catch (IOException e) {
                requireActivity().runOnUiThread(() -> {
                    tvStatus.setText("Connection Failed");
                    tvStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                });
                try {
                    if (bluetoothSocket != null) bluetoothSocket.close();
                } catch (IOException closeException) {}
            }
        }).start();
    }

    // THE TELEMETRY LOOP
    private void startTelemetryLoop() {
        new Thread(() -> {
            try {
                // 1. Initialize ELM327 Hardware
                sendCommand("ATZ\r"); Thread.sleep(1000); // Reset
                sendCommand("ATE0\r"); Thread.sleep(500); // Echo Off
                sendCommand("ATSP0\r"); Thread.sleep(500); // Auto-detect protocol

                // 2. Initialize Local Database (Phase 4)
                TelemetryDao dao = AppDatabase.getInstance(requireContext()).telemetryDao();

                // 3. The Infinite Loop
                while (isPolling && bluetoothSocket.isConnected()) {
                    int currentRpm = -1;
                    int currentTemp = -100;

                    // --- Request RPM (Mode 01, PID 0C) ---
                    String rpmHex = sendCommand("01 0C\r");
                    if (rpmHex.contains("410C") && rpmHex.length() >= 8) {
                        try {
                            int a = Integer.parseInt(rpmHex.substring(4, 6), 16);
                            int b = Integer.parseInt(rpmHex.substring(6, 8), 16);
                            currentRpm = ((a * 256) + b) / 4;

                            final int finalRpm = currentRpm;
                            requireActivity().runOnUiThread(() -> tvRpm.setText(String.valueOf(finalRpm)));
                        } catch (Exception e){ /* ignore parse error */ }
                    }
                    Thread.sleep(100); // Small delay between hardware requests

                    // --- Request Coolant Temp (Mode 01, PID 05) ---
                    String tempHex = sendCommand("01 05\r");
                    if (tempHex.contains("4105") && tempHex.length() >= 6) {
                        try {
                            int a = Integer.parseInt(tempHex.substring(4, 6), 16);
                            currentTemp = a - 40; // OBD-II formula for Temp

                            final int finalTemp = currentTemp;
                            requireActivity().runOnUiThread(() -> tvTemp.setText(finalTemp + " °C"));
                        } catch (Exception e){ /* ignore parse error */ }
                    }

                    // --- Phase 4: Save to Offline Local Database ---
                    if (currentRpm != -1 && currentTemp != -100) {
                        TelemetryLog log = new TelemetryLog(System.currentTimeMillis(), currentRpm, String.valueOf(currentTemp));
                        dao.insert(log); // Writes to SQLite without blocking UI
                    }

                    // Wait half a second before polling again
                    Thread.sleep(400);
                }

            } catch (Exception e) {
                e.printStackTrace();
                isPolling = false;
            }
        }).start();
    }

    // Helper method to send a command and read the response cleanly
    private String sendCommand(String cmd) throws IOException {
        outputStream.write(cmd.getBytes());
        outputStream.flush();
        byte[] buffer = new byte[1024];
        int bytesRead = inputStream.read(buffer);
        // Removes spaces and the ">" prompt character
        return new String(buffer, 0, bytesRead).trim().replaceAll("\\s", "").replace(">", "");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isPolling = false;
        try {
            if (bluetoothSocket != null) bluetoothSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}