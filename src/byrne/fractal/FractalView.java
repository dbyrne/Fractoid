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
  private boolean setFull = false, zoom = true;
  private ColorSet colorSet = ColorSet.RAINBOW;
  private Fractoid mFractoid;
  private int touchCount = 0;
  private float progress;
  
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
    startFractalTask();
  }
  
  public void setAlgorithm(Algorithm alg) {
    params.setAlgorithm(alg);
    params.resetValues();
    clearBackground();
    startFractalTask();
  }
  
    public void calibrateColors() {
    int[] colors = params.getColorSet();
    int[][] values = params.getValues();
    Bitmap b = Bitmap.createBitmap(params.getXRes(), params.getYRes(), Bitmap.Config.ARGB_8888);
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
      for (int row=0; row < params.getYRes(); row += 2-rpass) {
        for (int col=0; col < params.getXRes(); col++) {
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
    params.setTrapFactor(tf);
    params.resetValues();
    startFractalTask();
  }
  
  public int getTrapFactor() {
    return params.getTrapFactor();
  }
  
  public void setValues(int[][] v) {
    params.setValues(v);
  }
  
  public ComplexEquation getEquation() {
    return equation;
  }
  
  public void setEquation(ComplexEquation e) {
    equation = e;
  }
  
  public void setZoom(boolean z) {
    zoom = z;
  }
  
  public int getMaxIterations() {
    return params.getMaxIterations();
  }
  
  public void setMaxIterations(int i) {
    params.setMaxIterations(i);
    params.resetValues();
    startFractalTask();
  }
  
  public void recalculate() {

    double imagmin = params.getImagMin();
    double imagmax = params.getImagMax();
    double realmin = params.getRealMin();
    double realmax = params.getRealMax();
    double realRange = Math.abs(realmax-realmin);
    double imagRange = Math.abs(imagmax-imagmin);
    
    double centerX = (double)fractalBitmap.getCenterX();
    double centerY = (double)fractalBitmap.getCenterY();
    double scale = (double)fractalBitmap.getScale();
    
    double xres = (double)params.getXRes();
    double yres = (double)params.getYRes();

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
    if (fractalBitmap.getScale() > 1)
      params.setMaxIterations(params.getMaxIterations()+10);
    params.setCoords(realmin,realmax,imagmin,imagmax);
    params.resetValues();
    startFractalTask();

  }
  
  public FractalType getType() {
    return params.getType();
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

  public void startFractalTask() {
    setFull = true;
    progress = 0;
    calculationTime = null;
    if (mGenerateFractalTask != null && mGenerateFractalTask.getStatus() == Status.RUNNING) {
      mGenerateFractalTask.cancel(true);
    }
    
    mFractoid.setCalibrateButtonEnabled(false);
    
    calculateColors(1021);
    mGenerateFractalTask = new GenerateFractalTask(params,this);
    mGenerateFractalTask.execute();
  }

  public void removeTouch() {
    if (touchCount > 0)
      touchCount--;
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
    double realmax = params.getResRatio()*r_y/2;
    double realmin = params.getResRatio()*r_y/2*-1;
    
    params.randomizeShiftFactor();
    params.setEquation(equation);
    params.setCoords(realmin,realmax,imagmin,imagmax);
    params.resetValues();
    
    if (equation == ComplexEquation.PHOENIX) {
      /*
      The mandelbrot fractal for this equation is ugly so
      we only allow the user to explore the Julia version
      */
      params.setType(FractalType.JULIA);
      params.setP(0.56666667);
      params.setQ(-0.5);
      params.setMaxIterations(100);
    }
    else {
      params.setType(FractalType.MANDELBROT);
      params.resetMaxIterations();
    }
    
    clearBackground();
    
    setZoom(true);
    startFractalTask();
  }
      
  @Override protected void onSizeChanged(int width, int height, int oldw, int oldh) {
    params.setXRes(width);
    params.setYRes(height);
  }
  
  @Override public boolean onTouchEvent (MotionEvent event) {
    if (!zoom) {
      if (event.getAction() == MotionEvent.ACTION_DOWN) {
        double imagmin = params.getImagMin();
        double imagmax = params.getImagMax();
        double realmin = params.getRealMin();
        double realmax = params.getRealMax();
        double realRange = Math.abs(realmax-realmin);
        double imagRange = Math.abs(imagmax-imagmin);
        params.setP(realmin + ((event.getX()/params.getXRes())*realRange));
        params.setQ(imagmax - ((event.getY()/params.getYRes())*imagRange));
        imagmax = 1.4;
        imagmin = -1.4;
        imagRange = Math.abs(imagmax-imagmin);
        realmax = params.getResRatio()*imagRange/2;
        realmin = params.getResRatio()*-imagRange/2;

        params.setCoords(realmin, realmax, imagmin, imagmax);
        params.resetValues();
        params.resetMaxIterations();
        params.setType(FractalType.JULIA);
        clearBackground();
        startFractalTask();
      } else if (event.getAction() == MotionEvent.ACTION_UP) {
        setZoom(true);
      }
      return true;
    } else {
      if (fractalBitmap != null && touchCount < 4) {
        return multiTouchController.onTouchEvent(event);
      }
    }
    return true;
  }
  
  public void clearBackground() {
    backgroundBitmap = null;
  }
  
  public void mergeBitmaps() {
    Bitmap composite = Bitmap.createBitmap(params.getXRes(), params.getYRes(), Bitmap.Config.ARGB_8888);
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
    
    touchCount++;
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
      
      int xres = params.getXRes();
      int yres = params.getYRes();
      
      if (!zoom) {
        canvas.drawText("Touch to Generate Julia Set",(xres/2)-200,yres-5,p);
      } else {
        canvas.drawText("Pinch to Zoom",(xres/2)-120,yres-5,p);
      }
      p.setTextSize(25);
      
      String maxIterString = "MaxIter: " + params.getMaxIterations();
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
      width = params.getXRes();
      height = params.getYRes();
      displayHeight = params.getYRes();
      displayWidth = params.getXRes();
      setPos(params.getXRes()/2.0f, params.getYRes()/2.0f, 1.0f);
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