Fractoid - A fractal explorer for the Android platform

CREDITS
Copyright (C) 2010 David Byrne <david.r.byrne@gmail.com>
MultiTouch Controller - Luke Hutchison <luke.hutch@mit.edu>
Logo Design - Rhea dela Cruz <rhea.delacruz@gmail.com>
Special thanks to Jeffrey Blattman for his valuable user feedback.

INSTALLATION

1) Install the Android SDK and Android NDK

2) Copy local.properties.EXAMPLE to local.properties and set the correct Android SDK location.
   
3) Create a symbolic link in the NDK's "sources" directory pointing to native-src/FractalMath
   For example: ln -s /home/dbyrne/Fractoid/native-src/FractalMath/ FractalMath
   
4) Create a new directory in the NDK's "apps" directory called "Fractoid"

5) Copy Application.mk.EXAMPLE to Application.mk and move it tothe directory created in step 4.
   Then set APP_PROJECT_PATH to the location of the Fractoid project.
   
6) From the NDK directory, run "make app=Fractoid".  This will compile the native code.

7) From the Fractoid project directory, run "ant install".