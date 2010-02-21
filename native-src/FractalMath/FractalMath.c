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

#include "byrne_fractal_NativeLib.h"
#include <math.h>
#include <android/log.h>
#include <stdio.h>
#include <stdlib.h>

#ifndef maxVal
  #define maxVal( a, b ) ( ((a) > (b)) ? (a) : (b) )
#endif

#ifndef minVal
  #define minVal( a, b ) ( ((a) > (b)) ? (b) : (a) )
#endif

#ifndef abs
  #define abs( a ) ( ((a) < (0)) ? (a*-1) : (a) )
#endif

int xres=-1, yres=-1, equation=1,power=2,max=40, trapFactor=1, fractalType=1, alg=1;
double realmin, realmax, imagmin, imagmax,P,Q;

int** values;

double orbitDistance(double x, double y, jint trapFactor) {
  double gint_x = round(x*trapFactor)/trapFactor;
  double gint_y = round(y*trapFactor)/trapFactor;
  return sqrt((x - gint_x)*(x-gint_x) + (y-gint_y)*(y-gint_y));
}

JNIEXPORT void JNICALL Java_byrne_fractal_NativeLib_resetValues
(JNIEnv * env, jobject obj) {
  if (values == NULL) {
    int r;
    values = (int**) malloc(yres * sizeof(int*));
    for (r = 0; r < yres; r++) {
      values[r] = (int*) malloc(xres * sizeof(int));
    }
  }
  int row, col;
  for (row = 0; row < yres; row++)
    for (col = 0; col < xres; col++)
      values[row][col] = -2;
}

JNIEXPORT void JNICALL Java_byrne_fractal_NativeLib_freeValues
(JNIEnv * env, jobject obj) {
  if (values != NULL) {
    int r;
    for (r = 0; r < yres; r++) {
      free(values[r]);
    }
    free(values);
  }
}

JNIEXPORT void JNICALL Java_byrne_fractal_NativeLib_setResolution
(JNIEnv * env, jobject obj, jint jxres, jint jyres) {
  xres = jxres;
  yres = jyres;
}

JNIEXPORT void JNICALL Java_byrne_fractal_NativeLib_setEquation
(JNIEnv * env, jobject obj, jint jequation, jint jpower) {
  equation = jequation;
  power = jpower;
}

JNIEXPORT void JNICALL Java_byrne_fractal_NativeLib_setMaxIterations
(JNIEnv * env, jobject obj, jint jmax) {
  max = jmax;
}

JNIEXPORT void JNICALL Java_byrne_fractal_NativeLib_setTrapFactor
(JNIEnv * env, jobject obj, jint jtrapFactor) {
  trapFactor = jtrapFactor;
}

JNIEXPORT void JNICALL Java_byrne_fractal_NativeLib_setFractalType
(JNIEnv * env, jobject obj, jint jfractalType) {
  fractalType = jfractalType;
}

JNIEXPORT void JNICALL Java_byrne_fractal_NativeLib_setAlgorithm
(JNIEnv * env, jobject obj, jint jalg) {
  alg = jalg;
}

JNIEXPORT void JNICALL Java_byrne_fractal_NativeLib_setCValue
(JNIEnv * env, jobject obj, jdouble jP, jdouble jQ) {
  P = jP;
  Q = jQ;
}

JNIEXPORT void JNICALL Java_byrne_fractal_NativeLib_setCoords
(JNIEnv * env, jobject obj, jdouble jrealmin, jdouble jrealmax, jdouble jimagmin, jdouble jimagmax) {
  realmin = jrealmin;
  realmax = jrealmax;
  imagmin = jimagmin;
  imagmax = jimagmax;  
}

JNIEXPORT jdouble JNICALL Java_byrne_fractal_NativeLib_getRealMin(JNIEnv * env, jobject obj) {
  return realmin;
}
JNIEXPORT jdouble JNICALL Java_byrne_fractal_NativeLib_getImagMin(JNIEnv * env, jobject obj) {
  return imagmin;
}
JNIEXPORT jdouble JNICALL Java_byrne_fractal_NativeLib_getRealMax(JNIEnv * env, jobject obj) {
  return realmax;
}
JNIEXPORT jdouble JNICALL Java_byrne_fractal_NativeLib_getImagMax(JNIEnv * env, jobject obj) {
  return imagmax;
}
JNIEXPORT jint JNICALL Java_byrne_fractal_NativeLib_getXRes(JNIEnv * env, jobject obj) {
  return xres;
}
JNIEXPORT jint JNICALL Java_byrne_fractal_NativeLib_getYRes(JNIEnv * env, jobject obj) {
  return yres;
}

JNIEXPORT jintArray JNICALL Java_byrne_fractal_NativeLib_getFractalRow
(JNIEnv * env, jobject obj, jint row, jint state) {
  
  jintArray result;
  double xtmp=0,x=-1,y=-1,prev_x=-1,prev_y=-1,tmp_prev_x,tmp_prev_y,mu=1,xsq,ysq;
  int index;
  int lessThanMax;
  const double LOG_OF_TWO = log(2);
  const double SQRT_OF_TWO = sqrt(2);
  
  result = (*env)->NewIntArray(env, xres);
  if (result == NULL) {
    return NULL; /* out of memory error thrown */
  }

  double deltaP = (realmax - realmin)/xres;
  double deltaQ = (imagmax - imagmin)/yres;
  
  if (fractalType == 1) //Mandelbrot
    Q = imagmax - row*deltaQ;

  int col, step = 1;
  //TODO Find a more elegant way to handle 2x2 and 1x1 rendering
  if (state > 0)
    step = 2;
  for(col=(state%2); col < xres; col = col+step) {
    if (values[row][col] != -2) {
      continue;
    }
    if (fractalType == 1) { //Mandelbrot
  
      P = realmin + col*deltaP;
      x = y = 0.0;
      prev_x = prev_y = 0.0;
    } else { //Julia
      x = realmin + (double)col * deltaP;
      y = imagmax - (double)row * deltaQ;
      prev_x = x;
      prev_y = y;
    }

    lessThanMax = 0;

    int extraIterations = 0;
    double distance;
    if (alg == 2)
      distance = 99;
    else
      distance = 0;
      
    
    for (index = 0; index < max; index++) {
      
      if (alg == 3 && fractalType == 2) { //Gaussian Integer Average & Julia
        distance += orbitDistance(x,y,trapFactor);
      } else if (alg == 2 && fractalType == 2) { //Gaussian Integer Minimum & Julia
        distance = minVal(distance,orbitDistance(x,y,trapFactor));
      }
      
      xsq = x*x;
      ysq = y*y;

      if ((alg == 1 && xsq + ysq > 4) || xsq + ysq > 16) {
        if (alg > 1)
          break;
        //a few extra iterations improves color smoothing - why don't some equations work when a higher number is used?
        if (extraIterations == 2) { 
          lessThanMax = 1;
          mu = index + 2 - (log(log(sqrt(xsq + ysq))/LOG_OF_TWO)/log(power));
          break;
        } else {
          extraIterations++;
          index--;
        }
      }
      
      //TODO Refactoring needed.
      switch (equation) {
        case 1:
          xtmp = xsq - ysq + P;
          y = (2*x*y) + Q;
          break;
        case 2:
          xtmp = xsq*x - 3*x*ysq + P;
          y = -ysq*y + 3*xsq*y + Q;
          break;
        case 3:
          xtmp = xsq*xsq - 6*xsq*ysq + ysq*ysq + P;
          y = 4*xsq*x*y - 4*x*ysq*y + Q;
          break;
        case 4:
          xtmp = xsq*xsq*x-10*xsq*x*ysq+5*x*ysq*ysq + P;
          y=(5*xsq*xsq*y-10*xsq*ysq*y+ysq*ysq*y) + Q;
          break;
        case 5:
          xtmp = xsq*xsq*xsq-15*xsq*xsq*ysq+15*xsq*ysq*ysq-ysq*ysq*ysq + P;
          y=(6*xsq*xsq*x*y-20*xsq*x*ysq*y+6*x*ysq*ysq*y) + Q;
          break;
        case 6:
          xtmp = xsq*xsq - 6*xsq*ysq + ysq*ysq - (xsq*x - 3*x*ysq) - (xsq - ysq) + P;
          y = 4*xsq*x*y - 4*x*ysq*y - (-ysq*y + 3*xsq*y) - (2*x*y) + Q;
          break;
        case 7:
          xtmp = xsq*xsq*xsq-15*xsq*xsq*ysq+15*xsq*ysq*ysq-ysq*ysq*ysq - (xsq - ysq) + P;
          y = (6*xsq*xsq*x*y-20*xsq*x*ysq*y+6*x*ysq*ysq*y) - (2*x*y) + Q;
          break;
        case 8:
          xtmp = xsq - ysq + P;
          y = (2*abs(x)*abs(y)) - Q;
          break;
        case 9:
          tmp_prev_x = x;
          tmp_prev_y = y;
          xtmp = (xsq - ysq) + prev_x + P;
          y = (2*x*y) + prev_y + Q;
          prev_x = tmp_prev_x;
          prev_y = tmp_prev_y;
          break;
        case 10:
          tmp_prev_x = x;
          tmp_prev_y = y;
          xtmp = (xsq - ysq) + P + Q*prev_x;
          y = (2*x*y) + Q*prev_y;
          prev_x = tmp_prev_x;
          prev_y = tmp_prev_y;
          break;
      }
      x = xtmp;
      
      if (alg == 3 && fractalType == 1) { //Gaussian Integer Average & Mandelbrot
        distance += orbitDistance(x,y,trapFactor);
      } else if (alg == 2 && fractalType == 1) { //Gaussian Integer Minimum & Mandelbrot
        distance = minVal(distance,orbitDistance(x,y,trapFactor));
      }      
    }
    
    if (alg==3) { //Gaussian Integer average
      values[row][col] = maxVal(1,(int)(((distance/(index+1))/(SQRT_OF_TWO/trapFactor))*10200));
    } else if (alg==2) {
      values[row][col] = maxVal(1,(int)((distance/(SQRT_OF_TWO/trapFactor))*10200));
    } else if (lessThanMax == 1) {
      //char s[20];
      //sprintf(s,"%d",mu);
      //__android_log_write(ANDROID_LOG_DEBUG,"FRACTOID_DEBUG",s);
      values[row][col] = maxVal(1,((int)(mu*200)));
    } else {
      values[row][col] = -1;
    }
  }
  
  (*env)->SetIntArrayRegion(env, result, 0, xres, values[row]);
  return result;
}
