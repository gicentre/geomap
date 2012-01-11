package org.gicentre.tests;

import org.gicentre.geomap.GeoMap;
import org.gicentre.utils.move.ZoomPan;

import processing.core.PApplet;
import processing.core.PVector;

//  ****************************************************************************************
/** Tests shapefile reading into geoMap objects, query of the attribute file and mouse-based
 *  spatial query.
 *  @author Jo Wood, giCentre, City University London.
 *  @version 1.0, 11th January, 2012.
 */ 
//  ****************************************************************************************

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

@SuppressWarnings("serial")
public class ShapefileTest extends PApplet
{
	// ------------------------------ Starter method ------------------------------- 

	/** Runs the sketch as an application.
	 *  @param args Command line arguments (ignored). 
	 */
	public static void main(String[] args)
	{   
		PApplet.main(new String[] {"org.gicentre.tests.ShapefileTest"});
	}

	// ----------------------------- Object variables ------------------------------

	private GeoMap geoMap;
	private ZoomPan zoomer;

	// ---------------------------- Processing methods -----------------------------

	/** Initialises the sketch.
	 */
	public void setup()
	{   
		size(800,400);
		smooth();
		zoomer = new ZoomPan(this);

		geoMap = new GeoMap(this);
		geoMap.readFile("world");

		geoMap.getAttributes().writeAsTable(5);

	}

	/** Draws the shapefile data in the sketch.
	 */
	public void draw()
	{   
		background(180,210,240);
		zoomer.transform();
		fill(150,190,150);
		strokeWeight(0.1f);
		stroke(0,100);
		geoMap.draw();

		// Highlight Indonesia to test for multi-part polygons.
		fill(180,120,120);
		geoMap.draw("Indonesia",3);

		// Allow mouse to highlight countries.
		PVector zoomedCoords = zoomer.getMouseCoord();
		int id = geoMap.getID(zoomedCoords.x, zoomedCoords.y);
		
		if (id != -1)
		{
			fill(180,20,20);
			strokeWeight(1);
			geoMap.draw(id);
		}
	}
}