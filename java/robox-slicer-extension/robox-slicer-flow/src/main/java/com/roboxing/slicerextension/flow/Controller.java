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
        } else {
            slicerName = Slicers.DefaultAMCura.name();
        }

        // Fetch particular slicer's configuration (under key 'slicerXXX'), defaulting to empty JSON object if not present.
        JSONObject slicerConfig = JSONConfiguration.getJSONObject(configuration, "slicer" + slicerName).orElse(new JSONObject());

        // Instantiate slicer!
        Slicer slicer = toSlicer(slicerName, slicerConfig);

        slicer.setArguments(arguments);

        File slicerResultingGCode = new File(arguments.getOutputFile().getParentFile(), arguments.getOutputFile().getName() + ".slicer");

        slicer.invoke(slicerResultingGCode);

        slicer.postProcess(slicerResultingGCode, arguments.getOutputFile());
    }
}
