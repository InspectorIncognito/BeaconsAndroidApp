package blopa.beacons;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;
import android.provider.Settings.Secure;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.MacAddress;
import com.estimote.sdk.Region;
import com.estimote.sdk.SystemRequirementsChecker;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity implements EventActivityInterface {

    private ProgressDialog pDialog;

    private static String url ="http://200.9.100.91:8080/gpsonline/beacon/save";

    private String android_id;
    BeaconManager beaconManager;
    Region region;
    List<Beacon> nearestBeacons;
    List<StringMacAddress> majorMinor;
    List<EventLog> events;
    private NearestBeaconAdapter adapter;
    private BeaconLog beaconLog;
    private long threshold;
    private long time;
//    private static final String FILENAME = "testBeacon.txt";


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        threshold= 5;
        time= 0;
        android_id = Secure.getString(this.getContentResolver(),
                Secure.ANDROID_ID);

//        Spinner spinner = (Spinner) findViewById(R.id.logBeaconsSpinner);
//        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                if (position != 0)
//                    getLog(majorMinor.get(position).mAddress);
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> parent) {
//
//            }
//        });

        setupListView();
        setupBeaconSDK();
    }

    @Override
    protected void onResume() {
        super.onResume();
        SystemRequirementsChecker.checkWithDefaultDialogs(this);

        connectToService();
    }

    /**
     * Beacon Scanner
     **/
    public void connectToService() {
        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
            @Override
            public void onServiceReady() {
                beaconManager.startRanging(region);
            }
        });
    }

    @Override
    protected void onPause() {
        disconnectFromService();
        super.onPause();
    }

    public void disconnectFromService() {
        beaconManager.stopRanging(region);
    }


    /**
     * Initial Setup
     **/



    public void setupListView() {
        Calendar c = Calendar.getInstance();
        time = c.getTimeInMillis();

        ListView listView = (ListView) findViewById(R.id.nearestBeaconsList);
        events = new ArrayList<>();
        nearestBeacons = new ArrayList<>();
        majorMinor = new ArrayList<>();
        majorMinor.add(new StringMacAddress("",""));
        beaconLog = new BeaconLog();
        adapter = new NearestBeaconAdapter(this, nearestBeacons);
        listView.setAdapter(adapter);
    }

    public void setupBeaconSDK() {
        beaconManager = new BeaconManager(this);
        beaconManager.setForegroundScanPeriod(200, 0);

        region = new Region("Region", null/*UUID.fromString("7eccfcfa-f334-4042-9dc2-5b5432c33e06")*/, null, null);

        beaconManager.setRangingListener(new BeaconManager.RangingListener() {
            @Override
            public void onBeaconsDiscovered(Region region, List<Beacon> list) {
                nearestBeacons.clear();
                nearestBeacons.addAll(list);
                adapter.notifyDataSetChanged();

                Calendar c = Calendar.getInstance();
                if(c.getTimeInMillis()- time >= threshold *1000) {
                    addBeaconsToOptions(list);
                    beaconLog.addMeasure(list, getTime(c));
//                    updateSpinner();
                    time= c.getTimeInMillis();
                }
            }
        });
    }

    /**
     * Buttons
     **/

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.markEvent:
                markEvent();
                return true;

            case R.id.sendData:
                new AlertDialog.Builder(this)
                        .setTitle("Confirm")
                        .setMessage("Send your RSSI log to server?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener(){
                            public void onClick(DialogInterface dialog, int whichButton){
                                try {
                                    sendJson();
                                } catch (JSONException e) {
                                    Toast.makeText(MainActivity.this, "An error has occurred, check AndroidLog to more information", Toast.LENGTH_LONG);
                                    Log.e("Send data to server",e.toString());
                                } catch (IOException e) {
                                    Toast.makeText(MainActivity.this, "An error has occurred, check AndroidLog to more information", Toast.LENGTH_LONG);
                                    Log.e("Send data to server",e.toString());
                                }
                            }
                        })
                        .setNegativeButton("No",null)
                        .create()
                .show();

                return true;

            case R.id.getIdDevice:
                DialogFragment newFragment = new DeviceIDDialogFragment();
                Bundle bundle = new Bundle();
                bundle.putString("Device", this.android_id);
                newFragment.setArguments(bundle);
                newFragment.show(getSupportFragmentManager(), "events");
                return true;

            default:
                return super.onOptionsItemSelected(item);

        }
    }

    public void markEvent() {

        Calendar c = Calendar.getInstance();
        String strTime = getTime(c);

        DialogFragment newFragment = new EventDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putString("Time", strTime);
        newFragment.setArguments(bundle);
        newFragment.show(getSupportFragmentManager(), "events");
    }

    @Override
    public void onTextSend(String strTime, String text) {

        events.add(new EventLog(strTime,text));
    }

    public void sendJson() throws JSONException, IOException {
        JSONObject data= createJsonObject();
        JSONObject json= new JSONObject();
        json.put("DeviceID",android_id);
        json.put("MeasureData", data);
        new AsyncConnection(json).execute();
    }


    /**
     * Aux functions
     **/


    private String getTime(Calendar c) {
        SimpleDateFormat timeMeasure = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return timeMeasure.format(c.getTime());
    }

    private void addBeaconsToOptions(List<Beacon> list) {
        for(Beacon beacon: list){
            MacAddress mAddress = beacon.getMacAddress();
            if(beaconLog.exist(mAddress.toString()))
                continue;
            majorMinor.add(new StringMacAddress(String.format("%d:%d", beacon.getMajor(), beacon.getMinor()),mAddress.toString()));
        }
    }

    public void getLog(String mAddress) {
        Log.d("BeaconLog",beaconLog.getLog(mAddress));
    }

    public JSONObject createJsonObject() throws JSONException {
        JSONObject json = new JSONObject();

        JSONArray beacons = new JSONArray();
        for (StringMacAddress mAddress : majorMinor) {
            if (mAddress.majorMinor.equals("")) continue;
            beacons.put(beaconLog.createJsonBeacon(mAddress.mAddress));
        }

        json.put("Beacons", beacons);
        json.put("Events", createJsonEvents());

        return json;
    }

    private JSONArray createJsonEvents() throws JSONException {
        JSONArray eventList = new JSONArray();
        for (EventLog event : events) {
            eventList.put(event.getJsonObject());
        }
        return eventList;
    }


    public void reset(){
        beaconLog.reset();
        events = new ArrayList<>();
        majorMinor = new ArrayList<>();
        majorMinor.add(new StringMacAddress("",""));
    }


    /**
     * Async class
     *
     **/


    private class AsyncConnection extends AsyncTask<Void, Void, Void> {
        JSONObject json;

        AsyncConnection(JSONObject json){
            this.json =  json;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();

        }

        @Override
        protected Void doInBackground(Void... arg0) {
            HttpHandler sh = new HttpHandler();

            // Making a request to url and getting response
            String response = null;
            try {
                response = sh.makeServiceCall(url, json);
            }  catch (IOException e) {
                e.printStackTrace();
            }

            if (response != null) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(),
                                "Json sent",
                                Toast.LENGTH_LONG)
                                .show();
                        reset();
                    }
                });
            } else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(),
                                "Couldn't get json from server. Check LogCat for possible errors!",
                                Toast.LENGTH_LONG)
                                .show();
                    }
                });
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            if (pDialog.isShowing())
                pDialog.dismiss();
        }
    }

    /**
     * Unused
     *
     */

//    public void logToFile(View view) {
//        File file = this.getExternalCacheDir();
//        File file2 = new File(file, FILENAME);
//        try {
//            FileOutputStream fos = new FileOutputStream(file2);
//            for (StringMacAddress mAddress : majorMinor) {
//                if (mAddress.majorMinor.equals("")) continue;
//                String string = beaconLog.getLog(mAddress.mAddress);
//                fos.write(string.getBytes());
//            }
//            fos.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    public void updateSpinner(){
//        Spinner spinner = (Spinner) findViewById(R.id.logBeaconsSpinner);
//        ArrayAdapter<StringMacAddress> adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, majorMinor);
//        spinner.setAdapter(adapter2);
//    }
}

