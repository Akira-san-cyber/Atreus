#!/bin/bash
echo "1. Installing Java 17 (this takes 1-2 minutes)..."
sudo apt-get update -y
sudo apt-get install -y openjdk-17-jdk-headless wget unzip

echo "2. Setting up Android SDK..."
export JAVA_HOME=$(dirname $(dirname $(readlink -f $(which java))))
export PATH=$JAVA_HOME/bin:$PATH
mkdir -p $HOME/android-sdk/cmdline-tools
cd $HOME/android-sdk/cmdline-tools
wget -q https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip -O tools.zip
unzip -q tools.zip
mv cmdline-tools latest
rm tools.zip

export ANDROID_HOME=$HOME/android-sdk
export PATH=$PATH:$ANDROID_HOME/cmdline-tools/latest/bin

echo "3. Accepting licenses..."
yes | sdkmanager --licenses > /dev/null 2>&1
sdkmanager "platforms;android-35" "build-tools;35.0.0" "platform-tools"

echo "4. Starting Gradle Build..."
cd /workspaces/Atreus
echo "sdk.dir=$HOME/android-sdk" > local.properties
./gradlew assembleDebug
