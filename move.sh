#! /bin/bash

FILES=`ls *.jpeg`
for FILE in $FILES
do 
		mv $FILE screenshots
done
