# NoteCipher

A simple and secure note taking app previously known as notepadbot.

Showcase app for [SQLCipher](http://sqlcipher.net/sqlcipher-for-android) for
Android and [CacheWord](https://github.com/guardianproject/cacheword).

## Development Setup

Follow these steps to setup your dev environment:

1. Checkout the NoteCipher git repo
2. Init and update git submodules

    cd notepadbot
    git submodule update --init --recursive
    ./setup-ant.sh

3. Import Project

   **Using Eclipse**

    Create a new Eclipse *workspace* in the root director of the repo.

    For each of the following directories, "Import -> Android -> Existing Android Code Into Workspace":

        external/cacheword/cachewordlib/

    Then, "Import -> General -> Existing Projects Into Workspace" for the `app/` directory.

## License and Credits 

* Notepad Icon originally from
    http://yaromanzarek.deviantart.com/art/iPhone-style-Notepad-icon-133822563

This project is licensed under the Apache version 2.0 license

Copyright (C) 2013 Abel Luck <abel@guardianproject.info>
Copyright (C) 2011-2013 Nathan Freitas <nathan@freitas.net>
Copyright (C) 2011 Hans-Christoph Steiner <hans@eds.org>
Copyright (C) 2008 The Android Open Source Project
Copyright (C) 2008 Google Inc.

