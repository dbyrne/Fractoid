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
    
  public void setColorSet(ColorSet cs) {
    params.setColorSet(cs);
    backgroundBitmap = null;
    startFractalTask();
  }
  
  public void setAlgorithm(Algorithm alg) {
    params.setAlgorithm(alg);
    backgroundBitmap = null;
    startFractalTask();
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
    startFractalTask();
  }
  
  public FractalType getType() {
    return params.getType();
  }
  
  public Bitmap getFractal() {
    return fractalBitmap.getDrawable().getBitmap();
  }

  public void startFractalTask() {
    setFull = true;
    
    calculationTime = null;
    if (mGenerateFractalTask != null && mGenerateFractalTask.getStatus() == Status.RUNNING) {
      mGenerateFractalTask.cancel(true);
    }
    mGenerateFractalTask = new GenerateFractalTask(params,this);
    mGenerateFractalTask.execute();
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
    
    backgroundBitmap = null;
    
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
        params.resetMaxIterations();
        params.setType(FractalType.JULIA);
        backgroundBitmap = null;
        startFractalTask();
      } else if (event.getAction() == MotionEvent.ACTION_UP) {
        setZoom(true);
      }
      return true;
    } else {
      if (fractalBitmap != null)
        return multiTouchController.onTouchEvent(event);
      else
        return true;
    }
  }
  
  public void mergeBitmaps() {
    Bitmap composite = Bitmap.createBitmap(params.getXRes(), params.getYRes(), Bitmap.Config.ARGB_8888);
    Canvas canvas = new Canvas(composite);
    
    if (backgroundBitmap != null) {
      backgroundBitmap.draw(canvas);
      backgroundBitmap = null;
    }
    
    fractalBitmap.draw(canvas);
    BitmapDrawable bd = new BitmapDrawable(res, composite);
    fractalBitmap.setDrawable(bd);
    fractalBitmap.setFullScreen();
  }
  
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
      if (params.getColorSet() == ColorSet.BLACK_AND_WHITE ||
          params.getColorSet() == ColorSet.WINTER ||
          params.getColorSet() == ColorSet.NIGHT_SKY) {
        p.setColor(Color.RED);  
      } else {
        p.setColor(Color.WHITE);
      }
      
      
      p.setStyle(Paint.Style.FILL_AND_STROKE);
      p.setStrokeWidth(1);

      p.setTextSize(35);
      if (!zoom) {
        canvas.drawText("Touch to Generate Julia Set",(params.getXRes()/2)-200,params.getYRes()-5,p);
      } else {
        canvas.drawText("Pinch to Zoom",(params.getXRes()/2)-120,params.getYRes()-5,p);
      }
      p.setTextSize(25);
      
      String maxIterString = "MaxIter: " + params.getMaxIterations();
      canvas.drawText(maxIterString,5,params.getYRes()-5,p);
      
      if (calculationTime != null) {
        canvas.drawText(calculationTime,params.getXRes()-140,params.getYRes()-5,p);
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