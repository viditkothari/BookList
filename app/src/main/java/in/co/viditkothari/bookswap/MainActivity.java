package in.co.viditkothari.bookswap;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    BookAdapter bk_adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.iv_search).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String searchTextString = ((EditText)findViewById(R.id.et_searchText)).getText().toString();
                BookAsyncTask bg_task= new BookAsyncTask(searchTextString);
                bg_task.execute();
            }
        } ); // closing parenthesis of "setOnClickListener()" method

        findViewById(R.id.llayout_reset).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //ListView lv=(ListView)findViewById(R.id.lv_bookslist);
                bk_adapter.clear();
                bk_adapter.notifyDataSetChanged();
                //lv.setAdapter(null);

            }
        });

    }

    // ************ AsyncTask Class
    public class BookAsyncTask extends AsyncTask <URL,Void,ArrayList<Book>> {

        String searchText;

        // private constructor to initialize 'String searchText'
        private BookAsyncTask(String searchText){
            this.searchText=searchText;
        }

        // shows Indeterminate Progress Bar during network access operation
        @Override
        protected void onPreExecute() {
            findViewById(R.id.ll_pbar).setVisibility(View.VISIBLE);
        }

        // makes Http Request, fetches the JSON file/stream, parses it
            @Override
            protected ArrayList<Book> doInBackground(URL... searchURL) { /* Probably I'm doing something wrong here. The searchURL isn't being used anywhere */

                // Perform HTTP request to the URL and receive a JSON response back
                String jsonResponse = "";
                try {
                    jsonResponse = makeHttpRequest(createURL(searchText)); // createURL forms the URL String by formatting the search words, keyword
                } catch (IOException e) {
                    Log.i("makeHttpRequest()"," Error found while"); //  Handle the IOException
                }

                // Return the {@link Event} object as the result fo the {@link TsunamiAsyncTask}
                return extractDataFromJSON(jsonResponse);
        }

        @Override
        protected void onPostExecute(ArrayList<Book> books) {
            if(books.size()!=0){
                findViewById(R.id.ll_pbar).setVisibility(View.GONE);
                for(int i=0;i<books.size();i++) {
                    Log.i("JSONObj 1", books.get(i).getmTitle() + "");
                }
                bk_adapter=new BookAdapter(getApplicationContext(),0,books);
                ((ListView)findViewById(R.id.lv_bookslist)).setAdapter(bk_adapter);
            }
            else
                ((TextView)findViewById(R.id.tv_errormessage)).setText("No book found!");
        }

        // Creates URL object from a URL Search string
        private URL createURL(String url) {
            URL completeURL;
            if (!TextUtils.isEmpty(url)) {
                // Used regex to format string for the URL
                url = url.replaceAll("\\+", "%2B"); // save ('+') sign as a randomString "vv43" so as to conserve it for later
                url = url.replaceAll("(\\s+)|(,+)|( , +),(, +)", "+"); // replace 1 or more occurrence of blank space (' ') and\or commas (',') with plus sign ('+');
                url = url.replaceAll("\\++", "+"); // replace 1 or more occurrence of plus sign '+' with just one plus sign ('+');
                url = "https://www.googleapis.com/books/v1/volumes?q=" + url + "&maxResults=40";
                try {
                    completeURL = new URL(url);
                }
                catch (MalformedURLException except) {
                    Log.e(getLocalClassName(), "Error with URL creation", except);
                    return null;
                }
                return completeURL;
            } else
                return null;
        }

        // Checks for Network availability. Accessed via method call in makeHttpRequest()
        private boolean isNetworkAvailable() {
            // TODO: Check if accessing getApplicationContext() is causing some leak?
            ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(getApplicationContext().CONNECTIVITY_SERVICE);
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }

        /* readFromStream(InputStream) is accessed via makeHttpRequest(URL) method. It does following
         * reads from InputStream received from method call made in makeHttpRequest(URL) method
         * uses BfrdReader to read data in nice amount of chunks (lines)
         */
        private String readFromStream(InputStream iS) throws IOException {
            StringBuilder outSB = new StringBuilder();
            if (iS != null) {
                InputStreamReader iSReader = new InputStreamReader(iS,"UTF-8");
                BufferedReader reader = new BufferedReader(iSReader);
                String line = reader.readLine();
                while (line != null) {
                    outSB.append(line);
                    line = reader.readLine();
                }
            }
            return outSB.toString();
        }

        /* makeHttpRequest does following:
         * checks if Android Device is connected to a network
         * if yes, then uses the URL Object passed in as parameter {returned from the createURL method()} to create a 'HttpURLConnection' object
         * configure the HttpURLConnection object created
         * try {make HttpURLConnection} using 'connect()'
         * if successful, tries reading the InputStream returned.
         * handles IOExceptions while creating HttpURLConnection object or closing the InputStream or checks for errors if "getResponseCode()" is not '200'
         */
        private String makeHttpRequest(URL url) throws IOException {
            // String declaration for JSON Response to be fetched from the internet
            String jsonResponse = "";

            // HttpURLConnection object declaration
            HttpURLConnection urlconn = null;

            // InputStream object declaration to store the InputStream to be received from the HTTP request
            InputStream iS = null;

            if(!isNetworkAvailable())
                // TODO: check makeHttpRequest for 'null' value where its being called / accessed.
                return null;
            try {
                urlconn = (HttpURLConnection) url.openConnection();
                urlconn.setRequestMethod("GET");
                urlconn.setReadTimeout(9000 /* milliseconds */);
                urlconn.setConnectTimeout(12000 /* milliseconds */);
                urlconn.connect();
                if(urlconn.getResponseCode() == 200){
                    iS = urlconn.getInputStream();
                    Log.i("URL Connection Done"," Code 200");
                    Log.i("URL Connection Done",iS.toString());
                    jsonResponse = readFromStream(iS);
                }
                else
                    Log.i("Found Error code: ",urlconn.getResponseCode() + " ! ");
            }

            catch (IOException e) {
                // TODO: Handle the exception
                Log.i("Invalid URL! "," Please validity of URL: "+ url.getPath());
            }

            finally {
                if (urlconn != null) {
                    urlconn.disconnect();
                }
                if (iS != null) {
                    // function must handle java.io.IOException here
                    // TODO: why handle IOException here (again)?
                    try {
                        iS.close();
                    }
                    catch (IOException ioExcept){
                        Log.i("Closing InputStream: ","caused " + ioExcept + "to occur!");
                    }

                }
            }
            return jsonResponse;
        }



        private ArrayList<Book> extractDataFromJSON(String JSON_BooksList) {
            ArrayList<Book> books = new ArrayList<>();
            try {
                JSONObject rootJSONObject = new JSONObject(JSON_BooksList);
                JSONArray booksListArray = rootJSONObject.getJSONArray("items");

                String mImg = "Not available";
                String mTitle = "Not available";
                StringBuilder mAuthor = new StringBuilder("Not available");
                String mISBN = "Not available";
                String mInfoLink = "Not available";
                String mDesc = "Not available";

                // If there are results in the features array
                if (booksListArray.length() > 0) {
                    JSONObject bookObject;// = booksListArray.getJSONObject(0);
                    JSONObject volumeObject;// = bookObject.getJSONObject("volumeInfo");
                    JSONArray bookAuthors;// = volumeObject.getJSONArray("authors");
                    JSONArray bookIDs;// = volumeObject.getJSONArray("industryIdentifiers");
                    for(int i=0;i<booksListArray.length();i++){
                        bookObject = booksListArray.getJSONObject(i);
                        volumeObject = bookObject.getJSONObject("volumeInfo");

                        // logic for 'mImg'
                        if(volumeObject.getJSONObject("imageLinks").has("thumbnail"))
                            mImg = volumeObject.getJSONObject("imageLinks").getString("thumbnail");
                        Log.i("VIDIT: book image",mImg+"");

                        // logic for 'mTitle'
                        if(volumeObject.has("title"))
                            mTitle = volumeObject.getString("title");
                        Log.i("VIDIT: book title",mTitle+"");

                        // logic for 'mAuthor'
                        if(volumeObject.has("authors")) {
                            mAuthor.replace(0,mAuthor.length(),"");
                            bookAuthors = volumeObject.getJSONArray("authors");
                            for (int j = 0; j < bookAuthors.length(); j++) {
                                mAuthor.append(bookAuthors.getString(j)).append("  ");
                            }
                        }
                        Log.i("VIDIT: book authors",mAuthor.toString()+"");
                       /*
                       if(bookAuthors.length()==1)
                            mAuthor.append(bookAuthors.getString(i));
                        else {
                            for (j=0; j < bookAuthors.length() - 1; j++) {
                                mAuthor.append(bookAuthors.getString(j)).append(", ");
                            }
                            mAuthor.append(bookAuthors.getString(j+1));
                        }
                        */

                        if(volumeObject.has("industryIdentifiers")) {
                            bookIDs = volumeObject.getJSONArray("industryIdentifiers");
                            // logic for mISBN
                            for (int j = 0; j < bookIDs.length(); j++) {
                                if (bookIDs.getJSONObject(j).getString("type").equalsIgnoreCase("ISBN_13"))
                                    mISBN = bookIDs.getJSONObject(j).getString("identifier");
                            }
                        }
                            Log.i("VIDIT: book ISBN",mISBN+"");

                        // logic for 'mDesc'
                        if(volumeObject.has("description"))
                            if(volumeObject.getString("description").length()>100)
                                mDesc = volumeObject.getString("description").substring(0,100);
                        Log.i("VIDIT: book description",mDesc+"");

                        // logic for 'mInfoLink'
                        if(volumeObject.has("infoLink"))
                            mInfoLink = volumeObject.getString("infoLink");
                        Log.i("VIDIT: book Info Link",mInfoLink+"");

                        // Add 'Book' object to 'books' ArrayList
                        books.add(new Book(mImg, mTitle, mAuthor.toString(), mISBN, mDesc, mInfoLink));
                        mAuthor.replace(0,mAuthor.length(),"");
                    }
                    return books;
                }
                else
                    return null;
            }
        catch (JSONException e) {
                Log.e("JSON Reader Error!", "Problem JSON results:--------"+e);
            }
        return null;
        }
    } // End of BookAsyncTask (extends AsyncTask <URL, Void, Book>) class
} // End of MainActivity class