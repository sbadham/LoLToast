package com.neotech.loltoast;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class ServletPostAsyncTask extends AsyncTask<ServletParameters, Void, String> {

    private String region;
    private String name;
    private TextView showLevel;
    private SharedPreferences.Editor editor;

    @Override
    protected String doInBackground(ServletParameters... params) {
        region = params[0].getInputRegion();
        name = params[0].getInputName();
        name = name.toLowerCase().replaceAll("\\s","");

        showLevel = params[0].getTextView();
        editor = params[0].getEditor();

        HttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost("http://quiet-amp-91412.appspot.com/hello"); // 10.0.2.2 is localhost's IP address in Android emulator
        try {
            // Add data to request
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
            nameValuePairs.add(new BasicNameValuePair("region", region));
            nameValuePairs.add(new BasicNameValuePair("name", name));
            httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

            // Execute HTTP Post Request
            HttpResponse response = httpClient.execute(httpPost);
            if (response.getStatusLine().getStatusCode() == 200) {
                return EntityUtils.toString(response.getEntity());
            }
            return "Error: " + response.getStatusLine().getStatusCode() + " " + response.getStatusLine().getReasonPhrase();

        } catch (ClientProtocolException e) {
            return e.getMessage();
        } catch (IOException e) {
            return e.getMessage();
        }
    }

    @Override
    protected void onPostExecute(String result) {
        try {
            JSONObject json = new JSONObject(result);
            JSONObject summ = json.getJSONObject(name);
            String returnSummLevel = summ.getString("summonerLevel");

            showLevel.setText(returnSummLevel);

            //Debug to force notification
            //editor.putString("Summoner_Level", "1");
            editor.putString("Summoner_Level", returnSummLevel);
            editor.commit();
        } catch(Exception e) {
            try {
                JSONObject json = new JSONObject(result);
                JSONObject status = json.getJSONObject("status");
                String returnSummLevel = "LoL API Error Status: " + status.getString("message");
                showLevel.setText(returnSummLevel);
            } catch (Exception ne){
                String returnSummLevel = "Error with LoL JSON response 1: " + result;
                showLevel.setText(returnSummLevel);
            }
            showLevel.setText("Error with LoL JSON response 2: " + result);
        }
    }
}