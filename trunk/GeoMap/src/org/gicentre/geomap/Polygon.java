package org.gicentre.geomap;

import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PMatrix2D;
import processing.core.PVector;

//*****************************************************************************************
/** Class for drawing a polygon in screen coordinate space.
 *  @author Jo Wood and Iain Dillingham, giCentre, City University London.
 *  @version 1.1, 8th January, 2012
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

public class Polygon implements Feature
{
	// --------------------------------- Object variables ---------------------------------
	
    private Path2D.Float path;		// Internal representation of the polygon's geometry.
    private PApplet parent;			// Parent sketch.

    // ----------------------------------- Constructors -----------------------------------
    
    /** Constructs a new polygon object with the default geometry.
     *  @param parent The parent sketch.
     */
    public Polygon(PApplet parent)
    {
        this(null, null, parent);
    }

    /** Constructs a new polygon object with the given geometry.
     *  @param x x coordinates of the polygon.
     *  @param y y coordinates of the polygon.
     *  @param parent The parent sketch.
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
    
    // ------------------------------------- Methods -------------------------------------
    
    /** Adds a new part to the polygon. This allows complex polygons with islands, holes and multiple
     *  parts to be created.
     *  @param x x coordinates of the polygon.
     *  @param y y coordinates of the polygon.
     */
    public void addPart(float[] x, float[] y)
    {
    	if ((x != null) && (y != null) && (x.length == y.length))
        {
    		path.moveTo(x[0], y[0]);	
            for (int i=1; i<x.length; i++)
            {
                addPoint(x[i], y[i]);
            }
        }	
    }

    /** Draws the polygon in the parent sketch.
     *  @param transformer Class that handles the geographic to screen transformations.
     */
    public void draw(Geographic transformer)
    {
    	boolean containsGeom = false;
        PathIterator i = path.getPathIterator(new AffineTransform());
        float[] coords = new float[6];

        while (!i.isDone())
        {
            int segType = i.currentSegment(coords);
            
                        
            if (segType == PathIterator.SEG_MOVETO)
            {
            	if (containsGeom)
            	{
            		parent.endShape(PConstants.CLOSE);
            	}
            	parent.beginShape();
            	containsGeom = true;           	
            }
            
            if (segType == PathIterator.SEG_CLOSE)
            {
            	if (containsGeom)
            	{
            		parent.endShape(PConstants.CLOSE);
            		containsGeom = false;
            	}
            }
            
            if (containsGeom)
            {
            	PVector p = transformer.geoToScreen(coords[0], coords[1]);
            	parent.vertex(p.x,p.y);
            }
            
            i.next();
        }
        if (containsGeom)
        {
        	parent.endShape(PConstants.CLOSE);
        }
    }

    /** Tests whether the given point is contained within the polygon.
     *  @param x x coordinate in geographic coordinates.
     *  @param y y coordinate in geographic coordinates.
     *  @return True if the given point is contained within the polygon, false if not.
     */
    public boolean contains(float x, float y)
    {
        // Path coordinates may be subject to transformation on display, so
        // transform geometry before testing for containment.
        float[] m = ((PMatrix2D) parent.getMatrix()).get(new float[6]);
        AffineTransform affine = new AffineTransform(m[0], m[3], m[1], m[4], m[2], m[5]);
        return path.createTransformedShape(affine).contains(x, y);
    }

    /** Tests whether the given point is contained within the polygon.
     *  @param location of the point to test in geographic coordinates.
     *  @return True if the given point is contained within the polygon, false if not.
     */
    public boolean contains(PVector location)
    {
        return contains(location.x, location.y);
    }
    
    // --------------------------------------- Private methods --------------------------------------- 
    
    /** Adds a point to a new or the existing path.
     *  @param x x coordinate of the point to add.
     *  @param y y coordinate of the point to add.
     */
    private void addPoint(float x, float y)
    {
        if (path.getCurrentPoint() == null)
        {
            // New path.
            path.moveTo(x, y);
        } 
        else
        {
            // Existing path.
            path.lineTo(x, y);
        }
    }
}
