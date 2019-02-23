package com.ninadelou.mediaplayer.Services;

import android.os.AsyncTask;
import android.util.Log;
import android.webkit.CookieManager;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class GetDataTask extends AsyncTask<String, Void, Boolean> {
    private static final String LOG_TAG = GetDataTask.class.getSimpleName();

    private ApiService mService;
    private Document mDocument;

    GetDataTask(ApiService service) {
        mService = service;
        mDocument = null;
    }

    @Override
    protected Boolean doInBackground(String... params) {
        HttpURLConnection connection = null;

        Log.d(LOG_TAG, "URL: " + params[0]);

        try {
            int nbAttempt = 0;
            do {
                URL serviceURL = new URL(params[0]);
                connection = (HttpURLConnection) serviceURL.openConnection();

                connection.setRequestMethod("GET");
                connection.setDoOutput(true);
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(5000);

                CookieManager cookieManager = CookieManager.getInstance();
                String cookie = cookieManager.getCookie("http://uiteur.struct-it.fr/");
                if (cookie != null) {
                    connection.setRequestProperty("Cookie", cookie);
                } // Else do nothing

                connection.connect();

                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    Log.i(LOG_TAG, "Connection granted...");

                    BufferedReader reader = new BufferedReader(new InputStreamReader(
                            connection.getInputStream()));
                    StringBuffer buffer = new StringBuffer();
                    String inputLine;
                    while ((inputLine = reader.readLine()) != null) {
                        buffer.append(inputLine);
                    }
                    reader.close();

                    Log.d(LOG_TAG, "Answer: " + buffer.toString());

                    DocumentBuilderFactory builderFactory = DocumentBuilderFactory
                            .newInstance();
                    DocumentBuilder documentBuilder = builderFactory.newDocumentBuilder();
                    InputSource inputSource = new InputSource(new StringReader(buffer.toString()));

                    mDocument = documentBuilder.parse(inputSource);
                } else {
                    if (connection.getResponseMessage() != null) {
                        Log.e(LOG_TAG, "Connection error: " + connection.getResponseMessage());
                    } else {
                        Log.e(LOG_TAG, "Connection refused...");
                    }
                }

                nbAttempt++;
            } while(this.mDocument == null && nbAttempt < 3);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            } // Else do nothing
        }

        return this.mDocument != null;
    }

    @Override
    protected void onPostExecute(final Boolean success) {
        this.mService.notifyData(success, this.mDocument);
    }
}