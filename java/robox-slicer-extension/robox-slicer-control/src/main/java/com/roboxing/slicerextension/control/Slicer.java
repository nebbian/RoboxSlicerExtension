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

public class Slicer {

    private String label;
    private boolean enabled;

    protected Slicer() {
    }

    public Slicer(String label, boolean enabled) {
        this.label = label;
        this.enabled = enabled;
    }

    protected void setLabel(String label) { this.label = label; }

    public String getLabel() { return label; }

    protected void setEnabled(boolean enabled) { this.enabled = enabled; }

    public boolean isEnabled() { return enabled; }

}
