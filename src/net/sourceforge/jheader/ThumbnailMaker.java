
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
 * Tool for creating JFIF and EXIF thumbnails.
 *
 * You probably won't have to use this directly as JpegHeaders contains
 * the neccessary convenience routines.
 */
public class ThumbnailMaker
{
    public static final int DCF_ROWS = 120;
    public static final int DCF_COLS = 160;

    public class ThumbnailDetails
    {
	public int width;
	public int height;
	public int bpp;

	private ThumbnailDetails(int w, int h, int b)
	{
	    width = w;
	    height = h;
	    bpp = b;
	}
    }

    // needed only for creating ThumbnailDetails instance
    static private ThumbnailMaker s_instance = new ThumbnailMaker();

    private ThumbnailMaker() {}
    /**
     * Creates a thumbnail with the given dimensions.
     *
     * If the image has a different aspect ratio then the top-left corner
     * is used, such that the aspect ratio is preserved.
     *
     * @param in input stream
     * @param out output stream
     * @param rows number of rows for thumbnail
     * @param cols number of columns for thumbnail
     * @param bestRotation if true, rotate thumbnail if it makes the
     *                     dimensions fit better
     * @return actual size of thumbnail
     * @throws IOException if there is an error reading or writing
     */
    public static ThumbnailDetails
    createThumbnailWithExactDimensions(InputStream in, OutputStream out,
				       int rows, int cols,
				       boolean bestRotation)
	throws IOException
    {
	// read image
	BufferedImage img = createBufferedImage(in);

	// rotate if necessary
	if (bestRotation && ((img.getHeight() > img.getWidth() && cols > rows)
			     || img.getHeight() < img.getWidth() && cols < rows))
	    img = rotate(img);

	// scale 
	float xscale = (float)cols/img.getWidth();
	float yscale = (float)rows/img.getHeight();
	int newRows, newCols;
	if (xscale > yscale)
	{
	    newRows = (int)(xscale * img.getHeight());
	    newCols = cols;
	}
	else if (xscale < yscale)
	{
	    newRows = rows;
	    newCols = (int)(yscale * img.getWidth());
	}
	else
	{
	    newRows = rows;
	    newCols = cols;
	}
	img = scale(img,newRows,newCols);

	// crop
	img = crop(img,rows,cols);

	// write img
	return writeJpeg(out,img);
    }

    /**
     * Creates a thumbnail with the DCF dimensions (160x120)
     *
     * If the image has a different aspect ratio then the top-left corner
     * is used, such that the aspect ratio is preserved.
     *
     * @param in input stream
     * @param out output stream
     * @param bestRotation if true, rotate thumbnail if it makes the
     *                     dimensions fit better
     * @return actual size of thumbnail
     * @throws IOException if there is an error reading or writing
     */
    public static ThumbnailDetails createDcfThumbnail(InputStream in,
						      OutputStream out,
						      boolean bestRotation)
	throws IOException
    {
	return createThumbnailWithExactDimensions(in, out, DCF_ROWS, DCF_COLS,
					   bestRotation);
    }

    /**
     * Creates a thumbnail with the maximum given dimensions.
     *
     * Either the rows or cols in the thumbnail will be those given.
     * If the aspect ratio is the same, both dimensions will be those given,
     * otherwise one dimension will be smaller
     *
     * @param in input stream
     * @param out output stream
     * @param rows maximum number of rows for thumbnail
     * @param cols maximum number of columns for thumbnail
     * @param bestRotation if true, rotate thumbnail if it makes the
     *                     dimensions fit better
     * @return actual size of thumbnail
     * @throws IOException if there is an error reading or writing
     */
    public static ThumbnailDetails
    createThumbnailWithMaxDimensions(InputStream in, OutputStream out,
				     int rows, int cols,
				     boolean bestRotation)
	throws IOException
    {
	// read image
	BufferedImage img = createBufferedImage(in);

	// rotate if necessary
	if (bestRotation && ((img.getHeight() > img.getWidth() && cols > rows)
			     || img.getHeight() < img.getWidth() && cols < rows))
	    img = rotate(img);
	    
	// scale 
	float xscale = (float)cols/img.getWidth();
	float yscale = (float)rows/img.getHeight();
	int newRows, newCols;
	if (xscale < yscale)
	{
	    newRows = (int)(xscale * img.getHeight());
	    newCols = cols;
	}
	else if (xscale > yscale)
	{
	    newRows = rows;
	    newCols = (int)(yscale * img.getWidth());
	}
	else
	{
	    newRows = rows;
	    newCols = cols;
	}
	img = scale(img,newRows,newCols);

	// write image
	return writeJpeg(out,img);
    }

    private static BufferedImage createBufferedImage(int[] bytes)
	throws IOException
    {
	IntArrayInputStream in = new IntArrayInputStream(bytes);
	JPEGImageDecoder decoder =
	    JPEGCodec.createJPEGDecoder(in);
	return decoder.decodeAsBufferedImage();
    }

    private static ThumbnailDetails writeJpeg(OutputStream out,
					      BufferedImage img)
	throws IOException
    {
	// make JPEG
	IntArrayOutputStream aout = new IntArrayOutputStream();
	JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(aout);
	JPEGEncodeParam param = encoder.getDefaultJPEGEncodeParam(img);
	param.setQuality(0.75f,true);
	encoder.encode(img,param);
	int[] bytes = aout.toIntArray();
	ThumbnailDetails details = null;
	
	// strip APP0 and APP1
	try
	{
	    IntArrayInputStream ain = new IntArrayInputStream(bytes);
	    JpegHeaders headers = new JpegHeaders(ain);
	    details = s_instance.new ThumbnailDetails(headers.getWidth(),
					   headers.getHeight(),
					   headers.getBitsPerPixel());
	    headers.stripAppHeaders();
	    ain = new IntArrayInputStream(bytes);
	    headers.save(ain, out);
	}
	catch (ExifFormatException e)
	{
	    // won't happen as we have stripped the EXIF header
	    e.printStackTrace();
	}
	catch (TagFormatException e)
	{
	    // won't happen as we have stripped the EXIF header
	    e.printStackTrace();
	}
	catch (JpegFormatException e)
	{
	    // shouldn't happen as Java's lib should write valid JPEG!
	    e.printStackTrace();
	    throw new IOException("Cannot read back thumbnail - Java JPEG error");
	}
	return details;
    }

    private static BufferedImage createBufferedImage(InputStream in)
	throws IOException
    {
	JPEGImageDecoder decoder = JPEGCodec.createJPEGDecoder(in);
	return decoder.decodeAsBufferedImage();
    }

    private static BufferedImage rotate(BufferedImage img)
    {
	int w = img.getWidth();
	int h = img.getHeight();
	BufferedImage newImg
	    = new BufferedImage(h,w,BufferedImage.TYPE_INT_ARGB);
	for (int i=0; i<h; ++i)
	    for (int j=0; j<w; ++j)
		newImg.setRGB(i,w-j-1,img.getRGB(j,i));
	return newImg;
    }

    private static BufferedImage crop(BufferedImage img, int rows, int cols)
    {
	int w = img.getWidth();
	int h = img.getHeight();
	int newRows = h < rows ? h : rows;
	int newCols = w < cols ? w : cols;
	if (newRows == h && newCols == w) return img; // nothing to do
	
	BufferedImage newImg
	    = new BufferedImage(newCols,newRows,BufferedImage.TYPE_INT_ARGB);
	for (int i=0; i<newRows; ++i)
	    for (int j=0; j<newCols; ++j)
		newImg.setRGB(j,i,img.getRGB(j,i));
	return newImg;
    }

    private static BufferedImage scale(BufferedImage img, int newRows,
				       int newCols)
    {
	
	int w = img.getWidth();
	int h = img.getHeight();
	if (newRows == h && newCols == w) return img; // nothing to do

	float s = (float)w/newCols; // assume xscale == yscale
	
	BufferedImage newImg
	    = new BufferedImage(newCols,newRows,BufferedImage.TYPE_INT_BGR);
	for (int y=0; y<newRows; ++y)
	    for (int x=0; x<newCols; ++x)
	    {
		int i = (int)(s*y);
		int j = (int)(s*x);
		if (i >=h) i = newRows-1;
		if (j >=w) j = newCols-1;
		
		int im = i == 0 ? 0 : i-1;
		int ip = i == newRows-1 ? 0 : i+1;
		int jm = j == 0 ? 0 : j-1;
		int jp = j == newCols-1 ? 0 : j+1;
		
		int rgba0 = img.getRGB(j,i);
		int rgba1 = img.getRGB(jm,im);
		int rgba2 = img.getRGB(jm,ip);
		int rgba3 = img.getRGB(jp,im);
		int rgba4 = img.getRGB(jp,ip);

		int a0 = (rgba0>>24) & 0xff;
		int a1 = (rgba1>>24) & 0xff;
		int a2 = (rgba2>>24) & 0xff;
		int a3 = (rgba3>>24) & 0xff;
		int a4 = (rgba4>>24) & 0xff;
		int newA = (a0>>1) + (a1>>3) + (a2>>3) + (a3>>3) + (a4>>3);

		int r0 = (rgba0>>16) & 0xff;
		int r1 = (rgba1>>16) & 0xff;
		int r2 = (rgba2>>16) & 0xff;
		int r3 = (rgba3>>16) & 0xff;
		int r4 = (rgba4>>16) & 0xff;
		int newR = (r0>>1) + (r1>>3) + (r2>>3) + (r3>>3) + (r4>>3);

		int g0 = (rgba0>>8) & 0xff;
		int g1 = (rgba1>>8) & 0xff;
		int g2 = (rgba2>>8) & 0xff;
		int g3 = (rgba3>>8) & 0xff;
		int g4 = (rgba4>>8) & 0xff;
		int newG = (g0>>1) + (g1>>3) + (g2>>3) + (g3>>3) + (g4>>3);

		int b0 = (rgba0) & 0xff;
		int b1 = (rgba1) & 0xff;
		int b2 = (rgba2) & 0xff;
		int b3 = (rgba3) & 0xff;
		int b4 = (rgba4) & 0xff;
		int newB = (b0>>1) + (b1>>3) + (b2>>3) + (b3>>3) + (b4>>3);

		int newRgb = (newA<<24) | (newR<<16) | (newG<<8) | newB;
		newImg.setRGB(x,y,newRgb);
	    }
	return newImg;
    }
}
