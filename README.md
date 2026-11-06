# 1App

1App is an Android launcher for occasional household and municipal tasks. The first version covers Leonberg and the Böblingen district.

| Task | Official service |
| --- | --- |
| Book a Bürgeramt appointment | [Stadt Leonberg](https://www.leonberg.de/B%C3%BCrger-Stadt/Politik-und-Rathaus/Termine-online-buchen/) |
| Submit a meter reading | [Netze BW](https://www.netze-bw.de/zaehlerstandseingabe) |
| Find waste collection dates | [AWB Böblingen](https://www.awb-bb.de/abfuhrtermine) |
| Find a swimming course | [DLRG Leonberg](https://leonberg.dlrg.de/mitmachen/anmeldungen-und-lehrgaenge/) |
| Sign in to a utility account | [Netze BW customer portal](https://meine.netze-bw.de/) |

The launcher follows the device language and supports English and German. Open a task's details for preparation guidance and to add it to Favorites. The meter-reading task can also store a meter number for manual copying.

1App launches official HTTPS links with the default browser's Custom Tabs support when available and falls back to Android's web-link handler. The browser owns web sessions and credential handling. Favorites and the meter number are stored locally, with backup disabled. The app requests no Android permissions and has no backend.

## Build and run

Use JDK 17 and install Android SDK Platform 35 and Build Tools 35.0.0. Set `ANDROID_HOME` or configure the SDK in Android Studio. The minimum Android version is Android 8.0 (API 26).

```sh
./gradlew assembleDebug lintDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

The [Android build workflow](https://github.com/jaffa944099/1App/actions/workflows/android.yml) publishes the debug APK as an artifact after a successful build and lint run.

## Device smoke test

1. Open each task and confirm that it lands on the expected provider. Close the tab and return to 1App.
2. Add a favorite, select the Favorites filter, then relaunch 1App and confirm the favorite persists.
3. Save, copy and clear a test meter number from the meter-reading task. Check persistence after relaunch.
4. Try the Netze BW portal with your configured password manager. Actual sign-in and session expiry depend on the browser and provider.
