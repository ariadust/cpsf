package kadai.gps;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.math.BigDecimal;
import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class KadaiActivity extends Activity
implements LocationListener, Runnable, OnClickListener {
	//ロケーションマネージャーの定義
	private LocationManager locationManager;
	String path = Environment.getExternalStorageDirectory().getPath()+"/ariadustWIP.csv";
	//停止しているかどうかの判定
	boolean stop_hantei = false;
	//直前まで止まっていたかどうか
	boolean startflag = false;
	//動き出した瞬間の時間、停止した瞬間の時間
	int time=0,s_time=0;
	//停止時間用関数
	int s_hour=0,s_minute=0,s_second=0;
	//停止時間
	int stop_time[] = new int[1000];
	//停止回数
	int stopcount=0;
	//最終データ格納
	List<String>list= new ArrayList<String>();
	private Timer timer; 
	//緯度、経度
	double ido_now,keido_now,ido_old,keido_old;
	double delta_ido,delta_keido;
	int count=0;
	protected void onCreate(Bundle icicle) {
		Log.v("デバック","onCreate");
		super.onCreate(icicle);
		setContentView(R.layout.main);
		Button button = (Button)this.findViewById(R.id.button);
		button.setOnClickListener(this);
//		mRegisteredSensor = false;
		locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
		Thread th = new Thread(this);
		th.start();
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
	}
	protected void onResume() {
		super.onResume();
		Log.v("デバック","onResume");
	}
	protected boolean isRouteDisplayed() {
		return false;
	}
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}
	@Override
	public void onClick(View v){
		Log.v("debag","ファイルを作ります");
		String filePath = Environment.getExternalStorageDirectory().getPath()+"/idou.csv";
		File file = new File(filePath);
		file.getParentFile().mkdir();
		try{
			FileOutputStream fos = new FileOutputStream(file, true);
			OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
			BufferedWriter bw = new BufferedWriter(osw);
			bw.write(" \n");
			for (int i = 0; i < list.size(); i++) {
				bw.write(list.get(i)+"\n");
			}
			Log.v("debag","リストを格納し終わりました");
			bw.flush();
			bw.close();
		}catch(IOException e){
			e.printStackTrace();
		}
	} 
	public void GPS_update_by15sec() {
		Log.v("デバック","GPS_hoge 動いている");
		for(int i=0; i<15; i++) {
			try {
				Thread.sleep(1000);
				Log.v("debug","waiting....");
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			//15秒の計測中に止まった場合
			if(stop_hantei==true) {
				break;
			}
		}
		//動いてたらGPS測定
		if(stop_hantei==false) {
			Log.v("デバック","GPS_hoge GPS測定");
			list.add(System.currentTimeMillis()+","+ido_now+","+keido_now);
		}
	}   	
	@Override
	public void onLocationChanged(Location location) {	
		if(ido_now!=0&&keido_now!=0) {
			ido_old=ido_now;
			keido_old=keido_now;
			BigDecimal bi_ido_now = new BigDecimal(location.getLatitude());
			BigDecimal bi_keido_now = new BigDecimal(location.getLongitude());
			ido_now= bi_ido_now.setScale(5, BigDecimal.ROUND_DOWN).doubleValue();
			keido_now= bi_keido_now.setScale(5, BigDecimal.ROUND_DOWN).doubleValue();
			delta_ido=ido_now-ido_old;
			delta_keido=keido_now-keido_old;
			//止まってるか？
			if(delta_ido>-0.00008&& delta_ido<0.00008 && delta_keido>-0.00008 && delta_keido<0.00008) {
				if(count>3) {
					//止まった
					TextView text5 = (TextView)findViewById(R.id.text5);
					text5.setText("状態：止まったよ");
					stop_hantei=true;
					count=0;
				}else {
					count++;
				}
			}else{
				//動き出した
				TextView text5 = (TextView)findViewById(R.id.text5);
				text5.setText("状態：動いてるよ");
				stop_hantei=false;
				count=0;
			}	
		}else {
			BigDecimal bi_ido_now = new BigDecimal(location.getLatitude());
			BigDecimal bi_keido_now = new BigDecimal(location.getLongitude());
			ido_now= bi_ido_now.setScale(4, BigDecimal.ROUND_DOWN).doubleValue();
			keido_now= bi_keido_now.setScale(4, BigDecimal.ROUND_DOWN).doubleValue();
		}
		TextView text1 = (TextView)findViewById(R.id.text1);
		text1.setText("緯度："+ido_now+"　経度："+keido_now);
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
	@Override
	protected void onPause() {
		super.onPause();
	}
	@Override
	protected void onStop() {
		super.onStop();
		if (timer != null) timer.cancel();
		if (locationManager != null) locationManager.removeUpdates(this);
	}
	@Override
	public void run() {
		// TODO Auto-generated method stub
		while(true) {
			if(stop_hantei==false) {
				if(startflag) {
					list.add(System.currentTimeMillis()+
								","+ido_now+","+keido_now);
					startflag=false;
				}else {
					GPS_update_by15sec();
				}
			}
			//止まった瞬間
			else if(stop_hantei==true){
				if(startflag==false) {
					Log.v("デバック","止まった瞬間");
					startflag=true;
					stop_hantei=true;
				}
			}
		}
	}
}	