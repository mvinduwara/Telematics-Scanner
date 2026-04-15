package com.example.telematicsscanner.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.telematicsscanner.model.DiagnosticCode;
import com.example.telematicsscanner.R;
import com.example.telematicsscanner.adapter.DiagnosticAdapter;

import java.util.ArrayList;
import java.util.List;

public class DiagnosticsFragment extends Fragment {

    private RecyclerView recyclerView;
    private DiagnosticAdapter adapter;
    private List<DiagnosticCode> codeList;
    private Button btnScan, btnClear;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_diagnostics, container, false);

        recyclerView = view.findViewById(R.id.recycler_view_diagnostics);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        btnScan = view.findViewById(R.id.btn_scan_codes);
        btnClear = view.findViewById(R.id.btn_clear_codes);

        codeList = new ArrayList<>();
        adapter = new DiagnosticAdapter(codeList);
        recyclerView.setAdapter(adapter);

        btnScan.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Requesting codes from ECU...", Toast.LENGTH_SHORT).show();
            simulateReceivingCodes();
        });

        btnClear.setOnClickListener(v -> {
            codeList.clear();
            adapter.notifyDataSetChanged();
            Toast.makeText(getContext(), "Check Engine Light reset command sent.", Toast.LENGTH_SHORT).show();
        });

        return view;
    }

    private void simulateReceivingCodes() {
        codeList.clear();
        codeList.add(new DiagnosticCode("P0171", "System Too Lean (Bank 1)"));
        codeList.add(new DiagnosticCode("P0300", "Random/Multiple Cylinder Misfire Detected"));
        codeList.add(new DiagnosticCode("P0420", "Catalyst System Efficiency Below Threshold (Bank 1)"));
        adapter.notifyDataSetChanged();
    }
}
