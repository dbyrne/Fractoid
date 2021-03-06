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

int lessThanMax, xres=-1, yres=-1, equation,power,max=40,currentRow,rowsCached;
int trapFactor=1, fractalType=1, alg,minimum = 99999,maximum = 0,bailout,bsq;
double realmin, realmax, imagmin, imagmax,P,Q,deltaP,deltaQ,LOG_OF_TWO,SQRT_OF_TWO,LOG_OF_POWER;
double xtmp,ytmp,x=-1,y=-1,prev_x_2,prev_y_2,prev_x=-1,prev_y=-1,tia_prev_x=-1,tia_prev_y=-1,tmp_prev_x,tmp_prev_y,mu=1,xsq,ysq,rnv,inv,rdv,idv;

int** values;

/***Getters and Setters***/
JNIEXPORT jint JNICALL Java_byrne_fractal_NativeLib_getMin(JNIEnv * env, jobject obj) {
  return minimum;
}

JNIEXPORT jint JNICALL Java_byrne_fractal_NativeLib_getMax(JNIEnv * env, jobject obj) {
  return maximum;
}

JNIEXPORT void JNICALL Java_byrne_fractal_NativeLib_resetValues
(JNIEnv * env, jobject obj) {
  
  minimum = 99999;
  maximum = 0;
  rowsCached = 0; 
  
  if (values == NULL) {
    int r;
    alg = 1;
    bailout = 2;
    bsq = 4;
    equation = 1;
    power = 2;
    values = (int**) malloc(yres * sizeof(int*));
    for (r = 0; r < yres; r++) {
      values[r] = (int*) malloc(xres * sizeof(int));
    }
  }
}

JNIEXPORT void JNICALL Java_byrne_fractal_NativeLib_freeValues
(JNIEnv * env, jobject obj) {
  if (values != NULL) {
    int r;
    for (r = 0; r < yres; r++) {
      free(values[r]);
    }
    free(values);
    values = NULL;
  }
}

JNIEXPORT void JNICALL Java_byrne_fractal_NativeLib_setResolution
(JNIEnv * env, jobject obj, jint jxres, jint jyres) {
  LOG_OF_TWO = log(2);
  SQRT_OF_TWO = sqrt(2);
  xres = jxres;
  yres = jyres;
}

JNIEXPORT void JNICALL Java_byrne_fractal_NativeLib_setEquation
(JNIEnv * env, jobject obj, jint jequation, jint jpower) {
  equation = jequation;
  power = jpower;
  LOG_OF_POWER = log(power);
}

JNIEXPORT void JNICALL Java_byrne_fractal_NativeLib_setMaxIterations
(JNIEnv * env, jobject obj, jint jmax) {
  max = jmax;
}

JNIEXPORT void JNICALL Java_byrne_fractal_NativeLib_resetCurrentRow
(JNIEnv * env, jobject obj) {
  currentRow = 0;
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
(JNIEnv * env, jobject obj, jint jalg, jint jbailout) {
  alg = jalg;
  bailout = jbailout;
  bsq = bailout*bailout;
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
  deltaP = (realmax - realmin)/xres;
  deltaQ = (imagmax - imagmin)/yres;
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

/***Helper functions***/

void iterateZ() {
  switch (equation) {
    case 1: //Mandelbrot
      xtmp = xsq - ysq;
      y = 2*x*y;
      break;
    case 2: //Cubic Mandelbrot
      xtmp = xsq*x - 3*x*ysq;
      y = -ysq*y + 3*xsq*y;
      break;
    case 3: //Quartic Mandelbrot
      xtmp = xsq*xsq - 6*xsq*ysq + ysq*ysq;
      y = 4*xsq*x*y - 4*x*ysq*y;
      break;
    case 4: //Quintic Mandelbrot
      xtmp = xsq*xsq*x-10*xsq*x*ysq+5*x*ysq*ysq;
      y=(5*xsq*xsq*y-10*xsq*ysq*y+ysq*ysq*y);
      break;
    case 5: //Sextic Mandelbrot
      xtmp = xsq*xsq*xsq-15*xsq*xsq*ysq+15*xsq*ysq*ysq-ysq*ysq*ysq;
      y=(6*xsq*xsq*x*y-20*xsq*x*ysq*y+6*x*ysq*ysq*y);
      break;
    case 6: // Z^4 - Z^3 - Z^2 + C
      xtmp = xsq*xsq - 6*xsq*ysq + ysq*ysq - (xsq*x - 3*x*ysq) - (xsq - ysq);
      y = 4*xsq*x*y - 4*x*ysq*y - (-ysq*y + 3*xsq*y) - (2*x*y);
      break;
    case 7: // Z^6 - Z^2 + C
      xtmp = xsq*xsq*xsq-15*xsq*xsq*ysq+15*xsq*ysq*ysq-ysq*ysq*ysq - (xsq - ysq);
      y = (6*xsq*xsq*x*y-20*xsq*x*ysq*y+6*x*ysq*ysq*y) - (2*x*y);
      break;
    case 8: //Burning Ship
      xtmp = xsq - ysq;
      y = (2*abs(x)*abs(y));
      break;
    case 9: //Manowar
      xtmp = (xsq - ysq) + prev_x;
      y = (2*x*y) + prev_y;
      break;
    case 10: //Rudy's Cubic Mandelbrot
      xtmp = xsq*x - 3*x*ysq + (-0.7198*x - 0.9111*y);
      y = -ysq*y + 3*xsq*y + (-0.7198*y + 0.9111*x);
      break;
    case 11: //Z(n)^2 - Z(n-1)^2
      xtmp = (xsq - ysq) - (prev_x*prev_x - prev_y*prev_y);
      y = (2*x*y) - (2*prev_x*prev_y);
      break;
    case 12: //Phoenix Julia
      xtmp = xsq - ysq;
      y = 2*x*y;
  }
  x = xtmp;
}

double addC() {
  if (equation != 12) { 
    x += P;
    y -= Q;
  } else { //Phoenix Julia
    x += P + Q*prev_x;
    y += Q*prev_y;
  }
}

double gaussianIntDist() {
  double gint_x = round(x*trapFactor)/trapFactor;
  double gint_y = round(y*trapFactor)/trapFactor;
  return sqrt((x - gint_x)*(x-gint_x) + (y-gint_y)*(y-gint_y));
}

double epsilonCrossDist() {
  return minVal(abs(x),abs(y));
}

double stripes() {
  //double rad = sin(5.0 * log(sqrt(x*x+y*y)));
  double ang = sin(4 * atan2(y,x));
  return 0.5 + 0.5*(ang);
}
  
double curve_est() {
  double num_x = x - prev_x;
  double num_y = y - prev_y;
  double den_x = prev_x - prev_x_2;
  double den_y = prev_y - prev_y_2;
  double denom = (den_x*den_x + den_y*den_y);
  double real = (num_x*den_x + num_y*den_y)/denom;
  double imag = (den_x*num_y - den_y*num_x)/denom;
  return (abs(atan2(imag,real))/M_PI);

}

double TIA() {    
  
  double z_mag = sqrt(tia_prev_x*tia_prev_x + tia_prev_y*tia_prev_y);
  double c_mag;
  if (equation != 12) {
    c_mag = sqrt(P*P + Q*Q);
  } else { //Phoenix Julia
    double rt = P + Q*prev_x;
    double it = Q*prev_y;
    c_mag = sqrt(rt*rt + it*it);
  }
  double mn = z_mag - c_mag;
  mn = sqrt(mn*mn);
  double Mn = z_mag + c_mag;
  double num = sqrt(x*x + y*y) - mn;
  double den = Mn - mn;
  
  return num/den;
}

/***Main Loop***/
JNIEXPORT jintArray JNICALL Java_byrne_fractal_NativeLib_getFractalRow
(JNIEnv * env, jobject obj, jint row, jint state) {
  
  jintArray result;
  
  result = (*env)->NewIntArray(env, xres);
  if (result == NULL) {
    return NULL; /* out of memory error thrown */
  }
  
  if (fractalType == 1) //Mandelbrot
    Q = imagmax - row*deltaQ;

  int col, step = 1;
  
  currentRow++;
  
  //TODO Find a more elegant way to handle 2x2 and 1x1 rendering
  if (state > 0)
    step = 2;

  if (currentRow > rowsCached) {
    
    for(col=(state%2); col < xres; col = col+step) {
  
      if (fractalType == 1) { //Mandelbrot
    
        P = realmin + col*deltaP;
        x = y = 0.0;
        prev_x = prev_y = prev_x_2 = prev_y_2 = 0.0;
      } else { //Julia
        x = realmin + (double)col * deltaP;
        y = imagmax - (double)row * deltaQ;
        prev_x = prev_x_2 = x;
        prev_y = prev_y_2 = y;
      }
  
      lessThanMax = 0;
      int extraIterations = 0;
      double distance,distance1;
      if (alg == 2 || alg == 4)
        distance = 99;
      else
        distance = 0;
        
      int index;
      for (index = 0; index < max; index++) {
        
        xsq = x*x;
        ysq = y*y;
  
        if (alg == 1) {
          if (xsq + ysq > bsq) {
            if (extraIterations == 2) {
              mu = index + 2 - (log(log(sqrt(xsq + ysq))/LOG_OF_TWO)/LOG_OF_POWER);  
              lessThanMax = 1;
              break;
            } else {
              extraIterations++;
              index--;
            }
          }
        } else if (alg == 8 || alg == 7 || alg == 6 || alg == 2 || alg == 3) {
          
          if (xsq + ysq > bsq) {
            mu = (log(log(bailout)) - log(log(sqrt(xsq + ysq))))/LOG_OF_POWER + 1;
            lessThanMax = 1;
            break;
          }
        } else if (alg ==5) {
          if (epsilonCrossDist() < .015 && index > 0) {
            lessThanMax = 1;
            mu = index - (epsilonCrossDist()/.002);
          }
        } else {
          if (xsq+ysq > bsq) {
            break;
          }
        }
  
        tmp_prev_x = x;
        tmp_prev_y = y;
        
        iterateZ();

        if (alg == 7) {
          tia_prev_x = x;
          tia_prev_y = y;
        }
        
        addC();

        prev_x_2 = prev_x;
        prev_y_2 = prev_y;
        prev_x = tmp_prev_x;
        prev_y = tmp_prev_y;

        switch (alg) {
          case 2: //Gaussian Integer Min Distance
            distance1 = distance;
            distance = minVal(distance,gaussianIntDist());
            break;
          case 3: //Gaussian Integer Average Distance
            distance1 = distance;
            distance += gaussianIntDist();
            break;
          case 4: //Epsilon Cross Minimum Distance
            distance = minVal(distance,epsilonCrossDist());
            break;
          case 6: //Curvature_Estimation
            distance1 = distance;
            if (index > 1)
              distance += curve_est();
            break;
          case 7: //TIA
            distance1 = distance;
            if (index > 0)
              distance += TIA();
            break;
          case 8: //Stripes
            distance1 = distance;
            distance += stripes();
        }
      }
      
      if ((alg == 8 || alg == 7 || alg==3 || alg == 6) && lessThanMax == 1) { //Average Coloring
        distance1 = distance1/(index-1);
        distance = distance/index;
          
        values[row][col] = (int)((mu*distance + (1-mu)*distance1)*10200);

        minimum = minVal(minimum,values[row][col]);

      } else if (alg==2) { //Gaussian Int Min Distance
        values[row][col] = (int)((mu*distance + (1-mu)*distance1)*10200);

        minimum = minVal(minimum,values[row][col]);
      } else if (alg == 4) { //Epsilon Cross Min Dist
        values[row][col] = maxVal(1,(int)(log(1+distance) * 20400));
        minimum = minVal(minimum,values[row][col]);
      } else if (lessThanMax == 1) { //Mandelbrot Divergent
        //char s[20];
        //sprintf(s,"%d",mu);
        //__android_log_write(ANDROID_LOG_DEBUG,"FRACTOID_DEBUG",s);
        values[row][col] = maxVal(1,((int)(mu*200)));
        minimum = minVal(minimum,values[row][col]);
      } else { //Mandelbrot Convergent
        values[row][col] = -1;
      }
      maximum = maxVal(maximum,values[row][col]);
    }
    rowsCached++;
  }
  
  (*env)->SetIntArrayRegion(env, result, 0, xres, values[row]);
  return result;
}
