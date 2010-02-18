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

public class FractalParameters {
  
  final int STARTING_MAX_ITERATIONS = 40;
  private int maxIterations = STARTING_MAX_ITERATIONS;
  private int[][] values;
  
  public void setValues(int[][] v) {values = v;}
  public int[][] getValues() {
    if (values == null) {
      values = new int[yres][xres];
    }
    return values;
  }
  
  public void resetMaxIterations() {maxIterations=STARTING_MAX_ITERATIONS;}
  public void setMaxIterations(int i) {maxIterations = i;}
  public int getMaxIterations() {return maxIterations;}
  
  private int[] colorSet;
  public void setColorSet(int[] cs) {colorSet = cs;}
  public int[] getColorSet() {return colorSet;}
  
  private Algorithm alg = Algorithm.ESCAPE_TIME;
  public void setAlgorithm(Algorithm a) {alg = a;}
  public Algorithm getAlgorithm() {return alg;};
  
  private int xres = -1;
  public int getXRes() {return xres;}
  public void setXRes(int xr) {xres = xr;}
  
  private int trapFactor = 1;
  public int getTrapFactor() {return trapFactor;}
  public void setTrapFactor(int tf) {trapFactor = tf;}
  
  private int yres = -1;
  public int getYRes() {return yres;}
  public void setYRes(int yr) {yres = yr;}
  
  public double getResRatio() {return (double)xres/yres;}
  
  private FractalType type;
  public FractalType getType() {return type;}
  public void setType(FractalType t) {type = t;}
  
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
  
  public void resetValues() {
    values = null;
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