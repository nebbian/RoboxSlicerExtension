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
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Main class of robox slicer extension
 *
 */
public class Main {
    public static void main(String[] args) {

        Arguments arguments = new Arguments();
        arguments.process(args);

        Path currentDir = Paths.get(".").toAbsolutePath().normalize();


        File input = new File(arguments.getOutputFile());
        File output = new File(arguments.getOutputFile()+".orig");

        Slicer slicer;
        if (currentDir.toString().contains("PrintJobs")) {
            slicer = new Slic3r();
        } else {
            slicer = new DefaultAMCura();
        }
        slicer.setArguments(arguments);
        try {
            slicer.invoke();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            slicer.postProcess(input,output);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
