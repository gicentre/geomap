package org.gicentre.geomap;

import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.util.ArrayList;

import org.gicentre.handy.HandyRenderer;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PVector;

//*****************************************************************************************
/** Class for drawing a polygon in screen coordinate space.
 *  @author Jo Wood and Iain Dillingham, giCentre, City University London.
 *  @version 1.2, 29th October, 2013.
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
    private int numVertices;		// Number of vertices that make up the polygon (including parts).
    private ArrayList<Integer>subPartPointers;
    private Drawable renderer;		// Alternative renderer for sketchy graphics and other styles.

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
        renderer = null;
        path = new Path2D.Float();
        numVertices = 0;
        subPartPointers = new ArrayList<Integer>();

        if ((x != null) && (y != null) && (x.length == y.length))
        {
            for (int i = 0; i < x.length; i++)
            {
                addPoint(x[i], y[i]);
            }
            numVertices = x.length;
            subPartPointers.add(new Integer(0));
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
    		subPartPointers.add(new Integer(numVertices));
    		 
    		path.moveTo(x[0], y[0]);	
            for (int i=1; i<x.length; i++)
            {
                addPoint(x[i], y[i]);
            }
            numVertices += x.length;
        }	
    }
    
    /** Sets the renderer to be used for drawing this feature. This need only be set if some non-default
     *  rendering is required (such as the sketchy rendering produced by the Handy library).
     *  @param renderer New renderer to use or null if default rendering is to be used.
     */
    public void setRenderer(Drawable renderer)
    {
    	this.renderer = renderer;
    }

    /** Draws the polygon in the parent sketch.
     *  @param transformer Class that handles the geographic to screen transformations.
     */
    @SuppressWarnings("null")
	public void draw(Geographic transformer)
    {
    	if (renderer == null)
    	{
    		drawDefault(transformer);
    		return;
    	}
    	
    	// This will draw the feature using the stored renderer (e.g. sketchy graphics).
    	ArrayList<Float>x=null,y=null;
    	
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
            		renderer.shape(HandyRenderer.toArray(x),HandyRenderer.toArray(y));
            	}
            	x = new ArrayList<Float>();
            	y = new ArrayList<Float>();
            	containsGeom = true;           	
            }
            
            if (segType == PathIterator.SEG_CLOSE)
            {
            	if (containsGeom)
            	{
            		renderer.shape(HandyRenderer.toArray(x),HandyRenderer.toArray(y));
            		containsGeom = false;
            	}
            }
            
            if (containsGeom)
            {
            	PVector p = transformer.geoToScreen(coords[0], coords[1]);
            	x.add(new Float(p.x));
            	y.add(new Float(p.y));
            }
            
            i.next();
        }
        if (containsGeom)
        {
        	renderer.shape(HandyRenderer.toArray(x),HandyRenderer.toArray(y));
        }
    }  
    
    /** Reports the number of vertices that make up the polygon feature.
     *  @return number of vertices that make up the polygon.
     */
    public int getNumVertices()
    {
    	return numVertices;
    }
    
    /** Reports pointers to the vertex index for each of the partss that make up the polygon feature.
     *  Simple polygons have one part with a vertex index of 0. Complex polygons can comprise many parts
     *  such as islands, holes etc. The position in the list of coordinates returned by getXcoord()s and
     *  getYCoords() of the start of each part is returned here.
     *  @return Pointers to the start of each polygon part.
     */
    public ArrayList<Integer> getSubPartPointers()
    {
    	return subPartPointers;
    }
    
    /** Reports the x coordinates coordinates of the polygon feature. This includes the coordinates for
     *  all parts in multi-part polygons. To break down the coordinates into parts, call getSubPartPointers()
     *  to find the position in the array corresponding to the start of each part.
	 *  @return x coordinates of the polygon feature.
	 */
	public float[] getXCoords()
	{
		float[] x = new float[numVertices];
		
		boolean containsGeom = false;
        PathIterator i = path.getPathIterator(new AffineTransform());
        float[] coords = new float[6];
        int counter=0;

        while (!i.isDone())
        {
            int segType = i.currentSegment(coords);
                        
            if (segType == PathIterator.SEG_MOVETO)
            {
            	containsGeom = true;           	
            }
            if (segType == PathIterator.SEG_CLOSE)
            {
            	if (containsGeom)
            	{
            		containsGeom = false;
            	}
            }
            
            if (containsGeom)
            {
            	x[counter++] = coords[0];
            }
            i.next();
        }
		return x;
	}
	
	/** Reports the y coordinates coordinates of the polygon feature. This includes the coordinates for
     *  all parts in multi-part polygons. To break down the coordinates into parts, call getSubPartPointers()
     *  to find the position in the array corresponding to the start of each part.
	 *  @return y coordinates of the polygon feature.
	 */
	public float[] getYCoords()
	{
		float[] y = new float[numVertices];
		
		boolean containsGeom = false;
        PathIterator i = path.getPathIterator(new AffineTransform());
        float[] coords = new float[6];
        int counter=0;

        while (!i.isDone())
        {
            int segType = i.currentSegment(coords);
                        
            if (segType == PathIterator.SEG_MOVETO)
            {
            	containsGeom = true;           	
            }
            if (segType == PathIterator.SEG_CLOSE)
            {
            	if (containsGeom)
            	{
            		containsGeom = false;
            	}
            }
            
            if (containsGeom)
            {
            	y[counter++] = coords[1];
            }
            i.next();
        }
		return y;
	}
    
    /** Report the type of feature (polygon).
	 *  @return Type of feature
	 */
	public FeatureType getType()
	{
		return FeatureType.POLYGON;
	}

    /** Tests whether the given point is contained within the polygon.
     *  @param geoX x coordinate in geographic coordinates.
     *  @param geoY y coordinate in geographic coordinates.
     *  @return True if the given point is contained within the polygon, false if not.
     */
    public boolean contains(float geoX, float geoY)
    {
        // Path coordinates may be subject to transformation on display, so
        // transform geometry before testing for containment.
        //float[] m = ((PMatrix2D) parent.getMatrix()).get(new float[6]);
        //AffineTransform affine = new AffineTransform(m[0], m[3], m[1], m[4], m[2], m[5]);
        //return path.createTransformedShape(affine).contains(x, y);
    	
    	// The code above is no longer needed since the comparison is calculated in geographic units. 
    	return path.contains(geoX, geoY);
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
    
    /** Draws the polygon in the parent sketch using the default rendering style from the parent sketch.
     *  @param transformer Class that handles the geographic to screen transformations.
     */
    private void drawDefault(Geographic transformer)
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
}
