package org.gicentre.geomap;

import processing.core.PApplet;
import processing.core.PVector;

//*****************************************************************************************
/** Class for representing and drawing a point feature.
 *  @author Jo Wood , giCentre, City University London.
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

public class Point implements Feature
{
	// ---------------------------- Object and class variables ----------------------------
	
	private PVector p;				// Coordinates of the point.
    private PApplet parent;			// Parent sketch.
    private Drawable renderer;		// Renderer used for drawing feature in a non-defult style.
  
    private static float tolDistSq;	// Squared tolerance distance used for point matching.
    
    // ----------------------------------- Constructors -----------------------------------

    /** Constructs a new point object with the given 2d geometry.
     *  @param x x coordinates of the point.
     *  @param y y coordinates of the point.
     *  @param parent The parent sketch.
     */
    public Point(float x, float y, PApplet parent)
    {
        this.parent = parent;
        p = new PVector(x,y);
        tolDistSq = 0;
    }
    
    /** Constructs a new point object with the given 3d geometry.
     *  @param x x coordinates of the point.
     *  @param y y coordinates of the point.
     *  @param z z coordinates of the point.
     *  @param parent The parent sketch.
     */
    public Point(float x, float y, float z, PApplet parent)
    {
        this.parent = parent;
        p = new PVector(x,y,z);
        tolDistSq = 0;
    }
    
    // ------------------------------------- Methods -------------------------------------
    
    /** Reports the number of vertices that make up the point definition.
     *  @return number of vertices that make up the point. This will always be 1.
     */
    public int getNumVertices()
    {
    	return 1;
    }
    
    /** Report the type of feature (point).
	 *  @return Type of feature
	 */
	public FeatureType getType()
	{
		return FeatureType.POINT;
	}
	
	/** Reports the coordinates of the point feature.
	 *  @return Coordinates of the point feature.
	 */
	public PVector getCoords()
	{
		return p;
	}

    /** Draws the point in the parent sketch.
     *  @param transformer Class that handles the geographic to screen transformations.
     */
    public void draw(Geographic transformer)
    {
    	PVector screenCoord = transformer.geoToScreen(p.x, p.y);
    	if (renderer == null)
    	{
    		parent.point(screenCoord.x, screenCoord.y);
    	}
    	else
    	{
    		renderer.point(screenCoord.x,screenCoord.y);
    	}
    }   
    
    /** Sets the renderer to be used for drawing this feature. This need only be set if some
     *  non-default rendering is required (such as the sketchy rendering produced by the Handy 
     *  library).
     *  @param renderer New renderer to use or null if default rendering is to be used.
     */
    public void setRenderer(Drawable renderer)
    {
    	this.renderer = renderer;
    }
    
    /** Sets the tolerance values used for contains() testing. Any location within a distance of 
     *  the given tolerance of this point is considered to be at the same location. Note that
     *  this method is static, meaning that a single tolerance value is shared by all point objects.
     *  @param tolerance Tolerance distance in the same units as the point's coordinates.
     */
    public static void setTolerance(float tolerance)
    {
    	Point.tolDistSq = tolerance*tolerance;
    }
    
    /** Tests whether the given location matches this point. Coordinates should be in the
     *  same geographic units as the point feature.
     *  @param x x coordinate in geographic coordinates.
     *  @param y y coordinate in geographic coordinates.
     *  @return True if the given point matches this point, false if not.
     */
    public boolean contains(float x, float y)
    {
    	float distSq = (x-p.x)*(x-p.x) + (y-p.y)*(y-p.y);
    	if (distSq <= tolDistSq)
    	{
    		return true;
    	}
    	return false;
    }
}
