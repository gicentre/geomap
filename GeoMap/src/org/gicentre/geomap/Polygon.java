package org.gicentre.geomap;

import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import processing.core.PApplet;
import processing.core.PMatrix2D;
import processing.core.PVector;

//*****************************************************************************************
/** Class for drawing a polygon in screen coordinate space.
 *  @author Jo Wood and Iain Dillingham, giCentre, City University London.
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

public class Polygon
{

    private Path2D.Float path;		// Internal representation of the polygon's geometry.
    private PApplet parent;			// Parent sketch.

    /**
     * Constructs a new polygon object with the default geometry.
     * @param parent The parent sketch.
     */
    public Polygon(PApplet parent)
    {
        this(null, null, parent);
    }

    /**
     * Constructs a new polygon object with the given geometry.
     * @param x x coordinate.
     * @param y y coordinate.
     * @param parent The parent sketch.
     */
    public Polygon(float[] x, float[] y, PApplet parent)
    {
        this.parent = parent;
        path = new Path2D.Float();

        if ((x != null) && (y != null) && (x.length == y.length))
        {
            for (int i = 0; i < x.length; i++)
            {
                addPoint(x[i], y[i]);
            }
        }
    }

    /**
     * Adds a point to a new or the existing path.
     * @param x x coordinate.
     * @param y y coordinate.
     */
    private void addPoint(float x, float y)
    {
        if (path.getCurrentPoint() == null)
        {
            // New path.
            path.moveTo(x, y);
        } else
        {
            // Existing path.
            path.lineTo(x, y);
        }
    }

    /**
     * Draws the polygon in the parent sketch.
     */
    public void draw()
    {
        parent.beginShape();

        PathIterator i = path.getPathIterator(new AffineTransform());
        float[] coords = new float[6];

        while (!i.isDone())
        {
            int segType = i.currentSegment(coords);
            parent.vertex(coords[0], coords[1]);
            i.next();
        }

        parent.endShape(PApplet.CLOSE);
    }

    /**
     * Tests whether the given point is contained within the polygon.
     * @param x x coordinate
     * @param y y coordinate
     * @return True if the given point is contained within the polygon, false if not.
     */
    public boolean contains(float x, float y)
    {
        // Path coordinates may be subject to transformation on display, so
        // transform geometry before testing for containment.
        float[] m = ((PMatrix2D) parent.getMatrix()).get(new float[6]);
        AffineTransform affine = new AffineTransform(m[0], m[3], m[1], m[4], m[2], m[5]);
        return path.createTransformedShape(affine).contains(x, y);
    }

    /**
     * Tests whether the given point is contained within the polygon.
     * @param vector x and y coordinates.
     * @return True if the given point is contained within the polygon, false if not.
     */
    public boolean contains(PVector vector)
    {
        return contains(vector.x, vector.y);
    }
}
