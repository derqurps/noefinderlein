package at.qurps.noefinderlein.app;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import de.cketti.mailto.EmailIntentBuilder;


public class DialogFragment_LogView extends DialogFragment {

    public static final String KEY_ARGUMENT_TEXT = "arguments_text";
    public static final String TAG = "logViewDialogFragment";

    private String text;
    /*public DialogFragment_LogView() {

    }
    /*static DialogFragment_LogView newInstance() {
        return new DialogFragment_LogView();
    }*/

    @Override
    public void onStart()
    {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null)
        {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            dialog.getWindow().setLayout(width, height);
        }
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_dialog_log_view, container, false);
        View tv = v.findViewById(R.id.dfLogviewerText);
        ((TextView)tv).setText(this.text);

        View cancel = v.findViewById(R.id.logViewCancel);
        View ok = v.findViewById(R.id.logViewOk);
        ((Button)cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
        ((Button)ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean success = EmailIntentBuilder.from(getActivity())
                        .to("derqurps@gmail.com")
                        .subject("Log Data")
                        .body(text)
                        .start();
            }
        });
        return v;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.text = getArguments().getString(KEY_ARGUMENT_TEXT);
    }
}
