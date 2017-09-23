#!/bin/sh
TOPDIR=`pwd`

java -Dfile.encoding=UTF-8 -mx1848M -cp $TOPDIR/app/libs/kawa/kawa-1.11-modified.jar:$TOPDIR/app/libs/acra/acra-4.4.0.jar:$TOPDIR/app/libs/appinventor/YoungAndroidRuntime.jar:$TOPDIR/app/libs/appinventor/AndroidRuntime.jar:$TOPDIR/app/libs/android/5.1.1/android.jar kawa.repl -f $TOPDIR/app/src/main/res/raw/runtime.scm -d /tmp/ -P appinventor.ai_test.test1. -C $TOPDIR/app/libs/AppContainer.yail
cd /tmp && jar cf $TOPDIR/app/libs/AppContainer.jar com

