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
	private PVector p;				// Coordinates of the point.
    private PApplet parent;			// Parent sketch.


    /** Constructs a new point object with the given 2d geometry.
     *  @param x x coordinates of the point.
     *  @param y y coordinates of the point.
     *  @param parent The parent sketch.
     */
    public Point(float x, float y, PApplet parent)
    {
        this.parent = parent;
        p = new PVector(x,y);
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
    }
    
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
    	parent.point(screenCoord.x, screenCoord.y);
    }   
}
