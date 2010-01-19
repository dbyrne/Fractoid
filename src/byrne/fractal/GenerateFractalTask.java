package byrne.fractal;

import android.app.ProgressDialog;
import android.content.Context;
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
    
    double shiftFactor = params.getShiftFactor();

    final int numberOfColors = params.getMaxIterations()*100;
    int[] colorIntegers = new int[numberOfColors];
    
    for (int x = 0; x < numberOfColors; x++) {
      double data = x;
      data = 2*Math.PI*(data/5000);
      red = Math.sin(data + Math.PI*shiftFactor);
      green = Math.cos(data + Math.PI*shiftFactor);
      blue = -((red + green)*.707);
      red = (red*127.0) + 127.0;
      green = (green*127.0) + 127.0;
      blue = (blue*127.0) + 127.0;
      colorIntegers[x] = Color.rgb((int)red, (int)green, (int)blue);
    }
    
    return colorIntegers;
    
  }
  
  private Bitmap createBitmap() {
    
    int order = params.getOrder();
    
    double realmin = params.getRealMin();
    double realmax = params.getRealMax();
    double imagmin = params.getImagMin();
    double imagmax = params.getImagMax();
    
    double P = params.getP();
    double Q = params.getQ();
    
    double xtmp = 0;
    
    int xres = params.getXRes();
    int yres = params.getYRes();
    int mode = params.getMode();
    
    Bitmap b = Bitmap.createBitmap(xres, yres, Bitmap.Config.ARGB_8888);
    Canvas c = new Canvas(b);
    
    Paint paint = new Paint();
    paint.setStyle(Paint.Style.FILL_AND_STROKE);
    paint.setStrokeWidth(1);
    
    int[] colorIntegers = calculateColors();

    double x=-1, y=-1, xsq, ysq, mu = 1;
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
      if (mode == FractalConstants.MANDELBROT_MODE) {
        Q = imagmax - row*deltaQ;
      }

      for (int col=0; col < xres; col++) {
        if (mode == FractalConstants.MANDELBROT_MODE) {
          x = y = 0.0;
        } else if (mode == FractalConstants.JULIA_MODE) {

          x = realmin + (double)col * deltaP;
          y = imagmax - (double)row * deltaQ;
        }
        lessThanMax = false;
        
        for (index = 0; index < max; index++) {
          xsq = x*x;
          ysq = y*y;
    
          if (xsq + ysq > 4) {
            lessThanMax = true;
            mu = index - Math.log(Math.log(Math.sqrt(xsq + ysq)))/ Math.log(2.0);
            break;
          }
          
          if (order == FractalConstants.SECOND_ORDER) {
            xtmp = xsq - ysq;
            y = (2*x*y) + Q;
          } else if (order == FractalConstants.THIRD_ORDER) {
            xtmp = x*x*x - 3*x*y*y;
            y = -y*y*y + 3*x*x*y + Q;
            
          } else if (order == FractalConstants.FOURTH_ORDER) {
            xtmp = x*x*x*x - 6*x*x*y*y + y*y*y*y;
            y = 4*x*x*x*y - 4*x*y*y*y + Q;
            
          } else if (order == FractalConstants.FIFTH_ORDER) {
            xtmp = x*x*x*x*x-10*x*x*x*y*y+5*x*y*y*y*y;
            y=(5*x*x*x*x*y-10*x*x*y*y*y+y*y*y*y*y) + Q;
          }

          if (mode == FractalConstants.MANDELBROT_MODE) {
            x = (xtmp) + realmin + col * deltaP;
          } else if (mode == FractalConstants.JULIA_MODE) {
            x = (xtmp) + P;
          }
        }

        if (lessThanMax) {
          int colorIndex = Math.max(0,((int)Math.round(mu*100)-1));
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
