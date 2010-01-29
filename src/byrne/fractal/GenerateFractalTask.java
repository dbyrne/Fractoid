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

import android.os.AsyncTask;
import android.graphics.*;

public class GenerateFractalTask extends AsyncTask<Void, Bitmap, Bitmap> {
  
  FractalParameters params;
  FractalView fractalView;
  long startTime;
  
  public GenerateFractalTask(FractalParameters p, FractalView fv) {
    params = p;
    fractalView = fv;
  }
  
  private int[] calculateColors() {
    
    double red, green, blue;    
    final int numberOfColors = params.getMaxIterations()*10;
    int[] colorIntegers = new int[numberOfColors];   
    double shiftFactor = params.getShiftFactor();
    
    switch (params.getColorSet()) {
      case RAINBOW:
        for (int x = 0; x < numberOfColors; x++) {
          double data = x;
          data = 2*Math.PI*(data/500);
          red = Math.sin(data + Math.PI*shiftFactor);
          green = Math.cos(data + Math.PI*shiftFactor);
          blue = -((red + green)*.707);
          red = (red*127.0) + 127.0;
          green = (green*127.0) + 127.0;
          blue = (blue*127.0) + 127.0;
          colorIntegers[x] = Color.rgb((int)red, (int)green, (int)blue);
        }    
        break;
      
      case WINTER:
        for (int x = 0; x < numberOfColors; x++) {
          int value = x%510;
          int color;
          if (value <= 255)
            color = Math.abs(value);
          else color = Math.abs(255-(value-255));
          colorIntegers[x] = Color.rgb(255-color,255-color,255-color/3);
        }
        break;
      case RED:
        for (int x = 0; x < numberOfColors; x++) {
          int value = x%510;
          int color;
          if (value <= 255)
            color = Math.abs(value);
          else color = Math.abs(255-(value-255));
          colorIntegers[x] = Color.rgb(color,0,0);
        }
        break;
      
      case GREEN:
        for (int x = 0; x < numberOfColors; x++) {
          int value = x%510;
          int color;
          if (value <= 255)
            color = Math.abs(value);
          else color = Math.abs(255-(value-255));
          colorIntegers[x] = Color.rgb(0,color,0);
        }
        break;
      
      case YELLOW:
        for (int x = 0; x < numberOfColors; x++) {
          int value = x%510;
          int color;
          if (value <= 255)
            color = Math.abs(value);
          else color = Math.abs(255-(value-255));
          colorIntegers[x] = Color.rgb(color,color,0);
        }
        break;
        
      case BLACK_AND_WHITE:
        for (int x = 0; x < numberOfColors; x++) {
          int value = x%510;
          int color = Math.abs(255-value);
          colorIntegers[x] = Color.rgb(color,color,color);
        }     
    }
    return colorIntegers;   
  }
  

  private Bitmap createBitmap() {
    
    ComplexEquation equation = params.getEquation();
    
    double realmin = params.getRealMin();
    double realmax = params.getRealMax();
    double imagmin = params.getImagMin();
    double imagmax = params.getImagMax();
    
    double P = params.getP();
    double Q = params.getQ();
    
    double xtmp = 0;
    
    int xres = params.getXRes();
    int yres = params.getYRes();
    FractalType type = params.getType();
    
    Bitmap b = Bitmap.createBitmap(xres, yres, Bitmap.Config.ARGB_8888);
    Canvas c = new Canvas(b);
    
    Paint paint = new Paint();
    paint.setStyle(Paint.Style.FILL_AND_STROKE);
    paint.setStrokeWidth(1);
    
    int[] colorIntegers = calculateColors();

    double x=-1, y=-1, prev_x = -1, prev_y =-1,tmp_prev_x,tmp_prev_y, mu = 1;
    int index;
    boolean lessThanMax;

    double deltaP = (realmax - realmin)/xres;
    double deltaQ = (imagmax - imagmin)/yres;
    
    final int max = params.getMaxIterations();
    final int PASSES = 4;
    
    for (int rpass = 0; rpass < PASSES; rpass++) {
      for (int row=rpass; row < yres; row += PASSES) {
        if (row % 5 == 0) {
          if (isCancelled())
            return b;
          this.publishProgress(b);
        }
        if (type == FractalType.MANDELBROT) {
          Q = imagmax - row*deltaQ;
        }
  
        for (int col=0; col < xres; col++) {
          if (type == FractalType.MANDELBROT) {
            P = realmin + col*deltaP;
            x = y = 0.0;
            prev_x = prev_y = 0.0;
          } else if (type == FractalType.JULIA) {
  
            x = realmin + (double)col * deltaP;
            y = imagmax - (double)row * deltaQ;
            prev_x = x;
            prev_y = y;
          }
          lessThanMax = false;
          
          double xsq, ysq;
          int extraIterations = 0;
          for (index = 0; index < max; index++) {
            xsq = x*x;
            ysq = y*y;
      
            if (xsq + ysq > 4) {
              if (extraIterations == 2) {
                lessThanMax = true;
                mu = index - Math.log(Math.log(Math.sqrt(xsq + ysq)))/ Math.log(2.0);
                break;
              } else {
                extraIterations++;
                index--;
              }
            }
            
            switch (equation) {
              case SECOND_ORDER:
                xtmp = xsq - ysq + P;
                y = (2*x*y) + Q;
                break;
              case THIRD_ORDER:
                xtmp = xsq*x - 3*x*ysq + P;
                y = -ysq*y + 3*xsq*y + Q;
                break;
              case FOURTH_ORDER:
                xtmp = xsq*xsq - 6*xsq*ysq + ysq*ysq + P;
                y = 4*xsq*x*y - 4*x*ysq*y + Q;
                break;
              case FIFTH_ORDER:
                xtmp = xsq*xsq*x-10*xsq*x*ysq+5*x*ysq*ysq + P;
                y=(5*xsq*xsq*y-10*xsq*ysq*y+ysq*ysq*y) + Q;
                break;
              case SIXTH_ORDER:
                xtmp = xsq*xsq*xsq-15*xsq*xsq*ysq+15*xsq*ysq*ysq-ysq*ysq*ysq + P;
                y=(6*xsq*xsq*x*y-20*xsq*x*ysq*y+6*x*ysq*ysq*y) + Q;
                break;
              case Z4Z3Z2:
                xtmp = xsq*xsq - 6*xsq*ysq + ysq*ysq - (xsq*x - 3*x*ysq) - (xsq - ysq) + P;
                y = 4*xsq*x*y - 4*x*ysq*y - (-ysq*y + 3*xsq*y) - (2*x*y) + Q;
                break;
              case Z6Z2:
                xtmp = xsq*xsq*xsq-15*xsq*xsq*ysq+15*xsq*ysq*ysq-ysq*ysq*ysq - (xsq - ysq) + P;
                y = (6*xsq*xsq*x*y-20*xsq*x*ysq*y+6*x*ysq*ysq*y) - (2*x*y) + Q;
                break;
              case MANOWAR:
                tmp_prev_x = x;
                tmp_prev_y = y;
                xtmp = (xsq - ysq) + prev_x + P;
                y = (2*x*y) + prev_y + Q;
                prev_x = tmp_prev_x;
                prev_y = tmp_prev_y;
                break;
              case PHOENIX:
                tmp_prev_x = x;
                tmp_prev_y = y;
                xtmp = (xsq - ysq) + P + Q*prev_x;
                y = (2*x*y) + Q*prev_y;
                prev_x = tmp_prev_x;
                prev_y = tmp_prev_y;
                break;
            }
            x = xtmp;
          }
  
          if (lessThanMax) {
            int colorIndex = Math.max(0,((int)Math.round(mu*10)-1));
            paint.setColor(colorIntegers[colorIndex]);
          } else {
            paint.setColor(Color.BLACK);
          }
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
    fractalView.invalidate();
  }
  
  @Override protected void onPostExecute(Bitmap bitmap) {
    fractalView.setFractal(bitmap);
    fractalView.setTime(System.currentTimeMillis()-startTime);
    fractalView.invalidate();
  }
}
