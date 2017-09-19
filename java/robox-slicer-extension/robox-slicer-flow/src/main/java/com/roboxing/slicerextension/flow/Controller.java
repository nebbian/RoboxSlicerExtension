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

import static com.roboxing.slicerextension.flow.Slicers.toSlicer;
import static com.roboxing.slicerextension.flow.utils.JSONConfiguration.getConfString;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.json.JSONObject;

import com.roboxing.slicerextension.flow.utils.JSONConfiguration;

/**
 * This class orchestrates flow:
 * <ul>
 * <li> invoking slicer to produce original gcode</li>
 * <li> invoking pre-processing script over original gcode (if present)</li>
 * <li> invoking processor which translates original gcode to AM compatible gcode</li>
 * <li> invoking post-processing script over AM compatible gcode (if present)</li>
 * </ul>
 *
 */
public class Controller {
    private static final Logger LOGGER = Logger.getLogger(Controller.class.getName());

    private Arguments arguments;
    private JSONObject configuration;

    public Controller(Arguments arguments, JSONObject configuration) {
        this.arguments = arguments;
        this.configuration = configuration;
    }

    public void process() throws IOException, InterruptedException {

        Path currentDir = Paths.get(".").toAbsolutePath().normalize();

        String slicerName;
        if (currentDir.toString().contains("PrintJobs")) {
            // Use configuration to get slicer's name. If configuration's key 'slicer' does not exist, default to Slic3r
            slicerName = getConfString(configuration, "slicer").orElse(Slicers.Slic3r.name());
            LOGGER.fine("Selecting " + slicerName + " - current dir contains 'PrintJobs'; " + currentDir.toString());
        } else {
            slicerName = Slicers.DefaultAMCura.name();
            LOGGER.fine("Selecting " + slicerName + " - current dir does not contain 'PrintJobs'; " + currentDir.toString());
        }

        // Fetch particular slicer's configuration (under key 'slicerXXX'), defaulting to empty JSON object if not present.
        JSONObject slicerConfig = JSONConfiguration.getJSONObject(configuration, "slicer" + slicerName).orElse(new JSONObject());

        // Instantiate slicer!
        Slicer selectedSlicer = toSlicer(slicerName, slicerConfig);

        selectedSlicer.setArguments(arguments);

        File slicerResultingGCode = new File(arguments.getOutputFile().getParentFile(), arguments.getOutputFile().getName().replace(".gcode", ".slicer.gcode"));

        long timeBeforeSlicing = System.currentTimeMillis();

        // Do we really need this?
        //        LOGGER.fine("Setting time to stl files...");
        //        touchSTLFiles(currentDir, timeBeforeSlicing);
        //        LOGGER.fine("Finished setting time.");

        LOGGER.fine("Invoking " + slicerName);
        selectedSlicer.invoke(slicerResultingGCode);
        LOGGER.fine("Finished slicing with " + slicerName);

        if (!slicerResultingGCode.exists()) {
            LOGGER.warning("Cannot find resulting gcode file named " + slicerResultingGCode.getName());
            LOGGER.fine("Searching for gcode...");
            findGCodeFile(selectedSlicer, currentDir, slicerResultingGCode, timeBeforeSlicing);
            LOGGER.info("Found another later gcode file named " + slicerResultingGCode.getName());
        }

        LOGGER.fine("Invoking post processing " + slicerName + "...");
        selectedSlicer.postProcess(slicerResultingGCode, arguments.getOutputFile());
        LOGGER.fine("Finished post processing " + slicerName);
    }

    //    private void touchSTLFiles(Path currentDir, long now) {
    //        File currentDirFile = currentDir.toFile();
    //        for (File f : currentDirFile.listFiles()) {
    //            if (f.getName().endsWith(".stl") || f.getName().endsWith(".obj")) {
    //                f.setLastModified(now);
    //            }
    //        }
    //    }

    private void findGCodeFile(Slicer selectedSlicer, Path currentDir, File slicerResultingGCode, long timeBeforeSlicing) {
        List<File> foundFiles = new ArrayList<>();
        File currentDirFile = currentDir.toFile();
        for (File f : currentDirFile.listFiles()) {
            if (f.getName().endsWith(".gcode") && f.lastModified() >= timeBeforeSlicing) {
                foundFiles.add(f);
            }
        }

        File foundFile = null;

        if (foundFiles.size() == 0) {
            // what now?!
            LOGGER.severe("Cannot gcode file, aborting.");
            System.exit(15); // TODO enumerate all exit codes somewhere!!!
        }
        if (foundFiles.size() > 0) {
            foundFile = selectedSlicer.chooseOneFile(foundFiles);
        } else {
            foundFile = foundFiles.get(0);
        }

        foundFile.renameTo(slicerResultingGCode);
    }
}
