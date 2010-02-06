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

#ifndef maxVal
  #define maxVal( a, b ) ( ((a) > (b)) ? (a) : (b) )
#endif

#ifndef abs
  #define abs( a ) ( ((a) < (0)) ? (a*-1) : (a) )
#endif



JNIEXPORT jintArray JNICALL Java_byrne_fractal_NativeLib_getFractalRow
(JNIEnv * env, jobject obj,
 jint row, jint xres, jint yres, jint state,
 jint power, jint max, jint equation, jint fractalType, jdouble P, jdouble Q,
 jdouble realmin, jdouble realmax,
 jdouble imagmin, jdouble imagmax) {
  
  jintArray result;
  jdouble xtmp=0,x=-1,y=-1,prev_x=-1,prev_y=-1,tmp_prev_x,tmp_prev_y,mu=1,xsq,ysq;
  jint index;
  jint lessThanMax;
  
  result = (*env)->NewIntArray(env, xres);
  if (result == NULL) {
    return NULL; /* out of memory error thrown */
  }

  jint fractalRow[xres];

  jdouble deltaP = (realmax - realmin)/xres;
  jdouble deltaQ = (imagmax - imagmin)/yres;
  
  if (fractalType == 1)
    Q = imagmax - row*deltaQ;

  jint col, step = 1;
  //TODO Find a more elegant way to handle 2x2 and 1x1 rendering
  if (state > 0)
    step = 2;
  for(col=(state%2); col < xres; col = col+step) {
    
    if (fractalType == 1) {
  
      P = realmin + col*deltaP;
      x = y = 0.0;
      prev_x = prev_y = 0.0;
    } else {
      x = realmin + (double)col * deltaP;
      y = imagmax - (double)row *deltaQ;
      prev_x = x;
      prev_y = y;
    }

    lessThanMax = 0;

    jint extraIterations = 0;
    for (index = 0; index < max; index++) {
      xsq = x*x;
      ysq = y*y;

      if (xsq + ysq > 4) {
        //a few extra iterations improves color smoothing - why don't some equations work when a higher number is used?
        if (extraIterations == 2) { 
          lessThanMax = 1;
          mu = index + 2 - (log(log(sqrt(xsq + ysq))/ log(2.0))/log(power));
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
    }

    if (lessThanMax == 1) {
      fractalRow[col] = maxVal(0,((int)round(mu*10)-1))%(max*10);
    } else {
      fractalRow[col] = -1;
    }
  }
  //__android_log_write(ANDROID_LOG_DEBUG,"FRACTOID_DEBUG","Hello");
  (*env)->SetIntArrayRegion(env, result, 0, xres, fractalRow);
  return result;
}
