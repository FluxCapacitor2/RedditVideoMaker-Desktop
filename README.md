# RedditVideoMaker
A program that creates Reddit videos from start to finish, including taking screenshots, generating an audio track, adding an outro sequence, and uploading the final video to YouTube and adding a title, description, thumbnail, and tags.
*Once someone runs the server, the program can be run **completely** from the web!*
## Installation
### Step 1
Download the [Daniel TTS voice](https://github.com/FluxCapacitor2/RedditVideoMaker-Desktop/blob/master/Daniel%2022Khz%20MLG%20voice.exe?raw=true) (~90 MB) from this link and install it in the default directory.
### Step 2
Download the [RedditVideoMaker Executable JAR](https://github.com/FluxCapacitor2/RedditVideoMaker-Desktop/blob/master/out/artifacts/RedditVideoMaker_Desktop_jar/RedditVideoMaker-Desktop.jar?raw=true) (~50MB). *If you are having any problems with the JAR, execute with `java -jar RedditVideoMaker.jar` and check the output for error messages.*
### Step 3
Download the [Zipped RVM Repository](https://github.com/FluxCapacitor2/RedditVideoMaker-Desktop/archive/master.zip) and unzip it. Remember the location of the folder inside the master directory called "library_folder_template" for later.
### Step 4
Create an API key for the YouTube Data API v3:
1. Navigate to https://console.developers.google.com/apis/credentials
2. Sign in with your Google account (if you are already signed in, skip this step)
3. Create a project or use an existing one
   * If you see a message that says "To view this page, select a project", click the "Create" button on the right.
   * If not, you must already have created a project. You can make a new project if you want, or use this existing project.
4. Click "Create Credentials"
5. Click "OAuth Client ID"
6. Select "Other" for "Application Type"
7. Click "Create"
8. If a dialog titled "OAuth Client" pops up, click "OK"
9. Find the name of the credentials you just created in the list and click the Download icon on the right
10. Rename the file to "client_secrets.json"
11. Navigate to the library folder you just downloaded and extracted.
11. Place the file in the library folder template in the `[library folder root]/bin` folder.
12. Navigate to https://console.developers.google.com/apis/library/youtube.googleapis.com?id=125bab65-cfb6-4f25-9826-4dcc309bc508
13. Click "Enable"
### Step 5
1. Add background videos to the `backgrounds` folder inside the downloaded library folder template.
    * *Background videos are *not* repeated, so make sure they are at least a few minutes long! If the background file is not long enough, the background video will play behind the screenshot until it ends, and then the background will be black.*
2. Add background music tracks to the `audio` folder inside the downloaded library folder template.
    * *Background music files should be named as it will show up in the music credits. For example, `New Land by ALBIS.mp3` shows up in the music credits as `New Land by ALBIS`.*
    * *All formats accepted by FFmpeg are allowed, but all audio files must be the same format and codec.*
    * *There should be enough music in this folder to last about 30 minutes (enough to accommodate longer videos) if all of the files were to be played one after another.*
### Step 6
1. Launch the JAR and it will start up a server at `localhost:8080`
2. Go to that address or the address it says in the program's log
3. Go to any reddit post using the server
4. Click "Add" on any post that you want to be read
5. Select from the radio buttons if this is the first or last post in the video (or both, meaning this is the only post)
6. Change the text shown in the thumbnail (use < and > to highlight text)
7. If this post if the one to be featured in the video title, check that box.
8. Click "Send"
## Supported Subreddits
These subreddits are supported, but many, many more may be supported (but they aren't tested).
- r/AskReddit
- r/ProRevenge
- r/MaliciousCompliance
- r/NuclearRevenge
- r/PettyRevenge
- r/tifu
- r/AMA
- r/IAmA
- r/TalesFromRetail
- r/TalesFromTechSupport
- r/EntitledParents
- r/StoriesAboutKevin
- r/TalesFromTheFrontDesk

*All text-based subreddits are supported. Images don't really work yet (if you want image support, you can work on it! see the Contributing section below).*
## Sources
* You can look at or download the sources for the program in the GitHub repo: https://github.com/FluxCapacitor2/RedditVideoMaker-Desktop
* Compiled sources are located at `out/production/RedditVideoMaker-Desktop/`
* A compiled JAR can be found at `out/artifacts/RedditVideoMaker_Desktop_jar/RedditVideoMaker-Desktop.jar`. This JAR is directly executable and can be added to the startup folder on Windows.
## Contributing
### Contributors
* FluxCapacitor
* *Add your name here! All contributions, big or small, are welcome! Just make a pull request!*
### Code Style
You can download my code style settings from the EditorConfig files located in each repository (source links above). For PRs to be merged, it would be nice if you could follow my code style settings! Also, please consider taking a look at the *TODO* section below, it would really be appreciated if someone helped me implement these features.
### Testing
This program was *only* tested on Windows 10. Possibly others could test on other operating systems in the future, but some of the OS-specific config must be changed.
### TODO
- [ ] Ability to set TTS audio to be spoken for images
- [ ] Ability to narrate the text in screenshots instead of using text to speech (like a voice recorder)
- [ ] Ability to change TTS voice

## Open Source and Free Software
*Thanks to the authors of all of these programs/APIs! They've really helped me develop RVM quickly and easily.*
- [Balabolka](http://www.cross-plus-a.com/balabolka.htm) (Freeware, but I can't find the sources anywhere)
- [YouTube Data API](https://console.developers.google.com/apis/library/youtube.googleapis.com?id=125bab65-cfb6-4f25-9826-4dcc309bc508) (Closed-source)
- [FFmpeg and FFprob](http://ffmpeg.org/) (Open-source)
- [Jaffree](https://github.com/kokorin/Jaffree) (Open-source)
