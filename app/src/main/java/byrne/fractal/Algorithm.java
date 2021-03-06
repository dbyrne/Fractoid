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

public enum Algorithm {

  ESCAPE_TIME(1,2),GAUSSIAN_MINIMUM(2,16),GAUSSIAN_AVERAGE(3,16),
  EPSILON_CROSS(4,4),EPSILON_CROSS_BAILOUT(5,4),CURVATURE_ESTIMATION(6,200),TRIANGLE_INEQUALITY(7,200),STRIPES(8,200);
  private int native_integer,bailout;
  
  Algorithm(int i,int b) {
    native_integer = i;
    bailout = b;
  }
  
  int getInt() {
    return native_integer;
  }
  
  int getBailout() {
    return bailout;
  }
}