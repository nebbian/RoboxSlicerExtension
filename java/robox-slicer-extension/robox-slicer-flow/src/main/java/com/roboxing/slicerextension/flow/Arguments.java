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

import static java.util.Arrays.asList;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class Arguments {
    private static final Logger LOGGER = Logger.getLogger(Arguments.class.getName());

    private String slicer;

    private String[] originalArguments;

    private List<File> inputFiles = new ArrayList<>();

    private File outputFile;

    private String originalOutputFileString;

    private List<File> outputFiles = new ArrayList<>();

    private String roboxFile;

    private File amInstallationDir;

    public void process(String[] args) {
        this.originalArguments = args;
        List<String> arguments = asList(args);
        if (arguments.size() > 0 && arguments.get(0).startsWith("-v -p -c")) {
            String argsAll = arguments.get(0);
            // args are in one line need to split them
            arguments.remove(0);

            arguments.addAll(0, asList(argsAll));
        }
        LOGGER.fine("Invoked with arguments:");
        for (String a : arguments) {
            LOGGER.fine("  " + a);
        }
        int i = 0;
        while (i < arguments.size()) {
            String key = arguments.get(i);
            LOGGER.finer("Argument : " + key);
            i++;
            if (key.equals("-s")) {
                slicer = key;
            } else if (key.equals("-o") || key.equals("--output")) {
                if (i < arguments.size()) {
                    key = arguments.get(i);
                    i++;
                    originalOutputFileString = key;
                    outputFile = new File(key);
                    outputFiles.add(outputFile);
                }
            } else if (key.equals("-v")) {
                // Verbose output - ignore parameter
            } else if (key.equals("-p")) {
                // Show progress - ignore parameter
            } else if (key.equals("-c")) {
                if (i >= arguments.size()) {
                    System.err.println("Switch '-c' needs to be followed by file");
                } else {
                    roboxFile = arguments.get(i);
                    i++;
                }
            } else if (key.equals("--am-installation-dir")) {
                if (i < arguments.size()) {
                    key = arguments.get(i);
                    i++;
                    amInstallationDir = new File(key).getAbsoluteFile();
                    try {
                        amInstallationDir = amInstallationDir.getCanonicalFile();
                    } catch (IOException ignore) { }
                    LOGGER.config("Set AM installation dir to " + amInstallationDir);
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
        if (amInstallationDir ==  null) {
            try {
                File jarsFile = new File(getClass().getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
                File jarsDir = jarsFile.getParentFile().getAbsoluteFile();
                if ("Common".equalsIgnoreCase(jarsDir.getName())) {
                    amInstallationDir = jarsDir.getParentFile();
                    LOGGER.config("Assumed AM installation dir as " + jarsDir.getParentFile().getAbsolutePath());
                } else {
                    LOGGER.severe("Jar needs to be installed in AM's 'Common' dir or supply --am-installation-dir option with path to AM installation");
                    System.exit(1);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        if (outputFile == null/* && inputFiles.size() == 0*/) {
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
//                for (File file : folder.listFiles()) {
//                    System.out.println("File : " + file.getName());
//                    if (file.getName().endsWith(".orig")) {
//                        File gcodeFile = new File(file.getAbsolutePath().replace(".orig", ""));
//                        gcodeFile.delete();
//                        file.renameTo(new File(file.getAbsolutePath().replace(".orig", "")));
//                        break;
//                    }
//                }
                for (File file : folder.listFiles()) {
                    LOGGER.finer("File : " + file.getName());
                    if (file.getName().endsWith(".gcode") && !file.getName().endsWith("_robox.gcode")) {
                        //textFiles.add(file.getName());
                        //inputFiles.add(file);
                        //outputFile = new File(file.getAbsolutePath()+".orig");
                        //outputFiles.add(outputFile);
                        outputFile = file;
                        outputFiles.add(file);
                        break;
                    }
                    /*if (file.getName().endsWith(".stl")) {
                        //stl file
                        inputFiles.add(file);
                    }*/
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

    public List<File> getInputFiles() {
        return inputFiles;
    }

    public String getOriginalOutputFileString() {
        return originalOutputFileString;
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

    public File getAMInstallationDir() {
        return amInstallationDir;
    }
}
