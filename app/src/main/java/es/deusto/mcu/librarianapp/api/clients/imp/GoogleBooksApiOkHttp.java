package es.deusto.mcu.librarianapp.api.clients.imp;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.List;

import es.deusto.mcu.librarianapp.api.GoogleBooksUtils;
import es.deusto.mcu.librarianapp.api.clients.GoogleBooksAPI;
import es.deusto.mcu.librarianapp.model.Book;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;

public class GoogleBooksApiOkHttp implements GoogleBooksAPI {

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
        OkHttpClient client = new OkHttpClient();

        okhttp3.Request request = new okhttp3.Request.Builder().url(
                GoogleBooksUtils.buildURI(queryString).toString()).build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                callback.onError(e.getLocalizedMessage());
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull okhttp3.Response response) throws IOException {
                ResponseBody body = response.body();
                if (body != null) {
                    final String responseData = body.string();
                    List<Book> books = GoogleBooksUtils.fromResponseToBooks(responseData);
                    new Handler(Looper.getMainLooper()).post(() ->
                            callback.onSuccess(books.isEmpty() ? null : books.get(0))
                    );
                }
            }
        });
    }
}
