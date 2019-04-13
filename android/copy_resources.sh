#!/bin/bash -x
cd `dirname $0`
rm -rf app/src/main/res/raw/blog_resources.zip
mkdir -p app/src/main/res/raw
cd ../resources
zip -r ../android/app/src/main/res/raw/blog_resources.zip *
