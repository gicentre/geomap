package org.gicentre.tests;

import org.gicentre.geomap.DrawableFactory;
import org.gicentre.geomap.Feature;
import org.gicentre.geomap.FeatureType;
import org.gicentre.geomap.GeoMap;
import org.gicentre.geomap.Polygon;
import org.gicentre.handy.HandyRenderer;
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
		textFont(createFont("Sans-serif", 20));

		geoMap = new GeoMap(this);
		//geoMap.readFile("world");
		geoMap.readFile("usContinental");
		
		/* TEMPORARY: To test sketchy rendering.
		textFont(createFont("YWFTColtrane", 20));
		HandyRenderer handy = new HandyRenderer(this);
		handy.setFillGap(2);
		handy.setFillWeight(0.5f);
		for (Feature feature : geoMap.getFeatures().values())
		{
			if (feature.getType() != FeatureType.POINT)
			{
				feature.setRenderer(DrawableFactory.createHandyRenderer(handy));
			}
		}
		---- End of TEMPORARY section */
		
		geoMap.getAttributes().writeAsTable(5);

	}

	/** Draws the shapefile data in the sketch.
	 */
	public void draw()
	{   
		background(180,210,240);
		pushMatrix();
		zoomer.transform();
		
		// Draw entire map.
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
		String name = null;	
		if (id != -1)
		{
			fill(120,150,120);
			strokeWeight(1);
			geoMap.draw(id);
			
			// Full country name stored in column 3 (4th column) of the attribute table
			name = geoMap.getAttributes().getString(Integer.toString(id), 3);
		}
		
		popMatrix();
		
		// Display country name if it has been selected.
		if (name != null)
		{
			fill(0,200);
			text(name,15,20);
		}
	
		// Don't redraw unless instructed to do so.
		noLoop();
	}
	
	/** Updates the display whenever the mouse is moved.
	 */
	@Override
	public void mouseMoved()
	{
		loop();
	}
	
	/** Updates the display whenever the mouse is dragged
	 */
	@Override
	public void mouseDragged()
	{
		loop();
	}
}