package com.ayushnvijay.android.technicalproject99co;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.vision.barcode.Barcode;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

public class LocationPicker extends AppCompatActivity {

    private static final String PLACES_API_BASE = "https://maps.googleapis.com/maps/api/place";
    private static final String TYPE_AUTOCOMPLETE = "/autocomplete";
    private static final String OUT_JSON = "/json";
    static final String LONGITUDE = "Longitude";
    static final String LATITUDE = "Latitude";
    static final String CATEGORY = "CategoryId";
    class IntentObject{
        String str;
        double latitude, longitude;
        IntentObject(){

        }
        IntentObject(String str){
            this.str = str;
        }
        IntentObject(double lat, double lon){
            latitude = lat;
            longitude = lon;
        }
        public String getCategory(){
            return str;
        }
        public double getLatitude(){
            return latitude;
        }
        public double getLongitude(){
            return longitude;
        }
        public void setCategory(String str){
            this.str = str;
        }
        public void setLatitude(double latitude){
            this.latitude = latitude;
        }
        public void setLongitude(double longitude){
           this.longitude = longitude;
        }
    }

    IntentObject object;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        object = new IntentObject();
        setContentView(R.layout.activity_location_picker);
        AutoCompleteTextView autoCompleteTextView = (AutoCompleteTextView) findViewById(R.id.auto_complete);
            GooglePlacesAdapter placesAdapter = new GooglePlacesAdapter(this, R.layout.list_item);
            autoCompleteTextView.setAdapter(placesAdapter);

        autoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView parent, View view, int position, long id) {
                String str = (String) parent.getItemAtPosition(position);
                Log.i("Place selected is ", "" + str);
                Geolocation point = new Geolocation();
                Barcode.GeoPoint geoPoint = point.getLocationFromAddress(str, getApplicationContext());
                if(geoPoint!=null) {
                    Log.i("Longitudes & Latitudes", (int) geoPoint.lat+ " " + (int) geoPoint.lng);
                    object.setLatitude((geoPoint.lat));
                    object.setLongitude((geoPoint.lng));
                    //send by intent
                }
            }
        });
        RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        MasonryAdapter adapter = new MasonryAdapter(this);
        mRecyclerView.setAdapter(adapter);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
    }

    class GooglePlacesAdapter extends ArrayAdapter implements Filterable{
        ArrayList<String> arrayList;
        public GooglePlacesAdapter(Context context, int resource) {
            super(context, resource);
        }

        @Override
        public String getItem(int index){
            return arrayList.get(index);
        }

        @Override
        public int getCount(){
            return arrayList.size();
        }

        @Override
        public Filter getFilter(){
            Filter filter = new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    FilterResults results = new FilterResults();
                    if(constraint!=null){
                        arrayList = getStringList(constraint.toString());
                        results.values = arrayList;
                        results.count = arrayList.size();
                    }
                    return results;
                }

                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    if(results!=null && results.count > 0){
                        notifyDataSetChanged();
                    }
                    else notifyDataSetInvalidated();
                }
            };
            return filter;
        }
    }
    public static ArrayList getStringList(String input) {
        ArrayList resultList = null;
        HttpURLConnection conn = null;
        StringBuilder jsonResults = new StringBuilder();
        try {
            StringBuilder sb = new StringBuilder(PLACES_API_BASE + TYPE_AUTOCOMPLETE + OUT_JSON);
            sb.append("?key=" + ApiKeys.GOOGLE_API_KEY);
            sb.append("&components=country:us");
            sb.append("&input=" + URLEncoder.encode(input, "utf8"));
            URL url = new URL(sb.toString());
            conn = (HttpURLConnection) url.openConnection();
            InputStreamReader in = new InputStreamReader(conn.getInputStream());
            // Load the results into a StringBuilder
            int read;
            char[] buff = new char[1024];
            while ((read = in.read(buff)) != -1) {
                jsonResults.append(buff, 0, read);
            }
        } catch (MalformedURLException e) {
            Log.e("", "Error processing Places API URL", e);
            return resultList;
        } catch (IOException e) {
            Log.e("", "Error connecting to Places API", e);
            return resultList;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
        try {
            // Create a JSON object hierarchy from the results
            JSONObject jsonObj = new JSONObject(jsonResults.toString());
            JSONArray predsJsonArray = jsonObj.getJSONArray("predictions");
            // Extract the Place descriptions from the results
            resultList = new ArrayList(predsJsonArray.length());
            for (int i = 0; i < predsJsonArray.length(); i++) {
                resultList.add(predsJsonArray.getJSONObject(i).getString("description"));
            }
        } catch (JSONException e) {

        }
        return resultList;
    }
    class MasonryView extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView imageView;
        TextView textView;
        public MasonryView(View itemView) {
            super(itemView);
            imageView = (ImageView) itemView.findViewById(R.id.imageView);
            textView = (TextView) itemView.findViewById(R.id.textView);
            itemView.setOnClickListener(this);
        }


        @Override
        public void onClick(View v) {
            Log.d("TAG", "Item clicked at position " + getAdapterPosition());
            String text = textView.getText().toString();
            if(text.equals("F&B")){
                object.setCategory(ApiKeys.FOOD_AND_BAR_ID);
            }
            else if(text.equals("Entertainment")){
                object.setCategory(ApiKeys.ENTERTAINMETN_ID);
            }
            else if(text.equals("Medical")){
                object.setCategory(ApiKeys.MEDICAL_FACILITY_ID);
            }
            else object.setCategory(ApiKeys.PUBLIC_TRANSIT_SYSTEM_ID);

            Intent intent = new Intent(getApplicationContext(),MapsActivity.class);
            intent.putExtra(LATITUDE,object.getLatitude());
            intent.putExtra(LONGITUDE,object.getLongitude());
            intent.putExtra(CATEGORY,object.getCategory());
            startActivity(intent);


            //Intent
        }
    }
    class MasonryAdapter extends RecyclerView.Adapter<MasonryView> {
        private Context context;
        int[] imgList = {R.drawable.fb, R.drawable.entertainment, R.drawable.medical, R.drawable.transit};
        String[] nameList = {"F&B", "Entertainment", "Medical", "Public transit stations"};
        public MasonryAdapter(Context context) {
            this.context = context;
        }
        @Override
        public MasonryView onCreateViewHolder(ViewGroup parent, int viewType) {
            View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.tiles_item, parent, false);
            MasonryView masonryView = new MasonryView(layoutView);
            return masonryView;
        }

        @Override
        public void onBindViewHolder(MasonryView holder, int position) {
            holder.imageView.setImageResource(imgList[position]);
            holder.textView.setText(nameList[position]);
        }

        @Override
        public int getItemCount() {
            return nameList.length;
        }
    }
}
