package com.ayushnvijay.android.technicalproject99co;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;

import com.google.android.gms.vision.barcode.Barcode;

import java.util.List;

/**
 * Created by ayushnvijay on 1/13/16.
 */
public class Geolocation {
    public Barcode.GeoPoint getLocationFromAddress(String adr, Context context){
        Geocoder coder = new Geocoder(context);
        Barcode.GeoPoint point = new Barcode.GeoPoint();
        try{
            List<Address> address =  coder.getFromLocationName(adr,1);
            Address location = address.get(0);

            if(address==null) return null;

            point.lat = location.getLatitude();
            point.lng = location.getLongitude();
        }
        catch (Exception e){
            //exception has occured
            return null;
        }
        return point;
    }

}
