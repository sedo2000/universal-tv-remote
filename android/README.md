# Universal TV Remote — Android prototype

This is a native Android project, separate from the Vercel PWA.

## Build an APK for free with GitHub Actions
1. Push changes to the repository.
2. Open **Actions** in GitHub.
3. Select **Build Android APK** and run it, or wait for a push-triggered run.
4. Open a successful run and download **universal-tv-remote-debug-apk** from Artifacts.
5. Extract the ZIP and install `app-debug.apk` on your Android phone.

## Current implementation status
- Native Android shell and local mDNS/DNS-SD discovery for `_androidtvremote2._tcp.` and `_androidtvremote._tcp.`.
- Shows discovered service, local address, and port when Android resolves it.
- Basic remote and text-entry UI are intentionally marked as not active.
- Pairing, authenticated TLS/protobuf remote protocol, real key events, text injection, app launching, and voice control are **not implemented yet**. Do not represent discovery as successful control.
- The next engineering step is implementing and testing the Android TV Remote pairing and remote protocols against the actual television. Device firmware and installed Remote Service determine compatibility.

## Security notes
- No TV credentials are collected or transmitted.
- No ports are opened on the router.
- Use pairing initiated/approved by the TV. Never disable TV pairing security to make the app work.
