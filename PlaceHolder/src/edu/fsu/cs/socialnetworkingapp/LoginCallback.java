package edu.fsu.cs.socialnetworkingapp;

import java.util.List;

import android.location.Location;
import android.location.LocationManager;
import android.util.Log;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.model.GraphUser;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;

public class LoginCallback implements Session.StatusCallback {

	private final MainActivity mainActivity;

	public LoginCallback(MainActivity mainActivity) {
		this.mainActivity = mainActivity;
	}

	@Override
	public void call(Session session, SessionState state, Exception exception) {
		if(exception != null) {
			exception.printStackTrace();
		}
		if (session.isOpened()) {
			Request.executeMeRequestAsync(session, new Request.GraphUserCallback() {
				
				@Override
				public void onCompleted(final GraphUser user, Response response) {
					if (user != null) {
						mainActivity.getProfilePictureView().setProfileId(user.getId());
						mainActivity.getUserNameView().setText(user.getName());
						mainActivity.setPersonName(user.getName());
						mainActivity.setPersonID(user.getId());
						final ParseQuery<ParseObject> query = ParseQuery.getQuery("userObject");
						query.whereEqualTo("UserId", user.getId());
						query.getFirstInBackground(new GetCallback<ParseObject>() {
									
							@Override
							public void done(ParseObject object, ParseException e) {
								final ParseObject userObject = new ParseObject("userObject");
								if (object == null) {
									Location location = mainActivity.getLastLocation();
									ParseGeoPoint point = new ParseGeoPoint(location.getLatitude(), location.getLongitude());
									userObject.put("Username", user.getName());
									userObject.put("UserId", user.getId());
									userObject.put("Location", point);
									userObject.saveInBackground();
								} else {
									Location location = mainActivity.getLastLocation();
									if(location == null) {
										location = new Location(LocationManager.GPS_PROVIDER);
									}
									ParseGeoPoint point = new ParseGeoPoint(location.getLatitude(), location.getLongitude());
									object.put("Location", point);
									object.saveInBackground();
								}
								
								ParseGeoPoint userLocation = (ParseGeoPoint) (object == null ? userObject : object).get("Location");
								ParseQuery<ParseObject> query = ParseQuery.getQuery("userObject");
								query.whereWithinMiles("Location", userLocation, .5);
								query.whereNotEqualTo("UserId", user.getId());
								query.setLimit(100);
								query.findInBackground(new FindCallback<ParseObject>() {
									
									@Override
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
				}
			});
		}
	}

}
