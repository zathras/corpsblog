#!/bin/bash -x
cd `dirname $0`
rm -rf out
mkdir -p out/java
kotlinc -include-runtime -d out/corpsblog.jar \
        `find src -name '*.kt' -print` \
        `find src_dt -name '*.kt' -print` \
        `find src -name '*.java' -print`
javac -d out/java `find src -name '*.java' -print`
cd out/java
jar uf ../corpsblog.jar *
cd ../..
rm -rf out/java
echo "Created out/corpsblog.jar"
