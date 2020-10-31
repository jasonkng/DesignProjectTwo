package com.jason.designprojecttwo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.jason.designprojecttwo.Utility.StatusModel;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MaintenanceDetail extends AppCompatActivity {

    private TextView mUniqueId, mFault, mInitialD, mLocation, mDate, mRequested, mStatus;
    private Spinner mStatusSpinner;
    private ImageView mImageView;
    private LinearLayout linearLayout;
    String status[] = {"Please select status..", "Approved", "Rejected", "Completed", "Closed"};

    private static final String KEY_ID = "uniqueID";
    private static final String KEY_FAULT = "fault";
    private static final String KEY_DIAGNOSIS = "initialDiagnosis";
    private static final String KEY_DATE = "date";
    private static final String KEY_LOCATION = "location";
    private static final String KEY_REQUEST = "requested";
    private static final String KEY_STATUS = "status";
    private static final String KEY_URI = "imageuri";
    private String finalStatus = "";
    private String imageUri = "";
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    DocumentReference documentReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maintenance_detail);
        setTitle("Maintenance Details");

        //Getting Data from Firestore
        Intent intent = getIntent();
        String filePath = intent.getStringExtra("filePath");
        documentReference = db.document(filePath);

        linearLayout = findViewById(R.id.linearOne);
        linearLayout.setVisibility(View.INVISIBLE);

        mUniqueId = findViewById(R.id.amd_uniqueID);
        mFault = findViewById(R.id.amd_fault);
        mInitialD = findViewById(R.id.amd_initialD);
        mLocation = findViewById(R.id.amd_location);
        mDate = findViewById(R.id.amd_date);
        mRequested = findViewById(R.id.amd_requested);
        mStatus = findViewById(R.id.amd_status);
        mImageView = findViewById(R.id.amd_image);

        documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot snapshot) {
                Map<String, Object> info = snapshot.getData();
                mUniqueId.setText(info.get(KEY_ID).toString());
                mFault.setText(info.get(KEY_FAULT).toString());
                mInitialD.setText(info.get(KEY_DIAGNOSIS).toString());
                mLocation.setText(info.get(KEY_LOCATION).toString());
                mDate.setText(info.get(KEY_DATE).toString());
                mRequested.setText(info.get(KEY_REQUEST).toString());
                mStatus.setText(info.get(KEY_STATUS).toString());

                //TODO need to fix resize maybe
                imageUri = info.get(KEY_URI).toString();
                Picasso.get().load(imageUri).fit().centerInside().into(mImageView);
                linearLayout.setVisibility(View.VISIBLE);

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MaintenanceDetail.this, "Error Retrieving Information!", Toast.LENGTH_SHORT).show();
            }
        });

        //Populating Spinner
        mStatusSpinner = findViewById(R.id.amd_spinner);

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

            }
        });
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
            Toast.makeText(MaintenanceDetail.this, "Please select a status!", Toast.LENGTH_SHORT).show();
            return;
        } else {
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
}