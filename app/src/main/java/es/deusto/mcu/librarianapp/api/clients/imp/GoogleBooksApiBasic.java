package es.deusto.mcu.librarianapp.api.clients.imp;

import android.content.Context;
import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;


import javax.net.ssl.HttpsURLConnection;

import es.deusto.mcu.librarianapp.api.GoogleBooksUtils;
import es.deusto.mcu.librarianapp.api.clients.GoogleBooksAPI;
import es.deusto.mcu.librarianapp.model.Book;

public class GoogleBooksApiBasic implements GoogleBooksAPI {

    private Callback callback;

    @Override
    public void setContext(Context context) {
    }

    @Override
    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    @Override
    public void searchBook(String queryString) {
        new FetchBook(callback).execute(queryString);
    }

    /**
     * Static method to make the actual query to the Books API.
     *
     * @param queryString the query string.
     * @return the JSON response string from the query.
     */
    static String getBookInfo(String queryString) {
        // Set up variables for the try block that need to be closed in the
        // finally block.
        HttpsURLConnection urlConnection = null;
        BufferedReader reader = null;
        String bookJSONString = null;

        try {
            // Convert the URI to a URL,
            URL requestURL = new URL(GoogleBooksUtils.buildURI(queryString).toString());

            // Open the network connection.
            urlConnection = (HttpsURLConnection) requestURL.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Get the InputStream.
            InputStream inputStream = urlConnection.getInputStream();

            // Create a buffered reader from that input stream.
            reader = new BufferedReader(new InputStreamReader(inputStream));

            // Use a StringBuilder to hold the incoming response.
            StringBuilder builder = new StringBuilder();

            String line;
            while ((line = reader.readLine()) != null) {
                // Add the current line to the string.
                builder.append(line);

                // Since this is JSON, adding a newline isn't necessary (it won't
                // affect parsing) but it does make debugging a *lot* easier
                // if you print out the completed buffer for debugging.
                builder.append("\n");
            }

            if (builder.length() == 0) {
                // Stream was empty.  Exit without parsing.
                return null;
            }

            bookJSONString = builder.toString();

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // Close the connection and the buffered reader.
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        // Write the final JSON response to the log
        return bookJSONString;
    }


    public static class FetchBook extends AsyncTask<String, Void, String> {

        private final Callback callback;

        // Constructor, provides references to the views in MainActivity.
        FetchBook(Callback callback) {
            this.callback = callback;
        }

        /**
         * Use the getBookInfo() method in the NetworkUtils class to make
         * the connection in the background.
         *
         * @param strings String array containing the search data.
         * @return Returns the JSON string from the Books API, or
         * null if the connection failed.
         */
        @Override
        protected String doInBackground(String... strings) {
            return getBookInfo(strings[0]);
        }

        /**
         * Handles the results on the UI thread. Gets the information from
         * the JSON result and updates the views.
         *
         * @param s Result from the doInBackground() method containing the raw
         *          JSON response, or null if it failed.
         */
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (s != null) {
                List<Book> books = GoogleBooksUtils.fromResponseToBooks(s);
                callback.onSuccess(books.isEmpty() ? null : books.get(0));
            } else {
                callback.onError("Error requesting book.");
            }
        }
    }
}
