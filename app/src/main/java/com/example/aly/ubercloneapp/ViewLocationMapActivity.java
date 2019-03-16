package com.example.aly.ubercloneapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;

public class ViewLocationMapActivity extends FragmentActivity implements OnMapReadyCallback
{

    private GoogleMap mMap;

    private Button btnRide;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_location_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        btnRide = findViewById(R.id.btnRide);
        btnRide.setText("I want to give " + getIntent().getStringExtra("rUsername") + " a ride...");

        btnRide.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
/*                Toast.makeText(ViewLocationMapActivity.this,getIntent().getStringExtra("rUsername"),
                        Toast.LENGTH_SHORT).show();*/

                ParseQuery<ParseObject> carRequest = ParseQuery.getQuery("RequestCar");
                carRequest.whereEqualTo("username", getIntent().getStringExtra("rUsername"));
                carRequest.findInBackground(new FindCallback<ParseObject>()
                {
                    @Override
                    public void done(List<ParseObject> carRequest, ParseException e)
                    {
                        if (carRequest.size() > 0 && e == null)
                        {
                            for (ParseObject object : carRequest)
                            {
                                object.put("requestAccepted", true);

                                object.put("myDriver", ParseUser.getCurrentUser().getUsername());

                                object.saveInBackground(new SaveCallback()
                                {
                                    @Override
                                    public void done(ParseException e)
                                    {
                                        if (e == null)
                                        {
                                            Intent googleIntent = new Intent(Intent.ACTION_VIEW,
                                                    Uri.parse("http://maps.google.com/maps?saddr="
                                                            + getIntent().getDoubleExtra("dLat", 0)
                                                            + "," + getIntent().getDoubleExtra("dLong", 0)
                                                    + "&" + "daddr=" + getIntent().getDoubleExtra("pLat", 0)
                                                            + "," + getIntent().getDoubleExtra("pLong", 0)));

                                            startActivity(googleIntent);
                                        }
                                    }
                                });
                            }
                        }
                    }
                });
            }
        });
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

/*        Toast.makeText(this, getIntent().getDoubleExtra("dLat", 0)
                + "", Toast.LENGTH_SHORT).show();*/

        LatLng dLocation = new LatLng(getIntent().getDoubleExtra("dLat", 0),
                getIntent().getDoubleExtra("dLong", 0));

/*        mMap.addMarker(new MarkerOptions().position(dLocation).title("Driver"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(dLocation));*/

        LatLng pLocation = new LatLng(getIntent().getDoubleExtra("pLat", 0),
                getIntent().getDoubleExtra("pLong", 0));

/*        mMap.addMarker(new MarkerOptions().position(pLocation).title("Passenger"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(pLocation));*/

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        Marker driverMarker = mMap.addMarker(new MarkerOptions().position(dLocation).title("Driver"));
        Marker passengerMarker = mMap.addMarker(new MarkerOptions().position(pLocation).title(getIntent().getStringExtra("rUsername")));

        ArrayList<Marker> mymarkers = new ArrayList<>();
        mymarkers.add(driverMarker);
        mymarkers.add(passengerMarker);

        for (Marker marker : mymarkers)
        {
            builder.include(marker.getPosition());
        }

        LatLngBounds bounds = builder.build();

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 0);
        mMap.animateCamera(cameraUpdate);
    }
}
