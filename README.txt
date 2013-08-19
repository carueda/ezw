EZW Project
Carlos Rueda
UAM - Universidad Autónoma de Manizales
Manizales - Colombia

This is my implementation of the EZW image codec according to original
Shapiro's paper: "Embedded Image Coding Using Zerotrees of Wavelet Coefficients."
IEEE Trans. on signal proc. Vol. 41, No 12, December, 1993.

Thanks to Jorge Pinzón at NASA Goddard Space Flight Center for suggesting
me to work with this excellent paper to better understand the wavelet
transform and its advantages for image compression. Also, my thanks to
Angela Aristizábal at the Image and Sound Processing Group at Universidad
Autónoma de Manizales for her enthusiasm and exciting discussions.

To compile the package, just type:
    ant
which assumes you have the Apache Ant tool.

To run a complete encode/decode test:
    cd work/
    make image=al.pgm filter=haar
this will encode an image using the program is.ezw.SAQ and then the
encoded bit stream will be decoded and displayed progressively using
the program is.ezw.ProgressiveViewer. If you don't have a unix-like
environment, just have a look at the Makefile to see how you can
call the java programs directly.

Included images:
    al.pgm  256x256
    lena.jpg 512x512
    ingrid.raw 256x256
    uam.jpg 256x256
Any image that java can read can be used; the only requirement is that
it must be square with side length a power of 2. If the image is in color,
its green component is used for all processing.
    
These filters are implemented:
    haar
    daub4
    daub12
    
See Makefile for other parameters.

Note: this was a learning exercise mainly for my personal purposes.
I put some effort for a clear design but couldn't find the time
to properly comment the source code.
Anyway, hope you find the code and the demo program useful.

--Dec 12, 1999