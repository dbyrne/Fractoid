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

public enum ComplexEquation {
  SECOND_ORDER(2),
  THIRD_ORDER(3),
  FOURTH_ORDER(4),
  FIFTH_ORDER(5),
  SIXTH_ORDER(6),
  Z4Z3Z2(4),
  Z6Z2(6),
  MANOWAR(2),
  PHOENIX(2);
  
  private int power;
  
  ComplexEquation(int p) {
    power = p;
  }
  
  int getPower() {
    return power;
  }
}