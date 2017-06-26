# Robox Slicer Extension

This project is aimed at giving Robox users the option of using different slicers, apart from the default Cura Engine.

## Getting Started

These instructions will allow you to run Slic3r from within AutoMaker.

### Prerequisites

You must be using Mac OS X, have a copy of AutoMaker, and a copy of [Slic3r](https://github.com/prusa3d/Slic3r/releases).

You must also note that __using this software may void your Robox warranty.__


### Installing

* Download and unzip this repository.

* Rename the CuraEngine within the Common/Cura directory to CuraEngine_exe
```
mv /Applications/CEL/Common/Cura/CuraEngine /Applications/CEL/Common/Cura/CuraEngine_exe
```

* Copy the files in shell scripts to /Applications/CEL/Common/Cura/

```
cp shell\ scripts/CuraEngine /Applications/CEL/Common/Cura/
cp shell\ scripts/slic3r_postprocess.pl /Applications/CEL/Common/Cura/
```

* Open Slic3r, and choose "Load Config Bundle..." from the File menu.
* Choose "Robox_slic3r_config_bundle.ini" from the slic3r/config directory.

You should now see that you have some new presets for Print Settings, Filament Settings, and Printer Settings, all of which have "Robox" in the name. 

* Close slic3r

### Alternative Installation Method

Download [robox-extensions-installer.jar](robox-extensions-installer.jar) and run it with
```
java -jar robox-extensions-installer.jar
```
and it will do steps from above for you:
- copy CuraEngine to CuraEngine_orig (if not already exist)
- download CuraEngine and slic3r_postprocess.pl to the right paths
- and download Robox_slic3r_config_bundle.ini to <AM Install Dir>/Slic3r/Config for your convenience

All you need to do is last step from previous chapter to select Slic3r's configuration.

### Usage

To use the new slicing engine, open AutoMaker, and load an STL to print.  

Click "To Settings", and then click "Make".

Slic3r will now open.  Ensure that your Robox settings are chosen for Print settings, Filament Settings, and Printer Settings.

Change the slicing parameters, choose Preview to see how it will look once printed, and ensure that you're happy with the layout.

Choose "Export G-code..." from the Plater window, this will automatically export the code to the right place.

Now close Slic3r by pressing the red close button on the top left corner of the window.


Automaker will now postprocess the code, and start the print.

You can see the process here:
[![Using Slic3r with Automaker](http://img.youtube.com/vi/5YmH0T2vJbY/0.jpg)](http://www.youtube.com/watch?v=5YmH0T2vJbY "Automaker with slic3r on a Robox Dual")

### Caveats

This code should be considered pre-alpha, it hasn't been tested extensively.  Use at your own risk.

### Known issues

* The current version of Slic3r [has a bug when using multiple STL files](https://github.com/prusa3d/Slic3r/issues/313). 

### Authors

* **Ben Hitchcock** - *Initial work*
* **Daniel Sendula** - *Installer functionality*


### License

This project is licensed under the GNU GPL v3.0 License - see the [LICENSE.txt](LICENSE.txt) file for details
