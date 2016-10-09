package com.example.hedgehog.hedgehogapp;


import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.FrameLayout;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;


public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    FrameLayout container;
    FrameLayout mapContainer;

    FragmentManager fragmentManager;
    FacebookFragment facebookFragment;

    public GoogleMap mMap;
    boolean isMapReady = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);

        container = (FrameLayout) findViewById(R.id.container);
        mapContainer = (FrameLayout) findViewById(R.id.mapContainer);
        mapContainer.setAlpha(0);
        mapContainer.setScaleX(0.3f);
        mapContainer.setScaleY(0.3f);
        fragmentManager = getFragmentManager();
        facebookFragment = new FacebookFragment();
        facebookFragment.setMainActivity(this);

        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.container, facebookFragment, "facebook fragment");
        fragmentTransaction.commit();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        fragmentManager.beginTransaction().remove(facebookFragment);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        isMapReady = true;
    }
}
