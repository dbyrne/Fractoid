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

import android.view.View;
import android.graphics.*;
import android.graphics.drawable.BitmapDrawable;
import android.view.MotionEvent;
import android.content.Context;
import android.os.AsyncTask.Status;
import android.util.AttributeSet;
import android.content.res.Resources;
import android.content.Context;
import android.content.res.Configuration;
import android.util.AttributeSet;
import byrne.fractal.MultiTouchController.*;

public class FractalView extends View implements MultiTouchObjectCanvas<FractalView.Img> {
    
  private double minY,maxY,minX,maxX;
  private Img fractalBitmap, backgroundBitmap;
  private GenerateFractalTask mGenerateFractalTask;
  private String calculationTime;
  private ComplexEquation equation = ComplexEquation.SECOND_ORDER;  
  private FractalParameters params;
  private MultiTouchController<FractalView.Img> multiTouchController;
  private Resources res;
  private boolean setFull = false, zoom = true, greenLight = true;
  private ColorSet colorSet = ColorSet.RAINBOW;
  private FractalType fractalType = FractalType.MANDELBROT;
  int maxIterations = 40;
  int trapFactor = 1;
  private Fractoid mFractoid;
  private float progress;
  private final NativeLib mNativeLib = new NativeLib();
  
  public FractalView(Context context){
    super(context);
    res = context.getResources();
    multiTouchController = new MultiTouchController<FractalView.Img>(this, res);
    params = new FractalParameters();
  }
  
  public FractalView(Context context, AttributeSet attrs){
        super(context, attrs);
        res = context.getResources();
        multiTouchController = new MultiTouchController<FractalView.Img>(this, res);
        params = new FractalParameters();
  }
  
  public void setFractoid(Fractoid f) {
    mFractoid = f;
  }

  private void calculateColors(int numberOfColors) {
    
    double red, green, blue;
    int[] colorIntegers = new int[numberOfColors];   
    double shiftFactor = params.getShiftFactor();
    
    switch (colorSet) {
      case RAINBOW:
        for (int x = 0; x < numberOfColors; x++) {
          double data = x;
          data = 2*Math.PI*(data/1020);
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
          int value = (x%1020)/2;
          int color;
          if (value <= 255)
            color = value;
          else
            color = 255-(value-255);
          colorIntegers[x] = Color.rgb(255-color,255-color,255-color/3);
        }
        break;
      
      case SUMMER:
        for (int x = 0; x < numberOfColors; x++) {
          int value = (x%1020)/2;
          int color;
          if (value <= 255)
            color = value;
          else
            color = 255-(value-255);
          colorIntegers[x] = Color.rgb(255-color/3,255-color,128-color/2);
        }
        break;
      
      case NIGHT_SKY:
        for (int x = 0; x < numberOfColors; x++) {
          int value = (x%1020)/2;
          int color;
          if (value <= 255)
            color = value;
          else
            color = 255-(value-255);
          colorIntegers[x] = Color.rgb(color/2,color,127+color/2);
        }
        break;
      
      case ORANGE:
        for (int x = 0; x < numberOfColors; x++) {
          int value = (x%1020)/2;
          int color;
          if (value <= 255)
            color = value;
          else
            color = 255-(value-255);
          colorIntegers[x] = Color.rgb(color,color/2,0);
        }
        break;
      
      case RED:
        for (int x = 0; x < numberOfColors; x++) {
          int value = (x%1020)/2;
          int color;
          if (value <= 255)
            color = value;
          else
            color = 255-(value-255);
          colorIntegers[x] = Color.rgb(color,0,0);
        }
        break;
      
      case GREEN:
        for (int x = 0; x < numberOfColors; x++) {
          int value = (x%1020)/2;
          int color;
          if (value <= 255)
            color = value;
          else
            color = 255-(value-255);
          colorIntegers[x] = Color.rgb(0,color,0);
        }
        break;
      
      case YELLOW:
        for (int x = 0; x < numberOfColors; x++) {
          int value = (x%1020)/2;
          int color;
          if (value <= 255)
            color = value;
          else
            color = 255-(value-255);
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
    params.setColorSet(colorIntegers);
  }


  public void setColorSet(ColorSet cs) {
    colorSet = cs;
    calculateColors(1021);
    startFractalTask(false);
  }
  
  public void setAlgorithm(Algorithm alg) {
    mNativeLib.setAlgorithm(alg.getInt());
    startFractalTask(true);
  }
  
    public void calibrateColors() {
    int[] colors = params.getColorSet();
    int[][] values = mNativeLib.getValues();
    Bitmap b = Bitmap.createBitmap(mNativeLib.getXRes(), mNativeLib.getYRes(), Bitmap.Config.ARGB_8888);
    Canvas c = new Canvas(b);
    Paint p = new Paint();
    p.setStyle(Paint.Style.FILL_AND_STROKE);
    
    int max = 0, min = 999999;
    
    for (int[] colValues : values) {
      for (int val : colValues) {
        if (val != -1) {
          if (val > max)
            max = val;
          else if (val < min)
            min = val;
        }
      }
    }
    
    int range = max-min+1;   
    
    for (int rpass = 0; rpass < 2; rpass++) {
      p.setStrokeWidth(2-rpass);
      for (int row=0; row < mNativeLib.getYRes(); row += 2-rpass) {
        for (int col=0; col < mNativeLib.getXRes(); col++) {
          int cint = values[row][col];
          if (cint >= 0) {
            int i = Math.round(((float)(cint-min)/range)*1020);
            p.setColor(colors[i]);
            c.drawPoint(col,row,p);
          }
          else if (cint == -1) {
            p.setColor(Color.BLACK);
            c.drawPoint(col,row,p);
          }
        }
      }
    }
    setFractal(b);
  }
  
  public void setTrapFactor(int tf) {
    mNativeLib.setTrapFactor(tf);
    trapFactor = tf;
    startFractalTask(true);
  }
  
  public int getTrapFactor() {
    return trapFactor;
  }
  
  public ComplexEquation getEquation() {
    return equation;
  }
  
  public void setEquation(ComplexEquation e) {
    equation = e;
    mNativeLib.setEquation(e.getInt(),e.getPower());
  }
  
  public void setZoom(boolean z) {
    zoom = z;
  }
  
  public int getMaxIterations() {
    return maxIterations;
  }
  
  public void setMaxIterations(int i) {
    mNativeLib.setMaxIterations(i);
    maxIterations = i;
    startFractalTask(true);
  }
  
  public void recalculate() {

    double imagmin = mNativeLib.getImagMin();
    double imagmax = mNativeLib.getImagMax();
    double realmin = mNativeLib.getRealMin();
    double realmax = mNativeLib.getRealMax();
    double realRange = Math.abs(realmax-realmin);
    double imagRange = Math.abs(imagmax-imagmin);
    
    double centerX = (double)fractalBitmap.getCenterX();
    double centerY = (double)fractalBitmap.getCenterY();
    double scale = (double)fractalBitmap.getScale();
    
    double xres = (double)mNativeLib.getXRes();
    double yres = (double)mNativeLib.getYRes();

    double offsetX = realmin + realRange/2;
    double offsetY = imagmin + imagRange/2;
    
    realmin = realmin - offsetX;
    realmax = realmax - offsetX;
    imagmin = imagmin - offsetY;
    imagmax = imagmax - offsetY;
    
    double image_y_center = (imagmin + (centerY/yres)*imagRange)/scale;
    double image_x_center = (realmax - (centerX/xres)*realRange)/scale;
    
    imagmax = image_y_center + (imagRange/2)/scale;
    imagmin = image_y_center - (imagRange/2)/scale;
    realmin = image_x_center - (realRange/2)/scale; 
    realmax = image_x_center + (realRange/2)/scale;

    realmin = realmin + offsetX;
    realmax = realmax + offsetX;
    imagmin = imagmin + offsetY;
    imagmax = imagmax + offsetY;
    if (fractalBitmap.getScale() > 1) {
      maxIterations += 10;
      mNativeLib.setMaxIterations(maxIterations);
    }
    mNativeLib.setCoords(realmin,realmax,imagmin,imagmax);
    startFractalTask(true);

  }
  
  public FractalType getType() {
    return fractalType;
  }

  public void setProgress(float p) {
    progress = p;
  }
  
  public Bitmap getFractal() {
    return fractalBitmap.getDrawable().getBitmap();
  }
  
  public void turnCalibrateButtonOn() {
    mFractoid.setCalibrateButtonEnabled(true);
  }
  
  public void stopFractalTask() {
    if (mGenerateFractalTask != null && mGenerateFractalTask.getStatus() == Status.RUNNING) {
      mGenerateFractalTask.cancel(true);
    }
    
    while(!greenLight) {
      try {
        Thread.sleep(100);
      } catch (InterruptedException e) {}
    }
  }

  public void startFractalTask(boolean reset) {
    setFull = true;
    progress = 0;
    calculationTime = null;
    stopFractalTask();
    
    if (reset)
      mNativeLib.resetValues();
    
    mFractoid.setCalibrateButtonEnabled(false);
    
    calculateColors(1021);
    greenLight = false;
    mGenerateFractalTask = new GenerateFractalTask(params,this);
    mGenerateFractalTask.execute();
  }

  public void greenLight() {
    greenLight = true;
  }

  public void setFractal(Bitmap fa) {
    BitmapDrawable bd = new BitmapDrawable(res, fa);
    if (setFull) {    
      fractalBitmap = new Img(bd, res);
      fractalBitmap.setFullScreen();
      setFull = false;
    } else {
      fractalBitmap.setDrawable(bd);
    }
  }
  
  public void setTime(long t) {
    long time = t / 1000;  
    String seconds = Integer.toString((int)(time % 60));  
    String minutes = Integer.toString((int)(time / 60));  
    for (int i = 0; i < 2; i++) {  
      if (seconds.length() < 2) {  
        seconds = "0" + seconds;  
      }  
      if (minutes.length() < 2) {  
        minutes = "0" + minutes;  
      }
    }
    calculationTime = "Time: "+minutes+":"+seconds;
  }

  protected void resetCoords() {
    
    double imagmax, imagmin;
    
    if (equation == ComplexEquation.BURNING_SHIP) {
      //This equation needs a special range to be centered
      imagmax = 2.1;
      imagmin = -0.7;
    } else {
      imagmax = 1.4;
      imagmin = -1.4;    
    }
    
    double r_y = Math.abs(imagmax - imagmin);
    double realmax = ((double)mNativeLib.getXRes()/mNativeLib.getYRes())*r_y/2;
    double realmin = ((double)mNativeLib.getXRes()/mNativeLib.getYRes())*r_y/2*-1;
    
    params.randomizeShiftFactor();
    mNativeLib.setCoords(realmin,realmax,imagmin,imagmax);
    
    if (equation == ComplexEquation.PHOENIX) {
      /*
      The mandelbrot fractal for this equation is ugly so
      we only allow the user to explore the Julia version
      */
      mNativeLib.setFractalType(FractalType.JULIA.getInt());
      fractalType = FractalType.JULIA;
      mNativeLib.setCValue(0.56666667,-0.5);
      mNativeLib.setMaxIterations(100);
      maxIterations = 100;
    }
    else {
      mNativeLib.setFractalType(FractalType.MANDELBROT.getInt());
      mNativeLib.setMaxIterations(40);
      maxIterations = 40;
    }
    
    clearBackground();
    
    setZoom(true);
    startFractalTask(true);
  }
      
  @Override protected void onSizeChanged(int width, int height, int oldw, int oldh) {
    if (mNativeLib.getXRes() == -1) {
      mNativeLib.setResolution(width,height);
    }
  }
  
  @Override public boolean onTouchEvent (MotionEvent event) {
    if (!zoom) {
      if (event.getAction() == MotionEvent.ACTION_DOWN) {
        double imagmin = mNativeLib.getImagMin();
        double imagmax = mNativeLib.getImagMax();
        double realmin = mNativeLib.getRealMin();
        double realmax = mNativeLib.getRealMax();
        double realRange = Math.abs(realmax-realmin);
        double imagRange = Math.abs(imagmax-imagmin);
        mNativeLib.setCValue(realmin + ((event.getX()/mNativeLib.getXRes())*realRange),
                             imagmax - ((event.getY()/mNativeLib.getYRes())*imagRange));

        imagmax = 1.4;
        imagmin = -1.4;
        imagRange = Math.abs(imagmax-imagmin);
        realmax = ((double)mNativeLib.getXRes()/mNativeLib.getYRes())*imagRange/2;
        realmin = ((double)mNativeLib.getXRes()/mNativeLib.getYRes())*-imagRange/2;

        mNativeLib.setCoords(realmin, realmax, imagmin, imagmax);
        mNativeLib.setMaxIterations(40);
        maxIterations = 40;
        mNativeLib.setFractalType(FractalType.JULIA.getInt());
        fractalType = FractalType.JULIA;
        clearBackground();
        startFractalTask(true);
      } else if (event.getAction() == MotionEvent.ACTION_UP) {
        setZoom(true);
      }
      return true;
    } else {
      if (fractalBitmap != null) {
        if (greenLight){
          return multiTouchController.onTouchEvent(event);
        } else {
          stopFractalTask();  
        }
      }
    }
    return true;
  }
  
  public void clearBackground() {
    backgroundBitmap = null;
  }
  
  public void mergeBitmaps() {
    Bitmap composite = Bitmap.createBitmap(mNativeLib.getXRes(), mNativeLib.getYRes(), Bitmap.Config.ARGB_8888);
    Canvas canvas = new Canvas(composite);
    
    if (backgroundBitmap != null) {
      backgroundBitmap.draw(canvas);
      clearBackground();
    }
    
    fractalBitmap.draw(canvas);
    BitmapDrawable bd = new BitmapDrawable(res, composite);
    fractalBitmap.setDrawable(bd);
    fractalBitmap.setFullScreen();
  }
  
  /** See if there is a draggable object at the current point. Returns the object at the point, or null if nothing to drag. */
  public Img getDraggableObjectAtPoint(PointInfo pt) {
    
    if (mGenerateFractalTask != null && mGenerateFractalTask.getStatus() == Status.RUNNING) {
      mGenerateFractalTask.cancel(true);
    }
    
    mergeBitmaps();
    
    return fractalBitmap;
  }
  
  /**
  * Select an object for dragging. Called whenever an object is found to be under the point (non-null is returned by
  * getDraggableObjectAtPoint()) and a drag operation is starting. Called with null when drag op ends.
   */
  public void selectObject(Img img, PointInfo touchPoint) {
    if (img == null) {
      backgroundBitmap = fractalBitmap;
      recalculate();
    }
    invalidate();
  }

  /** Get the current position and scale of the selected image. Called whenever a drag starts or is reset. */
  public void getPositionAndScale(Img img, PositionAndScale objPosAndScaleOut) {
    objPosAndScaleOut.set(img.getCenterX(), img.getCenterY(), img.getScale());
  }

  /** Set the position and scale of the dragged/stretched image. */
  public boolean setPositionAndScale(Img img, PositionAndScale newImgPosAndScale, PointInfo touchPoint) {
    float x = newImgPosAndScale.getXOff();
    float y = newImgPosAndScale.getYOff();
    float scale = newImgPosAndScale.getScale();
    boolean ok = img.setPos(x, y, scale);
    if (ok)
      invalidate();
    return ok;
  }

  @Override protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    if (backgroundBitmap != null) {
      backgroundBitmap.draw(canvas);
    }
    
    if (fractalBitmap != null) {

      fractalBitmap.draw(canvas);

      Paint p = new Paint();
      if (colorSet == ColorSet.BLACK_AND_WHITE ||
          colorSet == ColorSet.WINTER ||
          colorSet == ColorSet.NIGHT_SKY) {
        p.setColor(Color.RED);  
      } else {
        p.setColor(Color.WHITE);
      }
      
      
      p.setStyle(Paint.Style.FILL_AND_STROKE);
      p.setStrokeWidth(1);

      p.setTextSize(35);
      
      int xres = mNativeLib.getXRes();
      int yres = mNativeLib.getYRes();
      
      if (!zoom) {
        canvas.drawText("Touch to Generate Julia Set",(xres/2)-200,yres-5,p);
      } else {
        canvas.drawText("Pinch to Zoom",(xres/2)-120,yres-5,p);
      }
      p.setTextSize(25);
      
      String maxIterString = "MaxIter: " + maxIterations;
      canvas.drawText(maxIterString,5,yres-5,p);
      
      if (calculationTime != null) {
        canvas.drawText(calculationTime,xres-140,yres-5,p);
      } else {
        Rect total = new Rect(xres-155,yres-30,xres-5,yres-5);
        int pleft = (int)((1-progress)*150);
        Rect prog = new Rect(xres-155,yres-30,xres-pleft-5,yres-5);
        canvas.drawRect(prog,p);
        p.setStyle(Paint.Style.STROKE);
        canvas.drawRect(total,p);
      }
      
    } else {
      resetCoords();
    }
  }
  
  class Img {

    private BitmapDrawable drawable;
    private int width, height, displayWidth, displayHeight;
    private float centerX, centerY, scale;
    private float minX, maxX, minY, maxY;
    private static final float SCREEN_MARGIN = 100;
   
    public Img(BitmapDrawable bd, Resources res) {
      this.drawable = bd;
    }

    public void setFullScreen() {
      width = mNativeLib.getXRes();
      height = mNativeLib.getYRes();
      displayHeight = mNativeLib.getYRes();
      displayWidth = mNativeLib.getXRes();
      setPos(mNativeLib.getXRes()/2.0f, mNativeLib.getYRes()/2.0f, 1.0f);
    }

    /** Set the position and scale of an image in screen coordinates */
    private boolean setPos(float centerX, float centerY, float scale) {
      
      float ws = (width / 2) * scale, hs = (height / 2) * scale;
      float newMinX = centerX - ws, newMinY = centerY - hs, newMaxX = centerX + ws, newMaxY = centerY + hs;
      if (newMinX > displayWidth - SCREEN_MARGIN || newMaxX < SCREEN_MARGIN || newMinY > displayHeight - SCREEN_MARGIN || newMaxY < SCREEN_MARGIN)
        return false;
      
      this.centerX = centerX;
      this.centerY = centerY;
      this.scale = scale;
      this.minX = newMinX;
      this.minY = newMinY;
      this.maxX = newMaxX;
      this.maxY = newMaxY;
      
      return true;
    }

    public void draw(Canvas canvas) {
      drawable.setBounds((int) minX, (int) minY, (int) maxX, (int) maxY);
      drawable.draw(canvas);
    }
  
    public BitmapDrawable getDrawable() {
      return drawable;
    }
  
    public void setDrawable(BitmapDrawable bd) {
      drawable = bd;
    }
  
    public int getWidth() {
      return width;
    }
  
    public int getHeight() {
      return height;
    }
  
    public float getCenterX() {
      return centerX;
    }
  
    public float getCenterY() {
      return centerY;
    }
  
    public float getScale() {
      return scale;
    }
  
    public float getMinX() {
      return minX;
    }
  
    public float getMaxX() {
      return maxX;
    }
  
    public float getMinY() {
      return minY;
    }
  
    public float getMaxY() {
      return maxY;
    }
  }
}