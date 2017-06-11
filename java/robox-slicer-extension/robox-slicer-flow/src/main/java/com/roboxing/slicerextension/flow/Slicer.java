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
 * This is base class for various slicers.
 *
 */
public abstract class Slicer {

    private String name;
    private Arguments args;
    private JSONObject slicerConfig;

    protected Slicer(String name, JSONObject slicerConfig) {
        this.name = name;
        this.slicerConfig = slicerConfig;
    }

    public String getName() {
        return name;
    }

    public void setArguments(Arguments args) {
        this.args = args;
    }

    public Arguments getArguments() {
        return args;
    }

    public JSONObject getSlicerConfig() {
        return slicerConfig;
    }

    public abstract void postProcess(File inputGCode, File resultGCode) throws IOException;

    public abstract void invoke(File resultGCode) throws IOException, InterruptedException;
}
