package byrne.fractal;

import java.io.OutputStream;
import android.net.Uri;
import android.app.Activity;
import android.app.WallpaperManager;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.os.Bundle;
import android.view.Window;
import android.view.Menu;
import android.view.SubMenu;
import android.view.MenuItem;
import android.view.MenuInflater;
import android.view.View;
import android.provider.MediaStore.Images.Media;
import android.content.ContentValues;
import android.widget.Button;
import android.widget.EditText;

public class Fractoid extends Activity {
    
  private FractalView fractalView;
  private MenuItem item2, item3, item4, item5, item6, item7, juliaItem;
  private final int MAX_ITERATIONS_DIALOG = 1;

  @Override public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    
    setContentView(R.layout.main_layout);
    
    fractalView = (FractalView) findViewById(R.id.mFractalView);
    
    final Button zoomOutButton = (Button) findViewById(R.id.zoomOutButton);
    zoomOutButton.setOnClickListener(new View.OnClickListener() {
      public void onClick(View v) {
	fractalView.zoomOut();
      }
    });
    
    Eula.showEula(this);

  }
  
  @Override protected Dialog onCreateDialog(int id) {
    Dialog dialog;
    switch(id) {
    case MAX_ITERATIONS_DIALOG:
        
	dialog = new Dialog(this);

	dialog.setContentView(R.layout.max_iterations_dialog);
	dialog.setTitle("Set Max Iterations");
	
	final EditText maxIterationsText = (EditText) dialog.findViewById(R.id.maxIterationsText);
	maxIterationsText.setText(Integer.toString(fractalView.getMaxIterations()));
	
	final Button setButton = (Button) dialog.findViewById(R.id.setButton);
	setButton.setOnClickListener(new View.OnClickListener() {
	  public void onClick(View v) {
	    try {
	       fractalView.setMaxIterations(Integer.parseInt(maxIterationsText.getText().toString()));
	       dismissDialog(MAX_ITERATIONS_DIALOG);
	    } catch (NumberFormatException e) {System.out.println(e);}
	  }
	});
	
        break;
    default:
        dialog = null;
    }
    return dialog;
  }
  
  public void setJuliaMenuEnabled(boolean b) {
    juliaItem.setVisible(b);
    juliaItem.setEnabled(b);
  }
  
  @Override public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.options_menu, menu);
    juliaItem = menu.findItem(R.id.julia_button);
    item2 = menu.findItem(R.id.z2_button);
    item3 = menu.findItem(R.id.z3_button);
    item4 = menu.findItem(R.id.z4_button);
    item5 = menu.findItem(R.id.z5_button);
    item6 = menu.findItem(R.id.z6_button);
    item7 = menu.findItem(R.id.z4z3z2_button);
    
    return true;
  }
  
  public boolean onOptionsItemSelected(MenuItem item) {

    switch (item.getItemId()) {
    case R.id.reset_button:
      setJuliaMenuEnabled(true);
      fractalView.resetCoords();
      return true;
    
    case R.id.max_iteration_button:
      showDialog(MAX_ITERATIONS_DIALOG);
      return true;

    case R.id.julia_button:
      setJuliaMenuEnabled(false);
      fractalView.setMode(FractalConstants.JULIA_MODE);
      fractalView.setZoom(false);
      fractalView.postInvalidate();
      return true;
   
    case R.id.z2_button:     
      if (!item2.isChecked()) {
        item2.setChecked(true);
	fractalView.setEquation(ComplexEquation.SECOND_ORDER);
	setJuliaMenuEnabled(true);
	fractalView.resetCoords();
      }
      return true;
    
    case R.id.z3_button:     
      if (!item3.isChecked()) {
        item3.setChecked(true);
	fractalView.setEquation(ComplexEquation.THIRD_ORDER);
	setJuliaMenuEnabled(true);
	fractalView.resetCoords();
      }
      return true;
    
    case R.id.z4_button:     
      if (!item4.isChecked()) {
        item4.setChecked(true);
	fractalView.setEquation(ComplexEquation.FOURTH_ORDER);
	setJuliaMenuEnabled(true);
	fractalView.resetCoords();
      }
      return true;
    
    case R.id.z5_button:
      if (!item5.isChecked()) {
        item5.setChecked(true);
        fractalView.setEquation(ComplexEquation.FIFTH_ORDER);
	setJuliaMenuEnabled(true);
	fractalView.resetCoords();
      }
      return true;
    
    case R.id.z6_button:
      if (!item6.isChecked()) {
        item6.setChecked(true);
        fractalView.setEquation(ComplexEquation.SIXTH_ORDER);
	setJuliaMenuEnabled(true);
	fractalView.resetCoords();
      }
      return true;
   
    case R.id.z4z3z2_button:
      if (!item7.isChecked()) {
        item7.setChecked(true);
        fractalView.setEquation(ComplexEquation.Z4Z3Z2);
	setJuliaMenuEnabled(true);
	fractalView.resetCoords();
      }
      return true;

    case R.id.save_button:
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

    case R.id.wallpaper_button:
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