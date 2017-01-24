package blopa.beacons;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

/**
 * Created by Blopa on 24-01-2017.
 */

public class DeviceIDDialogFragment extends DialogFragment {
    String id;
    EventActivityInterface eventCallback;

    @Override
    public void onAttach(Context activity) {
        super.onAttach(activity);

        Bundle bundle = getArguments();
        this.id = bundle.getString("Device");

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            eventCallback = (EventActivityInterface) getActivity();
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnHeadlineSelectedListener");
        }
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {
         return new AlertDialog.Builder(getActivity())
                .setTitle("DeviceID")
                 .setMessage(id)
                .create();
    }
}
