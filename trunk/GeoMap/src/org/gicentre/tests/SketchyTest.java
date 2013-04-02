package org.gicentre.tests;

import org.gicentre.geomap.DrawableFactory;
import org.gicentre.geomap.Feature;
import org.gicentre.geomap.FeatureType;
import org.gicentre.geomap.GeoMap;
import org.gicentre.handy.HandyRenderer;
import org.gicentre.utils.move.ZoomPan;

import processing.core.PApplet;
import processing.core.PVector;

//  ****************************************************************************************
/** Tests the Handy renderer plugin with a geoMap example.
 *  @author Jo Wood, giCentre, City University London.
 *  @version 1.1, 9th January, 2013.
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
public class SketchyTest extends PApplet
{
	// ------------------------------ Starter method ------------------------------- 

	/** Runs the sketch as an application.
	 *  @param args Command line arguments (ignored). 
	 */
	public static void main(String[] args)
	{   
		PApplet.main(new String[] {"org.gicentre.tests.SketchyTest"});
	}

	// ----------------------------- Object variables ------------------------------

	private GeoMap geoMap;				// Map feature data.
	private ZoomPan zoomer;				// For zooming and panning about the map
	private HandyRenderer handy;		// Hand drawn renderer.
	private boolean isHandy;			// Determines whether or not to use handy renderer.

	// ---------------------------- Processing methods -----------------------------

	/** Initialises the sketch.
	 */
	public void setup()
	{   
		size(800,400);
		//size(1700,900);
		smooth();
		zoomer = new ZoomPan(this);
		textFont(loadFont("ArchitectsDaughter-32.vlw"));
		isHandy = true;

		// Load US states as basemap.
		geoMap = new GeoMap(this);
		geoMap.readFile("usContinental");
		
		
		// Create a handy sketchy renderer and add it to each of the non-point features.
		handy = new HandyRenderer(this);
		handy.setIsHandy(isHandy);
		
		for (Feature feature : geoMap.getFeatures().values())
		{
			if (feature.getType() != FeatureType.POINT)
			{
				feature.setRenderer(DrawableFactory.createHandyRenderer(handy));
			}
		}
	}

	/** Draws the shapefile data in the sketch.
	 */
	public void draw()
	{   
		background(255);
		
		handy.setSeed(123);		// Comment this line out to introduce jittering on redraw.
		
		// Set sea drawing style.
		noStroke();
		float darken = 0.7f;
		fill(180*darken,210*darken,200,80);
		handy.setFillGap(1);
		handy.setFillWeight(6);
		handy.setBackgroundColour(color(255,1));
		handy.setSecondaryColour(color(50,50,90,10));
		handy.setUseSecondaryColour(true);
		handy.setHachurePerturbationAngle(0);
		handy.setHachureAngle(90);
		handy.rect(0,0,width,height);
		
		// Set land drawing style
		handy.setUseSecondaryColour(false);
		handy.setFillGap(4f);
		handy.setFillWeight(1.6f);
		handy.setBackgroundColour(color(255));
		handy.setHachurePerturbationAngle(4);
		handy.setHachureAngle(-49);
		
		pushMatrix();
		zoomer.transform();
		
		// Draw entire map.
		fill(150,190,150);
		strokeWeight(1.5f);
		stroke(0,100);
		geoMap.draw();

		// Allow mouse to highlight features.
		PVector zoomedCoords = zoomer.getMouseCoord();
		int id = geoMap.getID(zoomedCoords.x, zoomedCoords.y);
		String name = null;	
		if (id != -1)
		{
			fill(100,125,100);
			strokeWeight(2);
			geoMap.draw(id);
			
			// Feature name stored in column 3 (4th column) of the attribute table
			name = geoMap.getAttributes().getString(Integer.toString(id), 3);
			//name = geoMap.getAttributeAsString(Integer.toString(id),3);
		}
		
		popMatrix();
		
		// Display feature name if it has been selected.
		if (name != null)
		{
			fill(0,140);
			text(name,15,height-20);
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
	
	@Override
	public void keyPressed()
	{
		// Toggle the handy rendering.
		if (key == ' ') 
		{
			isHandy = !isHandy;
			handy.setIsHandy(isHandy);
			loop();
		}
	}
}