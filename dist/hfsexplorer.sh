#!/bin/sh
BASEDIR="`dirname "$0"`"
LIB="${BASEDIR}/lib"

UNAME=`uname`
if [ "${UNAME}" = "Darwin" ]; then
    DOCK_NAME="-Xdock:name=HFSExplorer"
    DOCK_ICON="-Xdock:icon=${BASEDIR}/res/icon.png"
fi
java ${DOCK_NAME:+"${DOCK_NAME}"} ${DOCK_ICON:+"${DOCK_ICON}"} -cp "$LIB/hfsx.jar" org.catacombae.hfsexplorer.FileSystemBrowserWindow "$@"
