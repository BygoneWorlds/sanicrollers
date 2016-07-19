# Sanic Rollers

Tools and an emulator for _Sonic Runners_, a mobile game by SEGA that is being shut down on July 27, 2016.

Tooling is all for the JVM, probably Kotlin but maybe also Scala.

## Building

    ./gradlew build

## Running

There are multiple subprojects. Some have runnables, others don't.

Currently, this will download a lot of assets from the CDN.

    ./gradlew :downloader:run

## License

Copyright (C) 2016 the authors. Available under the terms of the MIT License.