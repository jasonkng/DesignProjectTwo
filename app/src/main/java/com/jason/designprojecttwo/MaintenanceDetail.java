package com.jason.designprojecttwo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MaintenanceDetail extends AppCompatActivity {

    private TextView mUniqueId, mFault, mInitialD, mLocation, mDate, mRequested, mStatus;
    private Spinner mStatusSpinner;
    private Button mUpdateButton;

    private static final String KEY_ID = "uniqueID";
    private static final String KEY_FAULT = "fault";
    private static final String KEY_DIAGNOSIS = "initialDiagnosis";
    private static final String KEY_DATE = "date";
    private static final String KEY_LOCATION = "location";
    private static final String KEY_REQUEST = "requested";
    private static final String KEY_STATUS = "status";
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maintenance_detail);
        setTitle("Maintenance Details");

        //Getting Data from Firestore
        Intent intent = getIntent();
        String filePath = intent.getStringExtra("filePath");
        final DocumentReference documentReference = db.document(filePath);

        mUniqueId = findViewById(R.id.amd_uniqueID);
        mFault = findViewById(R.id.amd_fault);
        mInitialD = findViewById(R.id.amd_initialD);
        mLocation = findViewById(R.id.amd_location);
        mDate = findViewById(R.id.amd_date);
        mRequested = findViewById(R.id.amd_requested);
        mStatus = findViewById(R.id.amd_status);

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

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MaintenanceDetail.this, "Error Retrieving Information!", Toast.LENGTH_SHORT).show();
            }
        });

        //Populating Spinner
        mStatusSpinner = findViewById(R.id.amd_spinner);
        mUpdateButton = findViewById(R.id.amd_update);
        List<StatusModel> statusModel = new ArrayList<>();
        StatusModel statusOne = new StatusModel("Approved");
        statusModel.add(statusOne);
        StatusModel statusTwo = new StatusModel("Rejected");
        statusModel.add(statusTwo);
        StatusModel statusThree = new StatusModel("Completed");
        statusModel.add(statusThree);
        StatusModel statusFour = new StatusModel("Closed");
        statusModel.add(statusFour);

        ArrayAdapter<StatusModel> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, statusModel);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mStatusSpinner.setAdapter(adapter);

        mUpdateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StatusModel originalStatus = (StatusModel) mStatusSpinner.getSelectedItem();
                String statusName = originalStatus.getStatusName();
                documentReference.update(KEY_STATUS, statusName).addOnSuccessListener(new OnSuccessListener<Void>() {
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
        });
    }
}