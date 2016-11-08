
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
 * Convenient way of representing a portion of an int[] array.
 */
public class SubIntArray
{
    private int[] m_origArray;
    private int m_offset;
    private int m_length;
    private ByteOrder m_origByteOrder;
    private ByteOrder m_desiredByteOrder;
    
    /**
     * Construct from an original array that preserves byte order.
     *
     * Does not do bounds checking.
     *
     * @param array original int[] array.
     * @param offset point at which the subarray will start.
     * @param length length of subarray.
     */
    public SubIntArray(int[] array, int offset, int length)
    {
	m_origArray = array;
	m_offset = offset;
	m_length = length;
	m_origByteOrder = ByteOrder.MOTOROLA;
	m_desiredByteOrder = ByteOrder.MOTOROLA;
    }

    /**
     * Construct from an original array altering byte order.
     *
     * Does not do bounds checking.
     *
     * @param array original int[] array.
     * @param offset point at which the subarray will start.
     * @param length length of subarray.
     * @param origByteOrder byte order in original array
     * @param desiredByteOrder desired byte order
     */
    public SubIntArray(int[] array, int offset, int length,
		       ByteOrder origByteOrder, ByteOrder desiredByteOrder)
    {
	m_origArray = array;
	m_offset = offset;
	m_length = length;
	m_origByteOrder = origByteOrder;
	m_desiredByteOrder = desiredByteOrder;
    }

    /**
     * Construct from a subarray
     *
     * Does not do bounds checking.
     *
     * @param array int[] array.
     * @param offset point at which the subarray will start, relative to
     *               passed array.
     * @param length length of new subarray.
     */
    public SubIntArray(SubIntArray array, int offset, int length)
    {
	m_origArray = array.m_origArray;
	m_offset = offset + array.m_offset;
	m_length = length;
	m_origByteOrder = array.m_origByteOrder;
	m_desiredByteOrder = array.m_origByteOrder;
    }

    /**
     * Construct from an original array altering byte order.
     *
     * Does not do bounds checking.
     *
     * @param array int[] array.
     * @param offset point at which the subarray will start, relative to
     *               passed array.
     * @param length length of byte array.
     * @param desiredByteOrder desired byte order.
     */
    public SubIntArray(SubIntArray array, int offset, int length,
		       ByteOrder desiredByteOrder)
    {
	m_origArray = array.m_origArray;
	m_offset = offset + array.m_offset;
	m_length = length;
	m_origByteOrder = array.m_origByteOrder;
	m_desiredByteOrder = desiredByteOrder;
    }

    /**
     * Construct from an original array reassigning and altering byte order.
     *
     * Does not do bounds checking.
     *
     * @param array int[] array.
     * @param offset point at which the subarray will start, relative to
     *               passed array.
     * @param length length of byte array.
     * @param origByteOrder byte order of passed subarray (not necessarily that
     *                      of the original array not the desired byte order of
     *                      the passed subarray).
     * @param desiredByteOrder desired byte order.
     */
    public SubIntArray(SubIntArray array, int offset, int length,
		       ByteOrder origByteOrder, ByteOrder desiredByteOrder)
    {
	m_origArray = array.m_origArray;
	m_offset = offset + array.m_offset;
	m_length = length;
	m_origByteOrder = origByteOrder;
	m_desiredByteOrder = desiredByteOrder;
    }

    /**
     * Returns the given byte in the subarray.
     *
     * Does byte swapping if it was specified in the constructor.  Does
     * no bounds checking.
     *
     * @param i the index
     * @return the array cell.
     */
    public int get(int i)
    {
	if (m_origByteOrder == m_desiredByteOrder)
	    return m_origArray[i+m_offset];
	return m_origArray[m_length-i-1+m_offset];
    }

    /**
     * Returns the given byte in the subarray, overriding byte order
     *
     * Does no bounds checking.
     *
     * @param i index to retrieve.
     * @param desiredByteOrder desired byte order.
     * @return the array cell.
     */
    public int get(int i, ByteOrder desiredByteOrder)
    {
	if (m_origByteOrder == desiredByteOrder)
	    return m_origArray[i+m_offset];
	return m_origArray[m_length-i-1+m_offset];
    }

    /**
     * Sets the given byte in the subarray.
     *
     * Does byte swapping if it was specified in the constructor.  Does
     * no bounds checking.
     */
    public void set(int i, int value)
    {
	if (m_origByteOrder == m_desiredByteOrder)
	    m_origArray[i+m_offset] = value;
	m_origArray[m_length-i-1+m_offset] = value;
    }
    
    /**
     * Sets the given byte in the subarray, orderriding the byte order
     *
     * Does byte swapping if it was specified in the constructor.  Does
     * no bounds checking.
     *
     * @param i the index to set
     * @param value the value to set it to
     * @param desiredByteOrder the desired byte order.
     */
    public void set(int i, int value, ByteOrder desiredByteOrder)
    {
	if (m_origByteOrder == desiredByteOrder)
	    m_origArray[i+m_offset] = value;
	m_origArray[m_length-i-1+m_offset] = value;
    }
    
    /**
     * Returns the length of the subarray.
     *
     * @return length of the subarray.
     */
    public int length()
    {
	return m_length;
    }

    /**
     * Element by element comparison with another subarray.
     *
     * Does bounds checking.
     *
     * @param other subarray to compare with.
     * @return true if and only if this objects elements equal the passed
     * objects elements.
     */
    public boolean equals(SubIntArray other)
    {
	if (m_length != other.m_length) return false;
	for (int i=0; i<m_length; ++i)
	    if (get(i) != other.get(i)) return false;
	return true;
    }

    /**
     * Element by element comparison with an array.
     *
     * Does bounds checking.
     *
     * @param other array to compare with.
     * @return true if and only if this objects elements equal the passed
     * objects elements.
     */
    public boolean equals(int[] other)
    {
	if (m_length != other.length) return false;
	for (int i=0; i<m_length; ++i)
	    if (get(i) != other[i]) return false;
	return true;
    }

    /**
     * Element by element comparison with another subarray.
     *
     * Does bounds checking.
     *
     * @param other subarray to compare with.
     * @param n compare only first n bytes.
     * @return true if and only if this objects elements equal the passed
     * objects elements.
     */
    public boolean equals(SubIntArray other, int n)
    {
	if (m_length < n || other.m_length < n) return false;
	for (int i=0; i<n; ++i)
	{
	    if (i >= m_length || i >= other.m_length) return false;
	    if (get(i) != other.get(i)) return false;
	}
	return true;
    }

    /**
     * Element by element comparison with an array.
     *
     * Does bounds checking.
     *
     * @param other array to compare with
     * @param n compare only first n bytes.
     * @return true if and only if this objects elements equal the passed
     * objects elements.
     */
    public boolean equals(int[] other, int n)
    {
	if (m_length < n || other.length < n) return false;
	for (int i=0; i<n; ++i)
	{
	    if (i >= m_length || i >= other.length) return false;
	    if (get(i) != other[i]) return false;
	}
	return true;
    }

    /**
     * Return subarray as a new array
     *
     * @return an array
     */
    public int[] toArray()
    {
	int[] ret = new int[m_length];
	for (int i=0; i<m_length; ++i)
	    ret[i] = get(i);
	return ret;
    }

    /**
     * Return subarray as a new array
     *
     * @param n copy at most n bytes
     * @return an array
     */
    public int[] toArray(int n)
    {
	int[] ret = new int[m_length];
	for (int i=0; i<m_length&&i<n; ++i)
	    ret[i] = get(i);
	return ret;
    }
}
