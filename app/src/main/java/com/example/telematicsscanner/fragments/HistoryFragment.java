// app/src/main/java/com/example/telematicsscanner/fragments/HistoryFragment.java
package com.example.telematicsscanner.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.telematicsscanner.R;
import com.example.telematicsscanner.database.AppDatabase;
import com.example.telematicsscanner.database.TelemetryLog;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
import java.util.List;

public class HistoryFragment extends Fragment {

    private LineChart lineChart;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);
        lineChart = view.findViewById(R.id.line_chart_rpm);

        setupChartAppearance();
        loadChartData();

        return view;
    }

    private void setupChartAppearance() {
        // Removes the description text at the bottom right
        Description desc = new Description();
        desc.setText("");
        lineChart.setDescription(desc);

        // Make the chart interactive
        lineChart.setTouchEnabled(true);
        lineChart.setDragEnabled(true);
        lineChart.setScaleEnabled(true);
        lineChart.setDrawGridBackground(false);

        // Clean up the X-Axis
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
    }

    private void loadChartData() {
        // Database queries must run on a background thread
        new Thread(() -> {
            // 1. Get the data from SQLite
            List<TelemetryLog> logs = AppDatabase.getInstance(requireContext())
                    .telemetryDao()
                    .getAllLogsChronological();

            if (logs == null || logs.isEmpty()) {
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), "No driving history found yet.", Toast.LENGTH_SHORT).show()
                );
                return;
            }

            // 2. Convert Data into Chart "Entries"
            List<Entry> rpmEntries = new ArrayList<>();
            for (int i = 0; i < logs.size(); i++) {
                // X = time (index), Y = RPM value
                rpmEntries.add(new Entry(i, logs.get(i).engineRpm));
            }

            // 3. Style the Line
            LineDataSet dataSet = new LineDataSet(rpmEntries, "Engine RPM");
            dataSet.setColor(Color.parseColor("#EF4444")); // Red line
            dataSet.setLineWidth(2f);
            dataSet.setDrawCircles(false); // Disable dots for a smooth line
            dataSet.setDrawValues(false); // Hide the numbers on the line
            dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER); // Make it smooth and curvy

            // Fill underneath the line
            dataSet.setDrawFilled(true);
            dataSet.setFillColor(Color.parseColor("#FCA5A5"));

            // 4. Send to the UI Thread to draw
            LineData lineData = new LineData(dataSet);
            requireActivity().runOnUiThread(() -> {
                lineChart.setData(lineData);
                // Zooms in automatically if you have thousands of data points
                lineChart.setVisibleXRangeMaximum(100);
                lineChart.moveViewToX(logs.size()); // Scroll to the newest data on the right
                lineChart.invalidate(); // Refresh the chart
            });

        }).start();
    }
}