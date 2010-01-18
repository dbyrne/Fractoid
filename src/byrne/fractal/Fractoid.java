package byrne.fractal;

import java.io.OutputStream;
import android.net.Uri;
import android.app.Activity;
import android.app.WallpaperManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.os.Bundle;
import android.view.Window;
import android.view.Menu;
import android.view.MenuItem;
import android.provider.MediaStore.Images.Media;
import android.content.ContentValues;

public class Fractoid extends Activity {
    
  FractalView fractalView;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    fractalView = new FractalView(this);
    setContentView(fractalView);
    Eula.showEula(this);

  }
  
  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    menu.add(0, FractalConstants.RESET_IMAGE, 0, "Reset");
    menu.add(0, FractalConstants.JULIA_MODE, 0, "Julia Set Mode");
    menu.add(0, FractalConstants.SAVE_IMAGE, 0, "Save Image");
    menu.add(0, FractalConstants.SET_WALLPAPER, 0, "Set As Wallpaper");
    return true;
  }
  
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
    case FractalConstants.RESET_IMAGE:
      fractalView.resetCoords();
      return true;

    case FractalConstants.JULIA_MODE:
      fractalView.setMode(FractalConstants.JULIA_MODE);
      fractalView.setZoom(false);
      fractalView.postInvalidate();
      return true;

    case FractalConstants.SAVE_IMAGE:
      try {
	ContentValues values = new ContentValues(3);
	values.put(Media.DISPLAY_NAME, "Fractal");
	values.put(Media.DESCRIPTION, "Generated using Fractoid");
	values.put(Media.MIME_TYPE, "image/png");
	Uri uri = getContentResolver().insert(Media.EXTERNAL_CONTENT_URI, values);
        OutputStream os = getContentResolver().openOutputStream(uri);
	fractalView.getFractal().compress(CompressFormat.PNG, 100, os);
	os.flush();
        os.close();
      } catch (Exception e) {
        System.out.println(e);
      }
      return true;

    case FractalConstants.SET_WALLPAPER:
      WallpaperManager wm = WallpaperManager.getInstance(this);
      try {
        wm.setBitmap(fractalView.getFractal());
      } catch (Exception e) {
	System.out.println("IOException: " + e);
      }
      return true;
    }
    return false;
  }
}