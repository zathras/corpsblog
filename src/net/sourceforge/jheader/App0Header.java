
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
import java.awt.image.BufferedImage;
import com.sun.image.codec.jpeg.*;

/**
 * <p>Information parsed from an APP0 section or APP0 extension in a JPEG file.
 * </p>
 *
 * <p>An APP0 section is always at the start of a JFIF file and is absent
 * from an EXIF file.  An APP0 extension block may also optionally appear
 * after the APP0 block (see {@link #isExt()}).</p>
 */
public class App0Header
{
    /**
     * Units of x and y density.
     *
     * ASPECT_RATIO : xDensity/yDensity is an aspect ratio<br>
     * DOTS_PER_INCH : xDensity and yDensity are dots per inch<br>
     * DOTS_PER_CM : xDensity and yDensity are dots per cm<br>
     */
    public enum Units
    {
	ASPECT_RATIO,
	DOTS_PER_INCH,
	DOTS_PER_CM
    };

    public static int THUMB_TYPE_JPEG = 0x10;
    public static int THUMB_TYPE_GREY = 0x11;
    public static int THUMB_TYPE_RGB = 0x13;
    
    static private int[] s_identifier = new int[]{0x4a,0x46,0x49,0x46,0x00};
    static private int[] s_extIdentifier = new int[]{0x4a,0x46,0x58,0x58,0x00};
    
    private int m_length = 0; // in bytes
    private int[] m_identifier = null;
    private int m_majorVersion = 0;
    private int m_minorVersion = 0;
    private Units m_units = Units.ASPECT_RATIO; // units of m_xDensitiy and m_yDensity
    private int m_xDensity = 0;
    private int m_yDensity = 0;
    private int m_xThumbnail = 0; // cols in thumbnail
    private int m_yThumbnail = 0; // rows in thumbnail
    private int[] m_thumbnailRGB = null; // thumbnail
    private int[] m_thumbnailGrey = null; // thumbnail
    private int[] m_thumbnailJpeg = null; // thumbnail
    private int m_extThumbnailType = 0;
    private boolean m_isExt = false;

    /**
     * Constructs an empty APP0 tag.
     *
     * @param isExt if true, make this an APP0 extension tag
     */
    App0Header(boolean isExt)
    {
	m_isExt = isExt;
    }

    /**
     * Constructs from a stream of bytes read from the JPEG file.
     *
     * @param data entire APP0 block excluding the marker itself.
     *
     * @throws JpegFormatException if the file is not legal JPEG.
     * @throws IOException if there is an error decoding the thumbnail
     */
    App0Header(int[] data) throws JpegFormatException, IOException
    {
	if (data.length < 16)
	    throw new JpegFormatException("Error reading APP0 tag");
	m_length = readIntLittleEndian(data,0,2);
	m_identifier = copyBytes(data,2,5);
	if (Arrays.equals(s_identifier,m_identifier))
	{
	    m_majorVersion = readIntLittleEndian(data,7,1);
	    m_minorVersion = readIntLittleEndian(data,8,1);
	    int units = readIntLittleEndian(data,9,1);
	    if (units == 0) m_units = Units.ASPECT_RATIO;
	    else if (units == 1) m_units = Units.DOTS_PER_INCH;
	    else if (units == 2) m_units = Units.DOTS_PER_CM;
	    else throw new JpegFormatException("Error reading APP0 tag");
	    m_xDensity = readIntLittleEndian(data,10,2);
	    m_yDensity = readIntLittleEndian(data,12,2);
	    m_xThumbnail = readIntLittleEndian(data,14,1);
	    m_yThumbnail = readIntLittleEndian(data,15,1);
	    if (data.length < 16+(m_xThumbnail*m_yThumbnail*3))
		throw new JpegFormatException("Error reading APP0 tag " + data.length);
	    m_thumbnailRGB = copyBytes(data,16,m_xThumbnail*m_yThumbnail*3);
	    
	    encodeThumbnail();
	} // if (Arrays.equals(s_identifier,m_identifier))
	else if (Arrays.equals(s_extIdentifier,m_identifier))
	{
	    m_extThumbnailType = data[7];
	    if (m_extThumbnailType == THUMB_TYPE_JPEG)
	    {
		m_thumbnailJpeg = copyBytes(data,8,data.length-8);
		decodeThumbnail();
	    }
	    else if (m_extThumbnailType == THUMB_TYPE_GREY)
	    {
		m_xThumbnail = readIntLittleEndian(data,14,1);
		m_yThumbnail = readIntLittleEndian(data,15,1);
		if (data.length < 16+(m_xThumbnail*m_yThumbnail))
		    throw new JpegFormatException("Error reading APP0 tag");
		m_thumbnailGrey = copyBytes(data,16,m_xThumbnail*m_yThumbnail);
		encodeThumbnail();
	    }
	    else if (m_extThumbnailType == THUMB_TYPE_RGB)
	    {
		m_xThumbnail = readIntLittleEndian(data,14,1);
		m_yThumbnail = readIntLittleEndian(data,15,1);
		if (data.length < 16+(m_xThumbnail*m_yThumbnail*3))
		    throw new JpegFormatException("Error reading APP0 tag");
		m_thumbnailRGB
		    = copyBytes(data,16,m_xThumbnail*m_yThumbnail*3);
		encodeThumbnail();
	    }
	    else
		throw new JpegFormatException("Error reading APP0 EXT tag");
	    
	    m_isExt = true;
	    
	    System.out.println("Length=" + m_length 
			       +",xthumbnail="+m_xThumbnail+",ythumbnail="
			       +m_yThumbnail);
	} // if (Arrays.equals(s_identifier,m_identifier))
    }

    private int readIntBigEndian(int[] data, int off, int len)
    {
	int offset = 0;
	int shift = 0;
	for (int i=0; i<len; ++i)
	{
	    offset |= data[i+off] << shift;
	    shift += 8;
	}
	return offset;
    }

    private int readIntLittleEndian(int[] data, int off, int len)
    {
	int offset = 0;
	int shift = 0;
	for (int i=len-1; i>=0; --i)
	{
	    offset |= data[i+off] << shift;
	    shift += 8;
	}
	return offset;
    }

    private void writeIntBigEndian(int[] data, int off, int len, int num)
    {
	int shift = 0;
	for (int i=0; i<len; ++i, shift+=8)
	    data[i+off] = (num>>shift) & 0xff;
    }

    private void writeIntLittleEndian(int[] data, int off, int len, int num)
    {
	int shift = 0;
	for (int i=len-1; i>=0; --i, shift+=8)
	    data[i+off] = (num>>shift) & 0xff;
    }

    private int[] copyBytes(int[] data, int o, int n)
    {
	int[] newData = new int[n];
	for (int i=0; i<n; ++i)
	{
	    newData[i] = data[i+o];
	}
	return newData;
    }
 
    private void copyBytes(int[] to, int[] from, int tooff)
    {
	for (int i=0; i<from.length; ++i)
	    to[i+tooff] = from[i];
    }

   /**
     * Return the units of xDensity and yDensity.
     *
     * @return the units
     */
    public Units getUnits() {return m_units;}

    /**
     * Set the units of xDensity and yDensity.
     *
     * @param value the new value.
     */
    public void setUnits(Units value) {m_units = value;}

    /**
     * Return the x density.
     *
     * @return the density.
     */
    public int getXDensity() {return m_xDensity;}

    /**
     * Set the x density.
     *
     * @param value the new value.
     */
    public void setXDensity(int value) {m_xDensity = value;}

    /**
     * Return the y density.
     *
     * @return the density.
     */
    public int getYDensity() {return m_yDensity;}

    /**
     * Set the y density.
     *
     * @param value the new value.
     */
    public void setYDensity(int value) {m_yDensity = value;}

    /**
     * Return the number of cols in the thumbnail.
     *
     * @return the number of columns.
     */
    public int getXThumbnail() {return m_xThumbnail;}
    

    /**
     * Return the number of rows in the thumbnail.
     *
     * @return the number of rows.
     */
    public int getYThumbnail() {return m_yThumbnail;}

    /**
     * Return the thumbnail RGB values in scanlines of r,g,b triplets.
     *
     * @return xThumbnail*yThumbnail*3 ints.
     */
    public int[] getThumbnailAsRGB()
    {
	return m_thumbnailRGB;
    }

    /**
     * Return the thumbnail image as a JPEG.
     *
     * @return the thumbnail as a JPEG.
     */
    public int[] getThumbnailAsJpeg()
    {
	return m_thumbnailJpeg;
    }

    /**
     * Returns the thumbnail type if this is an APP0 extension, 0 otherwise
     *
     * @return 0, THUMB_TYPE_JPEG, THUMB_TYPE_GREY, THUMB_TYPE_RGB
     */
    public int getThumbnailType()
    {
	return m_extThumbnailType;
    }

    /**
     * Sets the thumbnail from RGB values.
     *
     * <p>Bytes are in scanline order as red,green,blue triples
     * (thus rows*cols*3 ints).</p>
     *
     * <p>If this is an APP0 extension block the this causes the thumbnail
     * to be stored as uncompressed RGB values.</p>
     *
     * @param cols the number of columns in the data passed.
     * @param rows the number of rows in the data passed.
     * @param bytes the thumbnail bytes in scanline order.
     *
     * @throws IOException if there is an error decoding the thumbnail
     */
    public void setThumbnailAsRGB(int cols, int rows, int[] bytes)
	throws IOException
    {
	if (m_isExt)
	    m_extThumbnailType = THUMB_TYPE_RGB;
	m_thumbnailRGB = bytes;
	m_thumbnailGrey = null;
	m_xThumbnail = cols;
	m_yThumbnail = rows;
	encodeThumbnail();
    }

    /**
     * Sets the thumbnail from grey values.
     *
     * <p>Bytes are in scanline order (thus rows*cols*3 ints).</p>
     *
     * <p>If this is an APP0 extension block the this causes the thumbnail
     * to be stored as uncompressed RGB values.  If this is not an extension
     * block then the thumbnail is converted to RGB before storing.
     * </p>
     *
     * @param cols the number of columns in the data passed.
     * @param rows the number of rows in the data passed.
     * @param bytes the thumbnail bytes in scanline order.
     *
     * @throws JpegFormatException if this is not an APP0 extension tag.
     * @throws IOException if there is an error decoding the thumbnail
     */
    public void setThumbnailAsGrey(int cols, int rows, int[] bytes)
	throws IOException, JpegFormatException
    {
	if (m_isExt)
	    m_extThumbnailType = THUMB_TYPE_GREY;
	m_thumbnailRGB = null;
	m_thumbnailGrey = bytes;
	m_xThumbnail = cols;
	m_yThumbnail = rows;
	encodeThumbnail();
    }

    /**
     * Sets the thumbnail from a JPEG.
     *
     * <p>If this is an APP0 extension block the this causes the thumbnail
     * to be stored as a JPEG.  If this is not an extension
     * block then the thumbnail is uncompressed to RGB before storing.
     * </p>
     *
     * @param bytes the thumbnail bytes as a JPEG
     *
     * @throws IOException if there is an error decoding the thumbnail
     */
    public void setThumbnailAsJpeg(int[] bytes)
	throws IOException
    {
	if (m_isExt)
	    m_extThumbnailType = THUMB_TYPE_JPEG;
	m_thumbnailJpeg = bytes;
	decodeThumbnail();
    }

    /**
     * Returns true iff this header is an APP0 extension header.
     *
     * A JFIF file can potentially contain two APP0 headers.  The second
     * is called the APP0 extension; its purpose is to store the thumbnail
     * as some format other than RGB.  The regular APP0 header must always
     * be present in a JFIF file; the extension header may additionally
     * appear.
     */
    public boolean isExt() {return m_isExt;}

    /**
     * Writes APP0 tag to an array of bytes.
     */
    int[] write()
    {
	int[] data = null;
	if (!m_isExt)
	{
	    m_length = 16 + m_thumbnailRGB.length;
	    data = new int[16+m_thumbnailRGB.length];
	    writeIntLittleEndian(data,0,2,m_length);
	    copyBytes(data,m_identifier,2);
	    writeIntLittleEndian(data,7,1,m_majorVersion);
	    writeIntLittleEndian(data,8,1,m_minorVersion);
	    if (m_units == Units.ASPECT_RATIO)
		writeIntLittleEndian(data,9,1,0);
	    else if (m_units == Units.DOTS_PER_INCH)
		writeIntLittleEndian(data,9,1,1);
	    else //if (m_units == Units.DOTS_PER_CM)
		writeIntLittleEndian(data,9,1,2);
	    writeIntLittleEndian(data,10,2,m_xDensity);
	    writeIntLittleEndian(data,12,2,m_yDensity);
	    writeIntLittleEndian(data,14,1,m_xThumbnail);
	    writeIntLittleEndian(data,15,1,m_yThumbnail);
	    copyBytes(data,m_thumbnailRGB,16);
	}
	else
	{
	    if (m_extThumbnailType == THUMB_TYPE_JPEG)
		m_length = 8 + m_thumbnailJpeg.length;
	    else if (m_extThumbnailType == THUMB_TYPE_GREY)
		m_length = 8 + m_thumbnailGrey.length;
	    else //if (m_extThumbnailType == THUMN_TYPE_RGB)
		m_length = 8 + m_thumbnailRGB.length;

	    if (m_extThumbnailType == THUMB_TYPE_JPEG)
		data = new int[8+m_thumbnailJpeg.length];
	    else if (m_extThumbnailType == THUMB_TYPE_GREY)
		data = new int[8+m_thumbnailGrey.length];
	    else //if (m_extThumbnailType == THUMB_TYPE_RGB)
		data = new int[8+m_thumbnailRGB.length];
	    writeIntLittleEndian(data,0,2,m_length);
	    copyBytes(data,m_identifier,2);
	    data[7] = m_extThumbnailType;
	    if (m_extThumbnailType == THUMB_TYPE_JPEG)
		copyBytes(data,m_thumbnailJpeg,8);
	    else if (m_extThumbnailType == THUMB_TYPE_GREY)
		copyBytes(data,m_thumbnailGrey,8);
	    else //if (m_extThumbnailType == THUMN_TYPE_RGB)
		copyBytes(data,m_thumbnailRGB,8);
	}
	return data;
    }
    
    private void encodeThumbnail() throws IOException
    {
	if (m_thumbnailRGB == null) m_thumbnailJpeg = null;
	else if (m_thumbnailRGB.length == 0)
	    m_thumbnailJpeg = null;
	else
	{
	    BufferedImage img = null;
	    if (m_extThumbnailType == THUMB_TYPE_GREY)
	    {
		img = new BufferedImage(m_xThumbnail,m_yThumbnail,
				    BufferedImage.TYPE_USHORT_GRAY);
		for (int i=0; i<m_yThumbnail; ++i)
		    for (int j=0; j<m_xThumbnail; ++j)
			img.setRGB(j,i,m_thumbnailGrey[i*m_xThumbnail+j]);
	    }
	    else
	    {
		img = new BufferedImage(m_xThumbnail,m_yThumbnail,
				    BufferedImage.TYPE_INT_ARGB);
		for (int i=0; i<m_yThumbnail; ++i)
		    for (int j=0; j<m_xThumbnail; ++j)
		    {
			int r = m_thumbnailRGB[i*m_xThumbnail+j];
			int g = m_thumbnailRGB[i*m_xThumbnail+j+1];
			int b = m_thumbnailRGB[i*m_xThumbnail+j+2];
			int argb = 0xff000000 | (r&0xff)<<16
			    | (g&0xff)<<8 | (b&0xff);
			img.setRGB(j,i,argb);
		    }
	    }
	    ByteArrayOutputStream out = new ByteArrayOutputStream();
	    JPEGImageEncoder encoder =
		JPEGCodec.createJPEGEncoder(out);
	    JPEGEncodeParam param =
		encoder.getDefaultJPEGEncodeParam(img);
	    param.setQuality(0.75f,true);
	    encoder.encode(img,param);
	    byte[] bytes = out.toByteArray();
	    if (bytes == null) m_thumbnailJpeg = null;
	    else m_thumbnailJpeg = new int[bytes.length];
	    for (int i=0; i<bytes.length; ++i)
	    {
		m_thumbnailJpeg[i] = bytes[i];
		// damned java and its lack of unisigned types
		if (m_thumbnailJpeg[i] < 0) 
		    m_thumbnailJpeg[i] = 256+m_thumbnailJpeg[i];
	    }
	}
    }

    private void decodeThumbnail() throws IOException
    {
	if (m_thumbnailJpeg == null) m_thumbnailRGB = null;
	else if (m_thumbnailJpeg.length == 0)
	    m_thumbnailRGB = null;
	else
	{
	    byte[] bytes = new byte[m_thumbnailJpeg.length];
	    for (int i=0; i<m_thumbnailJpeg.length; ++i)
		bytes[i] = (byte)m_thumbnailJpeg[i];
	    ByteArrayInputStream in = new ByteArrayInputStream(bytes);
	    JPEGImageDecoder decoder =
		JPEGCodec.createJPEGDecoder(in);
	    BufferedImage img = decoder.decodeAsBufferedImage();
	    if (img == null) m_thumbnailRGB = null;
	    else
	    {
		if (m_extThumbnailType == THUMB_TYPE_GREY)
		{
		    m_thumbnailRGB = null;
		    m_thumbnailGrey = new int[img.getWidth()*img.getHeight()];
		    m_xThumbnail = img.getWidth();
		    m_yThumbnail = img.getHeight();
		    for (int i=0; i<img.getHeight(); ++i)
			for (int j=0; j<img.getWidth(); ++j)
			{
			    int argb = img.getRGB(j,i);
			    int r = (argb&0x00ff0000)>>16;
			    int g = (argb&0x0000ff00)>>8;
			    int b = (argb&0x000000ff);
			    m_thumbnailGrey[i*m_xThumbnail] = argb&0xff;
			}
		}
		else
		{
		    m_thumbnailGrey = null;
		    m_thumbnailRGB = new int[img.getWidth()*img.getHeight()*3];
		    m_xThumbnail = img.getWidth();
		    m_yThumbnail = img.getHeight();
		    for (int i=0; i<img.getHeight(); ++i)
			for (int j=0; j<img.getWidth(); ++j)
			{
			    int argb = img.getRGB(j,i);
			    int r = (argb&0x00ff0000)>>16;
			    int g = (argb&0x0000ff00)>>8;
			    int b = (argb&0x000000ff);
			    m_thumbnailRGB[i*m_xThumbnail+j] = r;
			    m_thumbnailRGB[i*m_xThumbnail+j+1] = g;
			    m_thumbnailRGB[i*m_xThumbnail+j+2] = b;
			}
		}
	    }
	}
    }
}
