package org.gicentre.geomap;

import processing.core.PApplet;

/**
 * Class to draw a geographic map in Processing.
 * @author Iain Dillingham
 * @author Jo Wood
 */
public class GeoMap
{

    /**
     * The parent sketch.
     */
    private PApplet parent;
    /**
     * The minimum and maximum longitude values. These are mapped
     * (i.e. converted) to the x axis in screen coordinates.
     */
    private float minLon, maxLon;
    /**
     * The minimum and maximum latitude values. These are mapped
     * (i.e. converted) to the y axis in screen coordinates.
     */
    private float minLat, maxLat;
    /**
     * The bounds of the map in screen coordinates.
     */
    private float xOrigin, yOrigin, mapWidth, mapHeight;

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
    }
}
