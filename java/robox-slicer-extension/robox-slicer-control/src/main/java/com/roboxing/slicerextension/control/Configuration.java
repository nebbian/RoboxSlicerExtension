package com.roboxing.slicerextension.control;

public class Configuration {

    private String slicer;

    public String getSlicer() {
        return slicer;
    }

    public void setSlicer(String slicer) {
        this.slicer = slicer;
    }

    public static Configuration fromControlWindow(ControlWindow controlWindow) {
        Configuration configuration = new Configuration();

        configuration.setSlicer(controlWindow.getSelectedSlicer().getConfigString());

        return configuration;
    }
}
