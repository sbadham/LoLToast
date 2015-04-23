package com.neotech.loltoast;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
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

public class LoLToastService extends Service {

    private static final String TAG = "MyService";

    public LoLToastService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        //throw new UnsupportedOperationException("Not yet implemented");
        return null;
    }

    @Override
    public void onCreate() {
    }

    @Override
    public void onStart(Intent intent, int startId) {

        // Get the app data.
        SharedPreferences sharedPref = this.getSharedPreferences("LoLToast", Context.MODE_PRIVATE);

        // Get data
        String region = sharedPref.getString(getString(R.string.summoner_region), "");
        String name = sharedPref.getString(getString(R.string.summoner_name), "");

        ServletParametersService myParams = new ServletParametersService(region, name, this);
        new ServletPostAsyncService().execute(myParams);
    }

    @Override
    public void onDestroy() {
        Toast.makeText(this, "LoL Toast Stopped", Toast.LENGTH_LONG).show();
        Log.d(TAG, "onDestroy");
    }
}
