package com.roboxing.slicerextension.flow;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Arguments {

    private static final Logger LOGGER = Logger.getLogger(Arguments.class.getName());

    private String slicer;

    private List<String> inputFiles = new ArrayList<>();

    private String outputFile;

    private List<String> outputFiles = new ArrayList<>();

    private String roboxFile;

    public void process(String[] args) {
        int i = 0;
        while (i < args.length) {
            String key = args[i];
            LOGGER.log(Level.FINE, "Argument : "+key);
            i++;
            if (key.equals("-s")) {
                slicer = key;
            } else if (key.equals("-o") || key.equals("--output")) {
                outputFile = key;
                outputFiles.add(key);
            } else if (key.equals("-v")) {
                // Verbose output - ignore parameter
            } else if (key.equals("-p")) {
                // Show progress - ignore parameter
            } else if (key.equals("-c")) {
                if (i >= args.length) {
                    System.err.println("Switch '-c' needs to be followed by file");
                } else {
                    roboxFile = args[i];
                    i++;
                }
            } else if (key.endsWith("stl") || key.endsWith("obj")) {
                inputFiles.add(key);
            } else {
                System.err.println("Unknown option '" + key + "'");
            }
        }
    }

    public String getSlicer() {
        return slicer;
    }

    public List<String> getInptFiles() {
        return inputFiles;
    }

    public String getOutputFile() {
        return outputFile;
    }

    public List<String> getOutputFiles() {
        return outputFiles;
    }

    public String getRoboxFile() {
        return roboxFile;
    }
}
