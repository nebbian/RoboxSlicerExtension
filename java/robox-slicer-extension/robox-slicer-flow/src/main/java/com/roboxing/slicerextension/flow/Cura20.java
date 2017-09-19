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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONObject;

/**
 * This is Slic3r slicer implementation.
 *
 */
public class Cura20 extends Slicer {
    private static final Logger LOGGER = Logger.getLogger(DefaultAMCura.class.getName());

    public Cura20(JSONObject slicerConfig) {
        super("Cura20", slicerConfig);
    }

    @Override
    public void invoke(File resultGCode) throws IOException, InterruptedException {
        List<String> args = new ArrayList<>();
        args.add("/Applications/Cura.app/Contents/MacOS/cura");
        // args.add("-o");
        // args.add(resultGCode.getAbsolutePath());
        args.add("file");
        for (File f : getArguments().getInputFiles()) {
            args.add(f.getAbsolutePath());
        }

        LOGGER.fine("Invoking Cura20 with args: ");
        for (String a : args) {
            LOGGER.fine("  " + a);
        }
        String[] commandArray = args.toArray(new String[args.size()]);
        Process process = new ProcessBuilder(commandArray).start();

        // Wait to get exit value
        try {
            // process.waitFor();
            final int exitValue = process.waitFor();
            if (exitValue == 0) {
                LOGGER.fine("Successfully executed Cura20 with args: " + String.join(",", commandArray));
            } else {
                System.out.println("Failed to execute the following command: " + String.join(" ", commandArray) + " due to the following error(s):");
                try (final BufferedReader b = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                    String line;
                    if ((line = b.readLine()) != null) {
                        LOGGER.severe(line);
                    }
                } catch (final IOException e) {
                    LOGGER.log(Level.SEVERE, e.getMessage(), e);
                    e.printStackTrace();
                }
            }
        } catch (InterruptedException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            e.printStackTrace();
        }
        // process.waitFor();
    }

    @Override
    public void postProcess(File inputGCode, File resultGCode) throws IOException {
        // look for file in the printJob directory
        lookForNewGcodeFileToProcess(inputGCode);
        // isAbsoluteExtrusion = true;
        super.postProcess(inputGCode, resultGCode);
        System.out.println("total layers : " + layerCount);
    }

    private void lookForNewGcodeFileToProcess(File inputGCode) {
        Path currentDir = Paths.get(".").toAbsolutePath().normalize();
        if (!currentDir.toString().contains("PrintJobs")) {
            // If not in printJob dir abort operation
            return;
        }

        File dir = new File(currentDir.toString());

        // Look inside current folder
        for (File file : dir.listFiles()) {
            String fileName = file.getName();
            // System.out.println("File : " + fileName);
            LOGGER.finer("File : " + fileName);
            if (fileName.endsWith("-0.gcode")) {
                if (inputGCode.exists()) {
                    inputGCode.delete();
                }
                file.renameTo(inputGCode);
                return;
            }
        }
    }
}

