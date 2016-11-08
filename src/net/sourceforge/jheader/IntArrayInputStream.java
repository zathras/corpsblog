
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

import java.io.*;

/**
 * Similar to ByteArrayInputStream but for unsigned bytes
 */
public class IntArrayInputStream extends InputStream
{
    private int[] m_buf;
    private int m_numRead = 0;
    private int m_numReadSinceMark = 0;
    private int m_mark = 0;
    private int m_readAheadLimit = -1;

    /**
     * See {@link InputStream}.
     */
    public IntArrayInputStream(int[] bytes)
    {
	m_buf = bytes;
    }

    /**
     * See {@link InputStream}.
     */
    public int available()
    {
	return m_buf.length - m_numRead;
    }
    
    /**
     * See {@link InputStream}.
     */
    public void	close()
    {
    }
    
    /**
     * See {@link InputStream}.
     */
    public void mark(int readAheadLimit)
    {
	m_readAheadLimit = readAheadLimit;
	m_mark = m_numRead;
    }
    
    /**
     * See {@link InputStream}.
     */
    public boolean markSupported()
    {
	return true;
    }

    /**
     * See {@link InputStream}.
     */
    public int read()
    {
	if (m_readAheadLimit >= 0)
	{
	    m_numReadSinceMark++;
	    if (m_numReadSinceMark >= m_readAheadLimit)
	    {
		m_readAheadLimit = -1;
		m_mark = 0;
	    }
	}
	return m_buf[m_numRead++];
	
    }

    /**
     * See {@link InputStream}.
     */
    public int read(byte[] b, int off, int len)
    {
	if (m_readAheadLimit >= 0)
	{
	    m_numReadSinceMark += len;
	    if (m_numReadSinceMark >= m_readAheadLimit)
	    {
		m_readAheadLimit = -1;
		m_mark = 0;
	    }
	}
	int numRead = 0;
	for (int i=0; i<len&&m_numRead<m_buf.length; ++i,++numRead)
	    b[i+off] = (byte)m_buf[m_numRead++];
	return numRead;
    }
    
    /**
     * See {@link InputStream}.
     */
    public void	reset()
    {
	m_numRead = m_mark;
    }
    
    /**
     * See {@link InputStream}.
     */
    public long	skip(long n)
    {
	if ((int)n >= available())
	{
	    int ret = available();
	    m_numRead = m_buf.length;
	    return ret;
	}
	m_numRead += (int)n;
	return n;
    }
}
