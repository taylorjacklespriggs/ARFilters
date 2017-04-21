## Synopsis

ARFilters is an Android implementation of real-time camera operations for Android VR. Just acquire a Google Cardboard viewer or some other VR viewer for Android and your vision should be augmented with shaders in real-time provided your hardware is fast enough. Note that you may need to modify your viewer in order to ensure that the rear-facing camera is not obstructed.

## Motivation

This project was developed by Taylor Jackle Spriggs for his honours project credit at Carleton University.

## Installation

Import the project into Android Studio. Gradle will install all of the dependencies.

## Usage

Use the Cardboard trigger or tap the screen to swap image operations. The image operations in order are
* Pass through
* Hue rotation
* Daltonize protanope
* Daltonize deuteranope
* Daltonize tritanope
* Inverted colours
* Grey-scale edges
* Per-channel edges
* Per-channel grey-scale edges
* Linear zoom
* Nonlinear zoom
* Anaglyph
* Night vision
* Global linear contrast adjustment
* Global histogram equalization
* Windowed histogram equalization
* Local linear contrast adjustment
* Cartoon effect

## Testing

This application has only been tested on the Sony Xperia Z5 with Google Cardboard V1. The magnetic Cardboard trigger does not work with this device so the screen must be tapped in order to switch operations.

## License

Copyright (C) 2017  Taylor Jackle Spriggs

This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or any later version.

This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details. <http://www.gnu.org/licenses/>