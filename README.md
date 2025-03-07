# Soundboard - README

## Description
This project is a soundboard software developed in Java. It allows users to add MP3 and WAV sounds and play them in their microphone.  
For audio device management, i used **Voicemeeter Banana**, a software that combines multiple audio inputs into a single output. This allows users to speak while the soundboard plays sounds. 
Additionally, i used **VB-CABLE**, a virtual audio device that creates a virtual microphone input and output. The selected sound file is sent in the virtual microphone.  

## Challenges and Limitations
During development, i encountered several issues due to outdated software and Java version management conflicts in certain programs. Despite these challenges, i managed to get the soundboard working.  
However, i realized that Java is not the most efficient choice for building a soundboard. While I could add more features, I don't believe it's worth investing more time in this project. 
There are likely better soundboard implementations in other programming languages, but i wanted to challenge myself by making it work in Java.  

## Build and Release
To package the project, i used **Maven Shade Plugin** to generate a fat JAR containing all dependencies.  
To create a Windows executable (`soundboard.exe`), i used **Launch4j**, which wraps the JAR into a standalone `.exe` file for easier distribution.  

## Usage and Recommendation
The software functions as intended, and you can use it. However, i wouldn't recommend it as the best soundboard solution. If you're looking for a highly efficient and feature-rich soundboard, you might want to explore alternatives in other languages. 
Anyway, if you really want to try it out, you can refer to the **Settings Of Audio** file. All the audio settings are in this file, you just need to install **Voicemeeter**, **VB-CABLE** and **JAVA 8** or the latest ver, restart your computer, and follow the instructions in the **Settings Of Audio** file.  

Thanks for checking out this project!

![soundboard jpg](https://github.com/user-attachments/assets/11838a73-408e-4ecf-a7b7-4d67169ec1de)
