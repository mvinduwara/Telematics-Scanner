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
        Description desc = new Description();
        desc.setText("");
        lineChart.setDescription(desc);

        lineChart.setTouchEnabled(true);
        lineChart.setDragEnabled(true);
        lineChart.setScaleEnabled(true);
        lineChart.setDrawGridBackground(false);
        lineChart.setNoDataText("Loading vehicle telemetry...");

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
    }

    private void loadChartData() {
        new Thread(() -> {
            List<TelemetryLog> logs = AppDatabase.getInstance(requireContext())
                    .telemetryDao()
                    .getAllLogsChronological();

            if (logs == null || logs.isEmpty()) {
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), "Database empty. Generating Simulator Data!", Toast.LENGTH_LONG).show()
                );

                logs = new ArrayList<>();
                int simulatedRpm = 800;

                for (int i = 0; i < 50; i++) {
                    simulatedRpm += (int)(Math.random() * 600) - 200;
                    if (simulatedRpm < 600) simulatedRpm = 600;

                    logs.add(new TelemetryLog(System.currentTimeMillis() + (i * 1000), simulatedRpm, "90"));
                }
            }

            List<Entry> rpmEntries = new ArrayList<>();
            for (int i = 0; i < logs.size(); i++) {
                rpmEntries.add(new Entry(i, logs.get(i).engineRpm));
            }

            LineDataSet dataSet = new LineDataSet(rpmEntries, "Engine RPM");
            dataSet.setColor(Color.parseColor("#EF4444")); // Red line
            dataSet.setLineWidth(2f);
            dataSet.setDrawCircles(false);
            dataSet.setDrawValues(false);
            dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);

            dataSet.setDrawFilled(true);
            dataSet.setFillColor(Color.parseColor("#FCA5A5"));

            final int logSize = logs.size();
            LineData lineData = new LineData(dataSet);
            requireActivity().runOnUiThread(() -> {
                lineChart.setData(lineData);
                lineChart.setVisibleXRangeMaximum(100);
                lineChart.moveViewToX(logSize);
                lineChart.invalidate();
            });

        }).start();
    }
}