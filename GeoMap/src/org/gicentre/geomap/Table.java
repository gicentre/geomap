package org.gicentre.geomap;

import java.io.PrintWriter;

import processing.core.PApplet;
import processing.core.PConstants;

//  ****************************************************************************************
/** Class for representing a table of attributes suitable for querying.
 *  @author Ben Fry (http://ben.fry.com/writing/map/Table.pde) with modifications by Jo Wood.
 *  @version 2.2, 8th January, 2012.
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
public class Table
{
	// --------------------------------- Object variables ---------------------------------

	private String[] header;		// Column headings.
	private String[][] data;		// All data stored as strings.
	private int rowCount;			// Number of rows in the table.
	
	// ----------------------------------- Constructors -----------------------------------

	/** Creates a new empty 10 by 10 table.
	 *  @param parent Sketch controlling the location of files to load and save.
	 */
	public Table(PApplet parent) 
	{
		data = new String[10][10];
	}

	/** Creates a table from the given tab separated values file.
	 *  @param filename Name of file containing the TSVs.
	 */
	public Table(String filename, PApplet parent) 
	{
		String[] rows = parent.loadStrings(filename);
	
		if (rows == null)
		{
			System.err.println("Warning: "+filename+" not found. No data loaded into table.");
			return;
		}

		data = new String[rows.length][];

		for (int i=0; i<rows.length; i++)
		{
			if (PApplet.trim(rows[i]).length() == 0)
			{
				continue;     // Skip empty rows
			}
			if (rows[i].startsWith("#"))
			{
				if (header == null)
				{
					header = PApplet.split(rows[i].substring(1), PConstants.TAB);
				}
				continue;    // Skip comment lines
			}

			// Split the row on the tabs
			String[] pieces = PApplet.split(rows[i], PConstants.TAB);

			// Copy to the table array
			data[rowCount] = pieces;
			rowCount++;
		}

		// Resize the 'data' array as necessary
		data = (String[][]) PApplet.subset(data, 0, rowCount);
	}

	// ------------------------------------- Methods -------------------------------------


	/** Reports the number of rows in the table.
	 *  @return Number of rows in the table.
	 */
	public int getRowCount() 
	{
		return rowCount;
	}

	/** Finds a row by its name.
	 *  @param name Name to query.
	 *  @return Row index corresponding to the first primary attribute that matches the given name
	 *          or -1 if now row is matched.
	 */
	public int getRowIndex(String name)
	{
		for (int i=0; i<rowCount; i++)
		{
			if (data[i][0].equals(name)) 
			{
				return i;
			}
		}
		System.err.println("No row named '" + name + "' was found");
		return -1;
	}

	/** Reports the name of the given row (the item in column 0).
	 *  @param row Row index to query.
	 *  @return First column value associated with the given row index.
	 */
	public String getRowName(int row)
	{
		return getString(row, 0);
	}

	/** Reports the item at the given row and column location as a String.
	 *  @param rowIndex Row of the table value to retrieve.
	 *  @param column Column of the table value to retrieve.
	 *  @return Table value at the given column reported as a string.
	 */
	public String getString(int rowIndex, int column)
	{
		if (rowIndex < 0)
		{
			System.err.println("Unknown row index: "+rowIndex+" when querying table.");
			return "";
		}
		return data[rowIndex][column];
	}

	/** Reports the item in the given column and row with the given name as a String.
	 *  @param rowName Attribute value of column 0 (first row to contain it).
	 *  @param column Column of the table value to retrieve.
	 *  @return Table value at the given column reported as a string.
	 */
	public String getString(String rowName, int column)
	{
		return getString(getRowIndex(rowName), column);
	}

	/** Reports the item at the given row and column location as a whole number.
	 *  @param rowIndex Row of the table value to retrieve.
	 *  @param column Column of the table value to retrieve.
	 *  @return Table value at the given column reported as a whole number.
	 */
	public int getInt(int rowIndex, int column) 
	{
		return PApplet.parseInt(getString(rowIndex, column));
	}

	/** Reports the item at the given row and column location as a whole number.
	 *  @param rowName Attribute value of column 0 (first row to contain it).
	 *  @param column Column of the table value to retrieve.
	 *  @return Table value at the given column reported as a whole number.
	 */
	public int getInt(String rowName, int column) 
	{
		return PApplet.parseInt(getString(rowName, column));
	}

	/** Reports the item at the given row and column location as a decimal number.
	 *  @param rowIndex Row of the table value to retrieve.
	 *  @param column Column of the table value to retrieve.
	 *  @return Table value at the given column reported as a decimal number.
	 */
	public float getFloat(int rowIndex, int column)
	{
		return PApplet.parseFloat(getString(rowIndex, column));
	}

	/** Reports the item at the given row and column location as a decimal number.
	 *  @param rowName Attribute value of column 0 (first row to contain it).
	 *  @param column Column of the table value to retrieve.
	 *  @return Table value at the given column reported as a decimal number.
	 */
	public float getFloat(String rowName, int column)
	{
		return PApplet.parseFloat(getString(rowName, column));
	}


	/** Sets the name of the given row. This item will be stored in column 0 of the row.
	 *  @param row Row in which to set the name.
	 *  @param what New name (column 0 value) to be associated with the given row.
	 */
	public void setRowName(int row, String what)
	{
		data[row][0] = what;
	}

	/** Sets the value at the given row and column as the given String.
	 *  @param rowIndex Row of the table value to retrieve.
	 *  @param column Column of the table value to retrieve.
	 *  @param what New value to be associated with the given table cell.
	 */
	public void setString(int rowIndex, int column, String what) 
	{
		data[rowIndex][column] = what;
	}

	/** Sets the value at the given row and column as the given String.
	 *  @param rowName Attribute value of column 0 (first row to contain it).
	 *  @param column Column of the table value to retrieve.
	 *  @param what New value to be associated with the given table cell.
	 */
	public void setString(String rowName, int column, String what)
	{
		int rowIndex = getRowIndex(rowName);
		data[rowIndex][column] = what;
	}

	/** Sets the value at the given row and column as the given whole number.
	 *  @param rowIndex Row of the table value to retrieve.
	 *  @param column Column of the table value to retrieve.
	 *  @param what New whole number value to be associated with the given table cell.
	 */
	public void setInt(int rowIndex, int column, int what)
	{
		data[rowIndex][column] = PApplet.str(what);
	}

	/** Sets the value at the given row and column as the given whole number.
	 *  @param rowName Attribute value of column 0 (first row to contain it).
	 *  @param column Column of the table value to retrieve.
	 *  @param what New whole number value to be associated with the given table cell.
	 */
	public void setInt(String rowName, int column, int what) 
	{
		int rowIndex = getRowIndex(rowName);
		data[rowIndex][column] = PApplet.str(what);
	}

	/** Sets the value at the given row and column as the given decimal number.
	 *  @param rowIndex Row of the table value to retrieve.
	 *  @param column Column of the table value to retrieve.
	 *  @param what New decimal number value to be associated with the given table cell.
	 */
	public void setFloat(int rowIndex, int column, float what)
	{
		data[rowIndex][column] = PApplet.str(what);
	}

	/** Sets the value at the given row and column as the given decimal number.
	 *  @param rowName Attribute value of column 0 (first row to contain it).
	 *  @param column Column of the table value to retrieve.
	 *  @param what New decimal number value to be associated with the given table cell.
	 */
	public void setFloat(String rowName, int column, float what)
	{
		int rowIndex = getRowIndex(rowName);
		data[rowIndex][column] = PApplet.str(what);
	}

	/** Writes this table as a TSV file to the given writer.
	 *  @param writer Output writer in which to send table contents.
	 */
	public void write(PrintWriter writer) 
	{
		if (header != null)
		{
			for (int j=0; j<header.length; j++)
			{
				if (j != 0)
				{
					writer.print(PConstants.TAB);
				}
				if (header[j] != null)
				{
					if (j==0)
					{
						writer.print("#"+header[0]);
					}
					else
					{
						writer.print(header[j]);
					}
				}
			}
			writer.println();
		}
		
		
		for (int i=0; i<rowCount; i++)
		{
			for (int j=0; j<data[i].length; j++)
			{
				if (j != 0)
				{
					writer.print(PConstants.TAB);
				}
				if (data[i][j] != null)
				{
					writer.print(data[i][j]);
				}
			}
			writer.println();
		}
		writer.flush();
	}
	
	
	/** Writes this table as formatted text to the given writer. This is designed for producing 'pretty'
	 *  text output so table can be examined more easily.
	 *  @param numRows Maximum number of rows of the table to display.
	 *  @param writer Output writer in which to send table contents.
	 */
	public void writeAsTable(PrintWriter writer, int numRows) 
	{
		// Find out the number of columns and maximum width of each.
		
		if (header != null)
		{
			for (int j=0; j<header.length; j++)
			{
				if (j != 0)
				{
					writer.print(PConstants.TAB);
				}
				if (header[j] != null)
				{
					if (j==0)
					{
						writer.print("#"+header[0]);
					}
					else
					{
						writer.print(header[j]);
					}
				}
			}
			writer.println();
		}
		
		
		for (int i=0; i<rowCount; i++)
		{
			for (int j=0; j<data[i].length; j++)
			{
				if (j != 0)
				{
					writer.print(PConstants.TAB);
				}
				if (data[i][j] != null)
				{
					writer.print(data[i][j]);
				}
			}
			writer.println();
		}
		writer.flush();
	}
}
