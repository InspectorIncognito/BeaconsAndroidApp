package blopa.beacons;


import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;
import com.estimote.sdk.SystemRequirementsChecker;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Example extends AppCompatActivity {

        BeaconManager beaconManager;
        Region region;
        List<Beacon> nearestBeacons;
        private NearestBeaconAdapter adapter;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_example);

            Log.d("Probando", "onCreate");

            setupListView();
            setupBeaconSDK();
        }

        public void setupListView() {
            ListView listView = (ListView) findViewById(R.id.nearestBeaconsList);
            nearestBeacons = new ArrayList<>();
            adapter = new NearestBeaconAdapter(this, nearestBeacons);
            listView.setAdapter(adapter);
        }

        public void setupBeaconSDK() {
            beaconManager = new BeaconManager(this);
            beaconManager.setForegroundScanPeriod(500, 0);

            region = new Region("Region", null/*UUID.fromString("7eccfcfa-f334-4042-9dc2-5b5432c33e06")*/, null, null);

            beaconManager.setRangingListener(new BeaconManager.RangingListener() {
                @Override
                public void onBeaconsDiscovered(Region region, List<Beacon> list) {
                    nearestBeacons.clear();
                    nearestBeacons.addAll(list);
                    adapter.notifyDataSetChanged();
                }
            });
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

        public void disconnectFromService() {
            beaconManager.stopRanging(region);
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

                Log.d("Probando", "getView");
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
    }

