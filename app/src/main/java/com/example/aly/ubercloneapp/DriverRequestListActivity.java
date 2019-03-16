package com.example.aly.ubercloneapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.FindCallback;
import com.parse.LogOutCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class DriverRequestListActivity extends AppCompatActivity implements View.OnClickListener,
        AdapterView.OnItemClickListener
{
    private Button btnGetRequests;

    private LocationManager locationManager;
    private LocationListener locationListener;

    private ListView listView;
    private ArrayList<String> nearbyDriveRequests;
    private ArrayAdapter adapter;
    private ArrayList<Double> passengerLat;
    private ArrayList<Double> passengerLong;
    private ArrayList<String> requestCarUserName;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_request_list);

        btnGetRequests = findViewById(R.id.btnGetRequests);
        btnGetRequests.setOnClickListener(this);

        listView = findViewById(R.id.requestListView);
        nearbyDriveRequests = new ArrayList<>();
        adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, nearbyDriveRequests);
        listView.setAdapter(adapter);

        nearbyDriveRequests.clear();

        passengerLat = new ArrayList<>();
        passengerLong = new ArrayList<>();
        requestCarUserName = new ArrayList<>();

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        if (Build.VERSION.SDK_INT < 23 || ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        {
            locationListener = new LocationListener()
            {
                @Override
                public void onLocationChanged(Location location)
                {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                            0, 0, locationListener);
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras)
                {

                }

                @Override
                public void onProviderEnabled(String provider)
                {

                }

                @Override
                public void onProviderDisabled(String provider)
                {

                }
            };
        }

        listView.setOnItemClickListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.driver_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if (item.getItemId() == R.id.driverLogoutItem)
        {
            ParseUser.logOutInBackground(new LogOutCallback()
            {
                @Override
                public void done(ParseException e)
                {
                    if (e == null)
                    {
                        Toast.makeText(DriverRequestListActivity.this,
                                "Logout successfully.", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }
            });
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view)
    {


        if (Build.VERSION.SDK_INT < 23)
        {
            Location currentDriverLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            updateRequestListView(currentDriverLocation);
        }

        else if (Build.VERSION.SDK_INT >= 23)
        {

            if (ContextCompat.checkSelfPermission(DriverRequestListActivity.this,
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            {
                ActivityCompat.requestPermissions(DriverRequestListActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1000);
            }

            else
            {
                //locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                  //      0, 0, locationListener);

                Location currentDriverLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                updateRequestListView(currentDriverLocation);
            }
        }
    }

    private void updateRequestListView(Location driverLocation)
    {
        if (driverLocation != null)
        {
            final ParseGeoPoint driverCurrentLocation = new ParseGeoPoint(driverLocation.getLatitude(),
                    driverLocation.getLongitude());

            final ParseQuery<ParseObject> requestCarQuery = ParseQuery.getQuery("RequestCar");
            requestCarQuery.whereNear("passengerLocation", driverCurrentLocation);
            requestCarQuery.whereDoesNotExist("myDriver");
            requestCarQuery.findInBackground(new FindCallback<ParseObject>()
            {
                @Override
                public void done(List<ParseObject> objects, ParseException e)
                {
                    if (e == null)
                    {
                        if (objects.size() > 0)
                        {
                            if (nearbyDriveRequests.size() > 0)
                            {
                                nearbyDriveRequests.clear();
                            }

                            if (passengerLat.size() > 0)
                            {
                                passengerLat.clear();
                            }

                            if (passengerLong.size() > 0)
                            {
                                passengerLong.clear();
                            }

                            if (requestCarUserName.size() > 0)
                            {
                                requestCarUserName.clear();
                            }

                            for (ParseObject nearRequest : objects)
                            {
                                ParseGeoPoint pLocation =
                                        (ParseGeoPoint) nearRequest.get("passengerLocation");

                                Double kmDistanceToPassenger =
                                        driverCurrentLocation.distanceInKilometersTo(pLocation);
                                float roundedDistanceValue = Math.round(kmDistanceToPassenger * 10)/10;

                                nearbyDriveRequests.add("There is a request " + roundedDistanceValue
                                        + " km to " + nearRequest.get("username"));

                                passengerLat.add(pLocation.getLatitude());
                                passengerLong.add(pLocation.getLongitude());
                                requestCarUserName.add(nearRequest.get("username") + "");
                            }
                        }

                        else
                        {
                            Toast.makeText(DriverRequestListActivity.this,
                                    "There are no requests", Toast.LENGTH_SHORT).show();
                        }

                        adapter.notifyDataSetChanged();
                    }
                }
            });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1000 && grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED){

            if (ContextCompat.checkSelfPermission(DriverRequestListActivity.this,
                    Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
            {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                        0, 0, locationListener);

               Location currentDriverLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
               updateRequestListView(currentDriverLocation);
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
    {
          //  Toast.makeText(this, "Clicked", Toast.LENGTH_LONG).show();

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        {
            Location cdLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

            if (cdLocation != null)
            {
                Intent intent = new Intent(this, ViewLocationMapActivity.class);
                intent.putExtra("dLat", cdLocation.getLatitude());
                intent.putExtra("dLong", cdLocation.getLongitude());
                intent.putExtra("pLat", passengerLat.get(position));
                intent.putExtra("pLong", passengerLong.get(position));
                intent.putExtra("rUsername", requestCarUserName.get(position));

                startActivity(intent);
            }
        }
    }
}
