package com.neotech.loltoast;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

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

class ServletPostAsyncService extends AsyncTask<ServletParametersService, Void, String> {

    private String region;
    private String name;
    private Service host;

    @Override
    protected String doInBackground(ServletParametersService... params) {
        region = params[0].getInputRegion();
        name = params[0].getInputName();
        name = name.toLowerCase().replaceAll("\\s", "");
        host = params[0].getHost();

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

        String notificationText = "";
        String checkLevel = "";
        boolean error = false;

        try {
            JSONObject json = new JSONObject(result);
            JSONObject summ = json.getJSONObject(name);
            checkLevel = summ.getString(host.getString(R.string.json_level));
        } catch (Exception e) {
            try {
                JSONObject json = new JSONObject(result);
                JSONObject status = json.getJSONObject(host.getString(R.string.json_status));
                notificationText = "LoL API Error Status: " + status.getString(host.getString(R.string.json_message));
                error = true;
            } catch (Exception ne){
                notificationText = "Error with LoL JSON response: " + result;
                error = true;
            }
        }

        // Check if level has changed if no error
        if (!error) {
            // Get the app data.
            SharedPreferences sharedPref = host.getSharedPreferences("LoLToast", Context.MODE_PRIVATE);
            String level = sharedPref.getString(host.getString(R.string.summoner_level), "");
            if (level.compareTo(checkLevel) != 0) {
                // Create the notification text
                notificationText = sharedPref.getString(host.getString(R.string.summoner_msg), "") + " " + checkLevel;

                // Record the new level data
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString(host.getString(R.string.summoner_level), checkLevel);
                editor.commit();
            }
        }

        // Generate notification if error or level change
        if (!notificationText.equals("")) {

            int notificationId = 001;
            // Build intent for notification content
            Intent viewIntent = new Intent(host, MainActivity.class);
            PendingIntent viewPendingIntent =
                    PendingIntent.getActivity(host, 0, viewIntent, 0);

            // Set background image for wearable
            Bitmap lolBkg = new BitmapFactory().decodeResource(host.getResources(), R.drawable.loltoast_bkg);
            NotificationCompat.WearableExtender wearableExtender =
                    new NotificationCompat.WearableExtender()
                    .setBackground(lolBkg);

            // Create big text containing the full message
            NotificationCompat.BigTextStyle bigStyle = new NotificationCompat.BigTextStyle();
            bigStyle.bigText(notificationText);

            if(!error){
                // Create a normal message with the notification
                NotificationCompat.Builder notificationBuilder =
                        new NotificationCompat.Builder(host)
                                .setSmallIcon(R.mipmap.ic_launcher)
                                .setVibrate(new long[]{0, 300})
                                .setDefaults(Notification.DEFAULT_SOUND)
                                .setContentTitle(host.getString(R.string.notification_title))
                                .setContentText(notificationText)
                                .setContentIntent(viewPendingIntent)
                                .extend(wearableExtender);
                // Get an instance of the NotificationManager service
                NotificationManagerCompat notificationManager =
                        NotificationManagerCompat.from(host);

                // Build the notification and issues it with notification manager.
                notificationManager.notify(notificationId, notificationBuilder.build());
            } else {
                // Create a big style message with the error content
                NotificationCompat.Builder notificationBuilder =
                        new NotificationCompat.Builder(host)
                                .setSmallIcon(R.mipmap.ic_launcher)
                                .setVibrate(new long[]{0,300})
                                .setDefaults(Notification.DEFAULT_SOUND)
                                .setContentTitle(host.getString(R.string.notification_title))
                                .setContentText(host.getString(R.string.notification_error))
                                .setContentIntent(viewPendingIntent)
                                .setStyle(bigStyle)
                                .extend(wearableExtender);
                // Get an instance of the NotificationManager service
                NotificationManagerCompat notificationManager =
                        NotificationManagerCompat.from(host);

                // Build the notification and issues it with notification manager.
                notificationManager.notify(notificationId, notificationBuilder.build());
            }
        }
    }
}