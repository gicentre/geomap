package org.gicentre.geomap.io;

import java.io.*;
import java.util.*;
import java.util.logging.*;
import java.nio.*;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

//************************************************************************************************
/** Reads Dbase III files. This code is based on the class provided as part of the Geotools 
  * OpenSource mapping toolkit - <a href="http://www.geotools.org/">http://www.geotools.org/</a>
  * under the GNU Lesser General Public License. The general use of this class is:<code><pre>
  * FileChannel in = new FileInputStream("thefile.dbf").getChannel();
  * DbaseFileReader r = new DbaseFileReader( in )
  * Object[] fields = new Object[r.getHeader().getNumFields()];
  * while (r.hasNext()) 
  * {
  *    r.readEntry(fields);
  *    // do stuff
  * }
  * r.close();
  * </pre></code>
  * For consumers who wish to be a bit more selective with their reading of rows, the Row object 
  * has been added. The semantics are the same as using the readEntry method, but remember that
  * the Row object is always the same. The values are parsed as they are read, so it pays to copy
  * them out (as each call to Row.read() will result in an expensive String parse).
  * <br /><b>EACH CALL TO readEntry OR readRow ADVANCES THE FILE!</b><br />
  * An example of using the Row method of reading:
  * <code><pre>
  * FileChannel in = new FileInputStream("thefile.dbf").getChannel();
  * DbaseFileReader r = new DbaseFileReader(in)
  * int fields = r.getHeader().getNumFields();
  * while (r.hasNext()) 
  * {
  *   DbaseFileReader.Row row = r.readRow();
  *   for (int i = 0; i &lt; fields; i++) 
  *   {
  *     // do stuff
  *     Foo.bar(row.read(i));
  *   }
  * }
  * r.close();
  * </pre></code>
  * @author Ian Schneider with minor modifications by Jo Wood.
  * @version 2.4, 6th January 2012.
  */
//  ************************************************************************************************

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

public class DbaseFileReader 
{
    // ----------------- Object and Class Variables ------------------
  
    private DbaseFileHeader header;
    private ByteBuffer buffer;
    private ReadableByteChannel channel;
    private CharBuffer charBuffer;
    private CharsetDecoder decoder;
    private char[] fieldTypes;
    private int[] fieldLengths;
    private int cnt = 1;
    private Row row;
    private NumberParser numberParser = new NumberParser();
    
    // ------------------------- Constructors -------------------------
    
    /** Creates a new instance of DBaseFileReader.
      * @param channel The readable channel to use.
      * @throws IOException If an error occurs while initializing.
      */
    public DbaseFileReader(ReadableByteChannel channel) throws IOException 
    {
        this(channel,null);
    }
  
    /** Creates a new instance of DBaseFileReader with warning messages reported
      * to the given logger. 
      * @param channel The readable channel to use.
      * @param logger Logger to monitor warning messages.
      * @throws IOException If an error occurs while initializing.
      */
    public DbaseFileReader(ReadableByteChannel channel, Logger logger) throws IOException 
    {
        this.channel = channel;
        header = new DbaseFileHeader(logger);
        header.readHeader(channel);
    
        init();
    }
  
    // -------------------------- Methods ----------------------------

    /** Retrieves the header from this file. The header is read upon instantiation.
      * @return The header associated with this file or null if an error occurred.
      */
    public DbaseFileHeader getHeader() 
    {
        return header;
    }
  
    /** Cleans up all resources associated with this reader. Should be called after
      * all required data has been extracted from the database. 
      * @throws IOException If an error occurs.
      */
    public void close() throws IOException 
    {
        if (channel.isOpen()) 
        {
            channel.close();
        }
        
        buffer = null;
        channel = null;
        charBuffer = null;
        decoder = null;
        header = null;
        row = null;
    }
  
    /** Queries the reader as to whether there is another record.
      * @return True if more records exist, false otherwise.
      */
    public boolean hasNext() 
    {
        return cnt < header.getNumRecords() + 1;
    }
  
    /** Retrieves the next record (entry). Will return a new array of values.
      * @throws IOException If an error occurs.
      * @return A new array of values.
      */
    public Object[] readEntry() throws IOException 
    {
        return readEntry(new Object[header.getNumFields()],false);
    }
    
    /** Retrieves the next record (entry), formatted as a collection of numbers
      * and/or strings. Will return a new array of values.
      * @throws IOException If an error occurs.
      * @return A new array of values.
      */
    public Object[] readSimpleEntry() throws IOException 
    {
        return readEntry(new Object[header.getNumFields()],true);
    }
  
    /** Reads the next row from the database. Advances the file pointer to the
      * start of the next record.
      * @return Next row in the database.
      * @throws IOException if error occurs when reading.
      */
    public Row readRow() throws IOException 
    {
        read();
        return row;
    }
  
    /** Skips the next record.
      * @throws IOException if an error occurs.
      */
    public void skip() throws IOException 
    {
        boolean foundRecord = false;
        while (!foundRecord) 
        {
            bufferCheck();
      
            // Read the deleted flag.
            char tempDeleted = (char) buffer.get();
      
            // Skip the next bytes.
            buffer.position(buffer.position() + header.getRecordLength() - 1); //the 1 is for the deleted flag just read.
      
            // Add the row if it is not deleted.
            if (tempDeleted != '*') 
            {
                foundRecord = true;
            }
        }
    }
  
    /** Copies the next record into the array starting at offset.
      * @param entry The array to copy into.
      * @param offset The offset to start at.
      * @param doSimple Will format as strings/numbers if true.
      * @throws IOException id an error occurs.
      * @return The same array passed in.
      */
    public Object[] readEntry(Object[] entry, final int offset, boolean doSimple) throws IOException 
    {
        if (entry.length - offset < header.getNumFields()) 
        {
            throw new ArrayIndexOutOfBoundsException();
        }
    
        read();
    
        // Retrieve the record length.
        final int numFields = header.getNumFields();
    
        int fieldOffset = 0;
        for (int j=0; j < numFields; j++)
        {
            entry[j + offset] = readObject(fieldOffset,j,doSimple);
            fieldOffset += fieldLengths[j];
        }
    
        return entry;
    }

  /*
   * Transfer, by bytes, the next record to the writer.
   *
  public void transferTo(DbaseFileWriter writer) throws IOException {
      bufferCheck();
      buffer.limit(buffer.position() + header.getRecordLength());
      writer.channel.write(buffer);
      buffer.limit(buffer.capacity());
      
      cnt++;
  }
  */

    /** Copies the next entry into the array.
      * @param entry The array to copy into.
      * @param doSimple Will format as strings/numbers if true.
      * @return The same array passed in.
      * @throws IOException If an error occurs when trying to read entry from database.
      */
    public Object[] readEntry(Object[] entry, boolean doSimple) throws IOException 
    {
        return readEntry(entry,0,doSimple);
    }
  

    // --------------------------- Private Methods ----------------------------
    
    /** Fills the given byte buffer with contents via the given channel.
      * @param bBuffer Buffer to fill.
      * @param bChannel Channel from which to read database.
      * @return The number of bytes read, possibly zero, or -1 if the channel has reached end-of-stream.
      * @throws IOException If problem filling the buffer.
      */
    private int fill(ByteBuffer bBuffer, ReadableByteChannel bChannel) throws IOException 
    {
        int r = bBuffer.remaining();
        // channel reads return -1 when EOF or other error
        // because they are non-blocking reads, 0 is a valid return value!!
    
        while (bBuffer.remaining() > 0 && r != -1) 
        {
            r = bChannel.read(bBuffer);
        }
        if (r == -1) 
        {
            bBuffer.limit(bBuffer.position());
        }
        return r;
    }
  
    /** Ensures buffer is full with remains of last record.
      * @throws IOException If problem filling the buffer. 
      */
    private void bufferCheck() throws IOException 
    {
        // remaining is less than record length
        // compact the remaining data and read again
        if (!buffer.isReadOnly() && buffer.remaining() < header.getRecordLength()) 
        {
            buffer.compact();
            fill(buffer,channel);
            buffer.position(0);
        }
    }
  
    /** Reports the offset that the given column is from the start of a row.
      * @param column Column upon which to find offset.
      * @return Offset that the given column is from start. 
      */
    private int getOffset(int column) 
    {
        int offset = 0;
        for (int i = 0, ii = column; i < ii; i++) 
        {
            offset += fieldLengths[i];
        }
        return offset;
    }
  
    /** Initialises the reader.
      * @throws IOException If problem initialising the reader. 
      */
    private void init() throws IOException 
    {
        // create the ByteBuffer
        // if we have a FileChannel, lets map it
        if (channel instanceof FileChannel) 
        {
            FileChannel fc = (FileChannel) channel;
            buffer = fc.map(FileChannel.MapMode.READ_ONLY,0,fc.size());
            buffer.position((int) fc.position());
        }
        else 
        {
            // Some other type of channel.
            // start with a 8K buffer, should be more than adequate.
            int size = 8 * 1024;
            // If for some reason its not, resize it.
            size = header.getRecordLength() > size ? header.getRecordLength() : size;
            buffer = ByteBuffer.allocateDirect(size);
            // Dill it and reset.
            fill(buffer,channel);
            buffer.flip();
        }
    
        // The entire file is in little endian.
        buffer.order(ByteOrder.LITTLE_ENDIAN);
    
        // Set up some buffers and lookups for efficiency
        fieldTypes = new char[header.getNumFields()];
        fieldLengths = new int[header.getNumFields()];
        for (int i = 0, ii = header.getNumFields(); i < ii; i++) 
        {
            fieldTypes[i] = header.getFieldType(i);
            fieldLengths[i] = header.getFieldLength(i);
        }
    
        charBuffer = CharBuffer.allocate(header.getRecordLength() - 1);
        Charset chars = Charset.forName("ISO-8859-1");
        decoder = chars.newDecoder();
    
        row = new Row();
    }
   
    /** Reads a record.
      * @throws IOException If problem reading the record. 
      */ 
    private void read() throws IOException 
    {
        boolean foundRecord = false;
        while (!foundRecord) 
        {
            bufferCheck();
      
            // Read the deleted flag.
            char deleted = (char) buffer.get();
            if (deleted == '*') 
            {
                continue;
            }
      
            charBuffer.position(0);
            buffer.limit(buffer.position() + header.getRecordLength() - 1);
            decoder.decode(buffer,charBuffer,true);
            buffer.limit(buffer.capacity());
            charBuffer.flip();
      
            foundRecord = true;
        }
        cnt++;
    }
    
    /** Reads an object from the database.
      * @param fieldOffset Offset from the start of the record.
      * @param fieldNum FieldNum Index identifying the field to read.  
      * @param doSimple If true, all values will be either strings or numbers. If not, 
      *                 the record  can contain boolean values and dates as well as
      *                 strings and numbers. 
      * @return Object that has been read from database.
      * @throws IOException If problem reading the object. 
      */
    private Object readObject(final int fieldOffset,final int fieldNum, boolean doSimple) throws IOException 
    {
        final char type = fieldTypes[fieldNum];
        final int fieldLen = fieldLengths[fieldNum];
        Object object = null;
    
        //System.out.println( charBuffer.subSequence(fieldOffset,fieldOffset + fieldLen));
    
        if(fieldLen > 0) 
        {
            switch (type)
            {
                // (L)logical (T,t,F,f,Y,y,N,n)
                case 'l':
                case 'L':
                    switch (charBuffer.charAt(fieldOffset)) 
                    {
                        case 't': case 'T': case 'Y': case 'y':
                            if (doSimple)
                            {
                            	object = new String("true");
                            }
                            else
                            {
                            	object = Boolean.TRUE;
                            }
                            break;
                            
                        case 'f': case 'F': case 'N': case 'n':
                            if (doSimple)
                            {
                            	object = new String("false");
                            }
                            else
                            {
                            	object = Boolean.FALSE;
                            }
                            break;
                        
                        default:
                            throw new IOException("Unknown logical value : '" + charBuffer.charAt(fieldOffset) + "'");
                    }
                    break;
          
                // (C)character (String)
                case 'c':
                case 'C':
                    // oh, this seems like a lot of work to parse strings...but,
                    // For some reason if zero characters ( (int) char == 0 ) are allowed
                    // in these strings, they do not compare correctly later on down the
                    // line....
                    int start = fieldOffset;
                    int end = fieldOffset + fieldLen - 1;
                    // Trim off whitespace and 'zero' chars
                    while (start < end) 
                    {
                        char c = charBuffer.get(start);
                        if (c== 0 || Character.isWhitespace(c)) 
                        {
                            start++;
                        }
                        else
                        {
                            break;
                        }
                    }
                    while (end > start) 
                    {
                        char c = charBuffer.get(end);
                        if (c == 0 || Character.isWhitespace(c)) 
                        {
                            end--;
                        }
                        else
                        {
                            break; 
                        }
                    }
                    // Set up the new indexes for start and end
                    charBuffer.position(start).limit(end + 1);
                    String s = charBuffer.toString();
                    // This resets the limit...
                    charBuffer.clear();
                    object = s;
                    break;
                    
                // (D)date (Date)
                case 'd':
                case 'D':
                    try
                    {
                        String tempString = charBuffer.subSequence(fieldOffset,fieldOffset + 4).toString();
                        int tempYear = Integer.parseInt(tempString);
                        tempString = charBuffer.subSequence(fieldOffset + 4,fieldOffset + 6).toString();
                        int tempMonth = Integer.parseInt(tempString) - 1;
                        tempString = charBuffer.subSequence(fieldOffset + 6,fieldOffset + 8).toString();
                        int tempDay = Integer.parseInt(tempString);
                        Calendar cal = Calendar.getInstance();
                        cal.clear();
                        cal.set(Calendar.YEAR,tempYear);
                        cal.set(Calendar.MONTH, tempMonth);
                        cal.set(Calendar.DAY_OF_MONTH, tempDay);
                        if (doSimple)
                        {
                        	object =cal.getTime().toString();
                        }
                        else
                        {
                        	object = cal.getTime();
                        }
                    }
                    catch(NumberFormatException nfe)
                    {
                        // Do nothing.
                    }
                    break;
          
                // (F)floating (Double)
                case 'n':
                case 'N':
                    try 
                    {
                        if (header.getFieldDecimalCount(fieldNum) == 0) 
                        {
                            object = new Integer(numberParser.parseInt(charBuffer, fieldOffset, fieldOffset + fieldLen - 1));
                            break;
                        }
                        // else will fall through to the floating point number
                    }
                    catch (NumberFormatException e) 
                    {
                        // todo: use progresslistener, this isn't a grave error.

                        // Don't do this!!! the Double parse will be attempted as we fall
                        // through, so no need to create a new Object. -IanS
                        // object = new Integer(0);
          
                        // Lets try parsing a long instead...
                        try 
                        {
                            object = new Long(numberParser.parseLong(charBuffer,fieldOffset,fieldOffset + fieldLen - 1));
                            break;
                        }
                        catch (NumberFormatException e2) 
                        {
                            // Do nothing.
                        }
                    }
          
                    case 'f':
                    case 'F': // floating point number
                        try 
                        {
                            object = new Double(numberParser.parseDouble(charBuffer,fieldOffset, fieldOffset + fieldLen - 1));
                        }
                        catch (NumberFormatException e) 
                        {
                            // todo: use progresslistener, this isn't a grave error, though it
                            // does indicate something is wrong
           
                            // okay, now whatever we got was truly undigestable. Lets go with
                            // a zero Double.
                            object = new Double(0.0);
                        }
                        break;
                        
                    default:
                        throw new IOException("Invalid field type : " + type);
            }
        } 
        return object;
    }
    
       
    // --------------------------- Nested classes -----------------------------
  
    /** Stores an individual row in the database. 
      */
    public final class Row 
    {
        /** Reads in a single item from the database row at the given column.
          * @param column Column to read.
          * @return Object read from this row at the given column.
          * @throws IOException
          */  
        public Object read(int column) throws IOException 
        {
            int offset = getOffset(column);
            return readObject(offset, column,false);
        }
    }
}
