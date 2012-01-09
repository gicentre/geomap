package org.gicentre.tests;

import org.gicentre.geomap.GeoMap;
import org.gicentre.geomap.Table;

import processing.core.PApplet;

//  ****************************************************************************************
/** Tests shapefile reading into geoMap objects.
 *  @author Jo Wood, giCentre, City University London.
 *  @version 1.0, 9th January, 2012.
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
    
    // ---------------------------- Processing methods -----------------------------

    /** Initialises the sketch.
     */
    public void setup()
    {   
        size(800,600);
        smooth();
        
        geoMap = new GeoMap(this);
        geoMap.readFile("greaterLondonWardBoundaries");                
    }

    /** Draws the shapefile data in the sketch.
     */
    public void draw()
    {   
        background(255);
        fill(180,120,120,100);
        strokeWeight(0.3f);
        geoMap.draw();
        
        noLoop();
    }    
}