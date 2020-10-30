package com.jason.designprojecttwo.LoginPages;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.jason.designprojecttwo.MainActivity;
import com.jason.designprojecttwo.R;

public class FirstPage extends AppCompatActivity {

    FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    @Override
    protected void onStart() {
        super.onStart();
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first_page);

        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser firebaseUser = mFirebaseAuth.getCurrentUser();
                if (firebaseUser != null) {
                    Toast.makeText(FirstPage.this, "Login Success!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(FirstPage.this, MainActivity.class));
                } else
                    startActivity(new Intent(FirstPage.this, LoginActivity.class));
            }
        };

    }
}