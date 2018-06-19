package com.cuchichapp.android.cuchichapp;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;


public class ProfileActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    Intent intent;
    ImageView imageView;
    TextView UserName;
    TextView Email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        imageView = (ImageView) findViewById(R.id.ProfileIMG);
        UserName = (TextView) findViewById(R.id.UserNameTV);
        Email = (TextView) findViewById(R.id.EmailTV);

        mAuth = FirebaseAuth.getInstance();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (mAuth.getCurrentUser()!=null){

                    UserName.setText("Welcome: "+mAuth.getCurrentUser().getDisplayName());
                    Email.setText(mAuth.getCurrentUser().getEmail());
                    Glide.with(getApplicationContext())
                            .load(mAuth.getCurrentUser()
                                    .getPhotoUrl()).into(imageView);
                    String uName = mAuth.getCurrentUser().getDisplayName();
                    getSupportActionBar().setSubtitle(uName);

                }else{
                    intent = new Intent(getApplicationContext(),MainActivity.class);
                    startActivity(intent);
                }
            }
        };

    }

    @Override
    protected void onPause(){
        super.onPause();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    protected void onResume(){
        super.onResume();
        mAuth.addAuthStateListener(mAuthListener);
    }
}
