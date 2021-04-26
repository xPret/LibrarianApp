package es.deusto.mcu.librarianapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import es.deusto.mcu.librarianapp.api.GoogleBooksUtils;
import es.deusto.mcu.librarianapp.api.clients.GoogleBooksAPI;
import es.deusto.mcu.librarianapp.model.Book;
import es.deusto.mcu.librarianapp.utils.Chillida;
import es.deusto.mcu.librarianapp.utils.NetUtils;

public class MainActivity extends AppCompatActivity {

    private EditText mBookInput;
    private TextView mResultBookTitleText;
    private TextView mResultAuthorText;
    private TextView mResultBookPublisherNameText;
    private TextView mResultBookPublishedDateText;
    private TextView mResultBookRatingText;
    private ImageView mResultBookImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Load view fields from layout
        mBookInput = findViewById(R.id.etBookInput);
        mResultBookTitleText = findViewById(R.id.tvResultBookTitle);
        mResultAuthorText = findViewById(R.id.tvResultBookAuthors);
        mResultBookPublisherNameText = findViewById(R.id.tvResultBookPublisherName);
        mResultBookPublishedDateText = findViewById(R.id.tvResultBookPublishedDate);
        mResultBookRatingText = findViewById(R.id.tvResultBookRating);
        mResultBookImage = findViewById(R.id.ivBookImage);

        // Get button and add listener
        ImageButton searchButton = findViewById(R.id.ibSearchBooks);
        searchButton.setOnClickListener(view -> searchBooks());

        // Hide results
        setResultsVisibility(View.GONE);
    }

    // Method launched to search a book
    private void searchBooks() {
        // Get typed query, hide keyboard and reset the field
        String query = mBookInput.getText().toString();
        hideKeyboard();
        mBookInput.setText("");

        // Check if the query is empty
        if (query.length() > 0) {
            // Check if the devices has any type of connection
            if (NetUtils.isAnyConnected(getApplicationContext())) {

                // Hide results
                setResultsVisibility(View.GONE);

                // Create a callback to update the view
                GoogleBooksAPI.Callback callback = new GoogleBooksAPI.Callback() {
                    @Override
                    public void onSuccess(Book book) {
                        loadResultsFromBook(book);
                    }

                    @Override
                    public void onError(String msg) {
                        showMessage(msg);
                    }
                };
                // Create a random implementation of Google Books API Client and launch the query
                GoogleBooksAPI googleBooksAPI = GoogleBooksUtils.getRandomGoogleBooksAPI();
                googleBooksAPI.setContext(getApplicationContext());
                googleBooksAPI.setCallback(callback);
                googleBooksAPI.searchBook(query);
            } else {
                showMessage(getString(R.string.msg_error_no_internet));
            }
        } else {
            showMessage(getString(R.string.msg_error_no_query));
        }

    }


    // Shows a message in Toast mode
    private void showMessage(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }

    private void hideKeyboard() {
        InputMethodManager inputManager = (InputMethodManager)
                getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputManager != null) {
            inputManager.hideSoftInputFromWindow(mBookInput.getWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    private void setResultsVisibility(int visibility) {
        mResultBookTitleText.setVisibility(visibility);
        mResultAuthorText.setVisibility(visibility);
        mResultBookPublisherNameText.setVisibility(visibility);
        mResultBookPublishedDateText.setVisibility(visibility);
        mResultBookRatingText.setVisibility(visibility);
        mResultBookImage.setVisibility(visibility);
    }

    // It loads a Book model into the corresponding views
    private void loadResultsFromBook(Book book) {
        if (book != null) {
            mResultBookTitleText.setText(book.getTitle());
            mResultAuthorText.setText(book.getAuthor());
            mResultBookPublisherNameText.setText(book.getPublisher());
            mResultBookPublishedDateText.setText(book.getPublishedDate());
            mResultBookRatingText.setText(String.valueOf(book.getAverageRating()));
            setResultsVisibility(View.VISIBLE);
            loadImage(book.getThumbnail());
        }
    }

    // It loads the image passed as parameter using the Chillida class only using WiFi
    private void loadImage(String imageUrl) {
        // Hide the ImageView
        mResultBookImage.setVisibility(View.GONE);
        // Check WiFi connectivity
        if (NetUtils.isWiFiConnected(getApplicationContext())) {
            // Launch the download and load of the image
            Chillida.with(null).load(imageUrl).into(mResultBookImage);
        }
    }

}