package com.example.jasper;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/*
************************************
You have to include following two lines where you want to show a dialog box
* *************************************
*/
/**
 DialogBox dialogBox=new DialogBox();

 dialogBox.show(getSupportFragmentManager(),"exampleDialog");


 **/

public class DialogBox extends AppCompatDialogFragment {
    EditText editText;
    TextView title;

    /**You have to set the title of dialog box by setting text of "title" textview*/

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater=getActivity().getLayoutInflater();
        View view =inflater.inflate(R.layout.custom_dialog_box,null);


        editText=view.findViewById(R.id.name);
        title=view.findViewById(R.id.title);

        builder.setPositiveButton("Add",new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // FIRE ZE MISSILES!
            }
        })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });

        builder.setView(view);
        // Create the AlertDialog object and return it
        return builder.create();
    }
}
