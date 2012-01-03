package test.test;

import java.util.Calendar;
import java.util.List;

import test.test.R.id;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.TextView;

public class SensorTestActivity extends Activity
    implements SensorEventListener { 
    private boolean mRegisteredSensor;
    private SensorManager mSensorManager;
    int time=0,s_time=0;
    int x=0,y=0,z=0;
    boolean countflag = true;
    boolean subflag = true;
    int s_hour=0,s_minute=0,s_second=0;
    int stop_time[] = new int[1000];
    int stopcount=0;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        mRegisteredSensor = false;
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        {
            List<Sensor> sensors = mSensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER);  
            if (sensors.size() > 0) {
                Sensor sensor = sensors.get(0);
                mRegisteredSensor = mSensorManager.registerListener(this,
                    sensor,
                    SensorManager.SENSOR_DELAY_FASTEST);
                mRegisteredSensor = true;
            }
        }
    }
    @Override
    protected void onPause() {
        if (mRegisteredSensor) {
            mSensorManager.unregisterListener(this);
            mRegisteredSensor = false;
        }
        super.onPause();
    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
    @Override
    public void onSensorChanged(SensorEvent event) {
    	final Calendar calendar = Calendar.getInstance();
        TextView log = (TextView)findViewById(id.log);
        TextView log1 = (TextView)findViewById(id.log1);
        TextView log2 = (TextView)findViewById(id.log2);
        {
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            	final int hour = calendar.get(Calendar.HOUR_OF_DAY);
            	final int minute = calendar.get(Calendar.MINUTE);
            	final int second = calendar.get(Calendar.SECOND); 
            	z=Integer.valueOf((int) event.values[2]).intValue();
            	y=Integer.valueOf((int) event.values[1]).intValue();
            	x=Integer.valueOf((int) event.values[0]).intValue();
            	if(z<=1&&z>=-1) {
            		if(countflag) {
            			s_hour=hour;
            			s_minute=minute;
            			s_second=second;
            			countflag=false;
            			subflag=true;
            			s_time=3600*s_hour+60*s_minute+s_second;
            		}
            	}
            	else{
            		if(subflag) {
            			countflag=true;
            			subflag=false;
            			time=3600*hour+60*minute+second;
            			stop_time[stopcount]=time-s_time;
            			if(stop_time[stopcount]>=5) {
            				stopcount++;
            			}
            		}
            	}
            	log.setText(x+", "+y+", "+z+"\n");
    			log1.setText(s_time+", "+time+"\n");
    			log2.setText("停止回数："+stopcount+", 停止時間："+stop_time[stopcount]+"\n");
            }
        }
    }
}