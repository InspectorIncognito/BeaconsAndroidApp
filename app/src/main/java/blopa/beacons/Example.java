package blopa.beacons;


import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.MacAddress;
import com.estimote.sdk.Region;
import com.estimote.sdk.SystemRequirementsChecker;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

public class Example extends AppCompatActivity {

    BeaconManager beaconManager;
    Region region;
    List<Beacon> nearestBeacons;
    List<StringMacAddress> majorMinor;
    private NearestBeaconAdapter adapter;
    private BeaconLog beaconLog;
    private long threshold;
    private long time;
    private static final String FILENAME = "testBeacon.txt";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_example);
        Spinner spinner = (Spinner) findViewById(R.id.logBeaconsSpinner);
        threshold= 5;
        time= 0;
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position != 0)
                    getLog(majorMinor.get(position).mAddress);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        setupListView();
        setupBeaconSDK();
    }

    public void setupListView() {
        Calendar c = Calendar.getInstance();
        time = c.getTimeInMillis();

        ListView listView = (ListView) findViewById(R.id.nearestBeaconsList);
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

                    SimpleDateFormat dateMeasure = new SimpleDateFormat("dd/MM/yyyy");
                    SimpleDateFormat timeMeasure = new SimpleDateFormat("HH:mm:ss");
                    String strDate = dateMeasure.format(c.getTime());
                    String strTime = timeMeasure.format(c.getTime());
                    beaconLog.addMeasure(list, strDate, strTime);

                    updateSpinner();

                    time= c.getTimeInMillis();
                }
            }
        });
    }
    public void logToFile(View view) {
        File file = this.getExternalCacheDir();
        File file2 = new File(file, FILENAME);
        try {
            FileOutputStream fos = new FileOutputStream(file2);
            for (StringMacAddress mAddress : majorMinor) {
                if (mAddress.majorMinor.equals("")) continue;
                String string = beaconLog.getLog(mAddress.mAddress);
                fos.write(string.getBytes());
            }
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public JSONArray createJsonArray() throws JSONException {
        JSONArray json = new JSONArray();
        for (StringMacAddress mAddress : majorMinor) {
            if (mAddress.majorMinor.equals("")) continue;
            json.put(beaconLog.createJSONBeacon(mAddress.mAddress.toString()));
        }
        return json;
    }

    public void sendJson(View view) throws JSONException {
        Log.d("Json", createJsonArray().toString());
        //TODO Connect to server
    }

    private void addBeaconsToOptions(List<Beacon> list) {
        for(Beacon beacon: list){
            MacAddress mAddress = beacon.getMacAddress();
            if(beaconLog.exist(mAddress.toString()))
                continue;
            majorMinor.add(new StringMacAddress(String.format("%d:%d", beacon.getMajor(), beacon.getMinor()),mAddress.toString()));
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        SystemRequirementsChecker.checkWithDefaultDialogs(this);

        connectToService();
    }

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

    public void updateSpinner(){
        Spinner spinner = (Spinner) findViewById(R.id.logBeaconsSpinner);
        ArrayAdapter<StringMacAddress> adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, majorMinor);
        spinner.setAdapter(adapter2);
    }

    public void disconnectFromService() {
        beaconManager.stopRanging(region);
    }

    public void getLog(String mAddress) {
        Log.d("BeaconLog",beaconLog.getLog(mAddress));
    }

    class StringMacAddress {
        String majorMinor;
        String mAddress;

        StringMacAddress(String string, String mAddress){
            this.majorMinor = string;
            this.mAddress = mAddress;
        }

        @Override
        public String toString(){
            return this.majorMinor;
        }
    }





    private class NearestBeaconAdapter extends ArrayAdapter<Beacon> {
        Context context;
        List<Beacon> beacons;

        NearestBeaconAdapter(Context context, List<Beacon> objects) {
            super(context, -1, objects);
            this.context = context;
            this.beacons = objects;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View rowView = convertView;

            if (rowView == null) {
                LayoutInflater inflater = LayoutInflater.from(context);
                rowView = inflater.inflate(R.layout.beacon_row, null);
                ViewHolder viewHolder = new ViewHolder();
                viewHolder.power = (TextView) rowView.findViewById(R.id.powerTextView);
                viewHolder.minor = (TextView) rowView.findViewById(R.id.minorTextView);
                viewHolder.major = (TextView) rowView.findViewById(R.id.majorTextView);
                rowView.setTag(viewHolder);
            }

            ViewHolder holder = (ViewHolder) rowView.getTag();

            Beacon beacon = beacons.get(position);
            holder.power.setText(String.valueOf(beacon.getRssi()));
            holder.minor.setText(String.valueOf(beacon.getMinor()));
            holder.major.setText(String.valueOf(beacon.getMajor()));

            return rowView;
        }

        class ViewHolder {
            TextView power;
            TextView minor;
            TextView major;
        }
    }




    private class BeaconLog {

        HashMap<String, List<BeaconLogAdapter>> beaconsLog;

        BeaconLog(){
            beaconsLog = new HashMap<String, List<BeaconLogAdapter>>();
        }

        void addMeasure(List<Beacon> list, String date, String time){
            for(Beacon beacon: list) {
                String beaconAddress = beacon.getMacAddress().toString();
                if(beaconsLog.containsKey(beaconAddress)){
                    beaconsLog.get(beaconAddress).add(new BeaconLogAdapter(beacon, date, time));
                }
                else{
                    List<BeaconLogAdapter> beacons = new ArrayList<BeaconLogAdapter>();
                    beacons.add(new BeaconLogAdapter(beacon, date, time));
                    beaconsLog.put(beaconAddress, beacons);
                }
            }
        }

        boolean exist(String mAddress){
            return beaconsLog.containsKey(mAddress);
        }


        String getLog(String mAddress){
            StringBuilder log = new StringBuilder("");
            for (BeaconLogAdapter beaconLog: beaconsLog.get(mAddress)){
                log.append(beaconLog.toString()+"\n");
            }
            return log.toString();
        }

        JSONObject createJSONBeacon(String mAddress) throws JSONException {

            JSONObject beaconJson = new JSONObject();
            JSONArray logJson = new JSONArray();
            List<BeaconLogAdapter> beaconLogList = beaconsLog.get(mAddress);

            for (BeaconLogAdapter beacon: beaconLogList){
                JSONObject beaconJsArray = new JSONObject();

                beaconJsArray.put("Date", beacon.date);
                beaconJsArray.put("Time", beacon.time);
                beaconJsArray.put("RSSI", beacon.beacon.getRssi());

                logJson.put(beaconJsArray);
            }

            if(beaconLogList.isEmpty()) return beaconJson;

            BeaconLogAdapter beaconLog = beaconLogList.get(0);
            beaconJson.put("Mac Address", beaconLog.beacon.getMacAddress());
            beaconJson.put("UUID",beaconLog.beacon.getProximityUUID());
            beaconJson.put("Major",beaconLog.beacon.getMajor());
            beaconJson.put("Minor",beaconLog.beacon.getMinor());
            beaconJson.put("Measure Power", beaconLog.beacon.getMeasuredPower());
            beaconJson.put("Log", logJson);

            return beaconJson;
        }

        private class BeaconLogAdapter {
            Beacon beacon;
            String time;
            String date;

            BeaconLogAdapter(Beacon beacon, String date, String time) {
                this.beacon = beacon;
                this.time = time;
                this.date = date;
            }

            @Override
            public String toString() {
                return String.format("%s %s --> Beacon %s: UUID %s; Major %d; Minor %d; Measure Power %d; RSSI %d",
                        this.date, this.time, this.beacon.getMacAddress().toString(),
                        this.beacon.getProximityUUID().toString(),
                        this.beacon.getMajor(), this.beacon.getMinor(),
                        this.beacon.getMeasuredPower(),
                        this.beacon.getRssi());
            }
        }
    }
}

