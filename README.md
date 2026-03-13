# Kiosk-Browser
Full lockdown Kiosk Broswer for Android. This is my first ever Kotlin / Android Project the code is VERY rough.
This project is based on mrugacz95's "Kiosk" project. He did the hard part i just added an Browser 

### If you want to remove the app you will have to Factory reset / Wipe your device!

### Usage
1. Factory reset your device.
2. Skip adding google account
3. Install apk
    ```bash
    adb install path/to/kiosk.apk
    ```
4. Set device owner
    ```bash
    adb shell dpm set-device-owner pl.mrugacz95.kiosk/.MyDeviceAdminReceiver
    ```

### Screenshots


# Article

If you wish to know more about the Kiosk aspect of this app please [read mrugacz95's Article](https://github.com/mrugacz95/kiosk/blob/master/README.md). Its a nice read and basically a crash course about Kiosk devices 

### Summary

Android provides many new features which allow developers to create kiosks. Once enabled, users will not be able to exit the prepared application nor access device settings. One important thing to note is that the device should be packed in a case, to hide the power button, which cannot be disabled.

Sources
* [Build Applications for Single-Use Devices](https://codelabs.developers.google.com/codelabs/cosu/index.html)
* [Set up Single-Purpose Devices](https://developer.android.com/work/cosu.html)
* [Device Administration](https://developer.android.com/guide/topics/admin/device-admin.html)
* [Making an Android Kiosk App](http://wenchaojiang.github.io/blog/realise-Android-kiosk-mode/)
* [Updating Your Android Kiosk App](http://www.sureshjoshi.com/mobile/updating-android-kiosk-app/)
* [Published article - HOW TO TURN YOUR ANDROID APPLICATION INTO A KIOSK](https://snow.dog/blog/kiosk-mode-android/).
* [Article in form of presentation - COSU, czyli jak zamienić Androida w kiosk [PL]](https://drive.google.com/file/d/1uAX11bXR8aC-sg5VlybGaHo0vmuIw93l/view?usp=sharing).
