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
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.json.JSONObject;

/**
 * This is Slic3r slicer implementation.
 *
 */
public class Slic3r extends Slicer {
    private static final Logger LOGGER = Logger.getLogger(DefaultAMCura.class.getName());

    public Slic3r(JSONObject slicerConfig) {
        super("Slic3r", slicerConfig);
    }

    @Override
    public void invoke(File resultGCode) throws IOException, InterruptedException {
        List<String> args = new ArrayList<>();

        args.add(OS.detect().getDefaultSlicerPath());
        args.add("--gui");
        args.add("-o");
        args.add(resultGCode.getAbsolutePath());
        for (File f : getArguments().getInputFiles()) {
            args.add(f.getAbsolutePath());
        }

        LOGGER.fine("Invoking Slic3r with args: ");
        for (String a : args) {
            LOGGER.fine("  " + a);
        }
        Process process = new ProcessBuilder(args.toArray(new String[args.size()])).start();

        process.waitFor();
    }

    @Override
    public void postProcess(File inputGCode, File resultGCode) throws IOException {

        super.postProcess(inputGCode, resultGCode);
        System.out.println("total layers : " + layerCount);

    }


}

