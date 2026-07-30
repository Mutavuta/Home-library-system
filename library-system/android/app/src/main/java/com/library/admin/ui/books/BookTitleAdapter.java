package com.library.admin.ui.books;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.library.admin.R;
import com.library.admin.model.BookTitle;

import java.util.ArrayList;
import java.util.List;

// Displays the list of catalog titles on the Books screen.
// Each row shows title, author, category badge, and copy counts.
public class BookTitleAdapter extends RecyclerView.Adapter<BookTitleAdapter.ViewHolder> {

    private List<BookTitle> titles = new ArrayList<>();

    // Replaces the current list and redraws the RecyclerView
    // Called both on initial load and after a search filter changes
    public void setTitles(List<BookTitle> newTitles) {
        this.titles = newTitles;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_book_title, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BookTitle title = titles.get(position);

        holder.tvTitle.setText(title.title);
        holder.tvAuthor.setText("By " + title.author);
        holder.tvCategory.setText(capitalize(title.category));

        // Builds the title stats for the summary line
        String counts = title.totalCopies + " Copies    "
                + title.availableCopies + " Available    "
                + title.loanedCopies + " Loaned    "
                + title.reservedCopies + " Reserved";
        holder.tvCounts.setText(counts);
    }

    @Override
    public int getItemCount() {
        return titles.size();
    }

    private String capitalize(String text) {
        if (text == null || text.isEmpty()) return "";
        return Character.toUpperCase(text.charAt(0)) + text.substring(1);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvAuthor, tvCategory, tvCounts;

        ViewHolder(View itemView) {
            super(itemView);
            tvTitle    = itemView.findViewById(R.id.tvTitle);
            tvAuthor   = itemView.findViewById(R.id.tvAuthor);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvCounts   = itemView.findViewById(R.id.tvCounts);
        }
    }


}
