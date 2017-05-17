package at.qurps.noefinderlein.app;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by roman on 16.04.17.
 */

public class DialogFragment_PictureConsent extends DialogFragment {

    public interface NoticeDialogListener {
        public void onDialogPositiveClick(Uri pImageUri);
    }

    private NoticeDialogListener mListener;
    private Uri pImageUri;
    private Context mContext;



    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = this.getArguments();
        mContext=getActivity();
        pImageUri = Uri.parse(bundle.getString("imageUri"));
    }

    /** The system calls this only when creating the layout in a dialog. */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        View newView = inflater.inflate(R.layout.dialog_consent, null);

        TextView t2 = (TextView) newView.findViewById(R.id.dialogPictureText);
        t2.setMovementMethod(LinkMovementMethod.getInstance());

        ImageView iv = (ImageView) newView.findViewById(R.id.dialogPicture);
        iv.setImageURI(pImageUri);
        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(newView)
            // Add action buttons
            .setPositiveButton(R.string.consent, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    // sign in the user ...
                    mListener.onDialogPositiveClick(pImageUri);
                }
            })
            .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {

                }
            });
        return builder.create();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (NoticeDialogListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement NoticeDialogListener");
        }
    }
}
