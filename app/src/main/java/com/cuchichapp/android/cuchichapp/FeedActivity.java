package com.cuchichapp.android.cuchichapp;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v7.widget.Toolbar;

import com.cuchichapp.android.cuchichapp.adapters.FeedAdapter;
import com.cuchichapp.android.cuchichapp.model.Post;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class FeedActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    TextView textView;
    List<Post> post;
    List<Post> postsQueue;
    private DrawerLayout mDrawerLayout;
    Intent intent;
    FirebaseDatabase database;
    DatabaseReference myRef;
    FeedAdapter feedAdapter;
    RecyclerView recyclerView;
    public static String Key = "Key";
    private int Update = 0;
    FloatingActionButton Refresh;
    List<String> myKeyHashqueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed);

        database = FirebaseDatabase.getInstance();
        //database.setPersistenceEnabled(true);

        Refresh = findViewById(R.id.refreshFeedButton);

        mAuth = FirebaseAuth.getInstance();

        recyclerView = (RecyclerView) findViewById(R.id.feed_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this,
                LinearLayoutManager.VERTICAL,
                false));

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (mAuth.getCurrentUser()!=null){
                    myRef = database.getReference().child("Posts");
                    myRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            List<Post> newPosts = new ArrayList<>();
                            List<String> myKeyHash = new ArrayList<>();
                            for (DataSnapshot myDataSnapshot:
                                 dataSnapshot.getChildren()) {
                                Post addPost = myDataSnapshot.getValue(Post.class);
                                myKeyHash.add(myDataSnapshot.getKey().toString());
                                newPosts.add(addPost);
                            }
                            if(post ==null){
                                post=newPosts;
                                Inflate(myKeyHash);
                            }else if (post.size()!=0&&post.size()<newPosts.size()){

                                Refresh.setVisibility(View.VISIBLE);
                                myKeyHashqueue = myKeyHash;
                                postsQueue = newPosts;

                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                }else{
                    intent = new Intent(getApplicationContext(),MainActivity.class);
                    startActivity(intent);
                }
            }
        };

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setHomeAsUpIndicator(R.drawable.ic_menu);

        mDrawerLayout = findViewById(R.id.drawer_layout);

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                        String MenuSelection = item.getTitle().toString();
                        switch (item.getTitle().toString()){

                            case "Profile":
                                intent = new Intent(getApplicationContext()
                                        , ProfileActivity.class);
                                startActivity(intent);
                                break;
                            case "New Post":
                                intent = new Intent(getApplicationContext()
                                        , NewEntryActivity.class);
                                startActivity(intent);
                                break;

                            case "Log Out":
                                mAuth.signOut();
                                break;

                            default:
                                Toast.makeText(getApplicationContext(),"Option Not Found"
                                        ,Toast.LENGTH_SHORT)
                                        .show();
                                 break;
                        }

                        mDrawerLayout.closeDrawers();

                        return true;
                    }
                }
        );

        mDrawerLayout.addDrawerListener(
                new DrawerLayout.DrawerListener() {
                    @Override
                    public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {

                    }

                    @Override
                    public void onDrawerOpened(@NonNull View drawerView) {
                        actionbar.setHomeAsUpIndicator(R.drawable.ic_arrow_back_white);
                    }

                    @Override
                    public void onDrawerClosed(@NonNull View drawerView) {
                        actionbar.setHomeAsUpIndicator(R.drawable.ic_menu);
                    }

                    @Override
                    public void onDrawerStateChanged(int newState) {

                    }
                }
        );


        findViewById(R.id.refreshFeedButton).setOnClickListener((V->{

            post = postsQueue;
            Inflate(myKeyHashqueue);
            Refresh.setVisibility(View.GONE);


        }));


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
        if(postsQueue!= null&&postsQueue.size()>post.size()){
            post = postsQueue;
            Inflate(myKeyHashqueue);
            Refresh.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void Inflate(List<String> keys){

        if (post!=null){

            feedAdapter = new FeedAdapter(FeedActivity.this,post,keys);
            recyclerView.setAdapter(feedAdapter);

        }

    }

    @Override
    protected void onStart() {
        super.onStart();
    }
}
