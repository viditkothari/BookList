package in.co.viditkothari.bookswap;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;


class BookAdapter extends ArrayAdapter<Book> {
    BookAdapter(Context context, int resource, ArrayList<Book> books) {
        super(context, 0, books);
    }

    private ImageView bookImageView;

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null)
            convertView = LayoutInflater.from(this.getContext()).inflate(R.layout.book_listitem, parent, false);
        Book bookItem = getItem(position);

        Typeface typeface_b = Typeface.createFromAsset(getContext().getAssets(), "fonts/bn-bi.otf");
        Typeface typeface_r = Typeface.createFromAsset(getContext().getAssets(), "fonts/bn-r.otf");
        Typeface typeface_ri = Typeface.createFromAsset(getContext().getAssets(), "fonts/bn-ri.otf");

        bookImageView = (ImageView) convertView.findViewById(R.id.iv_bookthumbnail);
        if (!bookItem.getmImg().isEmpty())
            new DownloadImageTask().execute(bookItem.getmImg());
        ((TextView) convertView.findViewById(R.id.tv_booktitle)).setText(bookItem.getmTitle());
        TextView tv_author_ISBN = (TextView) convertView.findViewById(R.id.tv_bookauthor_ISBN);

        String str_getAuthor_ISBN;
        if (!bookItem.getmAuthor().isEmpty() && !bookItem.getmAuthor().isEmpty())
            str_getAuthor_ISBN = "Author(s): " + bookItem.getmAuthor() + " | ISBN-13: " + bookItem.getmISBN();
        else if (!bookItem.getmAuthor().isEmpty() && bookItem.getmAuthor().isEmpty())
            str_getAuthor_ISBN = "Author(s): " + bookItem.getmAuthor();
        else if (bookItem.getmAuthor().isEmpty() && bookItem.getmAuthor().isEmpty())
            str_getAuthor_ISBN = "ISBN-13: " + bookItem.getmISBN();
        else
            str_getAuthor_ISBN = bookItem.getmAuthor() + " | " + bookItem.getmISBN();
        tv_author_ISBN.setText(str_getAuthor_ISBN);

        ((TextView) convertView.findViewById(R.id.tv_bookdesc)).setText(bookItem.getmDesc());

        ((TextView) convertView.findViewById(R.id.tv_booktitle)).setTypeface(typeface_b);
        ((TextView) convertView.findViewById(R.id.tv_bookdesc)).setTypeface(typeface_r);
        tv_author_ISBN.setTypeface(typeface_ri);

        return convertView;
    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        protected Bitmap doInBackground(String... str) {
            Bitmap bmp = null;
            try {
                bmp = makeHttpRequestAndFetchImage(str[0]);
            } catch (IOException x) {
                Log.e("DownloadImageTask", "IOException while downloading image");
            }
            return bmp;
        }

        private Bitmap readFromStream(InputStream iS) throws IOException {
            Bitmap bmp = null;
            if (iS != null) {
                try {
                    bmp = BitmapFactory.decodeStream(iS);
                } catch (Exception e) {
                    Log.e("Error", e.getMessage());
                    e.printStackTrace();
                }
            }
            return bmp;
        }

        private Bitmap makeHttpRequestAndFetchImage(String url) throws IOException, NullPointerException {
            URL finalURL = null;
            try {
                finalURL = new URL(url);
            } catch (MalformedURLException except) {
                Log.e("Yo!", "Error with URL creation" + except);
            }

            // HttpURLConnection object declaration
            HttpURLConnection urlConnForImage = null;

            // InputStream object declaration to store the InputStream to be received from the HTTP request
            InputStream iS = null;
            Bitmap bmp = null;
            try {
                urlConnForImage = (HttpURLConnection) finalURL.openConnection();
                urlConnForImage.setRequestMethod("GET");
                urlConnForImage.setReadTimeout(9000 /* milliseconds */);
                urlConnForImage.setConnectTimeout(12000 /* milliseconds */);
                urlConnForImage.connect();
                if (urlConnForImage.getResponseCode() == 200) {
                    iS = urlConnForImage.getInputStream();
                    Log.i("URL Connection Done", " Code 200");
                    Log.i("URL Connection Done", iS.toString());
                    bmp = readFromStream(iS);
                } else
                    Log.i("Found Error code: ", urlConnForImage.getResponseCode() + " ! ");
            } catch (IOException e) {
                Log.i("Invalid URL! ", " Please validity of URL: " + finalURL.getPath());
            } finally {
                if (urlConnForImage != null) {
                    urlConnForImage.disconnect();
                }
                if (iS != null) {
                    try {
                        iS.close();
                    } catch (IOException ioExcept) {
                        Log.i("Closing InputStream: ", "caused " + ioExcept + "to occur!");
                    }

                }
            }
            return bmp;
        }

        protected void onPostExecute(Bitmap bmp) {
            bookImageView.setImageBitmap(bmp);
        }

    }
}
