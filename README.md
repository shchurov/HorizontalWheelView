# HorizontalWheelView
Custom view for user input that models horizontal wheel controller.
![preview](https://i.imgur.com/wWYbR8R.png)

![demo gif](http://i.giphy.com/vH1qSxcwBBOiQ.gif)

Integration
-------

Add jitpack.io repository to your root build.gradle:
```groovy
allprojects {
 repositories {
    jcenter()
    maven { url "https://jitpack.io" }
 }
}
```
Add the dependency to your module build.gradle:

`compile 'com.github.shchurov:horizontalwheelview:0.9.5'`

API
-------

Method | Description
--- | ---
`void setListener(Listener listener)` | Add a listener that will be invoked when the user interacts with the view
`void setRadiansAngle(double radians)` | Set the rotation angle in radians
`void setDegreesAngle(double degrees)` | Set the rotation angle in degrees
`void setCompleteTurnFraction(double fraction)` | Set the rotation angle in fraction, where 0f = 0°, 1.0f = 360°
`double getRadiansAngle()` | Get the rotation angle in radians (-2π, 2π)
`double getDegreesAngle()` | Get the rotation angle in degrees (-360°, 360°)
`double getCompleteTurnFraction()` | Get the roatation angle in fraction (0f, 1.0f), where 0f = 0°, 1.0f = 360°
`void setOnlyPositiveValues(boolean onlyPositiveValues)` | When true, all rotation getters return only positive values, in xml: `app:onlyPositiveValues`, default: false
`void setMarksCount(int marksCount)` | Set the total number of marks on the wheel, in xml: `app:marksCount`
`void setNormalColor(int color)` | Set the color of non-active marks, in xml: `app:normalColor`, default: ffffff
`void setActiveColor(int color)` | Set the color of active marks, in xml: `app:activeColor`, default: 54acf0
`void setShowActiveRange(boolean show)` | When true, all marks that satisfy the condition \|markAngle\| <= \|rotationAngle\| will be highlighted with the active color, in xml: `app:showActiveRange`, default: true
`void setEndLock(boolean lock)` | When true, it's not allowed to rotate the wheel past the edge values, default: false
`void setSnapToMarks(boolean snapToMarks)` | When true, user's rotations will snap to the marks, in xml: `app:snapToMarks`, default: false

License
-------

    Copyright 2016 Mykhailo Shchurov

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
