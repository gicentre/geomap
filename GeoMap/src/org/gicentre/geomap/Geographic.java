package org.gicentre.geomap;

import processing.core.PVector;

//*****************************************************************************************
/** Identifies the behaviour of a geographic object that can be drawn on screen.
 *  @author Jo Wood, giCentre, City University London.
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
public interface Geographic 
{
	/** Should provide the screen coordinates corresponding to the given geographic coordinates.
	 * @param geoX Geographic x coordinate.
	 * @param geoY Geographic y coordinate.
	 * @return Screen coordinate representation of the given geographic location.
	 */
	public abstract PVector geoToScreen(float geoX, float geoY);
	
	/** Should provide the geographic coordinates corresponding to the given screen coordinates.
	 *  @param screenX Screen x coordinate.
	 *  @param screenY Screen y coordinate.
	 *  @return Geographic coordinate representation of the given screen location.
	 */
	public abstract PVector screenToGeo(float screenX, float screenY);
}
