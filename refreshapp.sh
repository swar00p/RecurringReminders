#!/bin/zsh
adb shell pm clear com.swaroop.recurringreminders
adb uninstall com.swaroop.recurringreminders
adb install app/build/outputs/apk/debug/app-debug.apk
