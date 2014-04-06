#!/bin/bash

# we must use the same support lib jar in all the dependencies
good_jar="app/libs/android-support-v4.jar"

# all these libs depend on android-support-v4.jar
#mapfile <<END # requires newer bash than on MacOS X
#external/ActionBarSherlock/actionbarsherlock
#external/CacheWord/cachewordlib
#END

MAPFILE[0]='external/actionbarsherlock/actionbarsherlock'
MAPFILE[1]='external/cacheword/cachewordlib'


for project in "${MAPFILE[@]}"; do
    project=${project%$'\n'} # remove trailing newline
    echo "updating $good_jar in $project"
    cp -f $good_jar $project/libs
done

