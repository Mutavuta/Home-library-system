package com.library.admin.ui.holds;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.library.admin.R;
import com.library.admin.model.Hold;

import java.util.ArrayList;
import java.util.List;

// Displays hold requests. Which action buttons show depends on the hold's
// own status, regardless of which tab (Pending/Approved/All) is currently open.
public class HoldAdapter extends RecyclerView.Adapter<HoldAdapter.ViewHolder> {

    public interface OnHoldActionListener {
        void onApprove(Hold hold);
        void onExpire(Hold hold);
    }

    private List<Hold> holds = new ArrayList<>();
    private final OnHoldActionListener listener;

    public HoldAdapter(OnHoldActionListener listener) {
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
                .inflate(R.layout.item_hold, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Hold hold = holds.get(position);

        holder.tvBorrowerName.setText(hold.borrowerName != null ? hold.borrowerName : "Unknown Borrower");
        holder.tvDate.setText(hold.requestDate);
        holder.tvTitle.setText(hold.title);

        if (TextUtils.isEmpty(hold.assignedBarcodeId)) {
            holder.tvAssigned.setText(R.string.label_no_copy_assigned);
        } else {
            holder.tvAssigned.setText(holder.itemView.getContext()
                    .getString(R.string.label_copy) + hold.assignedBarcodeId);
        }

        // Button visibility depends purely on this hold's own status
        switch (hold.status) {
            case "pending":
                holder.btnApprove.setVisibility(View.VISIBLE);
                holder.btnExpire.setVisibility(View.VISIBLE);
                break;
            case "approved":
                holder.btnApprove.setVisibility(View.GONE);
                holder.btnExpire.setVisibility(View.VISIBLE);
                break;
            default: // collected, abandoned - terminal states, no actions left
                holder.btnApprove.setVisibility(View.GONE);
                holder.btnExpire.setVisibility(View.GONE);
        }

        holder.btnApprove.setOnClickListener(v -> listener.onApprove(hold));
        holder.btnExpire.setOnClickListener(v -> listener.onExpire(hold));
    }

    @Override
    public int getItemCount() {
        return holds.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvBorrowerName, tvDate, tvTitle, tvAssigned;
        Button btnApprove, btnExpire;

        ViewHolder(View itemView) {
            super(itemView);
            tvBorrowerName = itemView.findViewById(R.id.tvBorrowerName);
            tvDate         = itemView.findViewById(R.id.tvDate);
            tvTitle        = itemView.findViewById(R.id.tvTitle);
            tvAssigned     = itemView.findViewById(R.id.tvAssigned);
            btnApprove     = itemView.findViewById(R.id.btnApprove);
            btnExpire      = itemView.findViewById(R.id.btnExpire);
        }
    }
}
