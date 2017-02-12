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

class NativeLib {
  
  public native void resetValues();
  public native void resetCurrentRow();
  public native void freeValues();
  
  public native void setResolution(int xres, int yres);
  public native void setEquation(int equation, int power);
  public native void setCoords(double realmin, double realmax, double imagmin, double imagmax);
  public native void setMaxIterations(int max);
  public native void setTrapFactor(int trapFactor);
  public native void setFractalType(int type);
  public native void setAlgorithm(int alg, int bailout);
  public native void setCValue(double P, double Q);
  
  public native int[] getFractalRow(int row, int state);
  
  public native double getRealMin();
  public native double getRealMax();
  public native double getImagMin();
  public native double getImagMax();
  public native int getXRes();
  public native int getYRes();
  public native int getMin();
  public native int getMax();
  
  static {
    System.loadLibrary("FractalMath");
  }
}