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

import android.os.AsyncTask;
import android.graphics.*;

public class GenerateFractalTask extends AsyncTask<Void, Bitmap, Bitmap> {
  
  FractalView fractalView;
  long startTime;
  NativeLib mNativeLib;
  Paint paint;
  int[] colors;
  int prog = 0;
  boolean relative;
  
  public GenerateFractalTask(FractalView fv, boolean rel) {
    fractalView = fv;
    colors = fractalView.getColorSet();
    paint = new Paint();
    paint.setStyle(Paint.Style.FILL_AND_STROKE);
    relative = rel;
    mNativeLib = new NativeLib();
  }
    
  private Bitmap createBitmap() {
    
    double realmin = mNativeLib.getRealMin();
    double realmax = mNativeLib.getRealMax();
    double imagmin = mNativeLib.getImagMin();
    double imagmax = mNativeLib.getImagMax();
    
    int xres = mNativeLib.getXRes();
    int yres = mNativeLib.getYRes();
    
    int minimum = mNativeLib.getMin();
    int maximum = mNativeLib.getMax();
    int range = maximum-minimum+1;
    
    Bitmap b = Bitmap.createBitmap(xres, yres, Bitmap.Config.ARGB_8888);
    Canvas c = new Canvas(b);
    
    int[] rowColors;

    double deltaP = (realmax - realmin)/xres;
    double deltaQ = (imagmax - imagmin)/yres;
    
    final int PASSES = 2;
    int updateCount=0;
    int state = 0;
      
    for (int rpass = 0; rpass < PASSES; rpass++) {
      paint.setStrokeWidth(PASSES-rpass);
      for (int row=0; row < yres; row += PASSES-rpass) {
        prog++;
        updateCount++;
        
        if (isCancelled()) {
          return b;
        }
  
        if (updateCount % 15 == 0) {
          this.publishProgress(b);
        }
        if (row % 2 == 0) {
          if (rpass == 0) {
            state = 2;
          } else {
            state = 1;
          }
        } else {
          state = 0;
        }
        rowColors = mNativeLib.getFractalRow(row,state);

        for(int col=0; col < xres; col++) {
     	    if (rowColors[col] >= 0) {
     	    	rowColors[col] = colors[rowColors[col]%1000];
     	    } else {
     	    	rowColors[col] = 0;
     	    }
        }
        if (rpass==0) {
            // First pass, 2x downsampled
            for(int col=0; col < xres; col+=2) {
              // Double pixels size in row
            	rowColors[col+1] = rowColors[col];
       	    }
            // And double-copy to bitmap 
            b.setPixels(rowColors, 0, xres, 0, row, xres, 1);
            b.setPixels(rowColors, 0, xres, 0, row+1, xres, 1);
        } else {
            // Copy to bitmap
            b.setPixels(rowColors, 0, xres, 0, row, xres, 1);
        }
      }
    }

    return b;
  }
  
  @Override protected void onPreExecute() {
    startTime = System.currentTimeMillis();
  }
  @Override protected Bitmap doInBackground(Void... unused) {   
    return createBitmap();
  }
  @Override protected void onProgressUpdate(Bitmap... b) {
    fractalView.setFractal(b[0]);
    fractalView.setProgress(((prog*2)/3.0f)/mNativeLib.getYRes());
    fractalView.invalidate();
  }
  @Override protected void onPostExecute(Bitmap b) {
    fractalView.setFractal(b);
    fractalView.clearBackground();
    fractalView.setTime(System.currentTimeMillis()-startTime);
    fractalView.turnCalibrateButtonOn();
    fractalView.invalidate();
  }  
}
