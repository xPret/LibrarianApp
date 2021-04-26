package es.deusto.mcu.librarianapp.api;

import android.net.Uri;
import android.os.Build;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import es.deusto.mcu.librarianapp.api.clients.GoogleBooksAPI;
import es.deusto.mcu.librarianapp.api.clients.imp.GoogleBooksApiBasic;
import es.deusto.mcu.librarianapp.api.clients.imp.GoogleBooksApiOkHttp;
import es.deusto.mcu.librarianapp.api.clients.imp.GoogleBooksApiVolley;
import es.deusto.mcu.librarianapp.model.Book;

public class GoogleBooksUtils {

    private static final String TAG = GoogleBooksUtils.class.getName();

    // Base endpoint URL for the Books API.
    private static final String BOOK_BASE_URL = "https://www.googleapis.com/books/v1/volumes?";
    // Parameter for the search string.
    private static final String QUERY_PARAM = "q";
    // Parameter that limits search results.
    private static final String MAX_RESULTS_PARAM = "maxResults";
    // Parameter to filter by print type.
    private static final String PRINT_TYPE_PARAM = "printType";

    /**
     * Given a query it returns the needed Uri to request to Google Books
     * @param queryString contains the query to request
     * @return Uri object to request the query passed as parameter
     */
    public static Uri buildURI(String queryString) {
        return Uri.parse(BOOK_BASE_URL).buildUpon()
                .appendQueryParameter(QUERY_PARAM, queryString)
                .appendQueryParameter(MAX_RESULTS_PARAM, "10")
                .appendQueryParameter(PRINT_TYPE_PARAM, "books")
                .build();
    }

    /**
     * Given a json object which contains information about a book, it creates the
     * corresponding Book object
     * @param jsonObject in Google Book JSON Format
     * @return the corresponding Book object
     */
    public static Book fromJSONObjectToBook(JSONObject jsonObject) {
        Book retBook = new Book();

        JSONObject volumeInfo;

        try {
            volumeInfo = jsonObject.getJSONObject("volumeInfo");
            retBook.setTitle(volumeInfo.getString("title"));
            retBook.setThumbnail(volumeInfo.getJSONObject("imageLinks").getString("thumbnail"));
            JSONArray authorsArray = volumeInfo.getJSONArray("authors");
            retBook.setAuthor(authorsArray.join(","));
        } catch (JSONException e) {
            Log.w(TAG, "Omitting Book, at least one needed field is missing: "
                    + e.getLocalizedMessage());
            return null;
        }

        try {
            retBook.setPublisher(volumeInfo.getString("publisher"));
            retBook.setPublishedDate(volumeInfo.getString("publishedDate"));
            retBook.setAverageRating(volumeInfo.getInt("averageRating"));
        } catch (JSONException e) {
            Log.w(TAG, "Book incomplete, at least one secondary field is missing: "
                    + e.getLocalizedMessage());
        }

        return retBook;
    }

    /**
     * It transforms the content of the response to a list of Book objects
     * @param response is a JSON-based string received from the GBooks API
     * @return a list of Book objects
     */
    public static List<Book> fromResponseToBooks(String response) {
        List<Book> books = new ArrayList<>();

        try {
            // Convert the response into a JSON object.
            JSONObject jsonObject = new JSONObject(response);

            // Get the JSONArray of book items.
            JSONArray itemsArray = jsonObject.getJSONArray("items");

            // Iterate and add books to the list.
            int i = 0;
            Book book;
            while (i < itemsArray.length()) {
                // Get the current book information.
                book = GoogleBooksUtils.fromJSONObjectToBook(itemsArray.getJSONObject(i));
                // Add book to the list.
                if (book != null) books.add(book);
                // Move to the next item.
                i++;
            }
        } catch (JSONException e) {
            // Return an empty list in case of exception
            return Collections.emptyList();
        }
        return books;
    }

    // It gets the used random implementation of GoogleBooksAPI
    public static GoogleBooksAPI getRandomGoogleBooksAPI() {
        Random random = new Random();
        // OkHttp works on Android 5.0+ (API level 21+) and Java 8+.
        // Consequently, if the build version is less than Lollipop we will only choose between
        // Basic and Volley
        int bound = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) ? 3 : 2;

        switch (random.nextInt(bound)) {
            case 2:
                Log.d(TAG, "OkHttp API Client created!");
                return new GoogleBooksApiOkHttp();
            case 1:
                Log.d(TAG, "Volley API Client created!");
                return new GoogleBooksApiVolley();
            default:
            case 0:
                Log.d(TAG, "Basic API Client created!");
                return new GoogleBooksApiBasic();
        }
    }
}