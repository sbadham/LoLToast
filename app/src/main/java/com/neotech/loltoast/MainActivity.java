package com.neotech.loltoast;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

    private boolean service_status;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialise the region spinner data
        Spinner regionSpinner = (Spinner) findViewById(R.id.region);
        ArrayAdapter<CharSequence> regionAdapter = ArrayAdapter.createFromResource(this,
                R.array.region_options, android.R.layout.simple_spinner_item);
        regionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        regionSpinner.setAdapter(regionAdapter);
        // Set default region
        regionSpinner.setSelection(((ArrayAdapter)regionSpinner.getAdapter()).getPosition(getResources().getText(R.string.default_region)));

        // Initialise the refresh spinner data
        Spinner refreshSpinner = (Spinner) findViewById(R.id.refresh);
        ArrayAdapter<CharSequence> refreshAdapter = ArrayAdapter.createFromResource(this,
                R.array.refresh_options, android.R.layout.simple_spinner_item);
        refreshAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        refreshSpinner.setAdapter(refreshAdapter);
        // Set default refresh
        refreshSpinner.setSelection(((ArrayAdapter)refreshSpinner.getAdapter()).getPosition(getResources().getText(R.string.default_refresh)));

        // Repopulate user data
       restoreData();

        // Set the start/stop button label based on service state
        Button button = (Button) findViewById(R.id.activate_button);
        if(isMyServiceRunning(LoLToastService.class)){
            button.setText(R.string.stop);
            service_status = true;
        } else {
            button.setText(R.string.start);
            service_status = false;
        }

        // Initialise the Save Button listener
        addListenerOnButton();
    }

    public void addListenerOnButton(){
        Button button = (Button) findViewById(R.id.activate_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Clear the current level
                TextView levelEdit = (TextView) findViewById(R.id.summoner_level);
                levelEdit.setText("");

                // Save user data
                commitData();

                Context context = getApplicationContext();
                Button button = (Button) findViewById(R.id.activate_button);

                if (service_status) {
                    button.setText(R.string.start);

                    Intent lolIntent = new Intent(context, LoLToastService.class);
                    PendingIntent pLoLService = PendingIntent.getService(getApplicationContext(), 2222, lolIntent,
                            PendingIntent.FLAG_CANCEL_CURRENT);
                    AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                    alarmManager.cancel(pLoLService);
                    stopService(lolIntent);

                    service_status = false;
                } else {
                    button.setText(R.string.stop);

                    // Store level
                    Spinner regionSpinner = (Spinner) findViewById(R.id.region);
                    String inputRegion = regionSpinner.getSelectedItem().toString();
                    TextView nameEdit = (TextView) findViewById(R.id.summoner);
                    String inputName = nameEdit.getText().toString();

                    TextView levelView = (TextView) findViewById(R.id.summoner_level);
                    SharedPreferences sharedPref = getSharedPreferences("LoLToast", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPref.edit();

                    //Toast.makeText(getApplicationContext(), "LoL Toast Started", Toast.LENGTH_LONG).show();
                    ServletParameters myParams = new ServletParameters(inputRegion, inputName, levelView, editor);
                    new ServletPostAsyncTask().execute(myParams);

                    // Create the LoLToast Service and set an alarm manager to check for updates
                    Intent lolIntent = new Intent(getApplicationContext(), LoLToastService.class);
                    PendingIntent pLoLService = PendingIntent.getService(getApplicationContext(), 2222, lolIntent,
                        PendingIntent.FLAG_CANCEL_CURRENT);

                    Spinner refreshSpinner = (Spinner) findViewById(R.id.refresh);
                    String inputRefreshStr = refreshSpinner.getSelectedItem().toString();
                    Integer inputRefreshInt = Integer.valueOf(inputRefreshStr) * 60000;
                    AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
                    am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, inputRefreshInt, inputRefreshInt,pLoLService);

                    Toast.makeText(getApplicationContext(), "LoL Toast Started", Toast.LENGTH_LONG).show();
                    service_status = true;
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void restoreData() {

        // Restore the app data.
        SharedPreferences sharedPref = this.getSharedPreferences("LoLToast", Context.MODE_PRIVATE);

        // Restore region
        String regionVal = sharedPref.getString(getString(R.string.summoner_region), "");
        if(regionVal != ""){
            Spinner regionSpinner = (Spinner) findViewById(R.id.region);
            regionSpinner.setSelection(((ArrayAdapter)regionSpinner.getAdapter()).getPosition(regionVal));
        }

        // Restore name
        String sumName = sharedPref.getString(getString(R.string.summoner_name), "");
        EditText nameBox = (EditText) findViewById(R.id.summoner);
        nameBox.setText(sumName);

        // Restore refresh
        String refreshVal = sharedPref.getString(getString(R.string.summoner_refresh), "");
        if(refreshVal != ""){
            Spinner refreshSpinner = (Spinner) findViewById(R.id.refresh);
            refreshSpinner.setSelection(((ArrayAdapter)refreshSpinner.getAdapter()).getPosition(refreshVal));
        }

        // Restore message
        String sumMsg = sharedPref.getString(getString(R.string.summoner_msg), getString(R.string.default_message));
        EditText msgBox = (EditText) findViewById(R.id.message);
        msgBox.setText(sumMsg);

        // Restore level
        String sumLvl = sharedPref.getString(getString(R.string.summoner_level), "");
        TextView levelEdit = (TextView) findViewById(R.id.summoner_level);
        levelEdit.setText(sumLvl);
    }

    private void commitData() {

        // Store the app data.
        //SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences sharedPref = this.getSharedPreferences("LoLToast", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        // Store region
        Spinner regionSpinner = (Spinner) findViewById(R.id.region);
        String inputRegion = regionSpinner.getSelectedItem().toString();
        editor.putString(getString(R.string.summoner_region), inputRegion);

        // Store name
        EditText nameBox = (EditText) findViewById(R.id.summoner);
        String inputName = nameBox.getText().toString();
        editor.putString(getString(R.string.summoner_name), inputName);

        // Store refresh
        Spinner refreshSpinner = (Spinner) findViewById(R.id.refresh);
        String inputRefresh = refreshSpinner.getSelectedItem().toString();
        editor.putString(getString(R.string.summoner_refresh), inputRefresh);

        // Store message
        EditText msgBox = (EditText) findViewById(R.id.message);
        String inputMsg = msgBox.getText().toString();
        editor.putString(getString(R.string.summoner_msg), inputMsg);

        // Summoner level is stored by the ServletPostAsyncTask when it completes its fetch

        editor.commit();
    }

    @Override
    public void onPause() {

        commitData();

        super.onPause();
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
