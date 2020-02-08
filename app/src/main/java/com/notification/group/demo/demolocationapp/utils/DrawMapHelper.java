package com.notification.group.demo.demolocationapp.utils;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.notification.group.demo.demolocationapp.model.Bounds;
import com.notification.group.demo.demolocationapp.model.Tags;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public  class DrawMapHelper
{
    private static final String TAG = "DrawMapHelper";
    private static Context _context;

    private GoogleMap mMap;
    private List<Marker> _markerList = new ArrayList<>();
    private static final DrawMapHelper ourInstance = new DrawMapHelper();

    public static DrawMapHelper getInstance(Context context)
    {
        _context = context;
        return ourInstance;
    }


    public void drawPolygonFromJson(GoogleMap googleMap, String fileName)
    {
        mMap = googleMap;
        parseJsonFileData(fileName);

    }

    private void parseJsonFileData(String fileName)
    {
        try
        {
            JSONObject obj = new JSONObject(loadJSONFromAsset(fileName));
            JSONObject results = obj.getJSONObject("results");

            String type = results.getString("type");
            int id = results.getInt("id");

            JSONObject bounds = results.getJSONObject("bounds");
            Double minlat = bounds.getDouble("minlat");
            Double minlon = bounds.getDouble("minlon");
            Double maxlat = bounds.getDouble("maxlat");
            Double maxlon = bounds.getDouble("maxlon");

            Bounds bounds1 = new Bounds(minlat, minlon, maxlat, maxlon);


            JSONArray geomatry = results.getJSONArray("geometry");


            List<LatLng> latLngList = new ArrayList<>();
            for (int i = 0; i < geomatry.length(); i++)
            {
                JSONArray cordinatesArray = geomatry.getJSONArray(i);

                for (int j = 0; j < cordinatesArray.length(); j++)
                {
                    double lat = Double.parseDouble(cordinatesArray.getJSONObject(j).getString("lat"));
                    double lon = Double.parseDouble(cordinatesArray.getJSONObject(j).getString("lon"));

                    latLngList.add(new LatLng(lat, lon));
                }
            }

            JSONObject tags = results.getJSONObject("tags");
            Tags tags1 = new Tags(tags.getString("name"));


            Log.d(TAG, "drawPolygonFromJson: latLngList " + latLngList.get(0));

            drawGeoFencePolygon(fileName,latLngList, bounds1, tags1);

        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
    }

    private void drawGeoFencePolygon(String fileName,List<LatLng> latLngList, Bounds bounds1, Tags tags1)
    {
        PolygonOptions polygonOptions = new PolygonOptions().addAll(latLngList);

        Log.d(TAG, "drawPolygonFromJson: latLngList " + latLngList.size());
        Log.d(TAG, "drawPolygonFromJson: _markerList " + _markerList.size());
        Polygon polygon = mMap.addPolygon(polygonOptions);

        polygon.setStrokeColor(Color.rgb(255, 153, 0));
        polygon.setFillColor(Color.argb(150, 255, 184, 77));

        LatLng southWest = new LatLng(bounds1.getMinlat(), bounds1.getMinlon());
        LatLng northEast = new LatLng(bounds1.getMaxlat(), bounds1.getMaxlon());
        LatLngBounds boundsx = new LatLngBounds(southWest, northEast);
        mMap.setLatLngBoundsForCameraTarget(boundsx);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(getCenterOfPolygon(latLngList), getZoom(fileName)));

        mMap.addMarker(new MarkerOptions()
                .position(getCenterOfPolygon(latLngList))
                .title(tags1.getName()));

    }

    private String loadJSONFromAsset(String fileName)
    {
        String json;
        try
        {
            InputStream is = _context.getAssets().open(fileName);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, StandardCharsets.UTF_8);
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

    private static LatLng getCenterOfPolygon(List<LatLng> latLngList)
    {
        double[] centroid = {0.0, 0.0};
        for (int i = 0; i < latLngList.size(); i++)
        {
            centroid[0] += latLngList.get(i).latitude;
            centroid[1] += latLngList.get(i).longitude;
        }
        int totalPoints = latLngList.size();
        return new LatLng(centroid[0] / totalPoints, centroid[1] / totalPoints);
    }

    private float getZoom(String mapName){
        float zoom;
        if(mapName.equalsIgnoreCase("hub.json")){
            zoom = 15f;
        }else if(mapName.equalsIgnoreCase("hub2.json")) {
            zoom = 18f;
        }else {
            zoom = 15f;
        }
        return zoom;
    }

}
