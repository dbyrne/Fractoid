package byrne.fractal;

public class FractalParameters {
  
  private int maxIterations;
  public void setMaxIterations(int i) {maxIterations = i;}
  public int getMaxIterations() {return maxIterations;}
  
  private int order;
  public void setOrder(int o) {order = o;}
  public int getOrder() {return order;}
  
  private int xres;
  public int getXRes() {return xres;}
  public void setXRes(int xr) {xres = xr;}
  
  private int yres;
  public int getYRes() {return yres;}
  public void setYRes(int yr) {yres = yr;}
  
  public double getResRatio() {return (double)xres/yres;}
  
  private int mode;
  public int getMode() {return mode;}
  public void setMode(int m) {mode = m;}
  
  private double shiftFactor;
  public double getShiftFactor() {return shiftFactor;}
  public void randomizeShiftFactor() {shiftFactor = Math.random()*2;}
  
  private double realmax,realmin,imagmax,imagmin;
  public void setCoords(double rmin, double rmax, double imin, double imax) {
     realmin = rmin;
     realmax = rmax;
     imagmin = imin;
     imagmax = imax;
  }
  public double getRealMin() {return realmin;}
  public double getRealMax() {return realmax;}
  public double getImagMin() {return imagmin;}
  public double getImagMax() {return imagmax;}
  
  private double P=-1,Q=-1;
  public double getP() {return P;}
  public double getQ() {return Q;}
  public void setP(double p) {P=p;}
  public void setQ(double q) {Q=q;}
  
}