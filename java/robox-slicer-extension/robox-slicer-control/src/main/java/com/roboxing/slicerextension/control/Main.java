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
package com.roboxing.slicerextension.control;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.json.JSONObject;

/**
 * Main class
 *
 */
public class Main {
    public static void main(String[] args) {
        final ControlWindow controlWindow = new ControlWindow();

        controlWindow.setLeaveAction(() -> System.exit(0));

        controlWindow.setPreProcessorScriptPathChanged(path -> {
            File file = new File(path).getAbsoluteFile();
            if (!file.exists()) {
                controlWindow.setPreProcessorScriptPathError("Path does not exist");
            } else {
                controlWindow.setPreProcessorScriptPathError("");
            }
            updateSaveButton(controlWindow);
        });

        controlWindow.setPostProcessorScriptPathChanged(path -> {
            File file = new File(path).getAbsoluteFile();
            if (!file.exists()) {
                controlWindow.setPostProcessorScriptPathError("Path does not exist");
            } else {
                controlWindow.setPostProcessorScriptPathError("");
            }
            updateSaveButton(controlWindow);
        });

        controlWindow.setSaveAction(() -> save(controlWindow));
        controlWindow.setVisible(true);
    }

    private static void save(ControlWindow controlWindow) {
        Configuration configuration = Configuration.fromControlWindow(controlWindow);

        File configFile = new File(new File(OS.detect().getRoboxFolder()), ".slicerextension.config");
        JSONObject jsonConfig = new JSONObject(configuration);

        String configContents = jsonConfig.toString(2);
        System.out.println(configContents);

        try (FileWriter writer = new FileWriter(configFile)) {
            writer.write(configContents);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        System.exit(0);
    }

    private static void updateSaveButton(ControlWindow controlWindow) {
        File preProcessorScriptFile = new File(controlWindow.getPreProcessorScriptPath()).getAbsoluteFile();
        File postProcessorScriptFile = new File(controlWindow.getPostProcessorScriptPath()).getAbsoluteFile();
        controlWindow.setSaveButtonEnable(preProcessorScriptFile.exists() && postProcessorScriptFile.exists());
    }

}
