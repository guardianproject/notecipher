#!/bin/sh

projectname=`sed -n 's,.*name="app_name">\(.*\)<.*,\1,p' app/res/values/strings.xml`
target="android-19"

echo "Setting up build for $projectname"
echo ""

for f in `find external/ -name project.properties`; do
projectdir=`dirname $f`
    echo "Updating ant setup in $projectdir:"
    android update lib-project -p $projectdir -t $target
done
android update project -p app/ --subprojects --name "$projectname" -t $target
