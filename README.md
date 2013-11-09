# NoteCipher

A simple and secure note taking app previously known as notepadbot.

Showcase app for [SQLCipher for Android](http://sqlcipher.net/sqlcipher-for-android) and [CacheWord](https://github.com/guardianproject/cacheword).

```
 git clone https://github.com/guardianproject/notepadbot.git
 cd notepadbot
 git submodule update --init --recursive
 ./setup-ant.sh
 ant -buildfile app/build.xml clean debug
 ls -l app/bin/NoteCipher*.apk
```

## Development Setup

Follow these steps to setup your dev environment using Eclipse:

1. create a new Eclipse *workspace* in the root directory of the repo.
2. For Cacheword, import this directory `external/cacheword/cachewordlib/`, using "Import -> Android -> Existing Android Code Into Workspace":
3. Then, "Import -> General -> Existing Projects Into Workspace" for the `app/` directory.

## License and Credits 

* Notepad Icon originally from
    http://yaromanzarek.deviantart.com/art/iPhone-style-Notepad-icon-133822563

This project is licensed under the Apache version 2.0 license

Copyright (C) 2013 Abel Luck <abel@guardianproject.info>
Copyright (C) 2011-2013 Nathan Freitas <nathan@freitas.net>
Copyright (C) 2011 Hans-Christoph Steiner <hans@eds.org>
Copyright (C) 2008 The Android Open Source Project
Copyright (C) 2008 Google Inc.

