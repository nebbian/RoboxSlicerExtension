package com.roboxing.slicerextension.control;

import org.json.JSONObject;

import com.roboxing.slicerextension.control.utils.JSONConfiguration;

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

    public static Configuration fromJSON(JSONObject jsonConfig) {
        Configuration configuration = new Configuration();
        configuration.slicer = JSONConfiguration.getConfString(jsonConfig, "slicer").orElse("DefaultAMCura");

        return configuration;
    }

    public void updateControlWindow(ControlWindow controlWindow) {
        controlWindow.setSelectedSlicer(slicer);
    }
}
