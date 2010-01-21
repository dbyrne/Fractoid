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
import android.view.SubMenu;
import android.view.MenuItem;
import android.provider.MediaStore.Images.Media;
import android.content.ContentValues;

public class Fractoid extends Activity {
    
  private FractalView fractalView;
  private MenuItem item2, item3, item4, item5, item6, item7, juliaItem;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    fractalView = new FractalView(this);
    setContentView(fractalView);
    Eula.showEula(this);

  }
  
  public void setJuliaMenuEnabled(boolean b) {
    juliaItem.setVisible(b);
    juliaItem.setEnabled(b);
  }
  
  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    
    menu.add(0, FractalConstants.RESET_IMAGE, 0, "Reset");
    juliaItem = menu.add(0, FractalConstants.JULIA_MODE, 0, "Julia Set Mode");
    if (fractalView.getMode() == FractalConstants.JULIA_MODE) {
      setJuliaMenuEnabled(false);
    } else {
      setJuliaMenuEnabled(true);
    }
    
    SubMenu subMenu = menu.addSubMenu("Change Equation");
    item2 = subMenu.add(1, FractalConstants.SECOND_ORDER, 0, "Z^2 + C");
    item3 = subMenu.add(1, FractalConstants.THIRD_ORDER, 0, "Z^3 + C");
    item4 = subMenu.add(1, FractalConstants.FOURTH_ORDER, 0, "Z^4 + C");
    item5 = subMenu.add(1, FractalConstants.FIFTH_ORDER, 0, "Z^5 + C");
    item6 = subMenu.add(1, FractalConstants.SIXTH_ORDER, 0, "Z^6 + C");
    item7 = subMenu.add(1, FractalConstants.Z4Z3Z2, 0, "Z^4 - Z^3 - Z^2 + C");
    if (fractalView.getEquation() == FractalConstants.SECOND_ORDER) {
      item2.setChecked(true);
    } else if (fractalView.getEquation() == FractalConstants.THIRD_ORDER) {
      item3.setChecked(true);
    } else if (fractalView.getEquation() == FractalConstants.FOURTH_ORDER) {
      item4.setChecked(true);
    } else if (fractalView.getEquation() == FractalConstants.FIFTH_ORDER) {
      item5.setChecked(true);
    } else if (fractalView.getEquation() == FractalConstants.SIXTH_ORDER) {
      item6.setChecked(true);
    } else if (fractalView.getEquation() == FractalConstants.Z4Z3Z2) {
      item7.setChecked(true);
    }
    subMenu.setGroupCheckable(1, true, true);
    
    menu.add(0, FractalConstants.SAVE_IMAGE, 0, "Save Image");
    menu.add(0, FractalConstants.SET_WALLPAPER, 0, "Set As Wallpaper");
    
    return true;
  }
  
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
    case FractalConstants.RESET_IMAGE:
      setJuliaMenuEnabled(true);
      fractalView.resetCoords();
      return true;

    case FractalConstants.JULIA_MODE:
      setJuliaMenuEnabled(false);
      fractalView.setMode(FractalConstants.JULIA_MODE);
      fractalView.setZoom(false);
      fractalView.postInvalidate();
      return true;
   
    case FractalConstants.SECOND_ORDER:     
      if (!item2.isChecked()) {
        item2.setChecked(true);
	fractalView.setEquation(FractalConstants.SECOND_ORDER);
	setJuliaMenuEnabled(true);
	fractalView.resetCoords();
      }
      return true;
    
    case FractalConstants.THIRD_ORDER:     
      if (!item3.isChecked()) {
        item3.setChecked(true);
	fractalView.setEquation(FractalConstants.THIRD_ORDER);
	setJuliaMenuEnabled(true);
	fractalView.resetCoords();
      }
      return true;
    
    case FractalConstants.FOURTH_ORDER:     
      if (!item4.isChecked()) {
        item4.setChecked(true);
	fractalView.setEquation(FractalConstants.FOURTH_ORDER);
	setJuliaMenuEnabled(true);
	fractalView.resetCoords();
      }
      return true;
    
    case FractalConstants.FIFTH_ORDER:
      if (!item5.isChecked()) {
        item5.setChecked(true);
        fractalView.setEquation(FractalConstants.FIFTH_ORDER);
	setJuliaMenuEnabled(true);
	fractalView.resetCoords();
      }
      return true;
    
    case FractalConstants.SIXTH_ORDER:
      if (!item6.isChecked()) {
        item6.setChecked(true);
        fractalView.setEquation(FractalConstants.SIXTH_ORDER);
	setJuliaMenuEnabled(true);
	fractalView.resetCoords();
      }
      return true;
   
    case FractalConstants.Z4Z3Z2:
      if (!item7.isChecked()) {
        item7.setChecked(true);
        fractalView.setEquation(FractalConstants.Z4Z3Z2);
	setJuliaMenuEnabled(true);
	fractalView.resetCoords();
      }
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