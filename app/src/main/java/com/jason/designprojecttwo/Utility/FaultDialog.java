package com.jason.designprojecttwo.Utility;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.jason.designprojecttwo.R;

public class FaultDialog extends AppCompatDialogFragment {

    private EditText mFault;
    private FaultDialogListener listener;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.layout_dialog, null);

        builder.setView(view)
                .setTitle("New Fault")
                .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setPositiveButton("Update", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String fault = mFault.getText().toString().trim();
                        listener.applyText(fault);
                    }
                });
        mFault = view.findViewById(R.id.edit_fault);
        return builder.create();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            listener = (FaultDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + "Implement DescriptionDialogListener");
        }

    }

    public interface FaultDialogListener {
        void applyText(String fault);
    }
}
