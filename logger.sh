rm out.log
PACKAGE_NAME=$1
PID=$(adb shell ps | grep $PACKAGE_NAME | awk '{print $2}')
adb logcat -pid=$PID -v tag