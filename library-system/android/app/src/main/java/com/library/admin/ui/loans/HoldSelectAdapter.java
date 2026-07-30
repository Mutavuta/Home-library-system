package com.library.admin.ui.loans;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.library.admin.R;
import com.library.admin.model.Hold;

import java.util.ArrayList;
import java.util.List;

// Row adapter for the Select Hold popup - shown when confirming a loan.
// Tapping a row hands the chosen Hold back to SelectHoldDialog's listener.
public class HoldSelectAdapter extends RecyclerView.Adapter<HoldSelectAdapter.ViewHolder> {

    public interface OnHoldPickedListener {
        void onHoldPicked(Hold hold);
    }

    private List<Hold> holds = new ArrayList<>();
    private final OnHoldPickedListener listener;

    public HoldSelectAdapter(OnHoldPickedListener listener) {
        this.listener = listener;
    }

    public void setHolds(List<Hold> newHolds) {
        this.holds = newHolds;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_hold_select, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Hold hold = holds.get(position);
        holder.tvBorrowerName.setText(hold.borrowerName != null ? hold.borrowerName : "Unknown Borrower");
        holder.tvTitle.setText(hold.title);
        holder.tvAssignedBarcode.setText("Copy: " + hold.assignedBarcodeId);
        holder.itemView.setOnClickListener(v -> listener.onHoldPicked(hold));
    }

    @Override
    public int getItemCount() {
        return holds.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvBorrowerName, tvTitle, tvAssignedBarcode;

        ViewHolder(View itemView) {
            super(itemView);
            tvBorrowerName    = itemView.findViewById(R.id.tvBorrowerName);
            tvTitle           = itemView.findViewById(R.id.tvTitle);
            tvAssignedBarcode = itemView.findViewById(R.id.tvAssignedBarcode);
        }
    }
}
