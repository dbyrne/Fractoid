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
  
  FractalParameters params;
  FractalView fractalView;
  long startTime;
  NativeLib mNativeLib;
  Paint paint;
  int[] colors;
  int prog = 0;
  
  public GenerateFractalTask(FractalParameters p, FractalView fv) {
    params = p;
    fractalView = fv;
    colors = params.getColorSet();
    paint = new Paint();
    paint.setStyle(Paint.Style.FILL_AND_STROKE);
    
    mNativeLib = new NativeLib();
  }
    
  private Bitmap createBitmap() {
    
    double realmin = mNativeLib.getRealMin();
    double realmax = mNativeLib.getRealMax();
    double imagmin = mNativeLib.getImagMin();
    double imagmax = mNativeLib.getImagMax();
    
    double xtmp = 0;
    
    int xres = mNativeLib.getXRes();
    int yres = mNativeLib.getYRes();
    
    Bitmap b = Bitmap.createBitmap(xres, yres, Bitmap.Config.ARGB_8888);
    Canvas c = new Canvas(b);
    
    int[] rowColors;

    double x=-1, y=-1, prev_x = -1, prev_y =-1,tmp_prev_x,tmp_prev_y, mu = 1;
    int index;
    boolean lessThanMax;

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
          fractalView.greenLight();
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
        
        //TODO Find a more elegant way to handle 2x2 and 1x1 rendering
        int step = 1;
        if (state > 0)
          step = 2;
        for(int col=(state%2); col < xres; col = col+step) {
          
          if (rowColors[col] >= 0) {
              paint.setColor(colors[(rowColors[col]%10200)/10]);  
          } else {
            paint.setColor(Color.BLACK);
          }
          //TODO Store results so color changes don't require recalculation
          c.drawPoint(col,row,paint);
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
    fractalView.greenLight();
    fractalView.invalidate();
  }  
}
