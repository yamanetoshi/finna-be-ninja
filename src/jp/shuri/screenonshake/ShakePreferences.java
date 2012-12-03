package jp.shuri.screenonshake;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.util.Log;

public class ShakePreferences extends PreferenceActivity {
	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);

		// Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new GeneralPreferenceFragment())
                .commit();
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public static class GeneralPreferenceFragment extends PreferenceFragment 
		implements OnSharedPreferenceChangeListener {
		private String TAG = "GeneralPreferenceFragment";
		
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.pref_general);
		}

		@Override
		public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
				String key) {
			Log.d(TAG, "onSharedPreferenceChanged");
	        if (key.equals(getText(R.string.pref_key_social_recommendations))) {
	        	Log.d(TAG, "settings changed");
	        	CheckBoxPreference tmp = (CheckBoxPreference) findPreference(key);
	        	Log.d(TAG, "tmp.isChecked() is " + tmp.isChecked());
	        	Context ctx = (Context)GeneralPreferenceFragment.this.getActivity();
	        	
	        	if (tmp.isChecked()) {
	        		Log.d(TAG, "startService");
	        		ctx.startService(new Intent(ctx, ShakeWatchService.class));
	        	} else {
	        		Log.d(TAG, "stopService");
	        		ctx.stopService(new Intent(ctx, ShakeWatchService.class));
	        	}
	        }
		}
		
		@Override
		public void onResume() {
		    super.onResume();
		    getPreferenceScreen().getSharedPreferences()
		            .registerOnSharedPreferenceChangeListener(this);
		}

		@Override
		public void onPause() {
		    super.onPause();
		    getPreferenceScreen().getSharedPreferences()
		            .unregisterOnSharedPreferenceChangeListener(this);
		}
	}

}
