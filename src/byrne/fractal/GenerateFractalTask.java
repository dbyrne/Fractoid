package byrne.fractal;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.graphics.*;

public class GenerateFractalTask extends AsyncTask<Void, Integer, Bitmap> {
  
  ProgressDialog myProgressDialog;
  FractalParameters params;
  FractalView fractalView;
  
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
    
    double realmin = params.getRealMin();
    double realmax = params.getRealMax();
    double imagmin = params.getImagMin();
    double imagmax = params.getImagMax();
    
    double P = params.getP();
    double Q = params.getQ();
    
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

    for (int row=0; row < yres; row++) {
      if (row % 4 == 0) {
        this.publishProgress((int)Math.round(100*((double)row/yres)));
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
          y=2*x*y + Q;
          if (mode == FractalConstants.MANDELBROT_MODE) {
            x = xsq - ysq + realmin + col * deltaP;
          } else if (mode == FractalConstants.JULIA_MODE) {
            x = xsq - ysq + P;
            
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
    return b;
  }
  
  @Override protected void onPreExecute() {
    myProgressDialog = new ProgressDialog(fractalView.getContext());
    myProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
    myProgressDialog.setMessage("Generating Fractal...");
    myProgressDialog.setCancelable(false);
    myProgressDialog.show();
  }
  @Override protected Bitmap doInBackground(Void... unused) {
    return createBitmap();
  }
  
  @Override protected void onProgressUpdate(Integer... progress) {
    myProgressDialog.setProgress(progress[0]);
  }
  
  @Override protected void onPostExecute(Bitmap bitmap) {
    fractalView.setZoom(true);
    fractalView.setFractal(bitmap);
    fractalView.invalidate();
    myProgressDialog.dismiss();
  }
}
