package com.tessoft.mykaraoke;

import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by Daeyong on 2017-08-17.
 */
public class HttpAsyncTask extends AsyncTask<Void, Integer, String> {

    private TransactionDelegate delegate;
    int requestCode = 0;
    String url = "";

    public HttpAsyncTask( TransactionDelegate delegate, String url, int requestCode )
    {
        this.delegate = delegate;
        this.url = url;
        this.requestCode = requestCode;
    }

    protected void onPreExecute() {

    }

    protected String doInBackground(Void... params) {

        String result = "";

        try {

            HttpClient httpclient = new DefaultHttpClient();
            // Prepare a request object
            HttpGet httpget = new HttpGet(url);

            Log.d("HTTPRequest", "Request url:" + url);

            // Execute the request
            HttpResponse response = httpclient.execute(httpget);

            // Get hold of the response entity
            HttpEntity entity = response.getEntity();

            if (entity != null) {

                // A Simple JSON Response Read
                InputStream instream = entity.getContent();
                result= convertStreamToString(instream);

                Log.d("HTTPRequest", "Response String:" + result );

                // now you have the string representation of the HTML request
                instream.close();
            }

        } catch (Exception ex) {
            return Constants.FAIL;
        }

        return result;
    }

    private static String convertStreamToString(InputStream is) {
    /*
     * To convert the InputStream to String we use the BufferedReader.readLine()
     * method. We iterate until the BufferedReader return null which means
     * there's no more data to read. Each line will appended to a StringBuilder
     * and returned as String.
     */
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }

    protected void onProgressUpdate(Integer... progress) {

    }

    protected void onPostExecute(String result) {

        if ( delegate != null )
            delegate.doPostTransaction( requestCode, result );

    }
}
