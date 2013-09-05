package edu.fsu.cs.socialnetworkingapp;
import java.util.List;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;


public class GPSLocationListener implements LocationListener {
	
	private final MainActivity mainActivity;
	
	public GPSLocationListener(MainActivity mainActivity) {
		this.mainActivity = mainActivity;
	}

	@Override
	public void onLocationChanged(Location location) {
		mainActivity.setLastLocation(location);
		
		final ParseQuery<ParseObject> query = ParseQuery.getQuery("userObject");
		query.whereEqualTo("UserId", mainActivity.getPersonID());
		query.getFirstInBackground(new GetCallback<ParseObject>() {
			public void done(ParseObject object, ParseException e) {
					Location location = mainActivity.getLastLocation();
					ParseGeoPoint point = new ParseGeoPoint(location.getLatitude(), location.getLongitude());
					object.put("Location", point);
					object.saveInBackground();

				
				ParseGeoPoint userLocation = (ParseGeoPoint) object.get("Location");
				ParseQuery<ParseObject> query = ParseQuery.getQuery("userObject");
				query.whereWithinMiles("Location", userLocation, .5);
				query.whereNotEqualTo("UserId", mainActivity.getPersonID());
				query.setLimit(100);
				query.findInBackground(new FindCallback<ParseObject>() {
					public void done(List<ParseObject> nearList, ParseException e) {
						if (e == null) {
							mainActivity.getList().clear();
							mainActivity.getIDList().clear();
							for (int i = 0; i < nearList .size(); i++) {
								mainActivity.getList().add(nearList.get(i).getString("Username"));
								mainActivity.getIDList().add(nearList.get(i).getString("UserId"));
							}
							mainActivity.getAdapter().notifyDataSetChanged();
						} else {
							Log.d("DB", "Error: " + e.getMessage());
						}
					}
				});
			}
		});
	}
		
	@Override
	public void onProviderDisabled(String provider) {
		
	}

	@Override
	public void onProviderEnabled(String provider) {
		
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		
	}

}
