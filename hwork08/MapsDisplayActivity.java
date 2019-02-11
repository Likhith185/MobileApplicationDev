package com.example.likhi.hwork08;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;


import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;


import java.util.ArrayList;

public class MapsDisplayActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMapLoadedCallback  {

    private GoogleMap map;
    ArrayList<Places> places = new ArrayList<Places>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps_display);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        Bundle bundle=getIntent().getBundleExtra(ListViewDisp.EXTRA);
        Trip trip= (Trip) bundle.getSerializable(ListViewDisp.TRIP);
        places=trip.getPlaces();
    }


    @Override
    public void onMapLoaded() {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for(int j=0;j<places.size();j++){
            builder.include(new LatLng(Double.parseDouble(places.get(j).getLat()),Double.parseDouble(places.get(j).getLng())));
        }
        LatLngBounds bounds = builder.build();
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds,12);
        map.animateCamera(cameraUpdate);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;

        if (places.size() > 0) {
            for (int i = 0; i < places.size(); i++) {
                LatLng ltLn = new LatLng(Double.parseDouble(places.get(i).getLat()), Double.parseDouble(places.get(i).getLng()));
                map.addMarker((new MarkerOptions()).position(ltLn).title(places.get(i).getName()));

            }
            map.animateCamera(CameraUpdateFactory.zoomIn());
            map.setOnMapLoadedCallback((GoogleMap.OnMapLoadedCallback) MapsDisplayActivity.this);
        }

    }
}

