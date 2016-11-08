
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
import net.sourceforge.jheader.JpegHeaders.ByteOrder;

/**
 * Class for tags of UNDEFINED type.
 */
public class UndefinedTag
{
    private int[] m_bytes;
    private EnumeratedTag m_enum;

    /**
     * Construct from array of bytes.
     *
     * @param bytes the bytes, which must be between 0x00-0xff inclusive.
     */
    public UndefinedTag(int[] bytes)
    {
	m_bytes = new int[bytes.length];
	for (int i=0; i<bytes.length; ++i)
	    m_bytes[i] = bytes[i] & 0xff;
	m_enum = null;
    }

    /**
     * Construct from a unicode String.
     *
     * The lower byte of char 0 goes to byte 0, the upper byte of char 0 to
     * byte 1, the lower byte of char 2 to byte 2 etc.
     *
     * @param str the unicode string
     */
    public UndefinedTag(String str)
    {
	m_bytes = new int[str.length()*2];
	for (int i=0; i<str.length(); ++i)
	{
	    m_bytes[i*2] = ((int)str.charAt(i)) & 0xff;
	    m_bytes[i*2+1] = (((int)str.charAt(i))>>8) & 0xff;
	}
	m_enum = null;
    }

    /**
     * Construct from an enumeration.
     *
     * The long value is taken from the enumerated tag and is stored as
     * len bytes.  The enumerated tag itself can also be returned.
     *
     * @param e the enumerated tag
     * @param byteOrder the APP1 byte order
     * @param len the number of bytes this should be occupy as a tag.
     */
    public UndefinedTag(EnumeratedTag e, ByteOrder byteOrder, int len)
    {
	m_bytes = new int[len];

	if (len == 0)
	    ;
	else if (len == 1)
	{
	    m_bytes[0] = (int)(e.asLong() & 0xff);
	}
	else
	{
	    if (byteOrder == ByteOrder.MOTOROLA)
	    {
		m_bytes[1] = (int)(e.asLong() & 0xff);
		m_bytes[0] = (int)((e.asLong() & 0xff00) >> 8);
	    }
	    else
	    {
		m_bytes[0] = (int)(e.asLong() & 0xff);
		m_bytes[1] = (int)((e.asLong() & 0xff00) >> 8);
	    }
	}
	m_enum = e;
    }

    /**
     * Returns the bytes as an array of unsigned values.
     *
     * @return an array of unsigned bytes.
     */
    public int[] getBytes()
    {
	return m_bytes;
    }

    /**
     * Returns the bytes as an enumerated tag.
     *
     * @return an array of unsigned bytes.
     */
    public EnumeratedTag getEnum()
    {
	return m_enum;
    }

    /**
     * Formats the value assuming each byte is a unicode value.  Thus it
     * returns a string of 16 byte character.
     *
     * The bytes are assumed to be least significant first, ie the string is
     * constructed with 0th char byte[0]+(byte[1]<<8), the first char
     * as byte[2]+(byte[3]<<8) etc.
     */
    public String getAsUnicode()
    {
	StringBuffer buf = new StringBuffer();
	for (int i=0; i<m_bytes.length; i+=2)
	{
	    int ch = m_bytes[i];
	    if (i < m_bytes.length-1) ch += m_bytes[i+1]<<8;
	    buf.append((char)ch);
	}
	return buf.toString();
    }
    
    /**
     * Returns a string representation of this tag, truncated to given
     * number of bytes.
     *
     * A string representation means hexadecimal numbers (eg 0x1e), separated
     * by commas.
     *
     * Pass -1 to suppress truncation.
     */
    public String getAsString(int truncateTo)
    {
	if (m_enum != null) return m_enum.asString();
	
	StringBuffer buf = new StringBuffer();
	for (int i=0; i<m_bytes.length && (truncateTo<0||i<truncateTo); ++i)
	{
	    String hex = Integer.toHexString(m_bytes[i]);
	    if (i > 0) buf.append(",");
	    buf.append("0x");
	    if (hex.length() == 0) buf.append("00");
	    else if (hex.length() == 1) buf.append("0");
	    buf.append(hex);
	}
	if (truncateTo >= 0 && truncateTo < m_bytes.length)
	    buf.append(",...");
	return buf.toString();
    }

    /**
     * Returns a string representation of this tag.
     *
     * A string representation means hexadecimal numbers (eg 0x1e), separated
     * by commas.
     */
    public String getAsString()
    {
	return getAsString(-1);
    }

    /**
     * Same as getAsString(4).
     */
    public String toString()
    {
	return getAsString(4);
    }
}
