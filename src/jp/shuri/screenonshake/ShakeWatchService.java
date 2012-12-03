package jp.shuri.screenonshake;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class ShakeWatchService extends Service {
	private String TAG = "ShakeWatchService";

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onDestroy() {
		Log.d(TAG, "onDestroy");
	}
	
	@Override
	public void onStart(Intent intent, int startid) {
		Log.d(TAG, "onStart");
	}

}
