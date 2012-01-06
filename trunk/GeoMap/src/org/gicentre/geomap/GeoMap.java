package org.gicentre.geomap;

import java.util.HashMap;
import java.util.Map;
import processing.core.PApplet;

// *****************************************************************************************
/** Class for drawing geographic maps in Processing
 *  @author Iain Dillingham and Jo Wood, giCentre, City University London.
 *  @version 1.0, 6th January, 2012
 */
// *****************************************************************************************

/* This file is part of giCentre's geoMap library. geoMap is free software: you can
 * redistribute it and/or modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * geoMap is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * source code (see COPYING.LESSER included with this source code). If not, see
 * http://www.gnu.org/licenses/.
 */
public class GeoMap
{

    private PApplet parent;                                 // The parent sketch.
    private float minLon, maxLon;                           // The minimum and maximum longitude values. These are converted to the x axis in screen coordinates.
    private float minLat, maxLat;                           // Minimum and maximum latitude values. These are converted to the y axis in screen coordinates.
    private float xOrigin, yOrigin, mapWidth, mapHeight;    // The bounds of the map in screen coordinates.
    private Map<Integer, Feature> features;                 // The key/value pair for each feature.

    /**
     * Constructs a map with the default bounds.
     * @param parent The parent sketch.
     */
    public GeoMap(PApplet parent)
    {
        this(0, 0, parent.width, parent.height, parent);
    }

    /**
     * Constructs a map with the given bounds.
     * @param xOrigin The position of the map from the left of the sketch.
     * @param yOrigin The position of the map from the top of the sketch.
     * @param mapWidth The width of the map.
     * @param mapHeight The height of the map.
     * @param parent The parent sketch.
     */
    public GeoMap(float xOrigin, float yOrigin, float mapWidth, float mapHeight, PApplet parent)
    {
        this.parent = parent;
        this.xOrigin = xOrigin;
        this.yOrigin = yOrigin;
        this.mapWidth = mapWidth;
        this.mapHeight = mapHeight;
        this.features = new HashMap<Integer, Feature>();
    }

    /**
     * Reads geometry and attributes from a file.
     * @param fileName The name of the file.
     */
    public void readFile(String fileName)
    {
        String filePath = parent.dataPath(fileName);
    }

    /**
     * Draws the map in the parent sketch.
     */
    public void draw()
    {
        // Do nothing for the moment.
    }
}
