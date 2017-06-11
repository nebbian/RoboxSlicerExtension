/*
 * This file is part of Robox Slicer Extension.
 *
 * Robox Slicer Extension is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Robox Slicer Extension is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Robox Slicer Extension.  If not, see <http://www.gnu.org/licenses/>.
 *
*/
package com.roboxing.slicerextension.flow;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Arguments {

    private static final Logger LOGGER = Logger.getLogger(Arguments.class.getName());

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
            LOGGER.log(Level.FINE, "Argument : "+key);
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

        ensureArgumentsValid();
    }

    private void ensureArgumentsValid() {
        if (outputFile != null) {
            // Debug mode - values if no input/output arguments set
            String userCelRoboxPath = System.getProperty("user.home") + "/CEL Robox/";

            File dir = new File(userCelRoboxPath + "PrintJobs");

            // Set printJob as currentDir
            System.setProperty("user.dir", dir.getAbsolutePath());
            // Look inside first folder
            for (File folder : dir.listFiles()) {
                if (!folder.isDirectory()) {
                    continue;
                }

                // Rename orig if there is already one
                for (File file : folder.listFiles()) {
                    System.out.println("File : " + file.getName());
                    if (file.getName().endsWith(".orig")) {
                        File gcodeFIle = new File(file.getAbsolutePath().replace(".orig", ""));
                        gcodeFIle.delete();
                        file.renameTo(new File(file.getAbsolutePath().replace(".orig","")));
                        break;
                    }
                }
                for (File file : folder.listFiles()) {
                    System.out.println("File : " + file.getName());
                    if (file.getName().endsWith(".gcode") && !file.getName().endsWith("_robox.gcode")) {
                        //textFiles.add(file.getName());
                        inputFiles.add(file);
                        outputFile = new File(file.getAbsolutePath()+".orig");
                        outputFiles.add(outputFile);
                        break;
                    }
                }
                //stop at first valid folder
                break;
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
