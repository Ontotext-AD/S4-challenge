package com.classicharmony.speechzilla;

import android.location.Address;
import android.location.Geocoder;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class ToMapActivity extends FragmentActivity {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private String locations = "";
    private ArrayList<String> locations_list = new ArrayList<>();
    private ToMapActivity mContext;
    private Geocoder geocoder;
    private List<Address> addresses;
    private List<MarkerOptions> myMarkers_list = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_to_map);
        mContext = ToMapActivity.this;

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            locations =  extras.getString("locations_str");
        }

        geocoder = new Geocoder(mContext);
        locations_list = new ArrayList<String>(Arrays.asList(locations.split(",")));

        if (locations_list != null) {
            for (int i = 0; i < locations_list.size(); i++) {
                Log.i("getting coordinates", String.valueOf(locations_list.get(i)));

                try {
                    addresses = geocoder.getFromLocationName(String.valueOf(locations_list.get(i)), 1);

                    if (addresses.size() > 0) {
                        double latitude = addresses.get(0).getLatitude();
                        double longitude = addresses.get(0).getLongitude();
                        MarkerOptions mMarker = new MarkerOptions().position(new LatLng(latitude, longitude)).title( String.valueOf(locations_list.get(i)));
                        myMarkers_list.add(mMarker);

                    }
                } catch (IOException e) {
                    Log.e("can't get coordinates", String.valueOf(locations_list.get(i)));
                }
            }
        } else {
            Log.e("no locations", "no locations ....");
        }


        setUpMapIfNeeded();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {

        mMap.getUiSettings().setAllGesturesEnabled(true);
        for(int i=0;i<myMarkers_list.size();i++) {
            mMap.addMarker(myMarkers_list.get(i));
        }
    }
}
