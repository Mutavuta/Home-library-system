package com.library.admin.ui.books;

import android.app.Dialog;
import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.material.textfield.TextInputEditText;
import com.library.admin.R;
import com.library.admin.api.ApiService;
import com.library.admin.model.ApiResponse;
import com.library.admin.model.Book;
import com.library.admin.model.BookTitle;
import com.library.admin.utils.ApiErrorUtils;
import com.library.admin.utils.RetrofitClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// Popup shown after a barcode scan on the Books screen.
// Lets admin either create a brand new catalog title or add a copy to an
// existing one, then calls POST /books/admin/add.
public class AddBookDialog extends Dialog {

    public interface OnBookAddedListener {
        void onBookAdded();
    }

    private final String scannedBarcode;
    private final List<BookTitle> existingTitles;
    private OnBookAddedListener listener;
    private ApiService apiService;

    private TextInputEditText etBarcode, etTitle, etAuthor, etCoverUrl;
    private AutoCompleteTextView etCategory, etExistingTitle;
    private RadioGroup rgMode;
    private View llNewTitle, llExistingTitle;

    // existingTiles is passed in from BooksFragment's already-loaded list,
    // so I don't need a second network call just to populate the dropdown
    public AddBookDialog(@NonNull Context context, String scannedBarcode, List<BookTitle> existingTitles) {
        super(context, R.style.Theme_HomeLibraryAdmin_Dialog);
        this.scannedBarcode = scannedBarcode;
        this.existingTitles = existingTitles;
    }

    public void setOnBookAddedListener(OnBookAddedListener listener) {
        this.listener =listener;
    }

    @Override
    protected  void onCreate(android.os.Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_add_book);

        apiService = RetrofitClient.getInstance(getContext()).create(ApiService.class);

        bindViews();
        setupCategoryDropdown();
        setupExistingTitleDropdown();
        setupModeToggle();

        etBarcode.setText(scannedBarcode);

        findViewById(R.id.btnCancel).setOnClickListener(v -> dismiss());
        findViewById(R.id.btnAdd).setOnClickListener(v -> attemptAddBook());
    }

    private void bindViews() {
        etBarcode       = findViewById(R.id.etBarcode);
        etTitle         = findViewById(R.id.etTitle);
        etAuthor        = findViewById(R.id.etAuthor);
        etCategory      = findViewById(R.id.etCategory);
        etCoverUrl      = findViewById(R.id.etCoverUrl);
        etExistingTitle = findViewById(R.id.etExistingTitle);
        rgMode          = findViewById(R.id.rgMode);
        llNewTitle      = findViewById(R.id.llNewTitle);
        llExistingTitle = findViewById(R.id.llExistingTitle);
    }

    private void setupCategoryDropdown() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                getContext(), R.array.book_categories, android.R.layout.simple_dropdown_item_1line);
        etCategory.setAdapter(adapter);
    }

    // Populates the "existing title" dropdown with title strings, but I still
    // need the actual BookTitle object on submit - selectedExistingTitle tracks that
    private BookTitle selectedExistingTitle;

    private void setupExistingTitleDropdown() {
        List<String> titleNames = new ArrayList<>();
        for (BookTitle t : existingTitles) {
            titleNames.add(t.title + " - " + t.author);
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                getContext(), android.R.layout.simple_dropdown_item_1line, titleNames);
        etExistingTitle.setAdapter(adapter);

        etExistingTitle.setOnItemClickListener((parent, view, position, id) ->
                selectedExistingTitle = existingTitles.get(position));
    }

    private void setupModeToggle() {
        rgMode.setOnCheckedChangeListener((group, checkedId) -> {
                boolean isNew = checkedId == R.id.rbNew;
        llNewTitle.setVisibility(isNew ? View.VISIBLE : View.GONE);
        llExistingTitle.setVisibility(isNew ? View.GONE : View.VISIBLE);
        });
    }

    private void attemptAddBook() {
        String barcode = textOf(etBarcode);
        boolean isNewTitleMode = rgMode.getCheckedRadioButtonId() == R.id.rbNew;

        if (TextUtils.isEmpty(barcode)) {
            Toast.makeText(getContext(), R.string.error_fill_all_fields, Toast.LENGTH_SHORT).show();
        }

        Map<String, String> body = new HashMap<>();
        body.put("barcodeId", barcode);

        if (isNewTitleMode) {
            String title = textOf(etTitle);
            String author = textOf(etAuthor);
            String category = etCategory.getText().toString().trim();

            if (TextUtils.isEmpty(title) || TextUtils.isEmpty(author) || TextUtils.isEmpty(category)) {
                Toast.makeText(getContext(), R.string.error_fill_all_fields, Toast.LENGTH_SHORT).show();
            }
            body.put("title", title);
            body.put("author", author);
            body.put("category", category);
            body.put("coverImageUrl", textOf(etCoverUrl));
        } else {
            if (selectedExistingTitle == null) {
                Toast.makeText(getContext(), R.string.error_select_title, Toast.LENGTH_SHORT).show();
            }
            body.put("titleId", selectedExistingTitle.id);
        }

        Button btnAdd = findViewById(R.id.btnAdd);
        btnAdd.setEnabled(false);

        apiService.addBook(body).enqueue(new Callback<ApiResponse<Book>>() {
            @Override
            public void onResponse(Call<ApiResponse<Book>> call, Response<ApiResponse<Book>> response) {
                btnAdd.setEnabled(false);

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(getContext(), R.string.msg_book_added, Toast.LENGTH_SHORT).show();
                    if (listener != null) listener.onBookAdded();
                    dismiss();
                } else {
                    Toast.makeText(getContext(), ApiErrorUtils.getErrorMassage(response), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Book>> call, Throwable t) {
                btnAdd.setEnabled(true);
                Toast.makeText(getContext(), R.string.error_network, Toast.LENGTH_LONG).show();
            }
        });
    }

    private String textOf(TextInputEditText field) {
        return field.getText() != null ? field.getText().toString().trim() : "";
    }
}
