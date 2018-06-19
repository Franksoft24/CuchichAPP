package com.cuchichapp.android.cuchichapp;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;


public class MainActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (mAuth.getCurrentUser()!=null){
                    //Toast.makeText(getApplicationContext(),"User si logged",Toast.LENGTH_LONG).show();
                    /*Intent intent = new Intent(getApplicationContext(),FeedActivity.class);
                    startActivity(intent);*/
                    finish();
                }else{

                }
            }
        };


        findViewById(R.id.GetStarted).setOnClickListener((v->{
            Intent intent = new Intent(this,LogInActivity.class);
            startActivity(intent);
        }));

    }

    @Override
    protected void onPause(){
        super.onPause();
        mAuth.addAuthStateListener(mAuthListener);
        //finish();
    }

    @Override
    protected void onResume(){
        super.onResume();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        //finish();
    }
}
