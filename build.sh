#!/bin/bash
echo "This needs to be updated for new source orgination, post-Android."
echo "For now, just do this:"
echo "mv ./desktop/out/artifacts/desktop_jar/desktop.jar ~/lib/corpsblog.jar"
exit 1
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
