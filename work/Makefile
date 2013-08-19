image=lena.jpg
filter=haar
maxbytes=16000

# encoded image
bit=${image}-${filter}.bit

# delay between images in progressive rendering
delay=500

# expansion factor for rendering
expand=1

cp="${CLASSPATH}:../DIST/ezw.jar"

test: encode decode

encode:
	echo "Encoding image ${image} with ${filter} filter"
	java -cp ${cp} is.ezw.SAQ -encodeImage ${image} ${filter} 15 ${bit} ${maxbytes}

decode:
	echo "Progressively rendering bit stream ${bit}"
	java -cp ${cp} is.ezw.ProgressiveViewer -delay ${delay} -expand ${expand} ${bit}

view:
	java -cp ${cp} is.image.Viewer -expand ${expand} ${image}
	
clean:
	rm -f *.raw *.bit
	