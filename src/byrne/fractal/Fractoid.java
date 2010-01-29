/*
This file is part of Fractoid
Copyright (C) 2010 David Byrne

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

public class Fractoid extends Activity {

  private FractalView fractalView;
  private MenuItem item2, item3, item4, item5, item6, item7, item8, itemManowar, itemPhoenix;
  private MenuItem itemRainbow, itemRed, itemGreen, itemYellow, itemBlackAndWhite, itemWinter;
  private Button juliaButton;
  private final int MAX_ITERATIONS_DIALOG = 1;

  @Override public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    
    setContentView(R.layout.main_layout);
    
    fractalView = (FractalView) findViewById(R.id.mFractalView);
    
    juliaButton = (Button) findViewById(R.id.juliaButton);
    juliaButton.setOnClickListener(new View.OnClickListener() {
      public void onClick(View v) {
	setJuliaButtonEnabled(false);
	fractalView.setType(FractalType.JULIA);
	fractalView.setZoom(false);
	fractalView.postInvalidate();
      }
    });
    
    final Button zoomOutButton = (Button) findViewById(R.id.zoomOutButton);
    zoomOutButton.setOnClickListener(new View.OnClickListener() {
      public void onClick(View v) {
	fractalView.zoomOut();
      }
    });
    
    Eula.showEula(this);

  }
  
  @Override public void onResume() {
    super.onResume();
    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
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
  
  public Uri saveImage() {
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
  
  public void switchEquation(MenuItem item, ComplexEquation e) {
    if (!item.isChecked()) {
      item.setChecked(true);
      fractalView.setEquation(e);
      setJuliaButtonEnabled(true);
      fractalView.resetCoords();
    }
  }
  
  @Override public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.options_menu, menu);
    item2 = menu.findItem(R.id.z2_button);
    item3 = menu.findItem(R.id.z3_button);
    item4 = menu.findItem(R.id.z4_button);
    item5 = menu.findItem(R.id.z5_button);
    item6 = menu.findItem(R.id.z6_button);
    item7 = menu.findItem(R.id.z4z3z2_button);
    item8 = menu.findItem(R.id.z6z2_button);
    itemManowar = menu.findItem(R.id.manowar_button);
    itemPhoenix = menu.findItem(R.id.phoenix_button);
    itemRainbow = menu.findItem(R.id.rainbow_button);
    itemWinter = menu.findItem(R.id.winter_button);
    itemRed = menu.findItem(R.id.red_button);
    itemGreen = menu.findItem(R.id.green_button);
    itemYellow = menu.findItem(R.id.yellow_button);
    itemBlackAndWhite = menu.findItem(R.id.black_and_white_button);
    
    return true;
  }
  
  public boolean onOptionsItemSelected(MenuItem item) {

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
      switchColorSet(itemRainbow,ColorSet.RAINBOW);
      return true;
    
    case R.id.winter_button:
      switchColorSet(itemWinter,ColorSet.WINTER);
      return true;
  
    case R.id.red_button:
      switchColorSet(itemRed,ColorSet.RED);
      return true;
    
    case R.id.green_button:
      fractalView.setColorSet(ColorSet.GREEN);
      itemGreen.setChecked(true);
      return true;
    
    case R.id.yellow_button:
      switchColorSet(itemYellow,ColorSet.YELLOW);
      return true;
    
    case R.id.black_and_white_button:
      switchColorSet(itemBlackAndWhite,ColorSet.BLACK_AND_WHITE);
      return true;
   
    case R.id.z2_button:          
      switchEquation(item2,ComplexEquation.SECOND_ORDER);  
      return true;
    
    case R.id.z3_button:
      switchEquation(item3,ComplexEquation.THIRD_ORDER);
      return true;
    
    case R.id.z4_button:
      switchEquation(item4,ComplexEquation.FOURTH_ORDER);
      return true;
    
    case R.id.z5_button:
      switchEquation(item5,ComplexEquation.FIFTH_ORDER);
      return true;
    
    case R.id.z6_button:
      switchEquation(item6,ComplexEquation.SIXTH_ORDER);
      return true;
   
    case R.id.z4z3z2_button:
      switchEquation(item7,ComplexEquation.Z4Z3Z2);
      return true;
    
    case R.id.z6z2_button:
      switchEquation(item8,ComplexEquation.Z6Z2);
      return true;
    
    case R.id.manowar_button:
      switchEquation(itemManowar,ComplexEquation.MANOWAR);
      return true;
      
    case R.id.phoenix_button:
      if (!itemPhoenix.isChecked()) {
	itemPhoenix.setChecked(true);
	fractalView.setEquation(ComplexEquation.PHOENIX);
	setJuliaButtonEnabled(false);
	fractalView.resetCoords();
      }
      return true;

    case R.id.share_button:
	
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