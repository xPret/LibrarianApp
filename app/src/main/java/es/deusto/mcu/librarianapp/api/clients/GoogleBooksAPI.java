package es.deusto.mcu.librarianapp.api.clients;

import android.content.Context;

import es.deusto.mcu.librarianapp.model.Book;

public interface GoogleBooksAPI {
    abstract class Callback {
        public abstract void onSuccess(Book book);
        public abstract void onError(String msg);
    }
    void setContext(Context context);
    void setCallback(Callback callback);
    void searchBook(String queryString);
}
