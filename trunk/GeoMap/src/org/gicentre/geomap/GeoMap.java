package org.gicentre.geomap;

import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.gicentre.geomap.io.ShapefileReader;
import org.gicentre.geomap.io.ShapefileWriter;

import processing.core.PApplet;
import processing.core.PVector;

// *****************************************************************************************
/** Class for drawing geographic maps in Processing
 *  @author Iain Dillingham and Jo Wood, giCentre, City University London.
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
public class GeoMap implements Geographic
{
	// ----------------------------------- Object variables ------------------------------------

    private PApplet parent;                                // The parent sketch.
    private float minGeoX, maxGeoX;                        // The minimum and maximum geographic values in the x direction.
    private float minGeoY, maxGeoY;                        // Minimum and maximum geographic values in the y direction.
    private float xOrigin, yOrigin, mapWidth, mapHeight;   // The bounds of the map in screen coordinates.
    private Map<Integer, Feature> features;                // The key/value pair for each feature.
    private AttributeTable attributes;					   // Attribute table associated with feature collection.
    private int numPoints,numLines,numPolys;			   // Number of features of each type.
    private int numLineVertices, numPolygonVertices;	   // Total number of vertices in all features.
    private int numPolygonParts;
    												/** Value used to indicate no data. */
    public final static float NO_DATA = Float.MAX_VALUE;
    
 // --------------------------------------- Constructors ----------------------------------------

    /** Creates a map with the default bounds.
     *  @param parent The parent sketch.
     */
    public GeoMap(PApplet parent)
    {
        this(0, 0, parent.width, parent.height, parent);
    }

    /** Creates a map with the given bounds.
     *  @param xOrigin The position of the map from the left of the sketch.
     *  @param yOrigin The position of the map from the top of the sketch.
     *  @param mapWidth The width of the map.
     *  @param mapHeight The height of the map.
     *  @param parent The parent sketch.
     */
    public GeoMap(float xOrigin, float yOrigin, float mapWidth, float mapHeight, PApplet parent)
    {
        this.parent     = parent;
        this.xOrigin    = xOrigin;
        this.yOrigin    = yOrigin;
        this.mapWidth   = mapWidth;
        this.mapHeight  = mapHeight;
        this.minGeoX    = xOrigin;
        this.maxGeoX    = xOrigin+mapWidth;
        this.minGeoY    = yOrigin;
        this.maxGeoY    = yOrigin+mapHeight;	
        this.features   = new LinkedHashMap<Integer, Feature>();
        this.attributes = new AttributeTable(0,0,parent);
        this.numPoints  = 0;
        this.numLines   = 0;
        this.numPolys   = 0;
        this.numLineVertices= 0;
        this.numPolygonVertices =0;
        this.numPolygonParts = 0;
    }

    // --------------------------------------- Methods -----------------------------------------
    
    /** Draws the map in the parent sketch.
     */
    public void draw()
    {
        for (Feature feature : features.values())
        {
        	feature.draw(this);
        }
    }
    
    /** Draws the feature that matches the given id. If the id is not found, nothing is drawn.
     *  @param id ID of feature to draw.
     */
    public void draw(int id)
    {
    	Feature feature = features.get(new Integer(id));
		if (feature != null)
		{
			feature.draw(this);
		}
    }
    
    /** Draws all features that match the given attribute stored in the given column of
     *  the attribute table. If no features are found or the given column is out of bounds,
     *  nothing is drawn
     *  @param attribute Attribute identifying features to draw.
     *  @param col Column in the attribute table (where ID is column 0) to search.
     */
    public void draw(String attribute, int col)
    {
    	Set<Integer> ids = attributes.match(attribute, col);
    	
    	
    	for (Integer id : ids)
    	{
    		Feature feature = features.get(id);
    		if (feature != null)
    		{
    			feature.draw(this);
    		}
    	}
    }
    
    /** Reports the ID of the feature at the given location in screen coordinates or -1
     *  if no feature found.
     *  @param screenX x-coordinate of screen location to query.
     *  @param screenY y-coordinate of screen location to query.
     *  @return ID of feature at given coordinates or -1 if no feature found.
     */
    public int getID(float screenX, float screenY)
    {
    	// Convert screen to geographic coordinates before testing for containment.
    	PVector geo = screenToGeo(screenX, screenY);
    	
    	for (Integer id : features.keySet())
    	{
    		Feature feature = features.get(id);
    	
    		if (feature.contains(geo.x,geo.y))
    		{
    			return id.intValue();
    		}
    	}
    	
    	// No matches if we get to this stage.
    	return -1;
    }
    
    /** Should provide the screen coordinates corresponding to the given geographic coordinates.
	 * @param geoX Geographic x coordinate.
	 * @param geoY Geographic y coordinate.
	 * @return Screen coordinate representation of the given geographic location.
	 */
	public PVector geoToScreen(float geoX, float geoY)
	{
		return new PVector(PApplet.map(geoX, minGeoX, maxGeoX, xOrigin, xOrigin+mapWidth),
					       PApplet.map(geoY, minGeoY, maxGeoY, yOrigin+mapHeight, yOrigin));
	}
	
	/** Should provide the geographic coordinates corresponding to the given screen coordinates.
	 *  @param screenX Screen x coordinate.
	 *  @param screenY Screen y coordinate.
	 *  @return Geographic coordinate representation of the given screen location.
	 */
	public PVector screenToGeo(float screenX, float screenY)
	{
		return new PVector(PApplet.map(screenX, xOrigin, xOrigin+mapWidth, minGeoX, maxGeoX),
						   PApplet.map(screenY, yOrigin+mapHeight, yOrigin,minGeoY, maxGeoY));
	}
	
    /** Reads geometry and attributes from a shapefile.
     *  @param fileName The name of the file without extension. A shapefile consists of three separate files
     *                  called fileName.shp (the geometry), fileName.dbf (the attributes) and fileName.dbx
     *                  (an index file).
     */
    public void readFile(String fileName)
    {
    	ShapefileReader reader = new ShapefileReader(parent);
    	InputStream geomStream   = parent.createInput(fileName+".shp");
    	if (geomStream == null)
    	{
    		System.err.println("Cannot open shapefile geometry file: "+fileName+".shp");
    		return;
    	}
    	   	
    	InputStream attribStream = parent.createInput(fileName+".dbf");
    	if (attribStream == null)
    	{
    		System.err.println("Cannot open shapefile attribute file: "+fileName+".dbf");
    		return;
    	}
    	reader.read(geomStream,attribStream);
    	minGeoX = reader.getMinX();
    	minGeoY = reader.getMinY();
    	maxGeoX = reader.getMaxX();
    	maxGeoY = reader.getMaxY();
    	features = reader.getFeatures();
    	attributes = reader.getAttributes();
    	
    	numPoints += reader.getNumPoints();
    	numLines += reader.getNumLines();
    	numPolys += reader.getNumPolys();
    	
    	for (Feature feature : features.values())
    	{
    		if (feature.getType() == FeatureType.LINE)
    		{
    			numLineVertices += feature.getNumVertices();
    		}
    		else if (feature.getType() == FeatureType.POLYGON)
    		{
    			numPolygonVertices += feature.getNumVertices();
    			numPolygonParts += ((Polygon)feature).getSubPartPointers().size();
    		}
    	}
    }
    
    /** Writes geometry and attributes of this geoMap object as a shapefile.
     *  @param fileName The name of the file without extension. A shapefile consists of three separate files
     *                  called fileName.shp (the geometry), fileName.dbf (the attributes) and fileName.dbx
     *                  (an index file).
     */
    public void writeFile(String fileName)
    {
    	// Write the objects out as shapefiles (one triplet per feature type).
    	ShapefileWriter writer = new ShapefileWriter(this,parent);  	
    	writer.write(fileName);
    }
    
    /** Reports the number of point objects that are stored in this geoMap object.
	 *  @return Number of point objects stored.
	 */
	public int getNumPoints()
	{
		return numPoints;
	}
	
	/** Reports the number of line objects that are stored in this geoMap object.
	 *  @return Number of line objects stored.
	 */
	public int getNumLines()
	{
		return numLines;
	}
	
	/** Reports the number of polygon objects that are stored in this geoMap object.
	 *  Note that a complex polygon with several parts will be considered a single object.
	 *  @return Number of polygon objects stored.
	 */
	public int getNumPolys()
	{
		return numPolys;
	}
	
   /** Reports the total number of vertices in all line features stored in this geoMap object.
	 *  @return Number of line vertices stored.
	 */
	public int getNumLineVertices()
	{
		return numLineVertices;
	}
	
	/** Reports the total number of vertices in all polygon features stored in this geoMap object.
	 *  @return Number of polygon vertices stored.
	 */
	public int getNumPolygonVertices()
	{
		return numPolygonVertices;
	}
	
	/** Reports the total number of polygon parts in all polygon features stored in this geoMap object.
	 *  Simple polygons have one part each. More complex polygons can consist of several parts such as
	 *  islands, holes etc.
	 *  @return Number of polygon parts stored.
	 */
	public int getNumPolygonParts()
	{
		return numPolygonParts;
	}
	
	/** Reports the collection of features that make up this geoMap object.
	 *  @return Collection of features each addressable by some unique ID.
	 */
	public Map<Integer,Feature> getFeatures()
	{
		return features;
	}
	
	/** Reports the attribute table associated with this geoMap object.
	 *  @return Attribute table associated with this geoMap.
	 */
	public AttributeTable getAttributes()
	{
		return attributes;
	}
	
	/* * Reports the attribute as a string at the given column with the given ID. Column numbering starts
	 *  at 0 (corresponding to the ID itself), so getAttributeAsString(id,0) should always return id.
	 *  @param id ID that identifies the row in the attribute table to query.
	 *  @param columnNumber Column number to query.
	 *  @return Attribute with the given ID in the given column of the geomap's attribute table.
	 * /
	public String getAttributeAsString(String id, int columnNumber)
	{
		if (attributes == null)
		{
			System.err.println("Warning: No attribute table to query.");
			return "";
		}
		if (columnNumber >= attributes.findNumCols())
		{
			System.err.println("Warning: Attribute table has "+attributes.findNumCols()+" columns, so cannot query column "+columnNumber+". Returning ID.");
			return id;
		}		
		
		//HashMap<String,Integer> idLookup = attributes.getRowLookup(0);
		//int rowNum = idLookup.get(id).intValue();
		//return attributes.getString(rowNum, columnNumber);
		return attributes.getString(attributes.getRowIndex(id), columnNumber);
	}
	
	/* * Reports the attribute as an integer at the given column with the given ID. Column numbering starts
	 *  at 0 (corresponding to the ID itself), so getAttributeAsInt(id,0) should always return an integer version of the id.
	 *  @param id ID that identifies the row in the attribute table to query.
	 *  @param columnNumber Column number to query.
	 *  @return Attribute with the given ID in the given column of the geomap's attribute table. If the value in the table cannot be represented
	 *          as an integer, a value of 0 is returned.
	 * /
	public int getAttributeAsInt(String id, int columnNumber)
	{
		if (attributes == null)
		{
			System.err.println("Warning: No attribute table to query.");
			return 0;
		}
		if (columnNumber >= attributes.findNumCols())
		{
			System.err.println("Warning: Attribute table has "+attributes.findNumCols()+" columns, so cannot query column "+columnNumber+". Returning ID.");
			return PApplet.parseInt(id);	
		}		
		
		//HashMap<String,Integer> idLookup = attributes.getRowLookup(0);
		//int rowNum = idLookup.get(id).intValue();
		//return PApplet.parseInt(attributes.getString(rowNum, columnNumber));
		return PApplet.parseInt(attributes.getString(attributes.getRowIndex(id), columnNumber));
	}
	
	/ ** Reports the attribute as a decimal number at the given column with the given ID. Column numbering starts
	 *  at 0 (corresponding to the ID itself), so getAttributeAsFloat(id,0) should always return a numerical version of the id.
	 *  @param id ID that identifies the row in the attribute table to query.
	 *  @param columnNumber Column number to query.
	 *  @return Attribute with the given ID in the given column of the geomap's attribute table. If the value in the table cannot be represented
	 *          as a number, a value of 0 is returned.
	 * /
	public float getAttributeAsFloat(String id, int columnNumber)
	{
		if (attributes == null)
		{
			System.err.println("Warning: No attribute table to query.");
			return 0;
		}
		if (columnNumber >= attributes.findNumCols())
		{
			System.err.println("Warning: Attribute table has "+attributes.findNumCols()+" columns, so cannot query column "+columnNumber+". Returning ID.");
			return PApplet.parseFloat(id);
		}		
		
		//HashMap<String,Integer> idLookup = attributes.getRowLookup(0);
		//int rowNum = idLookup.get(id).intValue();
		//return PApplet.parseFloat(attributes.getString(rowNum, columnNumber));
		return PApplet.parseFloat(attributes.getString(attributes.getRowIndex(id), columnNumber));
	}
	*/
	
	/** Sets the attribute table to be associated with this geoMap object.
	 *  @param attributes New attribute table to be associated with this geoMap.
	 */
	public void setAttributes(AttributeTable attributes)
	{
		this.attributes = attributes;
	}
	
	/** Reports the minimum geographic coordinate in the x-direction.
	 *  @return Minimum geographic coordinate in the x-direction.
	 */
	public float getMinGeoX()
	{
		return minGeoX;
	}
	
	/** Reports the minimum geographic coordinate in the y-direction.
	 *  @return Minimum geographic coordinate in the y-direction.
	 */
	public float getMinGeoY()
	{
		return minGeoY;
	}
	
	/** Reports the maximum geographic coordinate in the x-direction.
	 *  @return Maximum geographic coordinate in the x-direction.
	 */
	public float getMaxGeoX()
	{
		return maxGeoX;
	}
	
	/** Reports the maximum geographic coordinate in the y-direction.
	 *  @return Maximum geographic coordinate in the y-direction.
	 */
	public float getMaxGeoY()
	{
		return maxGeoY;
	}
	
	// --------------------------------- Private methods ---------------------------------
	
	/* * Reports the ids of all items at the given column index that match the given text
	 *  @param attribute Text to search for.
	 *  @param columnIndex Column in table to search (first column is 0, second is 1 etc.).
	 *  @return List of ids corresponding to matched text. Will be an empty list if no matches found.
	 * /
    private Set<Integer>match(String attribute,int columnIndex)
    {

    	HashSet<Integer>matches = new HashSet<Integer>();
    	for (int row=0; row<attributes.getRowCount(); row++)
    	{
    		if (attribute.equals(attributes.getString(row,columnIndex)))
    		{
    			matches.add(new Integer(attributes.getString(row, 0)));
    		}
    	}
    	return matches;
    }
    */
}
