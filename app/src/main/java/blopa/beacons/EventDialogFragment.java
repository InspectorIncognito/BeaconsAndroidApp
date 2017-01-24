package blopa.beacons;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.support.v4.app.DialogFragment ;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.EditText;


/**
 * Created by Blopa on 10-01-2017.
 */

public class EventDialogFragment extends DialogFragment {

    String time;
    EventActivityInterface eventCallback;
    private EditText mEdit;

    @Override
    public void onAttach(Context activity) {
        super.onAttach(activity);

        Bundle bundle = getArguments();
        this.time = bundle.getString("Time");

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
        final String[] array = getResources().getStringArray(R.array.event_name_array);
        mEdit = new EditText(getActivity());
        mEdit.setHint("Other");
        mEdit.setPadding(35,0,0,10);
        return new AlertDialog.Builder(getActivity())
                .setTitle("Event Name")
                .setItems(R.array.event_name_array, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        eventCallback.onTextSend(time,array[which]);
                    }
                })
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        eventCallback.onTextSend(time, mEdit.getText().toString());
                    }
                })
                .setNegativeButton("CANCEL", null)
                .setView(mEdit)
                .create();
    }
}