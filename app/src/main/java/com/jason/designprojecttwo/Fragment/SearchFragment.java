package com.jason.designprojecttwo.Fragment;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.jason.designprojecttwo.NewMaintenanceActivity;
import com.jason.designprojecttwo.R;
import com.jason.designprojecttwo.Utility.ScanActivity;
import com.squareup.picasso.Picasso;

import java.util.Map;


public class SearchFragment extends Fragment {

    private EditText mEditText;
    private ImageButton mSearchButton, mCameraButton;
    private TextView mSpecification, mLocation, mContactCard, mMaintenanceDetail, mDSpecification;
    private CardView mCardViewOne, mCardViewTwo, mCardViewThree;
    private static final String TAG = "SearchFragment";

    private String uniqueID = "";
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private DocumentReference documentReference;

    private static final int REQUEST_CODE = 100;
    private static final int PERMISSION_REQUEST = 200;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_search, container, false);
        getActivity().setTitle("Element Information");

        mEditText = v.findViewById(R.id.search_search_bar);
        mSearchButton = v.findViewById(R.id.search_button);
        mCameraButton = v.findViewById(R.id.search_camera);
        mDSpecification = v.findViewById(R.id.default_specification);
        mSpecification = v.findViewById(R.id.search_specification);
        mMaintenanceDetail = v.findViewById(R.id.search_maintenance_details);
        mContactCard = v.findViewById(R.id.search_contact);

        mCardViewOne = v.findViewById(R.id.search_cardOne);
        mCardViewTwo = v.findViewById(R.id.search_cardTwo);
        mCardViewThree = v.findViewById(R.id.search_cardThree);

        mCardViewOne.setVisibility(View.INVISIBLE);
        mCardViewTwo.setVisibility(View.INVISIBLE);
        mCardViewThree.setVisibility(View.INVISIBLE);

        mCameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUEST);
                }
                mEditText.setText("");
                Intent intent = new Intent(getActivity(), ScanActivity.class);
                startActivityForResult(intent, REQUEST_CODE);
            }
        });

        mSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View view = getActivity().getCurrentFocus();
                if (view!= null){
                    InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }

                uniqueID = mEditText.getText().toString();
                if(uniqueID.isEmpty()){
                    mEditText.setError("Please enter unique identifier");
                    mEditText.requestFocus();
                }
                else{
                    String alphabetID = uniqueID.replaceAll("[^A-Za-z]+", "");

                    documentReference = db.collection(alphabetID).document(uniqueID);
                    documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot snapshot) {
                            if(snapshot.exists()){
                                Map<String, Object> info = snapshot.getData();

                                String defaultSpec = "Specification for " + mEditText.getText().toString();
                                mDSpecification.setText(defaultSpec);

                                String brand = info.get("productBrand").toString();
                                String productCode = info.get("productCode").toString();
                                String dimension = info.get("productDimension").toString();
                                String voltage = info.get("productOC").toString();
                                String cardViewOne = "Brand: " + brand + "\nProduct Code: " + productCode
                                        + "\nDimension: " + dimension + "\nOperating Voltage: " + voltage + "\n";

                                mSpecification.setText(cardViewOne);
                                mCardViewOne.setVisibility(View.VISIBLE);

                                String lastWorkOrderClosed = info.get("lastWorkOrderClosed").toString();
                                String maintenancePeriod = info.get("maintenancePeriod").toString();
                                String commonFault = info.get("commonFault").toString();
                                String solution = info.get("solution").toString();
                                String cardViewTwo = "Last Work Order: " + lastWorkOrderClosed + "\nMaintenance Period: " + maintenancePeriod
                                        + "\nCommon Fault: " + commonFault + "\nSolution: " + solution + "\n";

                                mMaintenanceDetail.setText(cardViewTwo);
                                mCardViewTwo.setVisibility(View.VISIBLE);

                                String supplierPOC = info.get("supplierPOC").toString();
                                String supplier = info.get("supplier").toString();
                                String supplierContact = info.get("supplierContact").toString();
                                String supplierEmail = info.get("supplierEmail").toString();
                                String cardViewThree = supplierPOC + "\n" + supplier + "\n" + supplierContact + "\n"
                                        + supplierEmail + "\n";

                                mContactCard.setText(cardViewThree);
                                mCardViewThree.setVisibility(View.VISIBLE);

                            }
                            else{
                                Toast.makeText(getActivity(), "Document does not exist", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }


            }
        });
        return v;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE && resultCode == -1) {
            final Barcode barcode = data.getParcelableExtra("barcode");
            mEditText.post(new Runnable() {
                @Override
                public void run() {
                    mEditText.setText(barcode.displayValue);
                }
            });
        }
    }

}
