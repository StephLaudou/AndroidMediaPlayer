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
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class UserLoginTask extends AsyncTask<String, Void, Boolean> {
    private static final String LOG_TAG = UserLoginTask.class.getSimpleName();

    private ApiService mService;
    private Document mDocument;

    /**
     * A dummy authentication store containing known user names and passwords.
     */
    public static final String[] DUMMY_CREDENTIALS = new String[]{
            "foo@example.com:hello", "bar@example.com:world"
    };

    UserLoginTask(ApiService service) {
        mService = service;
        mDocument = null;
    }

    @Override
    protected Boolean doInBackground(String... params) {
        for (String credential : DUMMY_CREDENTIALS) {
            String[] pieces = credential.split(":");
            if (pieces[0].equals(params[0])) {
                // Return null for XML document.
                return pieces[1].equals(params[1]);
            }
        }

        HttpURLConnection connection = null;

        String url = "http://uiteur.struct-it.fr/"
                + "login.php?user="
                + params[0]
                + "&pwd="
                + params[1];

        Log.d(LOG_TAG, "URL: " + url);

        try {
            int nbAttempt = 0;
            do {
                URL serviceURL = new URL(url);
                connection = (HttpURLConnection) serviceURL.openConnection();

                connection.setRequestMethod("GET");
                connection.setDoOutput(true);
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(5000);
                connection.connect();

                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    Log.i(LOG_TAG, "Log in granted...");

                    Map<String, List<String>> headerFields = connection.getHeaderFields();
                    List<String> cookiesHeader = headerFields.get("Set-Cookie");

                    if (cookiesHeader != null) {
                        for (String cookie : cookiesHeader) {
                            CookieManager cookieManager = CookieManager.getInstance();
                            cookieManager.setCookie("http://uiteur.struct-it.fr/", cookie);
                        }
                    }

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
                    this.mDocument = documentBuilder.parse(inputSource);
                } else {
                    if (connection.getResponseMessage() != null) {
                        Log.e(LOG_TAG, connection.getResponseMessage());
                    } else {
                        Log.e(LOG_TAG, "Log in refused...");
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
        this.mService.notifyUserLogin(success, this.mDocument);
    }
}