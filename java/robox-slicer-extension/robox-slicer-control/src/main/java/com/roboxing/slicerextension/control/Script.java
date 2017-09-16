package com.roboxing.slicerextension.control;

public class Script {

    private String label;

    protected Script() {
    }

    public Script(String label) {
        this.label = label;
    }

    protected void setLabel(String label) { this.label = label; }

    public String getLabel() { return label; }

}
