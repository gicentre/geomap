package org.gicentre.geomap.io;

import java.io.*;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.util.ArrayList;

import org.gicentre.geomap.Feature;
import org.gicentre.geomap.FeatureType;
import org.gicentre.geomap.GeoMap;
import org.gicentre.geomap.Line;
import org.gicentre.geomap.Point;
import org.gicentre.geomap.Polygon;
import org.gicentre.geomap.Table;

import processing.core.PApplet;

//  **************************************************************************************************
/** Writes out a geoMap object as a collection of ESRI shapefiles. A shapefile consists of 3 separate
 *  files - <code><i>name</i>.shp</code> containing the geometry; <code><i>name</i>.shx</code> 
 *  containing the file offsets for the components that make up the geometry; and 
 *  <code><i>name</i>.dbf</code> containing the attributes. If a geoMap object contains more than one
 *  object type (point, line or area), a triplet of files is written for each type.
 *  @author Jo Wood, giCentre.
 *  @version 3.1, 10th January, 2012.
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

public class ShapefileWriter 
{
	// ----------------------------------- Object variables ------------------------------------

	private int recordNumber;
	private PApplet parent;
	private GeoMap geoMap;						// Object to write as a shapefile.	

	// ------------------------------------- Constructor ---------------------------------------

	/** Creates the object capable of writing the given geoMap object as a shapefile.
	 *  @param geoMap geoMap object to write. 
	 *  @param parent Parent sketch.
	 */
	public ShapefileWriter(GeoMap geoMap, PApplet parent)
	{
		this.geoMap = geoMap;
		this.parent = parent;
	}

	// --------------------------------------- Methods -----------------------------------------

	/** Writes out one or more shapefiles representing the geoMap object supplied to the constructor.
	 *  The given fileName can be supplied with or without an extension, but this method will
	 *  write three files with the same base and extensions <code>.shp</code>, <code>.shx</code>
	 *  and <code>.dbf</code>. If the geoMap object contains more than one type, P, L or A will be 
	 *  appended to the relevant file name.
	 *  @param fileName Name of core of the three files to create.
	 *  @return True if written successfully.
	 */
	public boolean write(String fileName)
	{	
		// Generate names of the 3 output files.
		int dotIndex = fileName.lastIndexOf('.');
		String baseName = new String(fileName);

		if (dotIndex >0)
		{
			baseName = fileName.substring(0,dotIndex);
		}

		// Determine if we need to append the file name with the feature type (only if more than one feature type is to be written).
		boolean appendType = false;
		if (geoMap.getNumPoints()+geoMap.getNumLines()+geoMap.getNumPolys() > Math.max(geoMap.getNumPoints(), Math.max(geoMap.getNumLines(),geoMap.getNumPolys())))
		{
			appendType = true;
		}
		boolean status = true;
		
		if (geoMap.getNumPoints() > 0)
		{
			if (appendType == true)
			{
				status = write(baseName+"P",FeatureType.POINT);
			}
			else
			{
				status = write(baseName,FeatureType.POINT);
			}
		}
	
		if ((status == true) && (geoMap.getNumLines() > 0))
		{
			if (appendType == true)
			{
				status = write(baseName+"L",FeatureType.LINE);
			}
			else
			{
				status = write(baseName,FeatureType.LINE);
			}
		}

		if ((status == true) && (geoMap.getNumPolys() > 0))
		{
			if (appendType == true)
			{
				status = write(baseName+"A",FeatureType.POLYGON);
			}
			else
			{
				status = write(baseName,FeatureType.POLYGON);
			}
		}  
		return status;
	}

	// ------------------------------- Private Methods ----------------------------------

	/** Writes out the given feature types to a shapefile with the given basename.
	 *  @param fileName Basename of file to write.
	 *  @param type Feature type to write.
	 *  @return True if file written successfully.
	 */
	private boolean write(String fileName, FeatureType type)
	{
		OutputStream geomStream   = parent.createOutput(fileName+".shp");
		if (geomStream == null)
		{
			System.err.println("Cannot create shapefile geometry file: "+fileName+".shp");
			return false;
		}

		OutputStream attribStream = parent.createOutput(fileName+".dbf");
		if (attribStream == null)
		{
			System.err.println("Cannot create shapefile attribute file: "+fileName+".dbf");
			return false;
		}

		OutputStream indexStream = parent.createOutput(fileName+".dbx");
		if (indexStream == null)
		{
			System.err.println("Cannot create shapefile index file: "+fileName+".dbx");
			return false;
		}

		if (writeDBF(attribStream) == false)
		{
			return false;
		}
		if (writeShape(geomStream, indexStream, type) == false)
		{
			return false;
		} 
		return true;   
	}

	/** Writes out the attributes of the given node's children as a DBF file (dBase III format).
	 *  @param outStream Output stream pointing to the dbf file to write. 
	 *  @return True if attribute table written successfully. 
	 */
	private boolean writeDBF(OutputStream outStream)
	{
		try
		{
			// Create the dbase header.
			DbaseFileHeader header = new DbaseFileHeader();
			Table attributes = geoMap.getAttributes();

			String[] headings = attributes.getHeadings();
			int[] maxWidths = attributes.calcMaxWidths();

			for (int i=0; i<maxWidths.length; i++)
			{
				if (maxWidths[i] > 254)
				{
					System.err.println("Maximum label length is 254 characters in length. Some labels in column "+(i+1)+" in attribute table will be truncated during shapefile output.");
					maxWidths[i] = 254;
				}
				header.addColumn(headings[i].substring(0, Math.min(254,headings[i].length())), 'C', maxWidths[i], 0);
			}
			header.setNumRecords(attributes.getRowCount());

			WritableByteChannel channel = Channels.newChannel(outStream);
			DbaseFileWriter writer = new DbaseFileWriter(header,channel);

			for (int row=0; row<attributes.getRowCount(); row++)
			{
				Object rowObjects[] = new Object[maxWidths.length];
				for (int col=0; col<rowObjects.length; col++)
				{
					String attrib = attributes.getString(row, col);
					rowObjects[col] = attrib.substring(0, Math.min(254,attrib.length()));
				}
				writer.write(rowObjects);
			}
			writer.close();
		}
		catch (IOException e)
		{
			return false;
		}
		return true;  
	}

	/** Writes out the geometry of the objects of the given type as a shapefile.
	 *  @param geomOut Output stream pointing to the shapefile geometry file (.shp).
	 *  @param indexOut Output stream pointing to the shapefile index file (.shx).
	 *  @param type Type of objects to write (point, line or polygon).
	 *  @return True if geometry written successfully. 
	 */
	private boolean writeShape(OutputStream geomOut, OutputStream indexOut, FeatureType type)
	{        
		try
		{                  
			// Geometry type-specific information.
			int shpFileSize=0;
			int shxFileSize=0;

			int shapeType = 0;

			if (type == FeatureType.POINT)
			{
				shapeType = 1;
				shpFileSize = 50 + geoMap.getNumPoints()*14;
				shxFileSize = 50 + geoMap.getNumPoints()*4;
			}
			else if (type == FeatureType.LINE)
			{
				shapeType = 3;
				shpFileSize = 50 + geoMap.getNumLines()*28 + geoMap.getNumLineVertices()*8;
				shxFileSize = 50 + geoMap.getNumLines()*4;  
			}
			else if (type == FeatureType.POLYGON)
			{
				shapeType = 5;
				shpFileSize = 50 + geoMap.getNumPolys()*26 + geoMap.getNumPolygonParts()*2 + geoMap.getNumPolygonVertices()*8;
				shxFileSize = 50 + geoMap.getNumPoints()*4;  
			}
			else
			{
				System.err.println("Unknown geometry type passed to shapefile writer: "+type);
				return false;   
			}

			// Write out headers.
			writeIntBigEndian(9994,geomOut);         // Shapefile identifier.
			writeIntBigEndian(9994,indexOut);

			for (int i=0; i<5; i++)                     // 5 blank bytes.
			{
				writeIntBigEndian(0,geomOut);
				writeIntBigEndian(0,indexOut);
			}

			writeIntBigEndian(shpFileSize,geomOut);  // Length of file in 16-bit words.
			writeIntBigEndian(shxFileSize,indexOut);
			writeIntLittleEndian(1000,geomOut);      // Version number.
			writeIntLittleEndian(1000,indexOut);
			writeIntLittleEndian(shapeType,geomOut); // Type of shape (geometry).
			writeIntLittleEndian(shapeType,indexOut);

			writeDoubleLittleEndian(geoMap.getMinGeoX(),geomOut);
			writeDoubleLittleEndian(geoMap.getMinGeoX(),indexOut);
			writeDoubleLittleEndian(geoMap.getMinGeoY(),geomOut);
			writeDoubleLittleEndian(geoMap.getMinGeoY(),indexOut);
			writeDoubleLittleEndian(geoMap.getMaxGeoX(),geomOut);
			writeDoubleLittleEndian(geoMap.getMaxGeoX(),indexOut);
			writeDoubleLittleEndian(geoMap.getMaxGeoY(),geomOut);
			writeDoubleLittleEndian(geoMap.getMaxGeoY(),indexOut);

			// TODO: Do we want to store Measured or Z types? Currently assuming we do not.
			writeDoubleLittleEndian(0,geomOut);  // Zmin.
			writeDoubleLittleEndian(0,indexOut);
			writeDoubleLittleEndian(0,geomOut);  // Zmax.
			writeDoubleLittleEndian(0,indexOut);
			writeDoubleLittleEndian(0,geomOut);  // Measured min.
			writeDoubleLittleEndian(0,indexOut);
			writeDoubleLittleEndian(0,geomOut);  // Measured max.
			writeDoubleLittleEndian(0,indexOut);

			// Add geometry records.
			recordNumber = 1;
			int recordOffset = 50;

			if (type == FeatureType.POINT)
			{
				int recordLength = 10;  // All point records are the same length.

				for (Feature feature : geoMap.getFeatures().values())
				{
					if (feature.getType() == FeatureType.POINT)
					{
						Point point = (Point)feature;

						// Record header.
						writeIntBigEndian(recordNumber,geomOut);
						writeIntBigEndian(recordLength,geomOut);

						// Add index record.
						writeIntBigEndian(recordOffset,indexOut);
						writeIntBigEndian(recordLength,indexOut); 
						recordOffset += (recordLength+4);

						// Record contents.
						writeIntLittleEndian(1,geomOut);     // Point shape type.
						writeDoubleLittleEndian(point.getCoords().x,geomOut);
						writeDoubleLittleEndian(point.getCoords().x,geomOut);
						recordNumber++;
					}
				}
			}
			else if (type == FeatureType.LINE)
			{
				for (Feature feature : geoMap.getFeatures().values())
				{
					if (feature.getType() == FeatureType.LINE)
					{
						Line line = (Line)feature;
						int numCoords = line.getNumVertices();
						int recordLength = 2 + 16 + 2 + 2 + 2 + numCoords*8;

						// Record header.
						writeIntBigEndian(recordNumber,geomOut);
						writeIntBigEndian(recordLength,geomOut);

						// Add index record.
						writeIntBigEndian(recordOffset,indexOut);
						writeIntBigEndian(recordLength,indexOut); 
						recordOffset += (recordLength+4);

						// Record contents.
						writeIntLittleEndian(3,geomOut); 
	
						writeDoubleLittleEndian(geoMap.getMinGeoX(),geomOut);
						writeDoubleLittleEndian(geoMap.getMinGeoY(),geomOut);
						writeDoubleLittleEndian(geoMap.getMaxGeoX(),geomOut);
						writeDoubleLittleEndian(geoMap.getMaxGeoY(),geomOut);

						writeIntLittleEndian(1,geomOut);
						writeIntLittleEndian(numCoords,geomOut);  

						writeIntLittleEndian(0,geomOut);
						
						float[] x = line.getXCoords();
						float[] y = line.getYCoords();
						for (int coord=0; coord<numCoords; coord++)
						{
							writeDoubleLittleEndian(x[coord],geomOut);
							writeDoubleLittleEndian(y[coord],geomOut);
						}
						recordNumber++;
					}
				}
			}
			else if (type == FeatureType.POLYGON)
			{
				for (Feature feature : geoMap.getFeatures().values())
				{
					if (feature.getType() == FeatureType.POLYGON)
					{
						Polygon poly = (Polygon)feature;
						
						int numCoords = poly.getNumVertices();
						int numParts  = poly.getSubPartPointers().size();					
						float x[]   = poly.getXCoords();
						float y[]   = poly.getYCoords();
						ArrayList<Integer>subPathPointers = poly.getSubPartPointers();
						int recordLength = 2 + 16 + 2 + 2 + numParts*2 + numCoords*8;

						// Record header.
						writeIntBigEndian(recordNumber,geomOut);
						writeIntBigEndian(recordLength,geomOut);

						// Add index record.
						writeIntBigEndian(recordOffset,indexOut);
						writeIntBigEndian(recordLength,indexOut); 
						recordOffset += (recordLength+4);

						// Record contents.
						writeIntLittleEndian(5,geomOut); 

						writeDoubleLittleEndian(geoMap.getMinGeoX(),geomOut);
						writeDoubleLittleEndian(geoMap.getMinGeoY(),geomOut);
						writeDoubleLittleEndian(geoMap.getMaxGeoX(),geomOut);
						writeDoubleLittleEndian(geoMap.getMaxGeoY(),geomOut);

						writeIntLittleEndian(numParts,geomOut);
						writeIntLittleEndian(numCoords,geomOut);  

						for (int part=0; part<numParts; part++)
						{
							writeIntLittleEndian(subPathPointers.get(part).intValue(),geomOut);
						}

						for (int coord=0; coord<numCoords; coord++)
						{
							writeDoubleLittleEndian(x[coord],geomOut);
							writeDoubleLittleEndian(y[coord],geomOut);
						}
						recordNumber++;
					}
				} 
			}

			geomOut.close();
			indexOut.close();
		}
		catch (IOException e)
		{
			System.err.println("Problem writing shape file.");
			return false;
		}
		return true;
	}


	// -------------------------- Private file writing methods -------------------------------

	/** Writes a 32 bit unsigned big-endian ('Motorola') word of data to the
	 * given output stream.
	 * @param value Value to write to output stream.
	 * @param os Output stream to process.
	 * @return True if written successfully.
	 */
	private static boolean writeIntBigEndian(int value, OutputStream os) 
	{ 
		// 4 bytes 
		try
		{
			byte[] intBytes = new byte[4];
			intBytes[0] = (byte)((value >> 24)& 0xff);
			intBytes[1] = (byte)((value >> 16)& 0xff);
			intBytes[2] = (byte)((value >> 8) & 0xff);
			intBytes[3] = (byte) (value       & 0xff);
			//filePointer += 4;
			os.write(intBytes);
		}
		catch (IOException e)
		{
			System.err.println("Cannot write 32 bit word to output stream: "+e);
		}

		return true;
	}

	/** Writes a 32 bit unsigned little-endian ('Intel') word of data to the
	 * given output stream.
	 * @param value Value to write to output stream.
	 * @param os Output stream to process.
	 * @return True if written successfully.
	 */
	protected static boolean writeIntLittleEndian(int value, OutputStream os) 
	{ 
		// 4 bytes 
		try
		{
			byte[] intBytes = new byte[4];
			intBytes[0] = (byte) (value       & 0xff);
			intBytes[1] = (byte)((value >> 8) & 0xff);
			intBytes[2] = (byte)((value >> 16)& 0xff);
			intBytes[3] = (byte)((value >> 24)& 0xff);        	

			//filePointer += 4;
			os.write(intBytes);
		}
		catch (IOException e)
		{
			System.err.println("Cannot write 32 bit word to output stream: "+e);
		}

		return true;
	}

	/** Writes a little-endian 8-byte double to the given output stream.
	 * @param value Value to write to output stream.
	 * @param os Output stream.
	 * @return True if written successfully.
	 */
	protected static boolean writeDoubleLittleEndian(double value, OutputStream os) 
	{ 
		try
		{
			long doubleBits = Double.doubleToLongBits(value);

			byte[] doubleBytes = new byte[8];
			doubleBytes[0] = (byte) (doubleBits       & 0xff);
			doubleBytes[1] = (byte)((doubleBits >> 8) & 0xff);
			doubleBytes[2] = (byte)((doubleBits >> 16)& 0xff);
			doubleBytes[3] = (byte)((doubleBits >> 24)& 0xff);
			doubleBytes[4] = (byte)((doubleBits >> 32)& 0xff); 
			doubleBytes[5] = (byte)((doubleBits >> 40)& 0xff); 
			doubleBytes[6] = (byte)((doubleBits >> 48)& 0xff); 
			doubleBytes[7] = (byte)((doubleBits >> 56)& 0xff);         	

			//filePointer += 8;
			os.write(doubleBytes);          
		}
		catch (IOException e)
		{
			System.err.println("Cannot write binary stream: "+e);
			return false;
		}
		return true;
	}
}