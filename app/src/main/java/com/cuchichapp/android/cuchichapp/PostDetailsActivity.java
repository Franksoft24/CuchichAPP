package com.cuchichapp.android.cuchichapp;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.cuchichapp.android.cuchichapp.model.Post;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.io.FileNotFoundException;
import java.io.IOException;

public class PostDetailsActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private FirebaseDatabase mDataBase;
    private DatabaseReference myRef;


    TextView Title;
    TextView Content;
    TextView Auth;
    TextView Location;
    ImageView MainImage;

    Post post;


    public String PostKey;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_details);

        mAuth = FirebaseAuth.getInstance();
        mDataBase = FirebaseDatabase.getInstance();

        Title = (TextView) findViewById(R.id.TitleTVDetails);
        Content = (TextView) findViewById(R.id.ContentTVDetails);
        Auth = (TextView) findViewById(R.id.AuthorTVDetails);
        Location = (TextView) findViewById(R.id.LocationTVDetails);

        MainImage = (ImageView) findViewById(R.id.imageViewDetails);

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (mAuth.getCurrentUser()==null){
                    finish();
                }
            }
        };

        if(getIntent().hasExtra(FeedActivity.Key)){
            this.PostKey = getIntent().getStringExtra(FeedActivity.Key);
            if (PostKey!=null){
                Select();
            }else {
                Toast.makeText(this,"Empty Post Key",Toast.LENGTH_SHORT).show();
            }

        }else {
            finish();
        }

        findViewById(R.id.sharePostButton).setOnClickListener((v->{
            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            Uri TestUri = null;

            try {
                Bitmap TestMediaStore = MediaStore.Images.Media.getBitmap(this
                                .getContentResolver(), Uri.parse(post.Img));
                TestUri = Uri.parse(String.valueOf(TestMediaStore));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (TestUri!=null){
                shareIntent.putExtra(Intent.EXTRA_STREAM,TestUri);
                shareIntent.setType("image/*");
            }else{
                shareIntent.putExtra(Intent.EXTRA_TEXT, post.MyPostTitle);
                //shareIntent.putExtra(Intent.EXTRA_TEXT,post.MyPostTitle);
                shareIntent.setType("text/plain");
                //shareIntent.putExtra("String", post.MyPostTitle);

            }
            startActivity(Intent.createChooser(shareIntent, getResources().getText(R.string.send_to)));

        }));

    }


    @Override
    protected void onPause() {
        super.onPause();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mAuth.addAuthStateListener(mAuthListener);
    }

    public void ConstructUI() {
        if (post != null) {
            Title.setText(post.MyPostTitle);
            Content.setText(post.MyPostContent);

            Auth.setText("Autor: " + post.User);
            if (post.Location != null) {
                Location.setText("Ubicacion: " + post.Location);
            }else {
                Location.setVisibility(View.GONE);
            }

            Glide.with(this)
                    .load(post.Img)
                    .into(MainImage);
        }else {

        }
    }

    public void Select(){

        myRef = mDataBase.getReference().child("Posts/"+this.PostKey);
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                PostDetailsActivity.this.post = dataSnapshot.getValue(Post.class);
                ConstructUI();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }




}
