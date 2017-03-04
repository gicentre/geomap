package org.gicentre.geomap.io;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.LinkedHashMap;
import java.util.Map;

import org.gicentre.geomap.Feature;
import org.gicentre.geomap.GeoMap;
import org.gicentre.geomap.Line;
import org.gicentre.geomap.Point;
import org.gicentre.geomap.Polygon;

import processing.core.PApplet;
import processing.data.Table;

//  **************************************************************************************************
/** Readers an ESRI shapefile and populates a GeoMap object with its features and attributes.
 *  A shapefile should consist of 3 separate files - <code><i>name</i>.shp</code> containing the
 *  geometry; <code><i>name</i>.shx</code> containing the file offsets for the components that make 
 *  up the geometry; and <code><i>name</i>.dbf</code> containing the attributes.
 *  @author Jo Wood, giCentre, City University of London.
 *  @version 3.2, 4th March, 2017.
 */
//  **************************************************************************************************

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
public class ShapefileReader
{
	// ----------------------------------- Object variables ------------------------------------

	private long filePointer;						// Keeps track of read position in binary file.
	private int recordNumber;						// ID of each record in the shapefile.

	private LinkedHashMap<Integer, Feature>features;// Stores feature geometry.
	private Table attributes;						// Stores feature attributes.
	
	private PApplet parent;							// Parent sketch.
	
	private float minX,minY,maxX,maxY;				// Geographic bounds of the file read.
	
	private int numPts,numLns,numPlys;		        // Number of features of each type.
	
	private static final double ESRI_NODATA = -(10e38);	// Code used by ESRI to indicate no data.
	
	// ------------------------------------- Constructor ---------------------------------------

	/** Creates a shapefile reader that will scale any geographic data read to be within the given bounds.
	 *  @param parent Parent sketch that will draw the data to be read.
	 */
	public ShapefileReader(PApplet parent)
	{
		this.parent = parent;
		numPts  = 0;
		numLns  = 0;
		numPlys = 0;
	}

	// --------------------------------------- Methods -----------------------------------------

	/** Reads the given shapefile (requires stream representing the .shp file and the .dbf file).
	 *  @param geomInputStream Input stream representing the geometry (.shp) file.
	 *  @param dbInputStream Input stream representing the attributes (.dbf) file.
	 *  @return True if shapefiles were read successfully.
	 */
	public boolean read(InputStream geomInputStream, InputStream dbInputStream)
	{ 
		filePointer = 0;
		features = new LinkedHashMap<Integer, Feature>();     

		int fileSize;

		try
		{                  
			// File code should be 9994 for shapefiles.
			if (readIntBigEndian(geomInputStream) != 9994)
			{
				System.err.println("Warning: Does not appear to be a shape file.");
				return false;
			}

			// Skip the next five integers (should be 0).
			for (int i=0; i<5; i++)
			{
				readIntBigEndian(geomInputStream);
			}

			// File length (including this 100 byte header).
			fileSize = readIntBigEndian(geomInputStream)*2;

			// Version (should be 1000).
			readIntLittleEndian(geomInputStream);

			// Shape type.
			int shapeType = readIntLittleEndian(geomInputStream);

			// Boundaries            
			minX = (float)readDoubleLittleEndian(geomInputStream);
			minY = (float)readDoubleLittleEndian(geomInputStream);
			maxX = (float)readDoubleLittleEndian(geomInputStream);
			maxY = (float)readDoubleLittleEndian(geomInputStream);

			// zMin, zMax, mMin, mMax (all skipped)
			readDoubleLittleEndian(geomInputStream);
			readDoubleLittleEndian(geomInputStream);
			readDoubleLittleEndian(geomInputStream);
			readDoubleLittleEndian(geomInputStream);

			while (filePointer < fileSize)
			{
				// Record header
				recordNumber = readIntBigEndian(geomInputStream);
				int recordLength = readIntBigEndian(geomInputStream)*2;

				// Record contents
				shapeType = readIntLittleEndian(geomInputStream);  // Should be the same as file record type
																   // but ESRI say could vary in future versions.    

				switch (shapeType)
				{
					case 0:     // Null shape record.
						break;
	
					case 1:     // Point record.
						addPoint(geomInputStream,false,false);
	
						if (recordLength > 20)
						{
							System.err.println("Warning: skipping "+ (recordLength-20)+ " bytes in point record.");
							skip(geomInputStream,recordLength-20);
						}
						break;
	
					case 3:     // Polyline record.
						addPolyLine(geomInputStream);
						break;
	
					case 5:     // Polygon record.
						addPoly(geomInputStream);
						break;
	
					case 8:     // Multipoint record.
						addMultiPoint(geomInputStream,false,false);
						break;
	
					case 11:     // point z record.
						addPoint(geomInputStream,true,true);
						break;
	
					case 13:     // Polyline z record.
						addPolyLineZ(geomInputStream);
						break;
	
					case 15:     // Polygon z record.
						addPolyZ(geomInputStream);
						break;
	
					case 18:     // Multipoint z record.
						addMultiPoint(geomInputStream,true,true);
						break;
	
					case 21:     // Point measure record.
						addPoint(geomInputStream, false ,true);
						break;
	
					case 23:     // Polyline measure record.
						addPolyLineM(geomInputStream);
						break;
	
					case 25:     // Polygon measure record.
						addPolyM(geomInputStream);
						break;
	
					case 28:     // Multipoint measure record.
						addMultiPoint(geomInputStream,false,true);
						break;
	
					case 31:     // Multipatch record.
						System.err.println("Currently no support for multipatch records within shapefile.");
						geomInputStream.close();
						return false;
	
					default:
						System.err.println("Unknown shape type within shapefile: "+shapeType);
						geomInputStream.close();
						return false;
				}
			}        

			geomInputStream.close();
		}
		catch (Exception e)
		{
			System.err.println("Problem reading shape file.");
			e.printStackTrace();
			return false;
		}
		
		// Read attrbutes from the DBF file
		readDBF(dbInputStream);

		return true;
	}
	
	/** Provides the features that have been extracted from the shapefile.
	 *  @return Map that contains the features indexed by ID.
	 */
	public Map<Integer,Feature>getFeatures()
	{
		return features;
	}
	
	/** Provides the attribute table that has been extracted from the shapefile.
	 *  Now this method is deprecated, it may be slower than previously as it creates a new 
	 *  deprecated version of the table each time it is called.
	 *  @return Attribute table from shapefile.
	 *  @deprecated Use getAttributeTable instead that uses Processing's own Table class to store attributes.
	 */
	@Deprecated
	public org.gicentre.geomap.AttributeTable getAttributes()
	{
		return org.gicentre.geomap.AttributeTable.buildOldTable(attributes,parent);
	}
	
	/** Provides the attribute table that has been extracted from the shapefile.
	 *  @return Attribute table from shapefile.
	 */
	public Table getAttributeTable()
	{
		return attributes;
	}
	
	/** Reports the minimum geographic value in the x-direction.
	 *  @return minimum x value.
	 */
	public float getMinX()
	{
		return minX;
	}
	
	/** Reports the minimum geographic value in the y-direction.
	 *  @return minimum y value.
	 */
	public float getMinY()
	{
		return minY;
	}
	
	/** Reports the maximum geographic value in the x-direction.
	 *  @return maximum x value.
	 */
	public float getMaxX()
	{
		return maxX;
	}
	
	/** Reports the maximum geographic value in the y-direction.
	 *  @return maximum y value.
	 */
	public float getMaxY()
	{
		return maxY;
	}
	
	/** Reports the number of point objects that have been read by this reader.
	 *  @return Number of point objects read.
	 */
	public int getNumPoints()
	{
		return numPts;
	}
	
	/** Reports the number of line objects that have been read by this reader.
	 *  @return Number of line objects read.
	 */
	public int getNumLines()
	{
		return numLns;
	}
	
	/** Reports the number of polygon objects that have been read by this reader.
	 *  Note that a complex polygon with several parts will be considered a single object.
	 *  @return Number of polygon objects read.
	 */
	public int getNumPolys()
	{
		return numPlys;
	}
	
	// ---------------------------------------- Private methods ----------------------------------------

	/** Reads in a big-endian 4-byte integer from the given input stream.
	 *  @param is Input stream.
	 *  @return Value read from input stream.
	 */
	private int readIntBigEndian(InputStream is) 
	{ 
		// 4 bytes 
		int accum = 0; 
		try
		{
			for (int shiftBy=24; shiftBy>=0; shiftBy-=8) 
			{
				accum |= (is.read() & 0xff) << shiftBy; 
			}
		}
		catch (IOException e)
		{
			System.err.println("Cannot read binary stream: "+e);
		}

		filePointer += 4;
		return accum; 
	}

	/** Reads in a little-endian 4-byte integer from the given input stream.
	 *  @param is Input stream.
	 *  @return Value read from input stream.
	 */
	private int readIntLittleEndian(InputStream is) 
	{ 
		// 4 bytes 
		int accum = 0; 
		try
		{
			for (int shiftBy=0; shiftBy<32; shiftBy+=8)
				accum |= (is.read() & 0xff) << shiftBy;
		}
		catch (IOException e)
		{
			System.err.println("Cannot read binary stream: "+e);
		}

		filePointer += 4;
		return accum; 
	} 

	/** Reads in a little-endian 8-byte double from the given input stream.
	 *  @param is Input stream.
	 *  @return Value read from input stream.
	 */
	private double readDoubleLittleEndian(InputStream is) 
	{ 
		long accum = 0; 
		try
		{
			for (int shiftBy=0; shiftBy<64; shiftBy+=8 ) 
			{
				accum |= ( (long)(is.read() & 0xff)) << shiftBy; 
			}
		}
		catch (IOException e)
		{
			System.err.println("Cannot read binary stream: "+e);
		}
		filePointer += 8;
		return Double.longBitsToDouble(accum); 
	}

	/** Adds a point object to the geoMap from the given input stream.
	 *  @param inStream Input stream containing shapefile.
	 *  @param readZ Reads a z value if true.
	 *  @param readM Reads a measure if true. Note the measure value is not currently stored.
	 */
	private void addPoint(InputStream inStream, boolean readZ, boolean readM)
	{
		float x = (float)readDoubleLittleEndian(inStream);
		float y = (float)readDoubleLittleEndian(inStream);
		float z=0;

		if (readZ)
		{
			z = (float)readDoubleLittleEndian(inStream);
		}

		if (readM)
		{
			// NOTE: Measure currently ignored.
			readMeasure(inStream);
		}

		Point point;

		if (readZ)
		{
			point = new Point(x,y,z, parent);
		}
		else
		{
			point = new Point(x,y,parent);
		}

		features.put(new Integer(recordNumber),point);
		numPts++;
	}

	/** Adds a set of point objects to the geoMap from the given input stream.
	 *  @param inStream Input stream containing shapefile.
	 *  @param readZ Reads a z value if true.
	 *  @param readM Reads a measure if true. NOTE: measure value not currently stored.
	 */
	private void addMultiPoint(InputStream inStream, boolean readZ, boolean readM)
	{
		// Skip bounding box info.
		skip(inStream,32);

		int numPoints = readIntLittleEndian(inStream);

		float[] x = new float[numPoints],
		y = new float[numPoints],
		z=null,m=null;

		for (int i=0; i<numPoints; i++)
		{
			x[i] = (float)readDoubleLittleEndian(inStream);
			y[i] = (float)readDoubleLittleEndian(inStream);
		}

		if (readZ)  // Read possible z values.
		{
			z = new float[numPoints];

			skip(inStream,16); // Skip z range
			for (int i=0; i<numPoints; i++)
			{
				z[i] = (float)readDoubleLittleEndian(inStream);                   
			}
		}

		if (readM)  // Read possible measures.
		{
			m = new float[numPoints];

			skip(inStream,16); // Skip m range
			for (int i=0; i<numPoints; i++)
			{
				m[i] = readMeasure(inStream);                   
			}
		}

		// Store the points.
		for (int i=0; i<numPoints; i++)
		{
			Point point;
			if (readZ)
			{
				point = new Point(x[i],y[i],z[i], parent);
			}
			else
			{
				point = new Point(x[i],y[i],parent);
			}

			features.put(new Integer(recordNumber),point);
			numPoints++;
		}
	}

	/** Adds a polygon object to the geoMap collection from the given input stream.
	 *  @param inStream Input stream containing shapefile.
	 */
	private void addPoly(InputStream inStream)
	{
		// Skip bounding box info.
		skip(inStream,32);

		int numParts  = readIntLittleEndian(inStream);
		int numVertices = readIntLittleEndian(inStream);
		
		int[] partIndex = new int[numParts];     
		for (int i=0; i<numParts; i++)
		{
			partIndex[i] = readIntLittleEndian(inStream);
			if (partIndex[i] >= numVertices)
			{
				System.err.println("Warning: Part index "+partIndex[i]+" greater than number of vertices in shapefile "+numVertices+". Ignoring part "+i);
				numParts = i;
			}
		}

		Polygon poly = null;
		int currentPos = 0;
		int pointsInPart = 0;

		for (int part=0; part<numParts; part++)
		{
			if (part == numParts-1)  // Last part in list.
			{
				pointsInPart = numVertices-currentPos;
			}
			else
			{
				pointsInPart = partIndex[part+1]-currentPos;
			}
			
			if (pointsInPart >0)
			{
				float x[] = new float[pointsInPart];
				float y[] = new float[pointsInPart];

				for (int coord=0; coord<pointsInPart; coord++)
				{
					x[coord] = (float)readDoubleLittleEndian(inStream);
					y[coord] = (float)readDoubleLittleEndian(inStream);
				}

				if (poly == null)
				{
					poly = new Polygon(x,y,parent);
				}
				else
				{
					poly.addPart(x,y);
				}
				currentPos += pointsInPart;
			}
		}  

		if (poly != null)
		{
			features.put(new Integer(recordNumber),poly);
			numPlys++;
		}
	}

	/** Adds a 3d polygon object to to the geoMap collection from the given input stream.
	 *  Note that currently the z-values are not stored.
	 *  @param inStream Input stream containing shapefile.
	 */
	private void addPolyZ(InputStream inStream)
	{		
		// Skip bounding box info.
		skip(inStream,32);

		int numParts    = readIntLittleEndian(inStream);
		int numVertices = readIntLittleEndian(inStream);

		int[] partIndex = new int[numParts];     
		for (int i=0; i<numParts; i++)
		{
			partIndex[i] = readIntLittleEndian(inStream);
			if (partIndex[i] >= numVertices)
			{
				System.err.println("Warning: Part index "+partIndex[i]+" greater than number of points in shapefile "+numVertices+". Ignoring part "+i+" in polyZ");
				numParts = i;
			}
		}

		int currentPos = 0;
		int pointsInPart = 0;
		int numCoords = 0;
		Polygon poly = null;

		for (int part=0; part<numParts; part++)
		{
			if (part == numParts-1)  // Last part in list.
			{
				pointsInPart = numVertices-currentPos;
			}
			else
			{
				pointsInPart = partIndex[part+1]-currentPos;
			}

			float x[] = new float[pointsInPart];
			float y[] = new float[pointsInPart];

			for (int coord=0; coord<pointsInPart; coord++)
			{
				x[coord] = (float)readDoubleLittleEndian(inStream);
				y[coord] = (float)readDoubleLittleEndian(inStream);
			}
			numCoords += pointsInPart;

			if (poly == null)
			{
				poly = new Polygon(x,y,parent);
			}
			else
			{
				poly.addPart(x,y);
			}
			currentPos += pointsInPart;
		}

		// Skip the z-range data.
		skip(inStream,16);
		
		// Skip the the z-values 
		for (int i=0; i<numCoords;i++)
		{
        	readDoubleLittleEndian(inStream);
        }

		// Skip the remaining measure values.
		skip(inStream,16);				// measure range
		skip(inStream,numVertices*8);	// measure values
		
		if (poly != null)
		{
			features.put(new Integer(recordNumber),poly);
			numPlys++;
		}
	}
	
	/** Adds a polygon measure object to to the geoMap collection from the given input stream.
	 *  Note that currently the measure values are not stored.
	 *  @param inStream Input stream containing shapefile.
	 */
	private void addPolyM(InputStream inStream)
	{		
		// Skip bounding box info.
		skip(inStream,32);

		int numParts    = readIntLittleEndian(inStream);
		int numVertices = readIntLittleEndian(inStream);

		int[] partIndex = new int[numParts];     
		for (int i=0; i<numParts; i++)
		{
			partIndex[i] = readIntLittleEndian(inStream);
			if (partIndex[i] >= numVertices)
			{
				System.err.println("Warning: Part index "+partIndex[i]+" greater than number of points in shapefile "+numVertices+". Ignoring part "+i+" in polyM");
				numParts = i;
			}
		}

		int currentPos = 0;
		int pointsInPart = 0;
		int numCoords = 0;
		Polygon poly = null;

		for (int part=0; part<numParts; part++)
		{
			if (part == numParts-1)  // Last part in list.
			{
				pointsInPart = numVertices-currentPos;
			}
			else
			{
				pointsInPart = partIndex[part+1]-currentPos;
			}

			float x[] = new float[pointsInPart];
			float y[] = new float[pointsInPart];

			for (int coord=0; coord<pointsInPart; coord++)
			{
				x[coord] = (float)readDoubleLittleEndian(inStream);
				y[coord] = (float)readDoubleLittleEndian(inStream);
			}
			numCoords += pointsInPart;

			if (poly == null)
			{
				poly = new Polygon(x,y,parent);
			}
			else
			{
				poly.addPart(x,y);
			}
			currentPos += pointsInPart;
		}

		// Skip the measure-range data.
        skip(inStream,16);
        
        // Skip the the measure values 
		for (int i=0; i<numCoords; i++)
		{
        	readDoubleLittleEndian(inStream);
        }
		
		if (poly != null)
		{
			features.put(new Integer(recordNumber),poly);
			numPlys++;
		}
	}


	/** Adds a polyline object to the geoMap collection from the given input stream.
	 *  @param inStream Input stream containing shapefile.
	 */
	private void addPolyLine(InputStream inStream)
	{
		// Skip bounding box info.
		skip(inStream,32);

		int numParts    = readIntLittleEndian(inStream);
		int numVertices = readIntLittleEndian(inStream);

		int[] partIndex = new int[numParts];     
		for (int i=0; i<numParts; i++)
		{
			partIndex[i] = readIntLittleEndian(inStream);
			if (partIndex[i] >= numVertices)
			{
				System.err.println("Warning: Part index "+partIndex[i]+" greater than number of points in shapefile "+numVertices+". Ignoring part "+i+" in polyLine");
				numParts = i;
			}
		}

		int currentPos = 0;
		int pointsInPart = 0;

		for (int part=0; part<numParts; part++)
		{
			if (part == numParts-1)  // Last part in list.
			{
				pointsInPart = numVertices-currentPos;
			}
			else
			{
				pointsInPart = partIndex[part+1]-currentPos;
			}

			float x[] = new float[pointsInPart];
			float y[] = new float[pointsInPart];

			for (int coord=0; coord<pointsInPart; coord++)
			{
				x[coord] = (float)readDoubleLittleEndian(inStream);
				y[coord] = (float)readDoubleLittleEndian(inStream);
			}

			features.put(new Integer(recordNumber),new Line(x,y,parent));
			numLns++;
			currentPos += pointsInPart;
		}
	}

	/** Adds a 3d polyline object to the geoMap collection from the given input stream.
	 *  Note that currently the z values are not stored.
	 *  @param inStream Input stream containing shapefile.
	 */
	private void addPolyLineZ(InputStream inStream)
	{
		// Skip bounding box info.
		skip(inStream,32);

		int numParts    = readIntLittleEndian(inStream);
		int numVertices = readIntLittleEndian(inStream);

		int[] partIndex = new int[numParts];     
		for (int i=0; i<numParts; i++)
		{
			partIndex[i] = readIntLittleEndian(inStream);
			if (partIndex[i] >= numVertices)
			{
				System.err.println("Warning: Part index "+partIndex[i]+" greater than number of points in shapefile "+numVertices+". Ignoring part "+i+" in PolyLineZ");
				numParts = i;
			}
		}

		int currentPos = 0;
		int pointsInPart = 0;
		int numCoords = 0;

		for (int part=0; part<numParts; part++)
		{
			if (part == numParts-1)  // Last part in list.
			{
				pointsInPart = numVertices-currentPos;
			}
			else
			{
				pointsInPart = partIndex[part+1]-currentPos;
			}

			float x[] = new float[pointsInPart];
			float y[] = new float[pointsInPart];

			for (int coord=0; coord<pointsInPart; coord++)
			{
				x[coord] = (float)readDoubleLittleEndian(inStream);
				y[coord] = (float)readDoubleLittleEndian(inStream);
			}
			numCoords += pointsInPart;
			features.put(new Integer(recordNumber),new Line(x,y,parent));
			numLns++;
			currentPos += pointsInPart;
		}
		
		// Skip the z-range data.
		skip(inStream,16);
		
		// Skip the the z-values 
		for (int i=0; i<numCoords;i++)
		{
        	readDoubleLittleEndian(inStream);
        }

		// Skip the remaining measure values.
		skip(inStream,16);				// measure range
		skip(inStream,numVertices*8);	// measure values
	}

	
	/** Adds a polyline measure object to the geoMap collection from the given input stream.
	 *  Note that currently the measure values are not stored.
	 *  @param inStream Input stream containing shapefile.
	 */
	private void addPolyLineM(InputStream inStream)
	{
		// Skip bounding box info.
		skip(inStream,32);

		int numParts  = readIntLittleEndian(inStream);
		int numVertices = readIntLittleEndian(inStream);

		int[] partIndex = new int[numParts];     
		for (int i=0; i<numParts; i++)
		{
			partIndex[i] = readIntLittleEndian(inStream);
			if (partIndex[i] >= numVertices)
			{
				System.err.println("Warning: Part index "+partIndex[i]+" greater than number of points in shapefile "+numVertices+". Ignoring part "+i+" in PolyLineM");
				numParts = i;
			}
		}

		int currentPos = 0;
		int pointsInPart = 0;
		int numCoords = 0;

		for (int part=0; part<numParts; part++)
		{
			if (part == numParts-1)  // Last part in list.
			{
				pointsInPart = numVertices-currentPos;
			}
			else
			{
				pointsInPart = partIndex[part+1]-currentPos;
			}

			float x[] = new float[pointsInPart];
			float y[] = new float[pointsInPart];

			for (int coord=0; coord<pointsInPart; coord++)
			{
				x[coord] = (float)readDoubleLittleEndian(inStream);
				y[coord] = (float)readDoubleLittleEndian(inStream);
			}
			numCoords += pointsInPart;
			features.put(new Integer(recordNumber),new Line(x,y,parent));
			numLns++;
			currentPos += pointsInPart;
		}
		
		// Skip the measure-range data.
        skip(inStream,16);
        
        // Skip the the measure values 
		for (int i=0; i<numCoords; i++)
		{
        	readDoubleLittleEndian(inStream);
        }
	}

	/** Reads in a measure value from the given input stream.
	 *  @param inStream Input stream containing shapefile.
	 */
	private float readMeasure(InputStream inStream)
	{
		float measure = (float)readDoubleLittleEndian(inStream);

		if (measure < ESRI_NODATA)
		{
			return GeoMap.NO_DATA;
		}
		return measure; 
	}

	/** Reads a DBF file (dBase III format) and populates an attribute table with its contents.
	 *  @param inStream Input stream pointing to the DBF file to read.
	 *  @return True if table read successfully.
	 */
	private boolean readDBF(InputStream inStream)
	{
		try
		{
			ReadableByteChannel channel = Channels.newChannel(inStream);
			DbaseFileReader reader = new DbaseFileReader(channel);

			// Read in column name headings.   
			DbaseFileHeader header = reader.getHeader();
			String dBaseHeadings[] = new String[header.getNumFields()];
			String headings[] = new String[dBaseHeadings.length+1];
			headings[0] = new String("id");
			for (int i=1; i< headings.length; i++)
			{
				headings[i] = header.getFieldName(i-1);
			}
			
			// Note the first column will always be the numeric id associated with the geometric features.
			attributes = new Table();
			attributes.setColumnCount(header.getNumFields()+1);
			attributes.setRowCount(header.getNumRecords());
			attributes.setColumnTitles(headings);
			int id = 1;
			
			// TODO: SET COLUMN TYPE (INT, FLOAT, STRING) BASED ON DBF COL TYPES?
			while (reader.hasNext()) 
			{      
				Object[] atts = reader.readSimpleEntry();
				attributes.setInt(id-1, 0, id);
				
				for (int i=0; i<atts.length; i++)
				{
					attributes.setString(id-1, i+1, atts[i].toString());
				}
				id++;
			}
			reader.close();
		}

		catch (FileNotFoundException e)
		{
			System.err.println("Shapefile DBF not found. Using IDs only.");
			return false;
		}
		catch (IOException e)
		{
			System.err.println("Problem reading Shapefile DBF. Using IDs only.");
			return false;
		}

		return true;  
	}
	

	/** Skips the given number of bytes in the input stream.
	 *  @param is Input stream.
	 *  @param numBytes Number of 8-bit bytes to skip.
	 */
	private void skip(InputStream is, long numBytes) 
	{ 
		try
		{
			// Note that the skip() method in input stream is not efficient nor reliable.
			for (int i=0; i<numBytes; i++)
			{
				is.read();
			}  
			filePointer += numBytes;
		}
		catch (IOException e)
		{
			System.err.println("Cannot read binary stream: "+e);
		}
	}
}
