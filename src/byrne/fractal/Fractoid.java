/*
This file is part of Fractoid
Copyright (C) 2010 David Byrne
david.r.byrne@gmail.com

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package byrne.fractal;

import java.io.OutputStream;
import android.net.Uri;
import android.app.Activity;
import android.app.WallpaperManager;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.ActivityNotFoundException;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.os.Bundle;
import android.view.Window;
import android.view.Menu;
import android.view.SubMenu;
import android.view.MenuItem;
import android.view.MenuInflater;
import android.view.View;
import android.view.WindowManager;
import android.provider.MediaStore.Images.Media;
import android.content.ContentValues;
import android.widget.Button;
import android.widget.EditText;
import byrne.fractal.MultiTouchController;
import byrne.fractal.MultiTouchController.MultiTouchObjectCanvas;
import byrne.fractal.MultiTouchController.PointInfo;
import byrne.fractal.MultiTouchController.PositionAndScale;
//import android.os.Debug;

public class Fractoid extends Activity {

  private FractalView fractalView;
  private Button juliaButton, calibrateButton;
  MenuItem itemPhoenix;
  boolean relativeColors = false;
  private final int MAX_ITERATIONS_DIALOG = 1;
  private final int TRAP_FACTOR_DIALOG = 2;

  @Override public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
    
    setContentView(R.layout.main_layout);
    
    //Debug.startMethodTracing("calc");
    
    fractalView = (FractalView) findViewById(R.id.mFractalView);
    fractalView.setFractoid(this);
    
    juliaButton = (Button) findViewById(R.id.juliaButton);
    juliaButton.setOnClickListener(new View.OnClickListener() {
      public void onClick(View v) {
	setJuliaButtonEnabled(false);
	fractalView.setZoom(false);
	fractalView.postInvalidate();
      }
    });

    calibrateButton = (Button) findViewById(R.id.calibrateButton);
    calibrateButton.setOnClickListener(new View.OnClickListener() {
      public void onClick(View v) {
	if (!relativeColors) {
	  relativeColors = true;
	} else {
	  relativeColors = false;
	}
	fractalView.setRelative(relativeColors);
	fractalView.startFractalTask(false);
	fractalView.postInvalidate();
      }
    });
    
    Eula.showEula(this);

  }
  
  @Override public void onResume() {
    super.onResume();
    //new NativeLib().resetValues();
  }
  
  @Override public void onPause() {
    super.onPause();
    //Debug.stopMethodTracing();
  }
  
  @Override public void onDestroy() {
    super.onDestroy();
    fractalView.stopFractalTask();
    new NativeLib().freeValues();
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
    case TRAP_FACTOR_DIALOG:
      dialog = new Dialog(this);

      dialog.setContentView(R.layout.trap_factor_dialog);
      dialog.setTitle("Set Trap Density (default: 1)");
      
      final EditText trapFactorText = (EditText) dialog.findViewById(R.id.trapFactorText);
      trapFactorText.setText(Integer.toString(fractalView.getTrapFactor()));
      
      final Button setTrapFactorButton = (Button) dialog.findViewById(R.id.setTrapFactorButton);
      setTrapFactorButton.setOnClickListener(new View.OnClickListener() {
	public void onClick(View v) {
	  try {
	     fractalView.setTrapFactor(Integer.parseInt(trapFactorText.getText().toString()));
	     dismissDialog(TRAP_FACTOR_DIALOG);
	     fractalView.startFractalTask(true);
	  } catch (NumberFormatException e) {System.out.println(e);}
	}
      });
      break;
      
    default:
        dialog = null;
    }
    return dialog;
  }
  
  public Uri saveImage() {
    //TODO Display an error dialog if user tries to save while image is being rendered
    Uri uri = null;
    try{
      ContentValues values = new ContentValues(3);
      values.put(Media.DISPLAY_NAME, "Fractal");
      values.put(Media.DESCRIPTION, "Generated using Fractoid");
      values.put(Media.MIME_TYPE, "image/png");
      uri = getContentResolver().insert(Media.EXTERNAL_CONTENT_URI, values);
      OutputStream os = getContentResolver().openOutputStream(uri);
      fractalView.getFractal().compress(CompressFormat.PNG, 100, os);
      os.flush();
      os.close();
    } catch (Exception e) {
      System.out.println(e);
    }
    return uri;
  }
  
  public void setCalibrateButtonEnabled(boolean b, boolean r) {
    if (b) {
      relativeColors = r;
      if (relativeColors)
	calibrateButton.setText("Absolute Colors");
      else
	calibrateButton.setText("Relative Colors");
      calibrateButton.setVisibility(View.VISIBLE);     
    } else {
      calibrateButton.setVisibility(View.INVISIBLE);
    }
    calibrateButton.setEnabled(b);
  }
  
  public void setJuliaButtonEnabled(boolean b) {   
    if (b) {
      juliaButton.setVisibility(View.VISIBLE);     
    } else {
      juliaButton.setVisibility(View.INVISIBLE);
    }
    juliaButton.setEnabled(b);
  }
  
  public void switchColorSet(MenuItem item, ColorSet cs) {
    if (!item.isChecked()) {
      fractalView.setColorSet(cs);
      item.setChecked(true);
    }
  }
  
  public void switchAlgorithm(MenuItem item, Algorithm alg) {
    if (!item.isChecked()) {
      fractalView.setAlgorithm(alg);
      item.setChecked(true);
    }
  }
  
  public void switchEquation(MenuItem item, ComplexEquation e) {
    if (!item.isChecked()) {
      item.setChecked(true);
      fractalView.setEquation(e);
      if (item == itemPhoenix) {
	/*
	The mandelbrot fractal for this equation is ugly so
	we only allow the user to explore the Julia version
        */
	setJuliaButtonEnabled(false);
      } else {
	setJuliaButtonEnabled(true);
      }
      fractalView.resetCoords();
    }
  }
  
  @Override public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.options_menu, menu);
    itemPhoenix = menu.findItem(R.id.phoenix_button);

    return true;
  }
  
  public boolean onOptionsItemSelected(MenuItem item) {
    
    if (fractalView.getType() == FractalType.MANDELBROT) {
      setJuliaButtonEnabled(true);
      fractalView.setZoom(true);
    }

    switch (item.getItemId()) {
    case R.id.reset_button:
      if (!itemPhoenix.isChecked())
	setJuliaButtonEnabled(true);
      fractalView.resetCoords();
      return true;
    
    case R.id.max_iteration_button:
      showDialog(MAX_ITERATIONS_DIALOG);
      return true;
    
    case R.id.rainbow_button:
      switchColorSet(item,ColorSet.RAINBOW);
      return true;
    
    case R.id.winter_button:
      switchColorSet(item,ColorSet.WINTER);
      return true;
    
    case R.id.summer_button:
      switchColorSet(item,ColorSet.SUMMER);
      return true;
    
    case R.id.orange_button:
      switchColorSet(item,ColorSet.ORANGE);
      return true;
    
    case R.id.night_sky_button:
      switchColorSet(item,ColorSet.NIGHT_SKY);
      return true;
  
    case R.id.red_button:
      switchColorSet(item,ColorSet.RED);
      return true;
    
    case R.id.green_button:
      fractalView.setColorSet(ColorSet.GREEN);
      item.setChecked(true);
      return true;
    
    case R.id.yellow_button:
      switchColorSet(item,ColorSet.YELLOW);
      return true;
    
    case R.id.black_and_white_button:
      switchColorSet(item,ColorSet.BLACK_AND_WHITE);
      return true;
   
    case R.id.z2_button:          
      switchEquation(item,ComplexEquation.SECOND_ORDER);  
      return true;
    
    case R.id.z3_button:
      switchEquation(item,ComplexEquation.THIRD_ORDER);
      return true;
    
    case R.id.z4_button:
      switchEquation(item,ComplexEquation.FOURTH_ORDER);
      return true;
    
    case R.id.z5_button:
      switchEquation(item,ComplexEquation.FIFTH_ORDER);
      return true;
    
    case R.id.z6_button:
      switchEquation(item,ComplexEquation.SIXTH_ORDER);
      return true;
   
    case R.id.z4z3z2_button:
      switchEquation(item,ComplexEquation.Z4Z3Z2);
      return true;
    
    case R.id.z6z2_button:
      switchEquation(item,ComplexEquation.Z6Z2);
      return true;
    
    case R.id.burning_ship_button:
      switchEquation(item,ComplexEquation.BURNING_SHIP);
      return true;
    
    case R.id.manowar_button:
      switchEquation(item,ComplexEquation.MANOWAR);
      return true;
    
    case R.id.phoenix_button:
      switchEquation(item,ComplexEquation.PHOENIX);
      return true;
    
    case R.id.escape_time_button:
      switchAlgorithm(item,Algorithm.ESCAPE_TIME);
      return true;
    case R.id.gaussian_minimum_button:
      switchAlgorithm(item,Algorithm.GAUSSIAN_MINIMUM);
      //showDialog(TRAP_FACTOR_DIALOG);
      return true;
    case R.id.gaussian_average_button:
      switchAlgorithm(item,Algorithm.GAUSSIAN_AVERAGE);
      //showDialog(TRAP_FACTOR_DIALOG);
      return true;
    case R.id.epsilon_cross_button:
      switchAlgorithm(item,Algorithm.EPSILON_CROSS);
      return true;
    case R.id.epsilon_cross_bailout_button:
      switchAlgorithm(item,Algorithm.EPSILON_CROSS_BAILOUT);
      return true;
    case R.id.combo_trap_button:
      switchAlgorithm(item,Algorithm.COMBO_TRAP);
      return true;

    case R.id.share_button:
      //TODO Find a way to share image without saving it first
      Uri fractal = saveImage();
      try {
	Intent intent = new Intent(Intent.ACTION_SEND);
	intent.putExtra(Intent.EXTRA_STREAM,fractal);
	intent.setType("image/png");
	startActivity(Intent.createChooser(intent,"Pick an Application"));
      } catch(ActivityNotFoundException e) {}
      
      return true;

    case R.id.save_button:
      saveImage();
      return true;

    case R.id.wallpaper_button:
      //TODO Display an error dialog if user tries to set Wallpaper while image is being rendered
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