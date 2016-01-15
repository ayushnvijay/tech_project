package com.ayushnvijay.android.technicalproject99co;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    double longitude;
    double latitude;
    String categoryId;
    ArrayList<Points> pointsArrayList = new ArrayList<Points>();
    class Points{
        double lat;
        double lng;
        Points(int lat, int lng){
            this.lat = lat;
            this.lng = lng;
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        Intent getIntent = getIntent();
        longitude = getIntent.getDoubleExtra(LocationPicker.LONGITUDE, 0);
        latitude = getIntent.getDoubleExtra(LocationPicker.LATITUDE, 0);
        categoryId = getIntent.getStringExtra(LocationPicker.CATEGORY);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setBuildingsEnabled(true);
        FoursquareParsing foursquareParsing = new FoursquareParsing();
        foursquareParsing.execute(categoryId, longitude + "", latitude + "");

    }
    class FoursquareParsing extends AsyncTask<String, Void, String> {
        private static final String BASE_ENDPOINT = "https://api.foursquare.com/v2/venues/search?";
        private static final String CATEGORY = "&categoryId=";
        private static final String LONGITUDES_LATITUDES = "&ll=";
        private static final String CLIENT_ID = "client_id=";
        private static final String CLIENT_SECRET = "&client_secret=";



        @Override
        protected String doInBackground(String... params) {
            final OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder().url(BASE_ENDPOINT+CLIENT_ID+ApiKeys.FOURSQUARE_CLIENT_ID
                    +CLIENT_SECRET+ApiKeys.FOURSQUARE_CLIENT_SECRET+CATEGORY+params[0]
                    +LONGITUDES_LATITUDES+params[2]+","+params[1]
                    +"&v=20160101")
                    .build();
            try {
                Response response = client.newCall(request).execute();
                if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
                return response.body().string();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }


        @Override
        protected void onPostExecute(String result) {
            try {
                mMap.clear();
                JSONObject jsonObject = new JSONObject(result);
                if(jsonObject.has("response")){
                    if(jsonObject.getJSONObject("response").has("venues")){
                        JSONArray jsonArray = jsonObject.getJSONObject("response").getJSONArray("venues");
                        for(int i = 0; i < jsonArray.length(); i++) {
                            String name = jsonArray.getJSONObject(i).getJSONArray("categories").getJSONObject(0).getString("name");
                            if(jsonArray.getJSONObject(i).has("location")){
                                JSONObject location = jsonArray.getJSONObject(i).getJSONObject("location");
                                Points temp = new Points(0,0);
                                if(location.has("lat")){
                                    temp.lat = location.getDouble("lat");
                                    //Log.i("lat", temp.lat + "");
                                }
                                if(location.has("lng")){
                                    temp.lng = location.getDouble("lng");
                                    //Log.i("lng",temp.lng+"");
                                }
                                LatLng marker = new LatLng(temp.lat, temp.lng);
                                mMap.addMarker(new MarkerOptions().position(marker).title(name));
                                mMap.moveCamera(CameraUpdateFactory.newLatLng(marker));
                            }
                        }
                    }
                    mMap.moveCamera(CameraUpdateFactory.zoomBy(6));
                }
            }
            //Note: Points arraylist is not useful here, we can directly add markers while parsing json.
            catch (Exception e){

            }
        }
    }
}
