package ariadust.wip;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import ariadust.wip.PinItemizedOverlay;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;

public class AriadustWipActivity extends MapActivity 
	implements LocationListener, SensorEventListener {
	//ロケーションマネージャーの定義
	private LocationManager locationManager;
	//センサーマネージャーの定義
    private SensorManager mSensorManager;
    private boolean mRegisteredSensor;
    //場所情報のリスト作成
	private List<Location> locations = new ArrayList<Location>();	
	//マップ表示の定義
	private MapView map;
    //停止しているかどうかの判定
    boolean stop_hantei = false;
    //直前まで止まっていたかどうか
    boolean startflag = false;
    //動き出した瞬間の時間、停止した瞬間の時間
    int time=0,s_time=0;
    //３軸加速度
    int x=0,y=0,z=0;
    //停止時間用関数
    int s_hour=0,s_minute=0,s_second=0;
    //停止時間
    int stop_time[] = new int[1000];
    //停止回数
    int stopcount=0;
    //最終データ格納
    List<String>list= new ArrayList<String>();
    private Timer timer;
    //時刻
	final Calendar calendar = Calendar.getInstance();
    final int year = calendar.get(Calendar.YEAR);
    final int month = calendar.get(Calendar.MONTH);
    final int day = calendar.get(Calendar.DAY_OF_MONTH);
    final int hour = calendar.get(Calendar.HOUR_OF_DAY);
	final int minute = calendar.get(Calendar.MINUTE);
	final int second = calendar.get(Calendar.SECOND); 
	//緯度、経度
	double ido=0,keido=0;
	private List<String> providers;
	private Handler handler = new Handler();
    @Override
    protected void onCreate(Bundle icicle) {
    	Log.v("デバック","onCreate");
    	super.onCreate(icicle);
    	setContentView(R.layout.main);
    	map = (MapView)findViewById(R.id.map);
    	map.getController().setZoom(18);
    	mRegisteredSensor = false;
		locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
		providers = locationManager.getProviders(true);
    }
    protected void onResume() {
        super.onResume();
        Log.v("デバック","onResume");
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        List<Sensor> sensors = mSensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER);  
        if (sensors.size() > 0) {
        	Sensor sensor = sensors.get(0);
        	mRegisteredSensor = mSensorManager.registerListener((SensorEventListener) this,
        			sensor,
                    SensorManager.SENSOR_DELAY_FASTEST);
            mRegisteredSensor = true;
        }
    }
    @Override
    protected boolean isRouteDisplayed() {
    	return false;
    }
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
    public void onSensorChanged(SensorEvent event){
    	Log.v("デバック","onSensorChanged");
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
          	x=Integer.valueOf((int) event.values[0]).intValue();
           	y=Integer.valueOf((int) event.values[1]).intValue();
           	z=Integer.valueOf((int) event.values[2]).intValue();
           	Log.v("デバッグ","値："+x+" ,"+y+" ,"+z);
           	main_pro();
        }
    } 
    public void main_pro() {
    	if(Math.abs(x)+Math.abs(y)+Math.abs(z)>11) {
			Log.v("デバッグ2","値："+x+" ,"+y+" ,"+z);
			Log.v("デバック","main_pro");
			stop_hantei=false;
			//動き出した瞬間
			if(startflag) {
				Log.v("デバック","main_pro 動き出した瞬間");
				//GPS測定
		    	for (String provider : providers) {
		   			locationManager.requestLocationUpdates(provider, 0, 0, this);
		   		} 
	    		startTimer();
				//動き出した瞬間の時刻を計測
				time=3600*hour+60*minute+second;
				//停止時間を測定し５秒以上の場合のみ入れる
				stop_time[stopcount]=time-s_time;
				if(stop_time[stopcount]>=5) {
					stopcount++;
					//最終データを格納
					list.add("現在時刻："+year+"/"+month+"/"+day+"/"
							+hour+"/"+minute+"/"+second+
							" 停止時間："+stop_time[stopcount]
    							+" 緯度："+ido+"　経度："+keido);
					Log.v("測定","現在時刻："+year+"/"+month+"/"+day+"/"
							+hour+"/"+minute+"/"+second+
							" 停止時間："+stop_time[stopcount]
    							+" 緯度："+ido+"　経度："+keido);
				}
				locationManager.removeUpdates(AriadustWipActivity.this);
				startflag=false;
			}else {
				GPS_update_by60sec();
			}
		}
		//止まった瞬間
		else{
			if(startflag==false) {
				Log.v("デバック","main_pro 動き出した瞬間");
				//止まった瞬間の時間を計測
				s_hour=hour;
				s_minute=minute;
				s_second=second;
				s_time=3600*s_hour+60*s_minute+s_second;
				startflag=true;
				stop_hantei=true;
			}
		}
    }
    public void GPS_update_by60sec() {
    	Log.v("デバック","GPS_hoge 動いている");
    	for(int i=0; i<60; i++) {
    		try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
    		//６０秒の計測中に止まった場合
    		if(stop_hantei==true) {
    			break;
    		}
    	}
    	//動いてたらGPS測定
    	if(stop_hantei==false) {
    		Log.v("デバック","GPS_hoge GPS測定");
    		//GPS測定
        	for (String provider : providers) {
       			locationManager.requestLocationUpdates(provider, 0, 0, this);
       		} 
    		startTimer();
    		list.add("現在時刻："+year+"/"+month+"/"+day+"/"
    				+hour+"/"+minute+"/"+second+
					" 停止時間：なし"+" 緯度："+ido+"　経度："+keido);
			locationManager.removeUpdates(AriadustWipActivity.this);
    	}
    }   	
    @Override
    public void onLocationChanged(Location location) {
    	Log.v("デバック","onLocationChanged");
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
    //ピンを置く
    private void startTimer() {
    	Log.v("デバック","startTimer");
    	timer = new Timer();
    	timer.schedule(new TimerTask() {
    		@Override
    		public void run() {
    			if (locations.size() > 0) {
    				handler.post(new Runnable() {
    					@Override
    					public void run() {
    						timer.cancel();
    						Pin();
    						locationManager.removeUpdates(AriadustWipActivity.this);
    					}
    				});
    			}
    		}
    	}, 30);
    }
    private void addPin() {
    	for (Location location : locations) {
    		//座標点の取得
    		GeoPoint point = new GeoPoint((int)(location.getLatitude() * 1E6), 
    				(int)(location.getLongitude() * 1E6));
    		//pinの画像取得
    		Drawable pin = getResources().getDrawable(R.drawable.pin);
    		//マップにピンを描画する
    		PinItemizedOverlay overlay = new PinItemizedOverlay(pin) {
    			@Override
    			protected boolean onTap(int index) {
    				OverlayItem item = (OverlayItem)getItem(index);
    				map.getController().setCenter(item.getPoint());
    				return super.onTap(index);
    			}
    		};
    		map.getOverlays().add(overlay);
    		overlay.addPin(point,
    				"位置情報:" + new Date(location.getTime()).toLocaleString(),
    				"緯度:" + location.getLatitude() + "経度:" + location.getLongitude() + "\n精度:±" + location.getAccuracy() + "m");
    		map.getController().setCenter(point);
			ido=location.getLatitude();
			keido=location.getLongitude();
			Log.v("デバッグ","緯度："+ido+"　経度："+keido);
    	}
    	map.invalidate();
    }
    @Override
    protected void onPause() {
        if (mRegisteredSensor) {
            mRegisteredSensor = false;
        }
        super.onPause();
    }
    @Override
    protected void onStop() {
    	super.onStop();
    	if (timer != null) timer.cancel();
    	if (locationManager != null) locationManager.removeUpdates(this);
    	mSensorManager = (SensorManager) getSystemService( SENSOR_SERVICE );
        mSensorManager.unregisterListener( this );
    }
    private void Pin() {
    	Log.v("デバック","Pin");
    	for (Location location : locations) {
    		GeoPoint point = new GeoPoint((int)(location.getLatitude() * 1E6), 
    				(int)(location.getLongitude() * 1E6));
    		Drawable pin = getResources().getDrawable(R.drawable.pin);
    		PinItemizedOverlay overlay = new PinItemizedOverlay(pin) {
    			@Override
    			protected boolean onTap(int index) {
    				OverlayItem item = (OverlayItem)getItem(index);
    				map.getController().setCenter(item.getPoint());
    				return super.onTap(index);
    			}
    		};
    		map.getOverlays().add(overlay);
    		overlay.addPin(point,
    				"位置情報:" + new Date(location.getTime()).toLocaleString(),
    				"緯度:" + location.getLatitude() + "経度:" + location.getLongitude() + "\n精度:±" + location.getAccuracy() + "m");
    		map.getController().setCenter(point);
   // 		TextView next = (TextView)findViewById(R.id.next);
    //		next.setText("緯度:" + location.getLatitude() + "経度:" + location.getLongitude());
    	}
    	map.invalidate();
    }
}	