package blopa.beacons;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Blopa on 12-01-2017.
 */

public class EventLog{
    String timeStamp;
    String name;

    EventLog(String time, String text){
        timeStamp=time;
        name=text;
    }

    JSONObject getJsonObject() throws JSONException {
        JSONObject obj = new JSONObject();

        obj.put("Time", timeStamp );
        obj.put("Event", name );

        return obj;
    }
}
