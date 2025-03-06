This app uses Bluetooth classic to create simple serial communication between devices.

The user interface was written with Jetpack Compose instead of classic UI tools.

1. You usually don't need to open a server to connect to a companion devices. These devices usually act as a server. You should choose what to do based on the device's features.
2. To connect and start chat between two phones, one of the devices must act as a server. In this case, you can try to connect to nearby devices as a client or open a server and wait for connection requests from other devices.
3. When you set your device as a server, the first connection request is added to the device list and you can start a chat by connecting to this device.
4. It is mandatory to pair devices with each other so that every nearby device cannot connect to the server you have opened.

Getting started with compose
https://developer.android.com/develop/ui/compose/documentation?hl=tr



Getting started with bluetooth
https://developer.android.com/develop/connectivity/bluetooth?hl=tr
