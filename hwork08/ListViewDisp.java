package com.example.likhi.hwork08;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ListViewDisp extends AppCompatActivity {

    static final  int REQ=100;
    static final String TRIP="trip",EXTRA="extra";

    Button addTrip;
    ListView tripDisplay;
    TextView username;
    GoogleSignInClient googleSignInClient;
    ImageView logout;
    GoogleSignInClient   mGoogleSignInClient;
    DatabaseReference mRootRef;
    ArrayList<Trip> trips=new ArrayList<Trip>();
    ArrayList<String> tripNames=new ArrayList<String>();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_view_disp);

        mRootRef = FirebaseDatabase.getInstance().getReference();
        String userName=mAuth.getCurrentUser().getDisplayName();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        addTrip=findViewById(R.id.addTrip);
        tripDisplay=findViewById(R.id.tripDisplay);
        username=findViewById(R.id.userName);
        logout=findViewById(R.id.logOut);

        username.setText(userName);

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                GoogleSignInOptions  gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(getString(R.string.default_web_client_id))
                        .requestEmail()
                        .build();

                 mGoogleSignInClient = GoogleSignIn.getClient(ListViewDisp.this, gso);
                mGoogleSignInClient.signOut()
                        .addOnCompleteListener(ListViewDisp.this, new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Intent intent = new Intent(ListViewDisp.this, MainActivity.class);
                                startActivity(intent);
                            }
                        });
            }
        });


        addTrip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(ListViewDisp.this,AddTrip.class);
                startActivityForResult(intent,REQ);
            }
        });

        mRootRef.child("Trip").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                trips.clear();
                tripNames.clear();

                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    if (postSnapshot != null) {
                        Trip trip = new Trip();
                        trip.setTripName(postSnapshot.child("tripName").getValue().toString());
                        trip.setCity(postSnapshot.child("city").getValue().toString());
                        trip.setTripId(postSnapshot.child("tripId").getValue().toString());
                        trip.setDay(postSnapshot.child("day").getValue().toString());

                        Log.d("likki", trip.tripName);

                        GenericTypeIndicator<List<Places>> t = new GenericTypeIndicator<List<Places>>() {};
                        trip.setPlaces((ArrayList<Places>) postSnapshot.child("places").getValue(t));
                        trips.add(trip);
                        tripNames.add(trip.tripName);
                    }
                }
                ArrayAdapter<String> itemsAdapter =new ArrayAdapter<String>(ListViewDisp.this, android.R.layout.simple_list_item_1, tripNames);
                tripDisplay.setAdapter(itemsAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        tripDisplay.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Trip listObj=trips.get(position);
                Intent intent=new Intent(ListViewDisp.this,MapsDisplayActivity.class);
                Bundle bundle=new Bundle();
                bundle.putSerializable(TRIP,listObj);
                intent.putExtra(EXTRA,bundle);
                startActivity(intent);
            }
        });
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQ) {
            if (resultCode == 1) {

            }
        }
    }
}
