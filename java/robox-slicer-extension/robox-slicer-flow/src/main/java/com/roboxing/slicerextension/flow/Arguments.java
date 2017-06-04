package com.roboxing.slicerextension.flow;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Arguments {

    private String slicer;

    private String[] originalArguments;

    private List<File> inputFiles = new ArrayList<>();

    private File outputFile;

    private List<File> outputFiles = new ArrayList<>();

    private String roboxFile;

    public void process(String[] args) {
        this.originalArguments = args;
        int i = 0;
        while (i < args.length) {
            String key = args[i];
            i++;
            if (key.equals("-s")) {
                slicer = key;
            } else if (key.equals("-o") || key.equals("--output")) {
                outputFile = new File(key);
                outputFiles.add(outputFile);
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
                inputFiles.add(new File(key));
            } else {
                System.err.println("Unknown option '" + key + "'");
            }
        }
    }

    public String[] getOriginalArguments() {
        return originalArguments;
    }

    public String getSlicer() {
        return slicer;
    }

    public List<File> getInptFiles() {
        return inputFiles;
    }

    public File getOutputFile() {
        return outputFile;
    }

    public List<File> getOutputFiles() {
        return outputFiles;
    }

    public String getRoboxFile() {
        return roboxFile;
    }
}
