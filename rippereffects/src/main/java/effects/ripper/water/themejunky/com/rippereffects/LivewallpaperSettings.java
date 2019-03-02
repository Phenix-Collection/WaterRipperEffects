package effects.ripper.water.themejunky.com.rippereffects;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.provider.MediaStore.MediaColumns;
import android.util.Log;



public class LivewallpaperSettings extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		// TODO Auto-generated method stub
		WaterRipples.background_path = "stones.jpg";
		WaterRipples.ripple_size = Integer.parseInt(sharedPreferences.getString("ripple_size", "3"));
		
		WaterRipples.instance.updateTexture();
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		Log.d("asfdasd","onCreate 1");
		getPreferenceManager().setSharedPreferencesName(LiveWallpaper.SHARED_PREFS_NAME);
		Log.d("asfdasd","onCreate 2");
		addPreferencesFromResource(R.xml.photo_settings);
		Log.d("asfdasd","onCreate 3");
		getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
		Log.d("asfdasd","onCreate 4");

	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == 1) {
			if (resultCode == Activity.RESULT_OK) {
				Uri selectedImage = data.getData();
				String RealPath;
				SharedPreferences customSharedPreference = getSharedPreferences(LiveWallpaper.SHARED_PREFS_NAME,
						Context.MODE_PRIVATE);
				SharedPreferences.Editor editor = customSharedPreference.edit();
				RealPath = getRealPathFromURI(selectedImage);
				editor.putString("image_custom", RealPath);
				editor.commit();

			}
		}
	}
	
	
	@SuppressWarnings("deprecation")
	public String getRealPathFromURI(Uri contentUri) {
		String[] proj = { MediaColumns.DATA };
		Cursor cursor = managedQuery(contentUri, proj, // Which columns to return
				null, // WHERE clause; which rows to return (all rows)
				null, // WHERE clause selection arguments (none)
				null); // Order-by clause (ascending by name)
		int column_index = cursor.getColumnIndexOrThrow(MediaColumns.DATA);
		cursor.moveToFirst();
		return cursor.getString(column_index);
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void onDestroy() {
		getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
		super.onDestroy();
	}

}