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

import org.json.JSONException;
import org.json.JSONObject;

import com.roboxing.slicerextension.flow.utils.JSONConfiguration;

/**
 * Main class of robox slicer extension
 *
 */
public class Main {
    public static void main(String[] args) throws JSONException, IOException {

        Arguments arguments = new Arguments();
        arguments.process(args);

        File configFile = new File(new File(OS.detect().getRoboxFolder()), ".slicerextension.config");
        JSONObject configuration = JSONConfiguration.readConfig(configFile);

        Controller controller = new Controller(arguments, configuration);
        try {
            controller.process();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
