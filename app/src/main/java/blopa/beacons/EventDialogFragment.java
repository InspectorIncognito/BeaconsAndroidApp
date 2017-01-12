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

    EventActivityInterface eventCallback;
    private EditText mEdit;

    @Override
    public void onAttach(Context activity) {
        super.onAttach(activity);

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
        mEdit = new EditText(getActivity());
        return new AlertDialog.Builder(getActivity())
                .setMessage("Event Name")
                .setPositiveButton("OK",new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        eventCallback.onTextSend(mEdit.getText().toString());
                    }})
                .setNegativeButton("CANCEL", null).setView(mEdit).create();

    }
}