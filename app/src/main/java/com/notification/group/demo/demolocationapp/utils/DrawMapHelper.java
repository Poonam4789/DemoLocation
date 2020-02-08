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
    private Polygon mPolygon;
    private List<Marker> _markerList = new ArrayList<>();
    private List<LatLng> latLngList = new ArrayList<>();
    private static final DrawMapHelper ourInstance = new DrawMapHelper();

    public static DrawMapHelper getInstance(Context context)
    {
        _context = context;
        return ourInstance;
    }


    public void drawPolygonFromJson(GoogleMap googleMap, String fileName)
    {
        mMap = googleMap;
        mMap.clear();

        if(mPolygon!=null)
        {
            mPolygon.remove();
        }
        if(latLngList!=null && latLngList.size()>0){
            latLngList.clear();
        }
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
        mPolygon = mMap.addPolygon(polygonOptions);

        mPolygon.setStrokeColor(Color.rgb(255, 153, 0));
        mPolygon.setFillColor(Color.argb(150, 255, 184, 77));
 
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

    public List<LatLng> getLatLngList()
    {
        return latLngList;
    }

    public boolean PointIsInRegion(double x, double y, List<LatLng> thePath)
    {
        int crossings = 0;

        LatLng point = new LatLng (x, y);
        int count = thePath.size();
        // for each edge
        for (int i=0; i < count; i++)
        {
            LatLng a = thePath.get(i);
            int j = i + 1;
            if (j >= count)
            {
                j = 0;
            }
            LatLng b = thePath.get(j);
            if (RayCrossesSegment(point, a, b))
            {
                crossings++;
            }
        }
        // odd number of crossings?
        return (crossings % 2 == 1);
    }

    private boolean RayCrossesSegment(LatLng point, LatLng a, LatLng b)
    {
        double px = point.longitude;
        double py = point.latitude;
        double ax = a.longitude;
        double ay = a.latitude;
        double bx = b.longitude;
        double by = b.latitude;
        if (ay > by)
        {
            ax = b.longitude;
            ay = b.latitude;
            bx = a.longitude;
            by = a.latitude;
        }
        // alter longitude to cater for 180 degree crossings
        if (px < 0) { px += 360; };
        if (ax < 0) { ax += 360; };
        if (bx < 0) { bx += 360; };

        if (py == ay || py == by) py += 0.00000001;
        if ((py > by || py < ay) || (px > Math.max(ax, bx))) return false;
        if (px < Math.min(ax, bx)) return true;

        double red = (ax != bx) ? ((by - ay) / (bx - ax)) : Float.MAX_VALUE;
        double blue = (ax != px) ? ((py - ay) / (px - ax)) : Float.MAX_VALUE;
        return (blue >= red);
    }
}
