package blopa.beacons;


import android.content.Context;
import android.os.Environment;
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
import com.estimote.sdk.connection.DeviceConnection;
import com.estimote.sdk.connection.DeviceConnectionCallback;
import com.estimote.sdk.connection.DeviceConnectionProvider;
import com.estimote.sdk.connection.exceptions.DeviceConnectionException;
import com.estimote.sdk.connection.scanner.ConfigurableDevice;
import com.estimote.sdk.connection.scanner.ConfigurableDevicesScanner;
import com.estimote.sdk.connection.scanner.DeviceType;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
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
    FileOutputStream outputStream;
    FileInputStream inputStream;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_example);
        Spinner spinner = (Spinner) findViewById(R.id.logBeaconsSpinner);
        threshold= 5;
        time= 0;
        File file = new File(this.getFilesDir(), FILENAME);
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
        majorMinor = new ArrayList<StringMacAddress>();
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

                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                    String strDate = sdf.format(c.getTime());
                    beaconLog.addMeasure(list, strDate);

                    updateSpinner();

                    time= c.getTimeInMillis();
                }
            }
        });
    }
    public void logToFile(View view) {
        File file = this.getExternalCacheDir();
        file.mkdir();
        File file2 = new File(file, FILENAME);
        try {
            FileOutputStream fos = new FileOutputStream(file2);
            for (StringMacAddress mAddress : majorMinor) {
                if (mAddress.majorMinor == "") continue;
                String string = beaconLog.getLog(mAddress.mAddress);
                fos.write(string.getBytes());
            }
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
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

        public NearestBeaconAdapter(Context context, List<Beacon> objects) {
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

        public BeaconLog(){
            beaconsLog = new HashMap<String, List<BeaconLogAdapter>>();
        }

        public void addMeasure(List<Beacon> list, String time){
            for(Beacon beacon: list) {
                String beaconAddress = beacon.getMacAddress().toString();
                if(beaconsLog.containsKey(beaconAddress)){
                    beaconsLog.get(beaconAddress).add(new BeaconLogAdapter(beacon, time));
                }
                else{
                    List<BeaconLogAdapter> beacons = new ArrayList<BeaconLogAdapter>();
                    beacons.add(new BeaconLogAdapter(beacon, time));
                    beaconsLog.put(beaconAddress, beacons);
                }
            }
        }

        public boolean exist(String mAddress){
            return beaconsLog.containsKey(mAddress);
        }


        public String getLog(String mAddress){
            StringBuilder log = new StringBuilder("");
            for (BeaconLogAdapter beaconLog: beaconsLog.get(mAddress)){
                log.append(beaconLog.toString()+"\n");
            }
            return log.toString();
        }

        private class BeaconLogAdapter {
            Beacon beacon;
            String time;

            BeaconLogAdapter(Beacon beacon, String time) {
                this.beacon = beacon;
                this.time = time;
            }

            @Override
            public String toString() {
                return String.format("%s --> Beacon: UUID %s; Major %d; Minor %d; Measure Power %d; RSSI %d",
                        this.time, this.beacon.getProximityUUID().toString(),
                        this.beacon.getMajor(), this.beacon.getMinor(),
                        this.beacon.getMeasuredPower(),
                        this.beacon.getRssi());
            }
        }
    }
}

