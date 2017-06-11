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
package com.roboxing.slicerextension.flow.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Optional;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * This class loads JSON configuration file and provides some helper methods for reading configuration values.
 *
 */
public class JSONConfiguration {

   public static JSONObject readConfig(File configFile) throws JSONException, IOException {
       if (configFile.exists()) {
           return new JSONObject(new String(Files.readAllBytes(configFile.toPath())));
       } else {
           return new JSONObject();
       }
   }

   public static Optional<JSONObject> getJSONObject(JSONObject config, String name) {
       if (config.get(name) != null) {
           return Optional.of(config.getJSONObject(name));
       }
       return Optional.empty();
   }

   public static Optional<String> getConfString(JSONObject config, String name) {
       if (config.get(name) != null) {
           return Optional.of(config.getString(name));
       }
       return Optional.empty();
   }
}
