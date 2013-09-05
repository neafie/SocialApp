package edu.fsu.cs.socialnetworkingapp;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.facebook.Session;
import com.facebook.widget.ProfilePictureView;
import com.parse.Parse;
import com.parse.ParseAnalytics;

public class MainActivity extends Activity {
	
	private final String NO_USERS = "No users near current location.";
	
	private GPSLocationListener gpsLocationListener;
	private LocationManager locationManager;
	
	private String personName;
	private String personID;
	private Location lastLocation;
	
	private ArrayList<String> list = new ArrayList<String>();
	private ArrayList<String> IDList = new ArrayList<String>();
	private CustomAdapter adapter;
	private int clickInt = 0;
	String URL;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		if(gpsLocationListener == null) {
			gpsLocationListener = new GPSLocationListener(this);
		}
		
		if(locationManager == null) {
			locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
			lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 5, gpsLocationListener);
		}
		
		ListView listView = getListView();
		list.add(NO_USERS);
		adapter = new CustomAdapter();
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(clickListener);

		Parse.initialize(this, "aWaog3rUplg2COTUvsqsTROcOeUznnPIXm8o8Lv4", "RtSocgVJVZfajKuqukTUITcsUJWuVDrE6fasVYnj");
		ParseAnalytics.trackAppOpened(getIntent());

		Session.openActiveSession(this, true, new LoginCallback(this));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, gpsLocationListener);
	}
	
	@Override
	public void onPause() {
		super.onPause();
		locationManager.removeUpdates(gpsLocationListener);
	}
	
	public ProfilePictureView getProfilePictureView() {
		ProfilePictureView profilePictureView = (ProfilePictureView) findViewById(R.id.selection_profile_pic);
		profilePictureView.setCropped(true);
		return profilePictureView;
	}
	
	public ListView getListView() {
		return (ListView) findViewById(R.id.listview);
	}
	
	public CustomAdapter getAdapter() {
		return adapter;
	}
	
	public ArrayList<String> getList(){
		return list;
	}
	
	public ArrayList<String> getIDList(){
		return IDList;
	}
	
	public TextView getUserNameView() {
		return (TextView) findViewById(R.id.selection_user_name);
	}
	
	public Location getLastLocation() {
		return lastLocation;
	}
	
	public void setLastLocation(Location lastLocation) {
		this.lastLocation = lastLocation;
	}
	
	public String getPersonName() {
		return personName;
	}
	
	public void setPersonName(String personName) {
		this.personName = personName;
	}
	
	public String getPersonID() {
		return personID;
	}
	
	public void setPersonID(String personID) {
		this.personID = personID;
	}
	
class CustomAdapter extends ArrayAdapter<String> {

		
		public CustomAdapter() {
			super(MainActivity.this, R.layout.list_item, R.id.name, list);
			
		}
		
		@Override
		public View getView(int position, View view, ViewGroup parent) {
			View listItem = super.getView(position, view, parent);
			ImageView profilePic = (ImageView)listItem.findViewById(R.id.profilepic);
			getIDList().add("1");
			
			URL = "http://graph.facebook.com/"+getIDList().get(position)+"/picture";
			profilePic.setTag(URL);
			new DownloadsImagesTask().execute(profilePic);
			
			
			return(listItem);
		}
		
		public class DownloadsImagesTask extends AsyncTask<ImageView, Void, Bitmap> {

			ImageView imageView = null;

			@Override
			protected Bitmap doInBackground(ImageView... imageViews) {
			    this.imageView = imageViews[0];
			    return download_Image((String)imageView.getTag());
			}

			@Override
			protected void onPostExecute(Bitmap result) {
			    imageView.setImageBitmap(result);
			    
			}


			private Bitmap download_Image(String url) {
				Bitmap bmp =null;
		        try{
		            URL ulrn = new URL(url);
		            HttpURLConnection con = (HttpURLConnection)ulrn.openConnection();
		            InputStream is = con.getInputStream();
		            bmp = BitmapFactory.decodeStream(is);
		            if (null != bmp)
		                return bmp;

		            }catch(Exception e){}
		        return bmp;
			}
	}
}

	private OnItemClickListener clickListener = new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View v, int i, long l) { 
				clickInt = i;
				Intent facebookIntent = getOpenFacebookIntent(getApplicationContext());
				startActivity(facebookIntent);
        
			}                
	};
	
	public Intent getOpenFacebookIntent(Context context) {

	    try {
	        context.getPackageManager()
	                .getPackageInfo("com.facebook.katana", 0); //Checks if FB is even installed.
	        Log.d("tag", IDList.get(clickInt));
	        return new Intent(Intent.ACTION_VIEW,
	                Uri.parse("fb://profile/" + IDList.get(clickInt))); //Trys to make intent with FB's URI
	    } catch (Exception e) {
	        return new Intent(Intent.ACTION_VIEW,
	                Uri.parse("http://www.facebook.com/profile.php?id=" + IDList.get(clickInt))); //catches and opens a url to the desired page
	    }
	}


}
