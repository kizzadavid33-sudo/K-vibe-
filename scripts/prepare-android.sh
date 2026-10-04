#!/usr/bin/env bash
set -euo pipefail
if [ ! -d android ]; then npx cap add android; fi
npx cap sync android
python3 - <<'PY'
from pathlib import Path
p=Path('android/app/src/main/AndroidManifest.xml'); s=p.read_text()
perms=['    <uses-permission android:name="android.permission.INTERNET" />','    <uses-permission android:name="android.permission.CAMERA" />','    <uses-permission android:name="android.permission.RECORD_AUDIO" />']
if 'android.permission.CAMERA' not in s:
    idx=s.find('>'); s=s[:idx+1]+'\n'+'\n'.join(perms)+'\n'+s[idx+1:]
p.write_text(s)
PY
