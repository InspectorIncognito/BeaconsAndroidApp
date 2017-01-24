package blopa.beacons;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.estimote.sdk.Beacon;

import java.util.List;

/**
 * Created by Blopa on 12-01-2017.
 */



public class NearestBeaconAdapter extends ArrayAdapter<Beacon> {
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
            viewHolder.uuid = (TextView) rowView.findViewById(R.id.uuidTextView);
            viewHolder.minor = (TextView) rowView.findViewById(R.id.minorTextView);
            viewHolder.major = (TextView) rowView.findViewById(R.id.majorTextView);
            rowView.setTag(viewHolder);
        }

        ViewHolder holder = (ViewHolder) rowView.getTag();

        Beacon beacon = beacons.get(position);
        holder.power.setText(String.valueOf(beacon.getRssi()));
        holder.uuid.setText(String.valueOf(beacon.getProximityUUID()));
        holder.minor.setText(String.valueOf(beacon.getMinor()));
        holder.major.setText(String.valueOf(beacon.getMajor()));

        return rowView;
    }

    class ViewHolder {
        TextView power;
        TextView minor;
        TextView major;
        TextView uuid;
    }
}
