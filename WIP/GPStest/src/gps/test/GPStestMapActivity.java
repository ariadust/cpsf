package gps.test;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import gps.test.PinItemizedOverlay;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;


public class GPStestMapActivity extends MapActivity 
	implements View.OnClickListener, LocationListener {
	//ロケーションマネージャーの定義
	private LocationManager locationManager;
	private List<Location> locations = new ArrayList<Location>();	
	private MapView map;
	private Button buttonStart;
	private Timer timer;
    private ProgressDialog progressDialog;
    private Handler handler = new Handler();
     
    @Override
    protected void onCreate(Bundle icicle) {
    	super.onCreate(icicle);
    	setContentView(R.layout.main);
    	buttonStart = (Button)findViewById(R.id.button_start);
    	buttonStart.setOnClickListener(this);
    	map = (MapView)findViewById(R.id.map);
    	map.getController().setZoom(18);
    }
    
    @Override
    protected boolean isRouteDisplayed() {
    	return false;
    }

    @Override
    protected void onStop() {
    	super.onStop();
    	if (timer != null) timer.cancel();
    	if (locationManager != null) locationManager.removeUpdates(this);
    }
    	
    @Override
    public void onClick(View view) {
    	if (view.getId() == R.id.button_start) {
    		locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
    		List<String> providers = locationManager.getProviders(true);
    		for (String provider : providers) {
    			locationManager.requestLocationUpdates(provider, 0, 0, this);
    		}
    		buttonStart.setEnabled(false);
    		startTimer();
    		progressDialog = new ProgressDialog(this);
    		progressDialog.setIndeterminate(true);
    		progressDialog.setMessage("そーい");
    		progressDialog.show();
    	}
    }

    private void startTimer() {
    	timer = new Timer();
    	timer.schedule(new TimerTask() {
    		@Override
    		public void run() {
    			if (locations.size() > 0) {
    				handler.post(new Runnable() {
    					@Override
    					public void run() {
    						if (progressDialog != null && progressDialog.isShowing()) {
    							progressDialog.dismiss();
    						}
    						timer.cancel();
    						addPin();
    						locationManager.removeUpdates(GPStestMapActivity.this);
    						buttonStart.setEnabled(true);
    					}
    				});
    			}
    		}
    	}, 3000);
    }
        	
    @Override
    public void onLocationChanged(Location location) {
    	locations.add(location);
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
    
    private void addPin() {
    	for (Location location : locations) {
    		GeoPoint point = new GeoPoint((int)(location.getLatitude() * 1E6), 
    				(int)(location.getLongitude() * 1E6));
    		Drawable pin = getResources().getDrawable(R.drawable.pin);
    		PinItemizedOverlay overlay = new PinItemizedOverlay(pin) {
    			@Override
    			protected boolean onTap(int index) {
    				OverlayItem item = (OverlayItem)getItem(index);
    				TextView title = (TextView)findViewById(R.id.text_location_title);
    				title.setText(item.getTitle());
    				TextView snippet = (TextView)findViewById(R.id.text_location_snippet);
    				snippet.setText(item.getSnippet());
    				map.getController().setCenter(item.getPoint());
    				return super.onTap(index);
    			}
    		};
    		map.getOverlays().add(overlay);
    		overlay.addPin(point,
    				"位置情報:" + new Date(location.getTime()).toLocaleString(),
    				"緯度:" + location.getLatitude() + "経度:" + location.getLongitude() + "\n精度:±" + location.getAccuracy() + "m");
    		map.getController().setCenter(point);
    	}
    	map.invalidate();
    }
}	
	