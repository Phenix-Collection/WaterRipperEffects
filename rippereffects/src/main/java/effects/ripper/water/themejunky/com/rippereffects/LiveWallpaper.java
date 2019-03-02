package effects.ripper.water.themejunky.com.rippereffects;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import android.widget.Toast;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.badlogic.gdx.backends.android.AndroidLiveWallpaperService;

import java.io.File;

import static android.content.Context.WINDOW_SERVICE;


public class LiveWallpaper extends AndroidLiveWallpaperService {

	public static final String SHARED_PREFS_NAME="photo_in_water_settings";
	public static AssetManager astMgr = null;
	public static String tmp_path = "";
	public static Display display = null;
	public static LiveWallpaper instance = null;

	@Override
	public ApplicationListener createListener () {
		Log.d("asfdasd","createListener");
		Toast.makeText(this,"Please,slide on the screen.",Toast.LENGTH_LONG).show();
		astMgr = getAssets();
		SharedPreferences customSharedPreference = getSharedPreferences(LiveWallpaper.SHARED_PREFS_NAME, Context.MODE_PRIVATE);
		WaterRipples.background_path = customSharedPreference.getString("background_image", "stones.jpg");


		WaterRipples.ripple_size = Integer.parseInt(customSharedPreference.getString("ripple_size", "3"));

		DisplayMetrics dm = new DisplayMetrics();
		WaterRipples.portrait = (dm.widthPixels < dm.heightPixels)? 1 :0;
	    Log.d("turtle_screen", "" + dm.widthPixels + dm.heightPixels);
		return new WaterRipples();
	}

	@Override
	public AndroidApplicationConfiguration createConfig () {
		AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
		config.useGL20 = false;

		return config;
	}

	public void offsetChange (ApplicationListener listener, float xOffset, float yOffset, float xOffsetStep, float yOffsetStep,
		int xPixelOffset, int yPixelOffset) {
		//Gdx.app.log("LiveWallpaper", "offset changed: " + xOffset + ", " + yOffset);
	}

	@Override
	public void onCreate () {
		tmp_path = this.getFilesDir().getAbsolutePath();
		new File(tmp_path).mkdirs();
		LiveWallpaper.display = ((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay();
		LiveWallpaper.instance = this;
		super.onCreate();


	    WaterRipples.background_path = "stones.jpg";
	   
	}

}