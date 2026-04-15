package com.example.telematicsscanner.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.telematicsscanner.model.DiagnosticCode;
import com.example.telematicsscanner.R;

import java.util.List;

public class DiagnosticAdapter extends RecyclerView.Adapter<DiagnosticAdapter.ViewHolder> {

    private List<DiagnosticCode> codeList;

    public DiagnosticAdapter(List<DiagnosticCode> codeList) {
        this.codeList = codeList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_diagnostic_code, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DiagnosticCode currentCode = codeList.get(position);
        holder.tvCode.setText(currentCode.getCode());
        holder.tvDesc.setText(currentCode.getDescription());
    }

    @Override
    public int getItemCount() {
        return codeList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView tvCode;
        public TextView tvDesc;

        public ViewHolder(View itemView) {
            super(itemView);
            tvCode = itemView.findViewById(R.id.tv_fault_code);
            tvDesc = itemView.findViewById(R.id.tv_fault_desc);
        }
    }
}
