
This directory contains some test images to put in a gallery, plus a little
Kotlin script that was used to generate the images.  For the first four 
images, I took a picture with my cellphone camera in four different
orientations, so that EXIF orientation data would get tested.

The images new_?.jpg are to test the Exif parsing.  They were  taken
with a mobile phone, and were found to break a public-domain Exif
parser I had been using.
