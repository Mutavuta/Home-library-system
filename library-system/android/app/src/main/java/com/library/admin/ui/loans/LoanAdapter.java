package com.library.admin.ui.loans;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.library.admin.R;
import com.library.admin.model.Loan;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

// Displays loans on the Loans screen - status badge color and due date
// color both change depending on whether the loan is active or overdue.
public class LoanAdapter extends RecyclerView.Adapter<LoanAdapter.ViewHolder> {

    private List<Loan> loans = new ArrayList<>();

    public void setLoans(List<Loan> newLoans) {
        this.loans = newLoans;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_loan, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Loan loan = loans.get(position);
        android.content.Context context = holder.itemView.getContext();

        holder.tvBorrowerName.setText(loan.borrowerName != null ? loan.borrowerName : "Unknown Borrower");
        holder.tvBookTitle.setText(loan.bookTitle != null ? loan.bookTitle : "Unknown Book");
        holder.tvBorrowerEmail.setText(loan.borrowerEmail != null ? loan.borrowerEmail : "");

        // Determine visually whether this loan is overdue right now, regardless
        // of the stored status string (which the scheduler only updates once daily)
        boolean isOverdue = "overdue".equals(loan.status)
                || (loan.dueDate != null && loan.returnDate == null
                && loan.dueDate.compareTo(LocalDate.now().toString()) < 0);

        if (isOverdue) {
            holder.tvStatus.setText(context.getString(R.string.label_overdue));
            holder.tvStatus.setBackgroundTintList(
                    ColorStateList.valueOf(ContextCompat.getColor(context, R.color.overdue_red)));
            holder.tvDueDate.setText(context.getString(R.string.label_due) + loan.dueDate);
            holder.tvDueDate.setTextColor(ContextCompat.getColor(context, R.color.overdue_red));
        } else {
            holder.tvStatus.setText(context.getString(R.string.label_active));
            holder.tvStatus.setBackgroundTintList(
                    ColorStateList.valueOf(ContextCompat.getColor(context, R.color.dark_green)));
            holder.tvDueDate.setText(context.getString(R.string.label_due) + loan.dueDate);
            holder.tvDueDate.setTextColor(ContextCompat.getColor(context, R.color.hint_white));
        }
    }

    @Override
    public int getItemCount() {
        return loans.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvBorrowerName, tvStatus, tvBookTitle, tvBorrowerEmail, tvDueDate;

        ViewHolder(View itemView) {
            super(itemView);
            tvBorrowerName  = itemView.findViewById(R.id.tvBorrowerName);
            tvStatus        = itemView.findViewById(R.id.tvStatus);
            tvBookTitle     = itemView.findViewById(R.id.tvBookTitle);
            tvBorrowerEmail = itemView.findViewById(R.id.tvBorrowerEmail);
            tvDueDate       = itemView.findViewById(R.id.tvDueDate);
        }
    }
}
