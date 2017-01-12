package blopa.beacons;

import com.estimote.sdk.Beacon;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Blopa on 12-01-2017.
 */



public class BeaconLog {

    HashMap<String, List<BeaconLogAdapter>> beaconsLog;

    BeaconLog(){
        beaconsLog = new HashMap<String, List<BeaconLogAdapter>>();
    }

    void addMeasure(List<Beacon> list, String time){
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

    void reset(){
        beaconsLog = new HashMap<String, List<BeaconLogAdapter>>();
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

    JSONObject createJsonBeacon(String mAddress) throws JSONException {

        JSONObject beaconJson = new JSONObject();
        JSONArray logJson = new JSONArray();
        List<BeaconLogAdapter> beaconLogList = beaconsLog.get(mAddress);

        for (BeaconLogAdapter beacon: beaconLogList){
            JSONObject beaconJsArray = new JSONObject();

            beaconJsArray.put("Time", beacon.time);
            beaconJsArray.put("RSSI", beacon.beacon.getRssi());
            beaconJsArray.put("Measure Power", beacon.beacon.getMeasuredPower());

            logJson.put(beaconJsArray);
        }

        if(beaconLogList.isEmpty()) return beaconJson;

        BeaconLogAdapter beaconLog = beaconLogList.get(0);
        beaconJson.put("Mac Address", beaconLog.beacon.getMacAddress());
        beaconJson.put("UUID",beaconLog.beacon.getProximityUUID());
        beaconJson.put("Major",beaconLog.beacon.getMajor());
        beaconJson.put("Minor",beaconLog.beacon.getMinor());
        beaconJson.put("Log", logJson);

        return beaconJson;
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
            return String.format("%s --> Beacon %s: UUID %s; Major %d; Minor %d; Measure Power %d; RSSI %d",
                    this.time, this.beacon.getMacAddress().toString(),
                    this.beacon.getProximityUUID().toString(),
                    this.beacon.getMajor(), this.beacon.getMinor(),
                    this.beacon.getMeasuredPower(),
                    this.beacon.getRssi());
        }
    }
}