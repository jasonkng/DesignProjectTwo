package com.jason.designprojecttwo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.jason.designprojecttwo.Utility.FaultDialog;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

public class MaintenanceDetail extends AppCompatActivity implements FaultDialog.FaultDialogListener {

    private TextView mUniqueId, mFault, mDescription, mLocation, mDate, mRequested, mStatus;
    private Spinner mStatusSpinner;
    private ImageView mImageView;
    private LinearLayout linearLayout;
    private RelativeLayout relativeLayout;
    String[] status = {"Please select status..", "Approved", "Rejected", "Completed", "Closed"};

    private static final String KEY_ID = "uniqueID";
    private static final String KEY_FAULT = "fault";
    private static final String KEY_DESCRIPTION = "description";
    private static final String KEY_DATE = "date";
    private static final String KEY_LOCATION = "location";
    private static final String KEY_REQUEST = "requested";
    private static final String KEY_STATUS = "status";
    private static final String KEY_URI = "imageuri";
    private static final String C_HISTORY = "MaintenanceHistory";
    private String filePath = "";
    private String finalStatus = "";
    private String imageUri = "";
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    DocumentReference documentReference;

    Map<String, Object> info;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maintenance_detail);
        setTitle("Maintenance Details");

        //Getting Data from Firestore
        Intent intent = getIntent();
        filePath = intent.getStringExtra("filePath");
        documentReference = db.document(filePath);

        linearLayout = findViewById(R.id.linearOne);
        relativeLayout = findViewById(R.id.relativeOne);
        linearLayout.setVisibility(View.INVISIBLE);

        mUniqueId = findViewById(R.id.amd_uniqueID);
        mFault = findViewById(R.id.amd_fault);
        mDescription = findViewById(R.id.amd_description);
        mLocation = findViewById(R.id.amd_location);
        mDate = findViewById(R.id.amd_date);
        mRequested = findViewById(R.id.amd_requested);
        mStatus = findViewById(R.id.amd_status);
        mImageView = findViewById(R.id.amd_image);
        mStatusSpinner = findViewById(R.id.amd_spinner);

        getDetails(documentReference);
        populateSpinner();
    }

    private void getDetails(DocumentReference documentReference) {
        documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot snapshot) {
                info = snapshot.getData();
                mUniqueId.setText(info.get(KEY_ID).toString());
                mFault.setText(info.get(KEY_FAULT).toString());
                mDescription.setText(info.get(KEY_DESCRIPTION).toString());
                mLocation.setText(info.get(KEY_LOCATION).toString());
                mDate.setText(info.get(KEY_DATE).toString());
                mRequested.setText(info.get(KEY_REQUEST).toString());
                mStatus.setText(info.get(KEY_STATUS).toString());

                imageUri = info.get(KEY_URI).toString();
                Picasso.get().load(imageUri).fit().into(mImageView);
                linearLayout.setVisibility(View.VISIBLE);

                if (mStatus.getText().toString().equals("Closed")) {
                    relativeLayout.setVisibility(View.INVISIBLE);
                    mFault.setTextColor(mDate.getTextColors());
                }

                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText(KEY_ID, mUniqueId.getText().toString());
                clipboard.setPrimaryClip(clip);

                Toast.makeText(MaintenanceDetail.this, "ID copied to clipboard", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MaintenanceDetail.this, "Error Retrieving Information!", Toast.LENGTH_SHORT).show();
            }
        });

        mFault.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mStatus.getText().toString().equals("Closed")) {
                } else {
                    openDialog();
                }
            }
        });
    }

    private void populateSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, status);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mStatusSpinner.setAdapter(adapter);

        mStatusSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        finalStatus = "null";
                        break;
                    case 1:
                        finalStatus = "Approved";
                        break;
                    case 2:
                        finalStatus = "Rejected";
                        break;
                    case 3:
                        finalStatus = "Completed";
                        break;
                    case 4:
                        finalStatus = "Closed";
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Toast.makeText(MaintenanceDetail.this, "Please select an option", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openDialog() {
        FaultDialog descriptionDialog = new FaultDialog();
        descriptionDialog.show(getSupportFragmentManager(), "Description Dialog");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.save, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.save_request:
                saveRequest();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void saveRequest() {
        if (finalStatus.equals("null")) {
            finish();
        } else {
            if (finalStatus.equals("Closed")) {

                //Updating Datasheet
                String uniqueID = mUniqueId.getText().toString();
                String alphabetID = uniqueID.replaceAll("[^A-Za-z]+", "");
                DocumentReference mDocumentReference = db.collection(alphabetID).document(uniqueID);
                final String currentDate = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
                        .format(new Date());
                mDocumentReference.update("lastWorkOrderClosed", currentDate);

                //Create new Collection
                String documentFilePath = filePath.split("/")[1];
                DocumentReference historyReference = db.collection(C_HISTORY).document(documentFilePath);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    info.replace(KEY_STATUS, finalStatus);
                }
                historyReference.set(info);
            }

            documentReference.update(KEY_STATUS, finalStatus).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Toast.makeText(MaintenanceDetail.this, "Status Updated!", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(MaintenanceDetail.this, "Oops, something went wrong", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public void applyText(String fault) {
        mFault.setText(fault);
        documentReference.update(KEY_FAULT, fault).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(MaintenanceDetail.this, "Fault Updated!", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MaintenanceDetail.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
            }
        });

    }
}

