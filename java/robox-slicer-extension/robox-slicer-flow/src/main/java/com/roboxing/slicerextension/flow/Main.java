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
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.json.JSONException;
import org.json.JSONObject;

import com.roboxing.slicerextension.flow.utils.JSONConfiguration;

/**
 * Main class of robox slicer extension
 *
 */
public class Main {

    public static void main(String[] args) throws JSONException, IOException {

        File celFolder = new File(OS.detect().getRoboxFolder());
        File logFile = new File(celFolder, "robox-slicer-extension.log");

        System.setProperty("java.util.logging.SimpleFormatter.format", "%1$tH:%1$tM:%1$tS.%1$tL %4$-7s [%2$-15s] %5$s %6$s%n");
        Logger rootLogger = Logger.getLogger("");
        rootLogger.setLevel(Level.ALL);
        FileHandler fileLog = new FileHandler(logFile.getAbsolutePath());
        ConsoleHandler consoleHandler = new ConsoleHandler();

        SimpleFormatter formatterLog = new SimpleFormatter();
        fileLog.setFormatter(formatterLog);
        consoleHandler.setFormatter(formatterLog);
        rootLogger.addHandler(fileLog);
        rootLogger.addHandler(consoleHandler);

        System.out.println("Logging to " + logFile.getAbsolutePath());
        rootLogger.info("Started robox slicer extension");

        Arguments arguments = new Arguments();
        arguments.process(args);

        File configFile = new File(celFolder, ".slicerextension.config");
        JSONObject configuration = JSONConfiguration.readConfig(configFile);

        Controller controller = new Controller(arguments, configuration);
        try {
            controller.process();
            Logger LOGGER = Logger.getLogger(Main.class.getName());
            LOGGER.info("Finished slicing.");
            System.exit(0);
        } catch (Exception e) {
            Logger LOGGER = Logger.getLogger(Main.class.getName());
            LOGGER.log(Level.SEVERE, "Error processing : ", e);
            System.exit(1);
        }
    }
}
