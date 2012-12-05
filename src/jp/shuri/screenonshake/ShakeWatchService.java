package jp.shuri.screenonshake;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;

@SuppressLint("Wakelock")
public class ShakeWatchService extends Service {
	private String TAG = "ShakeWatchService";
	
	private boolean mRegisteredSensor;
	private SensorManager mySensorManager;
	private PowerManager mPowerManager;
	private PowerManager.WakeLock mWakelock;
    
    /* Here we store the current values of acceleration, one for each axis */
    private float xAccel;
    private float yAccel;
    private float zAccel;

    /* And here the previous ones */
    private float xPreviousAccel;
    private float yPreviousAccel;
    private float zPreviousAccel;

    /* Used to suppress the first shaking */
    private boolean firstUpdate = true;

    /*What acceleration difference would we assume as a rapid movement? */
    private final float shakeThreshold = 1.5f;
    
    /* Has a shaking motion been started (one direction) */
    private boolean shakeInitiated = false;
    
    // BroadcastReceiver for handling ACTION_SCREEN_OFF.
    public BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Check action just to be on the safe side.
            if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                Log.v("shake mediator screen off","trying re-registration");

                stopWatch();
                startWatch();
            }
        }
    };
    
    private void stopWatch(){
        mySensorManager.unregisterListener(mySensorEventListener);
        mWakelock = null;
    }
    
    private void startWatch(){
    	mySensorManager.registerListener(mySensorEventListener, mySensorManager
				.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
				SensorManager.SENSOR_DELAY_NORMAL);
    	
        mWakelock = mPowerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK
        		| PowerManager.ACQUIRE_CAUSES_WAKEUP
                | PowerManager.ON_AFTER_RELEASE, TAG);    
    }


    @Override
	public void onCreate() {
		super.onCreate();
		
		mRegisteredSensor = false;
		
        mySensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mPowerManager = (PowerManager) getSystemService(POWER_SERVICE);
        
        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
        registerReceiver(mReceiver, filter);
	}

	private final SensorEventListener mySensorEventListener = new SensorEventListener() {

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
                // TODO Auto-generated method stub
                
        }

        @Override
        public void onSensorChanged(SensorEvent event) {
                updateAccelParameters(event.values[0], event.values[1], event.values[2]);
                if ((!shakeInitiated) && isAccelerationChanged()) {
                        shakeInitiated = true; 
                } else if ((shakeInitiated) && isAccelerationChanged()) {
                        executeShakeAction();
                } else if ((shakeInitiated) && (!isAccelerationChanged())) {
                        shakeInitiated = false;
                }
        }
                        
    };

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onDestroy() {
		Log.d(TAG, "onDestroy");
        if (mRegisteredSensor) {
        	stopWatch();
            mRegisteredSensor = false;
        }
        unregisterReceiver(mReceiver);
	}
	
	@Override
	public void onStart(Intent intent, int startid) {
		Log.d(TAG, "onStart");
		startWatch();
		mRegisteredSensor = true;
	}

    /* Store the acceleration values given by the sensor */
    private void updateAccelParameters(float xNewAccel, float yNewAccel, float zNewAccel) {
            /* we have to suppress the first change of acceleration, it results from first values being initialized with 0 */
            if (firstUpdate) {  
                    xPreviousAccel = xNewAccel;
                    yPreviousAccel = yNewAccel;
                    zPreviousAccel = zNewAccel;
                    firstUpdate = false;
            } else {
                    xPreviousAccel = xAccel;
                    yPreviousAccel = yAccel;
                    zPreviousAccel = zAccel;
            }
            xAccel = xNewAccel;
            yAccel = yNewAccel;
            zAccel = zNewAccel;
    }
    
    private boolean isAccelerationChanged() {
            float deltaX = Math.abs(xPreviousAccel - xAccel);
            float deltaY = Math.abs(yPreviousAccel - yAccel);
            float deltaZ = Math.abs(zPreviousAccel - zAccel);
            return (deltaX > shakeThreshold && deltaY > shakeThreshold)
                            || (deltaX > shakeThreshold && deltaZ > shakeThreshold)
                            || (deltaY > shakeThreshold && deltaZ > shakeThreshold);
    }

    private void executeShakeAction() {
            Toast.makeText(this, "shaked", Toast.LENGTH_SHORT).show();
            mWakelock.acquire();
    }
}
