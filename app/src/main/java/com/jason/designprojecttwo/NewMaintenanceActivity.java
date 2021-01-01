package com.jason.designprojecttwo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.jason.designprojecttwo.Utility.ScanActivity;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class NewMaintenanceActivity extends AppCompatActivity {
    public static final int REQUEST_CODE_CAMERA = 102;
    private EditText editTextUniqueID, editTextFault, editTextDescription;
    private ImageButton idCamera, imagePickerButton;
    private TextView imagePickerText;
    private ImageView imagePickerView;
    private Uri mImageUri;

    private static final String TAG = "NewMaintenanceActivity";
    private static final String KEY_ID = "uniqueID";
    private static final String KEY_FAULT = "fault";
    private static final String KEY_DESCRIPTION = "description";
    private static final String KEY_DATE = "date";
    private static final String KEY_TIME = "time";

    private static final String KEY_LOCATION = "location";
    private static final String KEY_STATUS = "status";
    private static final String KEY_USER = "requested";
    private static final String KEY_URI = "imageuri";

    private static final String C_FAILEPATH = "MaintenanceRequest";
    private static final String C_HISTORY = "MaintenanceHistory";

    private static final String VALUE_STATUS = "Submitted";

    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private StorageReference storageReference;
    private DocumentReference documentReference;

    private static final int REQUEST_CODE = 100;
    private static final int PERMISSION_REQUEST = 200;
    private static final int PICK_IMAGE_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_maintenance);
        setTitle("Request Form");

        editTextUniqueID = findViewById(R.id.maintenance_unique_id);
        editTextFault = findViewById(R.id.maintenance_fault);
        editTextDescription = findViewById(R.id.maintenance_description);
        idCamera = findViewById(R.id.maintenance_camera);
        imagePickerButton = findViewById(R.id.image_picker_button);
        imagePickerText = findViewById(R.id.image_picker_text);
        imagePickerView = findViewById(R.id.image_picker_view);

        storageReference = FirebaseStorage.getInstance().getReference("maintenancePicture");

        idCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(NewMaintenanceActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(NewMaintenanceActivity.this, new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUEST);
                }
                editTextUniqueID.setText("");
                Intent intent = new Intent(NewMaintenanceActivity.this, ScanActivity.class);
                startActivityForResult(intent, REQUEST_CODE);
            }
        });

        imagePickerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent, PICK_IMAGE_REQUEST);

                View view = NewMaintenanceActivity.this.getCurrentFocus();
                if(view!= null){
                    InputMethodManager imm = (InputMethodManager) NewMaintenanceActivity.this.getSystemService(Activity.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
            }
        });
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK){
            switch (requestCode){
                case REQUEST_CODE:
                    final Barcode barcode = data.getParcelableExtra("barcode");
                    editTextUniqueID.post(new Runnable() {
                        @Override
                        public void run() {
                            editTextUniqueID.setText(barcode.displayValue);
                        }
                    });
                    break;

                case PICK_IMAGE_REQUEST:
                    mImageUri = data.getData();
                    Picasso.get().load(mImageUri).fit().into(imagePickerView);
                    imagePickerButton.setVisibility(View.INVISIBLE);
                    imagePickerText.setVisibility(View.INVISIBLE);
                    break;
            }
        }
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
        String uniqueID = editTextUniqueID.getText().toString();
        String fault = editTextFault.getText().toString().trim();

        if (uniqueID.isEmpty() || fault.isEmpty()) {
            Toast.makeText(this, "Please insert uniqueID and fault", Toast.LENGTH_SHORT).show();
            return;
        }

        if (mImageUri == null) {
            Toast.makeText(this, "Please select image!", Toast.LENGTH_SHORT).show();
            return;
        }

        findLocation(uniqueID);
    }

    private void findLocation(String uniqueID) {
        String alphabetID = uniqueID.replaceAll("[^A-Za-z]+", "");
        documentReference = db.collection(alphabetID).document(uniqueID);
        documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot snapshot) {
                if (snapshot.exists()) {
                    String location = snapshot.getString("location");
                    continueUpload(location);
                } else {
                    continueUpload(null);
                }
            }
        });
    }

    private void continueUpload(final String location) {
        final String uniqueID = editTextUniqueID.getText().toString();
        final String fault = editTextFault.getText().toString().trim();
        final String description = editTextDescription.getText().toString().trim();
        final String email = user.getEmail().split("@")[0];
        final String currentDate = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
                .format(new Date());
        final String currentTime = new SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                .format(new Date());


        // Issue solved by denying and allowing permission, bug in Firebase
        final String timeMillis = String.valueOf(System.currentTimeMillis());
        final StorageReference fileReference = storageReference.child(timeMillis + "." + getFileExtension(mImageUri));
        fileReference.putFile(mImageUri).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }
                return fileReference.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    Uri downloadUri = task.getResult();
                    Log.e(TAG, "then: " + downloadUri.toString());
                    String uriFinal = downloadUri.toString();

                    Map<String, Object> uploadingObject = new HashMap<>();
                    uploadingObject.put(KEY_ID, uniqueID);
                    uploadingObject.put(KEY_FAULT, fault);
                    uploadingObject.put(KEY_DESCRIPTION, description);
                    uploadingObject.put(KEY_DATE, currentDate);
                    uploadingObject.put(KEY_TIME, currentTime);
                    uploadingObject.put(KEY_LOCATION, location);
                    uploadingObject.put(KEY_USER, email);
                    uploadingObject.put(KEY_STATUS, VALUE_STATUS);
                    uploadingObject.put(KEY_URI, uriFinal);

                    db.collection(C_FAILEPATH).document(timeMillis).set(uploadingObject)
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(NewMaintenanceActivity.this, "Oops, something happened!", Toast.LENGTH_SHORT).show();
                                    Log.d(TAG, e.toString());
                                }
                            });

                    documentReference.update("lastWorkOrderSubmitted", currentDate)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(NewMaintenanceActivity.this, "Cloud Updated", Toast.LENGTH_SHORT).show();
                                    finish();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(NewMaintenanceActivity.this, "Oops, something happened!", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(NewMaintenanceActivity.this, "Upload failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(contentResolver.getType(uri));
    }
}