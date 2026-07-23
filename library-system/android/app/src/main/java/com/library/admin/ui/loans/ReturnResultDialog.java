package com.library.admin.ui.loans;

import android.app.Dialog;
import android.content.Context;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.library.admin.R;
import com.library.admin.model.Book;

// Final confirmation popup shown after a successful return.
// Note: the backend's return response doesn't indicate whether a waitlisted
// borrower was notified, so that line is left out rather than guessed at.
public class ReturnResultDialog extends Dialog {

    private final Book book;
    private final String borrowerName;

    public ReturnResultDialog(@NonNull Context context, Book book, String borrowerName) {
        super(context);
        this.book = book;
        this.borrowerName = borrowerName;
    }

    @Override
    protected void onCreate(android.os.Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_return_result);

        ((TextView) findViewById(R.id.tvBookTitle)).setText(book.title);
        ((TextView) findViewById(R.id.tvBorrowerName)).setText("Returned by " + borrowerName);

        findViewById(R.id.btnClose).setOnClickListener(v -> dismiss());
    }
}
