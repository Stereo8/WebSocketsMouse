# WebSockets Gyroscope Mouse Client

Hobby project, uses the gyroscope of an Android device to control the mouse on a PC or Mac, in the same manner a Wiimote would control the cursor on a Nintendo Wii console.

Server discovery is done over mDNS, then a WebSocket is opened between client and server. The [server](https://github.com/stereo8/WebSocketsServer) is written in Python 3.

Built using [Scarlet](https://github.com/Tinder/Scarlet).