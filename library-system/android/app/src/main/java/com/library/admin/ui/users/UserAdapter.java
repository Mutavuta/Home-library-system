package com.library.admin.ui.users;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.library.admin.R;
import com.library.admin.model.User;

import java.util.ArrayList;
import java.util.List;

// Displays users on the Users screen. Which action buttons show depends
// entirely on the user's own status:
// pending   -> Approve + Suspend
// approved  -> Suspend only
// suspended -> Reactivate + Delete
public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

    public interface OnUserActionListener {
        void onApprove(User user);
        void onSuspend(User user);
        void onReactivate(User user);
        void onDelete(User user);
    }

    private List<User> users = new ArrayList<>();
    private final OnUserActionListener listener;

    public UserAdapter(OnUserActionListener listener) {
        this.listener = listener;
    }

    public void setUsers(List<User> newUsers) {
        this.users = newUsers;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = users.get(position);

        holder.tvName.setText(user.fullName);
        holder.tvPhone.setText(user.phone != null ? user.phone : "");
        holder.tvEmail.setText(user.email);
        holder.tvDate.setText(user.createdAt);

        // Reset all four buttons hidden first, then show only what applies
        holder.btnApprove.setVisibility(View.GONE);
        holder.btnSuspend.setVisibility(View.GONE);
        holder.btnReactivate.setVisibility(View.GONE);
        holder.btnDelete.setVisibility(View.GONE);

        switch (user.status) {
            case "pending":
                holder.btnApprove.setVisibility(View.VISIBLE);
                holder.btnSuspend.setVisibility(View.VISIBLE);
                break;
            case "approved":
                holder.btnSuspend.setVisibility(View.VISIBLE);
                break;
            case "suspended":
                holder.btnReactivate.setVisibility(View.VISIBLE);
                holder.btnDelete.setVisibility(View.VISIBLE);
                break;
        }

        holder.btnApprove.setOnClickListener(v -> listener.onApprove(user));
        holder.btnSuspend.setOnClickListener(v -> listener.onSuspend(user));
        holder.btnReactivate.setOnClickListener(v -> listener.onReactivate(user));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(user));
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvPhone, tvEmail, tvDate;
        Button btnApprove, btnSuspend, btnReactivate, btnDelete;

        ViewHolder(View itemView) {
            super(itemView);
            tvName        = itemView.findViewById(R.id.tvName);
            tvPhone       = itemView.findViewById(R.id.tvPhone);
            tvEmail       = itemView.findViewById(R.id.tvEmail);
            tvDate        = itemView.findViewById(R.id.tvDate);
            btnApprove    = itemView.findViewById(R.id.btnApprove);
            btnSuspend    = itemView.findViewById(R.id.btnSuspend);
            btnReactivate = itemView.findViewById(R.id.btnReactivate);
            btnDelete     = itemView.findViewById(R.id.btnDelete);
        }
    }
}
