
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

import net.sourceforge.jheader.JpegHeaders.*;
import net.sourceforge.jheader.App1Header.*;

/**
 * Command line tool for manipulating JPEG headers
 *
 * <p><b>Usage</b></p>
 *
 * <p>java CJHeaderTool command [options] filename</p>
 *
 * <p>eg java CJHeaderTool show -bylocation myjpeg.jpg</p>
 *
 * <table border="1" cellspacing="0">
 * <tr>
 *   <td><b>Command</b></td>
 *   <td><b>Description</b></td>
 *   <td><b>Options</b></td>
 * <tr>
 * <tr>
 *   <td>show</td>
 *   <td>Display all tags in the given filename.</td>
 *   <td>
 *      -bylocation: show tags broken down by location within the
 *      EXIF header.<br> -raw: show tags keyword instead of textual name
 *      and numeric values instead of textual description.
 *   </td>
 * </tr>
 *
 * <tr>
 *   <td>getthumb</td>
 *   <td>Extract JPEG thumbnail (from APP1 if it exists, otherwise APP0).</td>
 *   <td>
 *     -filename:   Filename to write thumbmail to.
 *   </td>
 * </tr>
 *
 * <tr>
 *   <td>available</td>
 *   <td>Shows tags that may be set in the given filename.</td>
 *   <td></td>
 * </tr>
 *
 * <tr>
 *   <td>get</td>
 *   <td>Returns the value of a tag in the file.</td>
 *   <td>
 *     -tag:   Name of tag to set (eg XResolution - case insensitive).
 *   </td>
 * </tr>
 *
 * <tr>
 *   <td>set</td>
 *   <td>Sets a tag and updates the file.</td>
 *   <td>
 *      -tag:   Name of tag to set (eg XResolution - case insensitive).<br>
 *      -value: New value (eg "15", "72/1" "Foo bar" etc).
 *   </td>
 * </tr>
 *
 * <tr>
 *   <td>comments</td>
 *   <td>Manipulates JPEG comments (those with the M_COM marker).</td>
 *   <td>
 *      -add:   Comment to add.<br>
 *      -removeall: remove all comments).
 *   </td>
 * </tr>
 *
 * <tr>
 *   <td>makethumb</td>
 *   <td>Creates and stores a thumbnail.</td>
 *   <td>
 *      -rows:   Thumbnail will have no more than this number of rows.<br>
 *      -cols:   Thumbnail will have no more than this number of cols.<br>
 *      -exact:  Thumbnail will be exactly cols x rows (by cropping).<br>
 *      -rotate: Rotate thumbnail if it will fit the dimensions better.
 *      -removeall: remove all comments).
 *   </td>
 * </tr>
 *
 * <tr>
 *   <td>toexif</td>
 *   <td>Converts a file to EXIF.</td>
 *   <td>
 *   </td>
 * </tr>
 * </table>
 */
public class CJHeaderTool
{
    private static void usage()
    {
	StringBuffer buf = new StringBuffer();
	buf.append("Usage: java CJHeaderTool command [options] filename\n\n");
	buf.append("Command: show\n");
	buf.append("Options: -bylocation; -raw\n\n");
	buf.append("Command: getthumb\n");
	buf.append("Options: -filename name (req)\n\n");
	buf.append("Command: available\n");
	buf.append("Options: \n\n");
	buf.append("Command: get\n");
	buf.append("Options: -tag tagname\n\n");
	buf.append("Command: set\n");
	buf.append("Options: -tag tagname -value value\n\n");
	buf.append("Command: comments\n");
	buf.append("Options: -add comment -removeall\n\n");
	buf.append("Command: toexif\n");
	buf.append("Options: \n\n");
	buf.append("Command: makethumb\n");
	buf.append("Options: -rows -cols -exact -rotate\n\n");

	System.err.println(buf.toString());
	System.exit(1);
    }

    private CJHeaderTool() {}
    
    private static void doShow(String[] options, String filename)
	throws IOException, JpegFormatException, TagFormatException,
	ExifFormatException
    {
	JpegHeaders headers = new JpegHeaders(filename);
	boolean raw = false;
	boolean byLocation = false;
	for (String opt : options)
	{
	    if (opt.equals("-raw")) raw = true;
	    else if (opt.equals("-bylocation")) byLocation = true;
	    else usage();
	}

	App1Header app1Header = headers.getApp1Header();
	if (headers.getFileType() == FileType.JFIF)
	    System.out.println("JFIF file " + headers.getWidth() + "x" + headers.getHeight() + "x" + headers.getBitsPerPixel());
	else if (headers.getFileType() == FileType.EXIF)
	    System.out.println("EXIF file " + headers.getWidth() + "x" + headers.getHeight() + "x" + headers.getBitsPerPixel());
	else
	    System.out.println("JPEG file " + headers.getWidth() + "x" + headers.getHeight() + "x" + headers.getBitsPerPixel());
	System.out.println();

	String[] comments = headers.getComments();
	System.out.println("Comments:\n");
	for (String comment : headers.getComments())
	    System.out.println(comment);
	System.out.println();

	if (app1Header != null)
	{
	    boolean first = true;
	    if (byLocation)
	    {
		for (ExifLocation loc : ExifLocation.values())
		{
		    if (first)
		    first = false;
		    else
			System.out.println();
		    
		    System.out.println(loc + ":");
		    SortedMap<Tag,TagValue> tags = app1Header.getTags(loc);
		    for (Tag tag : tags.keySet())
		    {
			TagValue value = tags.get(tag);
			if (!tag.name.equals("Unknown"))
			{
			    if (raw)
			    {
				String value1;
				if (EnumeratedTag.class.isAssignableFrom(tag.implementingClass))
				    value1 = ""+value.getAsEnumeration().asLong();
				else
				    value1 = value.toString();
				if (tag.editable)
				    System.out.println(tag + " = " + value1);
				else
				    System.out.println("("+tag + " = " + value1+")");
			    }
			    else
			    {
				if (tag.editable)
				    System.out.println(tag.name + " = " + value.toString());
				else
				    System.out.println("("+tag.name + " = " + value.toString()+")");
			    }
			}
		    }
		}
	    } // if (byLocation)
	    else
	    {
		SortedMap<Tag,TagValue> tags = app1Header.getTags();
		for (Tag tag : tags.keySet())
		{
		    TagValue value = tags.get(tag);
		    if (!tag.name.equals("Unknown"))
		    {
			if (raw)
			{
			    String value1;
			    if (EnumeratedTag.class.isAssignableFrom(tag.implementingClass))
				value1 = ""+value.getAsEnumeration().asLong();
			    else
				value1 = value.toString();
			    if (tag.editable)
				System.out.println(tag + " = " + value1);
			    else
				System.out.println("("+tag + " = " + value1+")");
			}
			else
			{
			    if (tag.editable)
				System.out.println(tag.name + " = " + value.toString());
			    else
				System.out.println("("+tag.name + " = " + value.toString()+")");
			}
		    }
		}
	    } // !if (byLocation)
	}
    }
	
    private static void doGetThumb(String[] options, String filename)
	throws IOException, JpegFormatException, TagFormatException,
	ExifFormatException
    {
	String thumbFilename = null;
	for (int i=0; i<options.length; ++i)
	{
	    if (options[i].equals("-filename") && i < options.length-1)
		thumbFilename = options[++i];
	    else usage();
	}
	if (thumbFilename == null) usage();
	
	File file = new File(thumbFilename);
	if (file.exists()) file.delete();
	JpegHeaders headers = new JpegHeaders(filename);
	if (headers.getApp1Header() != null)
	{
	    int[] thumb = headers.getApp1Header().getThumbnailBytes();
	    if (thumb != null)
	    {
		FileOutputStream fstream = new FileOutputStream(thumbFilename);
		DataOutputStream stream = new DataOutputStream(fstream);
		for (int b : thumb)
		    stream.writeByte(b);
		stream.close();
		fstream.close();
	    }
	}
    }

    private static void doAvailable(String[] options, String filename)
	throws IOException, JpegFormatException, TagFormatException,
	ExifFormatException
    {
	JpegHeaders headers = new JpegHeaders(filename);
	App1Header app1Header = headers.getApp1Header();
	if (app1Header == null) return;
	CameraType camera = app1Header.getCameraType();
	SortedSet<Tag> tags = app1Header.getAvailableTags(camera);
	System.out.println("Available tags for " + camera.name + ":");
	for (Tag tag : tags)
	{
	    System.out.println(tag + " (0x" + HexUtils.toHex((long)tag.id)
			       + ") location " + tag.location + " format "
			       + tag.format + " name " + tag.name);
	    try
	    {
		if (EnumeratedTag.class.isAssignableFrom(tag.implementingClass))
		{		    
		    EnumeratedTag etag
			= EnumeratedTag.instantiate(tag.implementingClass,
						    (long)0);
		    SortedMap<Long,String> values = etag.getAll(false);
		    for (Long num : values.keySet())
			System.out.println("  "+num+" = " + values.get(num));
		}
	    }
	    catch (Exception e)
	    {
		e.printStackTrace();
	    }
	}
    }

    private static void doSet(String[] options, String filename)
	throws IOException, JpegFormatException, TagFormatException,
	ExifFormatException
    {
	String tag_s = null;
	String value = null;
	for (int i=0; i<options.length; ++i)
	{
	    if (options[i].equals("-tag")) tag_s = options[++i].toUpperCase();
	    else if (options[i].equals("-value")) value = options[++i];
	    else usage();
	}
	if (tag_s == null) usage();
	Tag tag = Tag.UNKNOWN;
	for (Tag t : Tag.values())
	{
	    if (t.toString().equals(tag_s))
	    {
		tag = t;
		break;
	    }
	}
	if (tag == Tag.UNKNOWN)
	{
	    System.err.println("Tag " + tag_s + " not found");
	}
	else
	{
	    JpegHeaders headers = new JpegHeaders(filename);
	    App1Header app1 = headers.getApp1Header();
	    if (app1 != null)
	    {
		 app1.setValue(tag, value);
		 headers.save(true);
	    }
	    else
	    {
		System.err.println("Not an EXIF file");
	    }
	}
    }
    
    private static void doGet(String[] options, String filename)
	throws IOException, JpegFormatException, TagFormatException,
	ExifFormatException
    {
	String tag_s = null;
	for (int i=0; i<options.length; ++i)
	{
	    if (options[i].equals("-tag")) tag_s = options[++i].toUpperCase();
	    else usage();
	}
	if (tag_s == null) usage();
	Tag tag = Tag.UNKNOWN;
	for (Tag t : Tag.values())
	{
	    if (t.toString().equals(tag_s))
	    {
		tag = t;
		break;
	    }
	}
	if (tag == Tag.UNKNOWN)
	{
	    System.err.println("Tag " + tag_s + " not found");
	}
	else
	{
	    JpegHeaders headers = new JpegHeaders(filename);
	    App1Header app1 = headers.getApp1Header();
	    if (app1 != null)
	    {
		TagValue value = app1.getValue(tag);
		if (value == null) System.out.println("unset");
		else System.out.println(value);
	    }
	    else
	    {
		System.err.println("Not an EXIF file");
	    }
	}
    }
    
    private static void doCompare(String[] options, String filename)
	throws IOException, JpegFormatException, TagFormatException,
	ExifFormatException
    {
	String with = null;
	for (int i=0; i<options.length; ++i)
	{
	    if (options[i].equals("-with")) with = options[++i];
	    else usage();
	}
	JpegHeaders headers1 = new JpegHeaders(filename);
	JpegHeaders headers2 = new JpegHeaders(with);

	List<JpegHeaders.Section> list1 = headers1.getSections();
	List<JpegHeaders.Section> list2 = headers2.getSections();

	int n = list1.size() > list2.size() ? list1.size() : list2.size();
	for (int i=0; i<n; ++i)
	{
	    JpegHeaders.Section sec1 = i < list1.size() ? list1.get(i) : null;
	    JpegHeaders.Section sec2 = i < list2.size() ? list2.get(i) : null;
	    if (i < list1.size() && i < list2.size())
		System.out.println(
		    String.format("%2s %04d %04d : %2s %04d %04d",
				  sec1.type, sec1.offset, sec1.data.length,
				  sec2.type, sec2.offset, sec2.data.length
				  ));
	}
    }
    
    private static void doComments(String[] options, String filename)
	throws IOException, JpegFormatException, TagFormatException,
	ExifFormatException
    {
	String add = null;
	boolean removeAll = false;
	for (int i=0; i<options.length; ++i)
	{
	    if (options[i].equals("-add")) add = options[++i];
	    else if (options[i].equals("-removeall")) removeAll = true;
	    else usage();
	}
	JpegHeaders headers = new JpegHeaders(filename);

	if (removeAll)
	{
	    int n = headers.getComments().length;
	    for (int i=0; i<n; ++i)
		headers.removeComment(0);
	}
	if (add != null)
	    headers.addComment(add);

	headers.save(true);
    }
    
    private static void doToExif(String[] options, String filename)
	throws IOException, JpegFormatException, TagFormatException,
	ExifFormatException
    {
	String add = null;
	boolean removeAll = false;
	for (int i=0; i<options.length; ++i)
	{
	    usage();
	}
	JpegHeaders headers = new JpegHeaders(filename);
	if (headers.getApp1Header() != null)
	{
	    System.out.println("A;ready EXIF");
	}
	else
	{
	    headers.convertToExif();
	    headers.save(true);
	}
    }
    
    private static void doMakeThumb(String[] options, String filename)
	throws IOException, JpegFormatException, TagFormatException,
	ExifFormatException, NumberFormatException
    {
	String add = null;
	boolean rotate = false;
	boolean exact = false;
	int rows = 0, cols = 0;
	for (int i=0; i<options.length; ++i)
	{
	    if (options[i].equals("-rows")) rows = Integer.parseInt(options[++i]);
	    else if (options[i].equals("-cols")) cols = Integer.parseInt(options[++i]);
	    else if (options[i].equals("-exact")) exact = true;
	    else if (options[i].equals("-rotate")) rotate = true;
	    else usage();
	}
	if (rows == 0 || cols == 0) usage();
	JpegHeaders headers = new JpegHeaders(filename);
	FileInputStream stream = new FileInputStream(filename);
	if (exact)
	    headers.makeThumbnailWithExactDimensions(stream, rows, cols,
						     rotate);
	else
	    headers.makeThumbnailWithMaxDimensions(stream, rows, cols,
						   rotate);
	stream.close();
	headers.save(true);
    }
    
    /**
     * See class documentation for command line usage.
     */
    public static void main(String[] args)
    {
	try
	{
	    if (args.length < 2) usage();
	    String cmd = args[0];
	    String[] options = new String[args.length-2];
	    for (int i=0; i<args.length-2; ++i)
		options[i] = args[i+1];
	    String filename = args[args.length-1];
	    
	    if (cmd.equals("show")) doShow(options, filename);
	    else if (cmd.equals("getthumb")) doGetThumb(options, filename);
	    else if (cmd.equals("available")) doAvailable(options, filename);
	    else if (cmd.equals("compare")) doCompare(options, filename);
	    else if (cmd.equals("set")) doSet(options, filename);
	    else if (cmd.equals("get")) doGet(options, filename);
	    else if (cmd.equals("comments")) doComments(options, filename);
	    else if (cmd.equals("toexif")) doToExif(options, filename);
	    else if (cmd.equals("makethumb")) doMakeThumb(options, filename);
	    else usage();
	    System.exit(0);
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
    }
}
