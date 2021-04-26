package es.deusto.mcu.librarianapp.api.clients.imp;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.lang.ref.WeakReference;
import java.util.List;

import es.deusto.mcu.librarianapp.api.GoogleBooksUtils;
import es.deusto.mcu.librarianapp.api.clients.GoogleBooksAPI;
import es.deusto.mcu.librarianapp.model.Book;

public class GoogleBooksApiVolley implements GoogleBooksAPI {

    private WeakReference<Context> context;
    private GoogleBooksAPI.Callback callback;

    @Override
    public void setContext(Context context) {
        this.context = new WeakReference<>(context);
    }

    @Override
    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    @Override
    public void searchBook(String queryString) {
        RequestQueue queue = Volley.newRequestQueue(context.get());

        StringRequest stringRequest = new StringRequest(
                Request.Method.GET,
                GoogleBooksUtils.buildURI(queryString).toString(),
                response -> {
                    if (callback != null) {
                        List<Book> books = GoogleBooksUtils.fromResponseToBooks(response);
                        callback.onSuccess(books.isEmpty() ? null : books.get(0));
                    }
                },
                error -> {
                    if (callback != null)
                        callback.onError(error.getLocalizedMessage());
                });

        queue.add(stringRequest);
    }
}
