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

import org.json.JSONObject;

/**
 * This is default AutoMaker Cura slicer implementation.
 *
 */
public class DefaultAMCura extends Slicer {

    public DefaultAMCura(JSONObject slicerConfig) {
        super("DefaultAMCura", slicerConfig);
    }

    @Override
    public void postProcess(File slicerGCode, File resultGCode) throws IOException {
        if (!slicerGCode.renameTo(resultGCode)) {
            throw new IOException("Cannot rename '" + slicerGCode.getPath() + "' to '" + resultGCode.getPath() + "'");
        }
    }

    @Override
    public void invoke(File resultGCode) throws IOException, InterruptedException {
        OS os = OS.detect();

        String[] args = getArguments().getOriginalArguments();
        String[] commandAndArgs = new String[args.length + 1];
        commandAndArgs[0] = os.getCuraEngineOrigPath();
        System.arraycopy(args, 0, commandAndArgs, 1, args.length);

        Process process = new ProcessBuilder(commandAndArgs).start();

        process.waitFor();
    }
}
