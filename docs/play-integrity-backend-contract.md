# Play Integrity backend contract

CrackMyDroid never includes Google Play credentials, service-account files, or private keys. The
Android client reads only the public HTTPS URL from the manifest metadata key
`com.crackmydroid.INTEGRITY_VERIFICATION_ENDPOINT`.

The client uses Ktor with JSON serialization, strict HTTPS endpoint validation and request binding
validation. Debug logs include only method, host/path, response status and headers with sensitive
values masked; request and response bodies are never logged.

## Client request

`POST {endpoint}` with JSON body:

```json
{ "integrityToken": "encrypted Google token", "requestBinding": "nonce sent to Play Integrity" }
```

The backend authenticates to Google at runtime using workload identity or a secret manager, calls
`v1/{packageName}:decodeIntegrityToken`, validates package name, request binding, timestamp and
replay conditions, then returns only:

```json
{
  "requestBinding": "same nonce",
  "basicIntegrity": true,
  "deviceIntegrity": true,
  "appRecognitionVerdict": "PLAY_RECOGNIZED",
  "appLicensingVerdict": "LICENSED",
  "deviceRecognitionVerdicts": ["MEETS_DEVICE_INTEGRITY"],
  "playProtectVerdict": "NO_ISSUES",
  "verifiedAtEpochMs": 0,
  "details": "Verified backend verdict"
}
```

The verdict attests only CrackMyDroid and its execution device. It must never be copied onto scanned
third-party apps.
