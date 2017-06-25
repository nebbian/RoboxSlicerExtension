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
import java.util.logging.Logger;

import org.json.JSONObject;

/**
 * This is default AutoMaker Cura slicer implementation.
 *
 */
public class DefaultAMCura extends Slicer {
    private static final Logger LOGGER = Logger.getLogger(DefaultAMCura.class.getName());

    public DefaultAMCura(JSONObject slicerConfig) {
        super("DefaultAMCura", slicerConfig);
    }

    @Override
    public void invoke(File resultGCode) throws IOException, InterruptedException {
        OS os = OS.detect();
        File amInstallationDir = getArguments().getAMInstallationDir();
        File curaEngineOrig = new File(amInstallationDir, os.getCuraEngineOrigPath());

        LOGGER.fine("Invoking default slicer; " + curaEngineOrig.getAbsolutePath());

        String[] args = getArguments().getOriginalArguments();
        String[] commandAndArgs = new String[args.length + 1];
        commandAndArgs[0] = curaEngineOrig.getAbsolutePath();
        System.arraycopy(args, 0, commandAndArgs, 1, args.length);

        for (int i = 0; i < commandAndArgs.length; i++) {
            if (commandAndArgs[i].equals(getArguments().getOriginalOutputFileString())) {
                LOGGER.finest("Replaced " + commandAndArgs[i] + " with " + resultGCode.getAbsolutePath());
                commandAndArgs[i] = resultGCode.getAbsolutePath();
            }
        }
        LOGGER.fine("Invoking default Cura slicer with args: ");
        for (int i = 0; i < commandAndArgs.length; i++) {
            LOGGER.fine("  " + commandAndArgs[i]);
        }
        Process process = new ProcessBuilder(commandAndArgs).start();

        process.waitFor();
    }

    @Override
    public void postProcess(File slicerGCode, File resultGCode) throws IOException {
        if (!slicerGCode.renameTo(resultGCode)) {
            throw new IOException("Cannot rename '" + slicerGCode.getPath() + "' to '" + resultGCode.getPath() + "'");
        }
    }
}
