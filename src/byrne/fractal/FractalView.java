package byrne.fractal;

import android.view.View;
import android.graphics.*;
import android.view.MotionEvent;
import android.content.Context;
import android.os.AsyncTask.Status;

public class FractalView extends View {
    
  private double minY,maxY,minX,maxX;
  private Bitmap fractalBitmap;
  private RectF selection;
  private boolean zoom;
  private double touched_x=-1, touched_y=-1;
  private GenerateFractalTask mGenerateFractalTask;
  private String calculationTime;
  private int equation = FractalConstants.SECOND_ORDER;
  
  private FractalParameters params;
  
  public FractalView(Context context){
    super(context);
    zoom = true;
    params = new FractalParameters();
    params.setMaxIterations(FractalConstants.STARTING_MAX_ITERATIONS);
  }
  
  public void setZoom(boolean z) {
    zoom = z;
  }
  
  public int getEquation() {
    return equation;
  }
  
  public void setEquation(int o) {
    equation = o;
  }
  
  public int getMode() {
    return params.getMode();
  }
  
  public void setMode(int m) {
    params.setMode(m);
  }
  public Bitmap getFractal() {
    return fractalBitmap;
  }

  public void startFractalTask() {
    calculationTime = null;
    if (mGenerateFractalTask != null && mGenerateFractalTask.getStatus() == Status.RUNNING) {
      mGenerateFractalTask.cancel(true);
    }
    mGenerateFractalTask = new GenerateFractalTask(params,this);
    mGenerateFractalTask.execute();
  }

  public void setFractal(Bitmap fa) {
    fractalBitmap = fa;
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

    double imagmax = 1.4;
    double imagmin = -1.4;

    double r_y = Math.abs(imagmax - imagmin);
    double realmax = params.getResRatio()*r_y/2;
    double realmin = params.getResRatio()*r_y/2*-1;
    
    params.randomizeShiftFactor();
    params.setEquation(equation);
    params.setCoords(realmin,realmax,imagmin,imagmax);
    params.setMode(FractalConstants.MANDELBROT_MODE);
    params.setMaxIterations(FractalConstants.STARTING_MAX_ITERATIONS);
    
    startFractalTask();
  }
      
  @Override protected void onSizeChanged(int width, int height, int oldw, int oldh) {
    params.setXRes(width);
    params.setYRes(height);
  }
  
  @Override public boolean onTouchEvent (MotionEvent event) {

    double realmax = params.getRealMax();
    double realmin = params.getRealMin();
    double imagmin = params.getImagMin();
    double imagmax = params.getImagMax();

    if (zoom) {
      if (event.getAction() == MotionEvent.ACTION_DOWN) {
        touched_x = event.getX();
        touched_y = event.getY();
      } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
        if (event.getY() > touched_y) {
          maxY=event.getY();
          minY=(double)touched_y;
        } else {
          maxY=(double)touched_y;
          minY=Math.max(event.getY(),0);
        }
        if (event.getX() > touched_x) {
          maxX=event.getX();
          minX=(double)touched_x;
        } else {
          maxX=(double)touched_x;
          minX=event.getX();
        }
        double x_range = Math.abs(maxX-minX);
        double y_range = Math.abs(maxY-minY);
        double inv_ratio = (double)params.getYRes()/params.getXRes();
        double sel_ratio = x_range/y_range;

        if (params.getResRatio() > sel_ratio) {
          if (maxX == event.getX()) {
            maxX = minX+(params.getResRatio()*y_range);
          } else {
            minX = maxX-(params.getResRatio()*y_range);
          }
        } else {
          if (maxY == event.getY()) {
            maxY = minY+(inv_ratio*x_range);
          } else {
            minY = maxY-(inv_ratio*x_range);
          }
        }

        selection = new RectF(Math.max((float)minX,0),Math.min((float)maxY,params.getYRes()),Math.min((float)maxX,params.getXRes()),Math.max((float)minY,0));
        postInvalidate();
        
      } else if (event.getAction() == MotionEvent.ACTION_UP) {

        double x_range = (double)Math.abs(realmax-realmin);
        double y_range = (double)Math.abs(imagmax-imagmin);  

        realmax = realmin + (maxX/params.getXRes()) * x_range;		
        realmin = realmin + (minX/params.getXRes()) * x_range;
        imagmin = imagmax - (maxY/params.getYRes()) * y_range;
        imagmax = imagmax - (minY/params.getYRes()) * y_range;
        selection = null;
        
        params.setCoords(realmin,realmax,imagmin,imagmax);
        params.setMaxIterations(params.getMaxIterations() + 15);
        startFractalTask();
      }
    } else if (!zoom) {
      if (event.getAction() == MotionEvent.ACTION_DOWN) {
        
        double x_range = (double)Math.abs(realmax-realmin);
        double y_range = (double)Math.abs(imagmax-imagmin);
        touched_x = event.getX();
        touched_y = event.getY();
        params.setP(realmin + ((touched_x/params.getXRes())*x_range));
        params.setQ(imagmax - ((touched_y/params.getYRes())*y_range));

        imagmax = 1.4;
        imagmin = -1.4;
        y_range = (double)Math.abs(imagmax-imagmin);
        realmax = (params.getResRatio())*y_range/2;
        realmin = (params.getResRatio())*y_range/2*-1;
        
        params.setCoords(realmin,realmax,imagmin,imagmax);
        params.setMaxIterations(FractalConstants.STARTING_MAX_ITERATIONS);
        startFractalTask();
      } else if (event.getAction() == MotionEvent.ACTION_UP) {
        setZoom(true);
      }
    } 
    return true;
  }

  @Override protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    if (fractalBitmap != null) {
      canvas.drawBitmap(fractalBitmap,0,0,null);

      Paint p = new Paint();
      p.setColor(Color.WHITE);	
      p.setTextSize(20);

      if (selection != null) {
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(2);
        canvas.drawRect(selection,p);
      }
      p.setStyle(Paint.Style.FILL_AND_STROKE);
      p.setStrokeWidth(1);
      if (zoom) {
        canvas.drawText("Drag to zoom",(params.getXRes()/2)-50,params.getYRes()-5,p);
      }
      else {
        canvas.drawText("Touch Screen to Generate Julia Set",(params.getXRes()/2)-125,params.getYRes()-5,p);
      }
      String maxIterString = "MaxIter: " + params.getMaxIterations();
      canvas.drawText(maxIterString,5,params.getYRes()-5,p);
      
      if (calculationTime != null) {
        canvas.drawText(calculationTime,params.getXRes()-120,params.getYRes()-5,p);
      }
      
    } else {
      resetCoords();
    }
  }
}