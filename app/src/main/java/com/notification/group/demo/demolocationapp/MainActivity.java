package com.notification.group.demo.demolocationapp;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.notification.group.demo.demolocationapp.utils.DrawMapHelper;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Set;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatSpinner;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, AdapterView.OnItemSelectedListener
{
    private static final String TAG = "MainActivity";
    private  ArrayList<String> _listOfKeys;
    private LinkedHashMap<String,String> _mapList = new LinkedHashMap<>();
    private GoogleMap mMap;
    private SupportMapFragment mapFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        InItViews();
    }

    private void InItViews()
    {
        setUpMapIfNeeded();
        setSpinnerAdapter();
    }


    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mapFragment == null) {
            mapFragment = ((SupportMapFragment) getSupportFragmentManager().
                    findFragmentById(R.id.map));
            // Check if we were successful in obtaining the map.
            if (mapFragment != null) {
                mapFragment.getMapAsync(this);
                // The Map is verified. It is now safe to manipulate the map.
            }
        }
    }
    private void setSpinnerAdapter()
    {
        AppCompatSpinner appCompatSpinner = findViewById(R.id.geofenceSelector);

        _mapList.put("Hub 1","hub.json");
        _mapList.put("Hub 2","hub2.json");
        _mapList.put("Hub 3","hub3.json");

        Set<String> keySet = _mapList.keySet();
        _listOfKeys = new ArrayList<>(keySet);


        ArrayAdapter adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,_listOfKeys);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        appCompatSpinner.setAdapter(adapter);
        appCompatSpinner.setOnItemSelectedListener(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        mMap = googleMap;
        if(mMap!=null)
        {
            DrawMapHelper.getInstance(this).drawPolygonFromJson(mMap, "hub.json");
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
    {
        String mapName = _mapList.get(_listOfKeys.get(position));
        Log.d(TAG, "onItemSelected: "+mapName);

        if(mMap!=null)
        {
            DrawMapHelper.getInstance(this).drawPolygonFromJson(mMap, mapName);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent)
    {

    }
}
