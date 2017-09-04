package com.example.matth.patientinformationforum;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.support.annotation.IdRes;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

    EditText goalText, ailmentText, userName;
    TextView uploadQuery;
    RadioGroup radioGroup;
    Button uploadButt, completeButt;
    RadioButton selectedHand = null;
    private static final int PICKFILE_RESULT_CODE = 1;
    Uri FilePathAttatch=null;
    boolean fileFound = false;

    String FilePathText = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        setContentView(R.layout.activity_main);
        goalText = (EditText) findViewById(R.id.goalText);
        ailmentText = (EditText) findViewById(R.id.ailmentText);
        radioGroup = (RadioGroup) findViewById(R.id.radioGroup);
        uploadQuery = findViewById(R.id.uploadQuery);
        uploadButt = (Button) findViewById(R.id.uploadButt);
        completeButt = (Button) findViewById(R.id.complete);
        userName = (EditText)findViewById(R.id.username);


        completeButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pressedStart();
            }
        });

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, @IdRes int i) {
                selectedHand = findViewById(i);
                Log.e("Radio", i + " selected");
            }
        });

        uploadButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //open file locator
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");
                startActivityForResult(intent, PICKFILE_RESULT_CODE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub

        switch (requestCode) {
            case PICKFILE_RESULT_CODE:
                //the user choose a file
                if (resultCode == RESULT_OK) {
                    FilePathAttatch = data.getData();
                    uploadQuery.setText("File Uploaded: " + FilePathAttatch);
                    fileFound = true;
                } else {
                    fileFound = false;
                }
                break;
        }
    }

    public void pressedStart() {
        if (determineCompleteFill()) {
            emailFile(composeFile(),FilePathAttatch);
        }
    }

    public boolean determineCompleteFill() {
        radioGroup.getCheckedRadioButtonId();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

// 2. Chain together various setter methods to set the dialog characteristics

        if (goalText.getText().toString().equals("")) {
            builder.setTitle("Please list some goals");
            builder.setPositiveButton("close", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // User clicked OK button
                }
            });

            final AlertDialog dialog = builder.create();
            dialog.show();
            return false;
        }
        if (ailmentText.getText().toString().equals("") && !fileFound) {
            builder.setTitle("Please type out your medical history or upload medical files");
            builder.setNegativeButton("Close", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
            return false;
        }
        if (selectedHand == null) {
            builder.setTitle("Please choose a hand size");
            builder.setPositiveButton("Close", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
            return false;
        }
        return true;

    }


    public File composeFile() {
        //generate a text file to send in the email
        try {
            File root = new File(Environment.getExternalStorageDirectory(), "Notes");
            if (!root.exists()) {
                root.mkdirs();
            }
            File gpxfile = new File(root, "dataToSend.txt");

            FileWriter writer = new FileWriter(gpxfile);
            writer.append("Patient Name: "+ userName.getText().toString()+"\n");
            writer.append("Hand Size: "+ selectedHand.getText().toString()+"\n");
            writer.append("Patient Goals: "+ goalText.getText().toString()+"\n");
            writer.append("Patient Medical Ailment: "+ailmentText.getText().toString()+"\n");
            writer.flush();
            writer.close();
            Toast.makeText(getApplicationContext(), "Saved", Toast.LENGTH_SHORT).show();
            return gpxfile;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    public void emailFile(File file1,Uri file2) {
        //start email
        if(file1==null&&file2==null){
            Toast toast = Toast.makeText(getApplicationContext(),"There was an error accessing memory",Toast.LENGTH_LONG);
        }else{
            String [] reciever = new String[]{"mcstaffo@buffalo.edu"};
            String subject = ("Medical Information");

            Intent mailIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
            mailIntent.putExtra(Intent.EXTRA_EMAIL, reciever);
            mailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
            ArrayList<Uri> uris = new ArrayList<Uri>();
           if(file1!=null) {
                uris.add(Uri.fromFile(file1));
            }
           if(file2 !=null){
               uris.add(file2);
            }
            mailIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
            mailIntent.putExtra(Intent.EXTRA_TEXT, "Patient "+userName.getText().toString()+" is sending you an email with his/her medical information.");
            mailIntent.setType("message/rfc822");
            startActivity(Intent.createChooser(mailIntent, "Choose an application to send your mail with"));
        }
    }
}