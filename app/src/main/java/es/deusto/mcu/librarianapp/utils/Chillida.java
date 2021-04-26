package es.deusto.mcu.librarianapp.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ImageView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.Volley;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Chillida is an emulation of how could be implemented a Picasso-like library.
 *
 */
public class Chillida {

    private WeakReference<Context> context = null;
    private String url = null;

    private Chillida() {}

    private static final Chillida instance = new Chillida();

    /**
     * Set the context to use in download.
     * @param context is the context of the app. (if context is passed Volley is used, otherwise
     *                an AsyncTask with HttpURLConnection is used.
     * @return the singleton instance of the class.
     */
    public static Chillida with(Context context) {
        instance.context = new WeakReference<>(context);
        return instance;
    }

    /**
     * It sets the url which is going to be requested.
     * @param url is the string with contains the Url.
     * @return the singleton instance of the class.
     */
    public Chillida load(String url) {
        // It forces to use https queries
        instance.url = url.replace("http:", "https:");
        return instance;
    }

    /**
     * It sets the view which is going to be updated with the downloaded image. It triggers the
     * download. Thus the load method should be previously invoked.
     * @param imageView is the ImageView object.
     */
    public void into(ImageView imageView) {
        imageView.setVisibility(View.INVISIBLE);
        if (context.get() != null) {
            loadImageVolley(imageView);
        } else {
            loadImageAsyncTask(imageView);
        }
    }

    /**
     * Internal method to request the Image donwloading and the update of the ImageView.
     * @param imageView is the object to be updated.
     */
    private void loadImageVolley(final ImageView imageView) {
        ImageRequest request = new ImageRequest(
                url,
                bitmap -> {
                    imageView.setImageBitmap(bitmap);
                    imageView.setVisibility(View.VISIBLE);
                },
                500, 100,
                ImageView.ScaleType.CENTER_INSIDE,
                Bitmap.Config.RGB_565,
                error -> {
                });

        RequestQueue queue = Volley.newRequestQueue(this.context.get());
        queue.add(request);
    }

    /**
     * Internal method to request the Image donwloading and the update of the ImageView.
     * @param imageView is the object to be updated.
     */
    private void loadImageAsyncTask(ImageView imageView) {
        new DownloadTask(imageView).execute(url);
    }

    /**
     * Inner class to download an image and update a view with it.
     */
    static class DownloadTask extends AsyncTask<String, Void, Bitmap> {
        private final WeakReference<ImageView> imageView;

        DownloadTask(ImageView imageView) {
            this.imageView = new WeakReference<>(imageView);
        }

        @Override
        protected Bitmap doInBackground(String... urls) {
            String url = urls[0];
            HttpURLConnection urlConnection = null;
            Bitmap myBitmap = null;

            try {
                // Convert the URI to a URL,
                URL requestURL = new URL(url);

                // Open the network connection.
                urlConnection = (HttpURLConnection) requestURL.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Create bitmap
                myBitmap = BitmapFactory.decodeStream(urlConnection.getInputStream());

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                // Close the connection and the buffered reader.
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
            return myBitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            imageView.get().setImageBitmap(bitmap);
            imageView.get().setVisibility(View.VISIBLE);
        }
    }
}