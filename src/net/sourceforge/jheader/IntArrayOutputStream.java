
/*
JHeader, a Java library and tools for reading and editing EXIF headers.
Copyright (C) 2005 Matthew Baker

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.

For further information, please contact Matthew Baker on msb@safe-mail.net.
Project home page: www.sourceforge.net/jheader
*/
package net.sourceforge.jheader;
import java.util.*;
import java.io.*;

/**
 * Like {@link ByteArrayOutputStream} but writes to an array of ints.
 *
 * Sign is automatically converted.
 */
public class IntArrayOutputStream extends OutputStream
{
    private Vector<Integer> m_buf;

    /**
     * Construct a new stream
     */
    public IntArrayOutputStream()
    {
	m_buf = new Vector<Integer>();
    }

    /**
     * Construct a new stream with given initial capacity
     *
     * @param capacity initial capacity
     */
    public IntArrayOutputStream(int capacity)
    {
	m_buf = new Vector<Integer>(capacity);
    }

    /**
     * Does nothing
     */
    public void	close()
    {
    }
    
    /**
     * See {@link OutputStream}.
     */
    public void	reset()
    {
	m_buf = new Vector<Integer>();
    }
    
    /**
     * Returns the number of bytes already written.
     *
     * @return number of bytes written
     */
    public int size()
    {
	return m_buf.size();
    }

    /**
     * Returns the bytes written so far.
     *
     * @return array of unsigned bytes written
     */
    public int[] toIntArray()
    {
	int[] bytes = new int[m_buf.size()];
	for (int i=0; i<bytes.length; ++i)
	    bytes[i] = m_buf.elementAt(i);
	return bytes;
    }

    /**
     * Writes the given bytes.
     *
     * Any negative values are converted to positive by two's complement.
     *
     * @param b the bytes to write.
     * @param off offset to start writing from.
     * @param len number of bytes to write.
     */
    public void write(byte[] b, int off, int len)
    {
	for (int i=0; i<len; ++i)
	{
	    int v = b[i+off];
	    if (v < 0) v = 256 + v;
	    m_buf.add(v);
	}
    }
    
    /**
     * Writes the given byte.
     *
     * Negative values are converted to positive by two's complement.
     *
     * @param b the byte to write.
     */
    public void write(int b)
    {
	if (b < 0) b = 256 + b;
	m_buf.add(b);
    }
    
    /**
     * Writes everything to the given output stream.
     *
     * @param out stream to write to.
     */
    public void writeTo(OutputStream out)
	throws IOException
    {
	for (Integer i : m_buf)
	    out.write(i.intValue());
    }
}
