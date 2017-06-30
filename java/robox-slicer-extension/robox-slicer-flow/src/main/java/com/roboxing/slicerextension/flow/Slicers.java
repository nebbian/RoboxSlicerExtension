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

import java.lang.reflect.Constructor;

import org.json.JSONObject;

/**
 * Convenient ENUM to slicer implementation class.
 *
 */
public enum Slicers {
    DefaultAMCura,
    Slic3r,
    Cura20;

    public static Slicer toSlicer(String selected, JSONObject slicerConfig) {
        return toSlicer(Slicers.valueOf(selected), slicerConfig);
    }

    public static Slicer toSlicer(Slicers selected, JSONObject slicerConfig) {
        String className = Slicers.class.getPackage().getName() + "." + selected.name();
        try {
            @SuppressWarnings("unchecked")
            Class<Slicer> slicerClass = (Class<Slicer>)Class.forName(className);
            Constructor<Slicer> constructor = slicerClass.getConstructor(JSONObject.class);
            return constructor.newInstance(slicerConfig);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
