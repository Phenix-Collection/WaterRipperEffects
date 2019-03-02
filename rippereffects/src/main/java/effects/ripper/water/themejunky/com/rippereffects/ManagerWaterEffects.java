package effects.ripper.water.themejunky.com.rippereffects;

import android.app.Activity;
import android.app.WallpaperManager;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Build;
import android.widget.Toast;



/**
 * Created by Junky2 on 4/26/2018.
 */

public class ManagerWaterEffects {
    public ManagerWaterEffects() {

    }
    public void setWaterEffects(Activity activity){
        try {
            if (Build.VERSION.SDK_INT >= 16) {

                Intent intent = new Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER);
                intent.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                        new ComponentName(activity, LiveWallpaper.class));
                activity.startActivity(intent);

            }

            else {
                Toast.makeText(activity, R.string.app_name, 1).show();
                activity.startActivity(new Intent("android.service.wallpaper.LIVE_WALLPAPER_CHOOSER"));

                return;

            }

        } catch (Exception e4) {
            return;
        }
    }
}
