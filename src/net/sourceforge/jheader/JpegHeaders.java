
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
import net.sourceforge.jheader.enumerations.Compression;
import net.sourceforge.jheader.App1Header.Tag;

/**
 * <p>Class to read comments and EXIF and JFIF headers from JPEG
 * images and write them back.</p>
 *
 * <p>This class can also return individual sections from the header (a section
 * is what follows a marker).</p>
 *
 * <p>This is the core class to the JHeader API and is the one you will use
 * to parse files as well as to save them.</p>
 */
public class JpegHeaders
{
    // markers
    public static final int M_SOF0 = 0xC0; // Start Of Frame N
    public static final int M_SOF1 = 0xC1; // N indicates which compression process
    public static final int M_SOF2 = 0xC2; // Only SOF0-SOF2 are now in common use
    public static final int M_SOF3 = 0xC3; // 
    public static final int M_DHT = 0xC4; // Huffman table
    public static final int M_SOF5 = 0xC5; // NB: codes C4 and CC are NOT SOF markers
    public static final int M_SOF6 = 0xC6; 
    public static final int M_SOF7 = 0xC7; 
    public static final int M_SOF9 = 0xC9; 
    public static final int M_SOF10 = 0xCA; 
    public static final int M_SOF11 = 0xCB; 
    public static final int M_SOF13 = 0xCD; 
    public static final int M_SOF14 = 0xCE; 
    public static final int M_SOF15 = 0xCF; 
    public static final int M_SOI = 0xD8; // Start of image (beginning of datastream)
    public static final int M_EOI = 0xD9; // End of image (end of datastream)
    public static final int M_SOS = 0xDA; // Start of scan (begins compressed data)
    public static final int M_DQT = 0xDB; // Quantization table - 69 bytes
    public static final int M_JFIF = 0xE0; // Jfif marker
    public static final int M_EXIF = 0xE1; // Exif marker
    public static final int M_COM = 0xFE; // Image Title
    public static final int M_XFF = 0xFF; // starts all markers

    private int m_width = 0;
    private int m_height = 0;
    private int m_bpp = 0;

    private String m_filename = null;
    
    /**
     * JPEG file type.
     *
     * UNKNOWN = not a recognized JPEG file<br>
     * EXIF = Exif file (contains an APP1 header)<br>
     * JFIF = JFIF file (contains an APP0 header)<br>
     *
     * If the file contains an APP1 and APP2 header it is considered EXIF.
     */
    public enum FileType {UNKNOWN, EXIF, JFIF}

    /**
     * Byte order within the file (high byte first, low byte first).
     */
    public enum ByteOrder {MOTOROLA, INTEL}
    
    /**
     * Stores an unparsed section from the JPEG file.
     *
     * <p>This is only needed if you call getSections().  Mostly you will not
     * want to.</p>
     */
    public class Section
    {
	public int   type = 0;
	public int   offset = 0;
	public int[] data = null;
	public byte[] asBytes()
	{
	    byte[] b = new byte[data.length];
	    for (int i=0; i<b.length; ++i)
		b[i] = (byte)(data[i]&0xff);
	    return b;
	}
	public Section() {}
	public Section(int t, int o, int[] d)
	    {type = t; offset = o; data = d;}
    }

    private FileType m_format = FileType.UNKNOWN;
    private ArrayList<Section> m_sectionList = new ArrayList<Section>();
    private ArrayList<String> m_comments = new ArrayList<String>();
    private int m_numCommentsInFile = 0;

    // JFIF data
    private App0Header m_app0Header = null;
    private App0Header m_app0HeaderExt = null;

    // exif data
    private App1Header m_app1Header = null;

    private boolean m_noParsing = false;

    /**
     * Construct from a JPEG file.
     *
     * Loads all JFIF and EXIF headers and all comments.  Extracts all
     * sections (markers).
     *
     * @param filename name of file to read.
     *
     * @throws IOException if reading the input generated an I/O error.
     * @throws FileNotFoundException if filename does not exist.
     * @throws JpegFormatException if there was an error in the format
     *                             of the JPEG.
     * @throws ExifFormatException if there was an error in the format
     *                             of the EXIF header.
     * @throws TagFormatException  if there was an error in the format
     *                             of one of the EXIF tags.
     */
    public JpegHeaders(String filename)
	throws IOException, FileNotFoundException, JpegFormatException,
               ExifFormatException, TagFormatException
    {
	loadFromFile(filename);
    }

    /**
     * Construct from a JPEG file.
     *
     * Loads all JFIF and EXIF headers and all comments.  Extracts all
     * sections (markers).
     *
     * @param filename name of file to read.
     * @param noParsing do not parse the APP0, APP1, M_COM tags - save them
     *                  verbatim only.
     *
     * @throws IOException if reading the input generated an I/O error.
     * @throws FileNotFoundException if filename does not exist.
     * @throws JpegFormatException if there was an error in the format
     *                             of the JPEG.
     * @throws ExifFormatException if there was an error in the format
     *                             of the EXIF header.
     * @throws TagFormatException  if there was an error in the format
     *                             of one of the EXIF tags.
     */
    public JpegHeaders(String filename, boolean noParsing)
	throws IOException, FileNotFoundException, JpegFormatException,
               ExifFormatException, TagFormatException
    {
	m_noParsing = noParsing;
	loadFromFile(filename);
    }

    /**
     * Construct from a JPEG input stream
     *
     * Loads all JFIF and EXIF headers and all comments.  Extracts all
     * sections (markers).
     *
     * @param stream input stream to read.
     *
     * @throws IOException if reading the input generated an I/O error.
     * @throws FileNotFoundException if filename does not exist.
     * @throws JpegFormatException if there was an error in the format
     *                             of the JPEG.
     * @throws ExifFormatException if there was an error in the format
     *                             of the EXIF header.
     * @throws TagFormatException  if there was an error in the format
     *                             of one of the EXIF tags.
     */
    public JpegHeaders(InputStream stream)
	throws IOException, FileNotFoundException, JpegFormatException,
               ExifFormatException, TagFormatException
    {
	loadFromStream(stream);
    }

    private void loadFromFile(String filename)
	throws IOException, FileNotFoundException, JpegFormatException,
               ExifFormatException, TagFormatException
    {
	m_filename = filename;
	FileInputStream stream = new FileInputStream(filename);
	loadFromStream(stream);
	stream.close();
    }
    
    private void loadFromStream(InputStream istream)
	throws IOException, JpegFormatException,
               ExifFormatException, TagFormatException
    {
	DataInputStream stream = new DataInputStream(istream);
	validateHeader(stream);
	loadSections(stream);
	stream.close();

	if (!m_noParsing)
	{
	    for (Section sec : m_sectionList)
	    {
		if (sec.type == M_EXIF)
		    parseExifSection(sec);
		else if (sec.type == M_JFIF)
		    parseJfifSection(sec);
		else if (sec.type == M_COM)
		    parseCommentSection(sec);
	    }
	}
    }

    /**
     * Saves a JPEG file back to its source filename.
     *
     * <p>Call this after you have made changes to the comments, APP0 and APP1
     * data.</p>
     *
     * <p>In order to save a file, it must have been read using the
     * JpegHeaders(String) constructor, not the JpegHeaders(InputStream)
     * constructor.  If you haven't, IOException is thrown.</p>
     *
     * <p>This automatically updates the DATETIME tag in EXIF files.</p>
     *
     * <p>Some JPEGs seem to have both APP0 and APP1 tags.  This is
     * not strictly legal.  It happens, for example, when some image
     * manipulation software is asked to rotate an EXIF image.  It
     * puts a new JFIF header in and also includes the EXIF header
     * after it.  This save method will only write one.  If it sees both
     * tags it writes the APP0, the rationale being the APP1 is likely
     * to be corrupt.</p>
     *
     * @param keepBackup if true, backs up original file to
     *                   &lt;filename&gt;.old
     * @throws IOException if there is an I/O error writing the file.
     * @throws FileNotFoundException if the input file as disappeared (it
     *                               must be rescanned for the image data).
     * @throws JpegFormatException if there is an error in the format
     *                             of the JPEG.
     * @throws ExifFormatException if there is an error in the format
     *                             of the EXIF header.
     * @throws TagFormatException  if there is an error in the format
     *                             of one of the EXIF tags.
     */
    public void save(boolean keepBackup)
	throws IOException, FileNotFoundException, JpegFormatException,
	ExifFormatException, TagFormatException
    {
	if (m_filename == null)
	    throw new IOException("Can only save if you read from a file");
	// open input file and temp output file
	File inFile = new File(m_filename);
	File outFile = File.createTempFile("save",".tmp",
					   inFile.getParentFile());

	try
	{
	    FileInputStream infstream = new FileInputStream(m_filename);
	    FileOutputStream outfstream = new FileOutputStream(outFile);

	    save(infstream, outfstream);
	    
	    infstream.close();
	    outfstream.close();

	    // back up old file and move temp file back to original filename
	    if (keepBackup)
	    {
		File renamedFile = new File(m_filename+".old");
		if (renamedFile.exists()) renamedFile.delete();
		inFile.renameTo(renamedFile);
	    }
	    File newOutFile = new File(m_filename);
	    if (newOutFile.exists()) newOutFile.delete();
	    outFile.renameTo(newOutFile);
	}
	finally
	{
	    if (outFile.exists()) outFile.delete();
	}
    }

    private void writeNewComments(OutputStream outstream)
	throws IOException
    {
	// write rest of comments
	for (int i=m_numCommentsInFile; i<m_comments.size();
	     ++i)
	{
	    String com = m_comments.get(i);
	    int len = com.length();
	    byte[] bytes = new byte[len+4];
	    bytes[0] = (byte)M_XFF;
	    bytes[1] = (byte)M_COM;
	    bytes[2] = (byte)((len+2 >> 8) & 0xff);
	    bytes[3] = (byte)((len+2) & 0xff);
	    for (int j=0; j<len; ++j)
		bytes[j+4] = (byte)(((int)(com.charAt(j)))&0xff);
	    outstream.write(bytes,0,bytes.length);
	}

    	    m_numCommentsInFile = m_comments.size();
    }

    private void copyFile(File from, File to)
	throws FileNotFoundException, IOException
    {
	FileInputStream froms = new FileInputStream(from);
	FileOutputStream tos = new FileOutputStream(to);
	byte[] bytes = new byte[1024];
	int n;
	while (froms.available()>0)
	{
	    n = froms.read(bytes);
	    tos.write(bytes,0,n);
	}
    }
    
    private void validateHeader(DataInputStream stream)
	throws IOException, JpegFormatException
    {
	try
	{
	    int firstByte = stream.readUnsignedByte();
	    int secondByte = stream.readUnsignedByte();
	    if (firstByte != M_XFF || secondByte != M_SOI)
		throw new IOException("Not a JPEG file");
	}
	catch (EOFException e)
	{
	    e.printStackTrace();
	    throw new JpegFormatException("Not a JPEG file");
	}
    }

    private void loadSections(DataInputStream stream)
	throws IOException, JpegFormatException
    {
	boolean moreSections = true;
	int offset = 0;
	while (moreSections)
	{
	    // find the next marker
	    boolean sofFound = false;
	    int type = 0;
	    while (!sofFound)
	    {
		try
		{
		    type = stream.readUnsignedByte();
		    offset++;
		}
		catch (EOFException e)
		{
		    return; // no more tags
		}
		sofFound = type != M_XFF;
	    }
	    if (!sofFound)
		throw new IOException("Invalid JPEG - cannot find next marker");
	    if (type == M_JFIF) m_format = FileType.JFIF;
	    else if (type == M_EXIF) m_format = FileType.EXIF;
	    
	    switch (type)
	    {
	    case M_SOI:
		break;
	    case M_EOI:
		break;

	    case M_SOS:
		moreSections = false;
		break;

	    case M_SOF0:
	    case M_SOF1:
	    case M_SOF2:
	    case M_SOF3:
	    case M_SOF5:
	    case M_SOF6: 
	    case M_SOF7: 
	    case M_SOF9: 
	    case M_SOF10: 
	    case M_SOF11: 
	    case M_SOF13: 
	    case M_SOF14: 
	    case M_SOF15:
	    {
		Section sec = readSection(stream, type, offset);
		if (sec.data.length >= 2)
		{
		    offset += sec.data.length;
		    m_width = readIntLittleEndian(sec.data,5,2);
		    m_height = readIntLittleEndian(sec.data,3,2);
		    m_bpp = readIntLittleEndian(sec.data,2,1)
			* readIntLittleEndian(sec.data,7,1);
		    m_sectionList.add(sec);
		}
	    }
	    break;
	    case M_DHT:
	    case M_DQT:
	    case M_JFIF:
	    case M_EXIF:
	    case M_COM:
	    case M_XFF:
	    {
		Section sec =readSection(stream, type, offset);
		if (sec.data.length >= 2)
		{
		    m_sectionList.add(sec);
		    offset += sec.data.length;
		}
	    }
	    break;
	    }
	}
    }

    private void readBytes(DataInputStream stream, int[] bytes, int n, int o)
	throws IOException, EOFException
    {
	for (int i=o; i<n+o; ++i)
	    bytes[i] = stream.readUnsignedByte();
    }

    private Section readSection(DataInputStream stream, int type, int offset)
	throws IOException, JpegFormatException
    {
	try
	{
	    // read the size
	    Section sec = new Section();
	    sec.type = type;
	    int size1 = stream.readUnsignedByte();
	    int size2 = stream.readUnsignedByte();
	    int size = (size1 << 8 ) | size2;
	    // read the data
	    sec.data = new int[size];
	    readBytes(stream, sec.data, size-2, 2);
	    if (sec.data.length < 2)
	    {
		System.err.println("Corrupt JPEG section " + HexUtils.toHex(type) + " at offset " + offset + " - skipping");
		return sec;
	    }
	    sec.data[0] = size1;
	    sec.data[1] = size2;
	    sec.offset = offset;
	    return sec;
	}
	catch (EOFException e)
	{
	    e.printStackTrace();
	    throw new JpegFormatException("Unexpected end of JPEG file");
	}
    }

    private void parseExifSection(Section sec)
	throws IOException, ExifFormatException, TagFormatException
    {
	m_app1Header = new App1Header(sec.data);
    }
    
    private void parseJfifSection(Section sec)
	throws IOException, JpegFormatException
    {
	if (m_app0Header == null)
	{
	    m_app0Header = new App0Header(sec.data);
	}
	else
	{
	    m_app0HeaderExt = new App0Header(sec.data);
	}
    }
    
    private void parseCommentSection(Section sec)
    {
	StringBuffer comment = new StringBuffer();
	for (int i=2; i<sec.data.length; ++i)
	    comment.append((char)(sec.data[i]&0xff));
	m_comments.add(comment.toString());
	m_numCommentsInFile = m_comments.size();
    }

    ///////////////////////////////////////////////////////////////////////
    // methods to return things

    /**
     * Returns the file type.
     *
     * @return file type
     */
    public FileType getFileType() {return m_format;}
    
    /**
     * Returns the JFIF (APP0) header.
     *
     * @return the APP0 header or null if one isn't defined.
     */
    public App0Header getApp0Header()
    {
	return m_app0Header;
    }

    /**
     * Returns the APP0 extension header.
     *
     * See {@link App0Header} for details on what this is.
     *
     * @return the APP0 extension header or null if one isn't defined.
     */
    public App0Header getApp0ExtHeader()
    {
	return m_app0Header;
    }

    /**
     * Returns the EXIF (APP1) header.
     *
     * @return the APP1 header or null if one isn't defined.
     */
    public App1Header getApp1Header()
    {
	return m_app1Header;
    }

    /**
     * Returns all sections as raw data.
     *
     * @return list of sections.
     */
    public List<Section> getSections()
    {
	return Collections.unmodifiableList(m_sectionList);
    }

    /**
     * Returns the actual image width (read from image data not headers).
     *
     * @return the image width.
     */
    public int getWidth() {return m_width;}
    
    /**
     * Returns the actual image height (read from image data not headers).
     *
     * @return the image height.
     */
    public int getHeight() {return m_height;}

    /**
     * Returns the number of bits per pixe; (read from image data not headers).
     *
     * @return the bits per pixel.
     */
    public int getBitsPerPixel() {return m_bpp;}

    /**
     * Returns an array of comments read from the JPEG.
     *
     * <p>These are data following M_COM markers.</p>
     *
     * @return array of comments in file order.
     */
    public String[] getComments()
    {
	String[] comments = new String[m_comments.size()];
	m_comments.toArray(comments);
	return comments;
    }

    //////////////////////////////////////////////////////////////////////
    // Methods to modify things

    /**
     * Converts a file to JFIF.
     *
     * <p>Does nothing if the file is already JFIF.  If the file contains
     * an APP1 tag, it is removed.  An empty APP0 tag is created.  Nothing
     * is transferred over.  The file is not saved.</p>
     *
     * <p>Note that some JPEGs have both an APP0 and an APP1 tag.  This is
     * not strictly legal (and the save() method will only ever write one
     * of them).  If you have an image of this type, calling convertToJfif()
     * will remove the APP1 tag and leave the APP0 one intact.</p>
     *
     * @param createExt if true, an APP0 extension section is also created.
     */
    public void convertToJfif(boolean createExt)
    {
	for (int i=0; i<m_sectionList.size(); ++i)
	{
	    if (m_sectionList.get(i).type == M_EXIF)
	    {
		m_sectionList.remove(i);
		break;
	    }
	}
	m_app1Header = null;
	if (m_app0Header == null)
	{
	    m_app0Header = new App0Header(false);
	    m_sectionList.add(new Section(M_JFIF, 0, m_app0Header.write()));
	}
	if (createExt && m_app0HeaderExt == null)
	{
	    m_app0HeaderExt = new App0Header(true);
	    m_sectionList.add(new Section(M_JFIF, 0, m_app0HeaderExt.write()));
	}
	m_format = FileType.JFIF;
    }

    /**
     * Converts a file to EXIF.
     *
     * <p>Does nothing if the file is already EXIF.  If the file contains
     * an APP0 tag, it is removed.  An empty APP1 tag is created.  Nothing
     * is transferred over.  The file is not saved.</p>
     *
     * <p>Note that some JPEGs have both an APP0 and an APP1 tag.  This is
     * not strictly legal (and the save() method will only ever write one
     * of them).  If you have an image of this type, calling convertToExif()
     * will remove the APP0 tag and leave the APP1 one intact.</p>
     */
    public void convertToExif() 
    {
	for (int i=0; i<m_sectionList.size(); ++i)
	{
	    if (m_sectionList.get(i).type == M_JFIF)
	    {
		System.out.println("* remove JFIF");
		m_sectionList.remove(i);
		break;
	    }
	}
	m_app0Header = null;
	m_app0HeaderExt = null;
	if (m_app1Header == null)
	    m_app1Header = new App1Header();
	try
	{
	    m_sectionList.add(new Section(M_EXIF, 0, m_app1Header.write()));
	}
	catch (TagFormatException e)
	{
	    // header is empty so this will not be thrown
	    e.printStackTrace();
	}
	m_format = FileType.EXIF;
    }

    /**
     * Strips APP0 and APP1 headers.
     *
     * This is necessary for example when making a thumbnail.
     */
    public void stripAppHeaders() 
    {
	for (int i=0; i<m_sectionList.size(); ++i)
	{
	    if (m_sectionList.get(i).type == M_JFIF
		|| m_sectionList.get(i).type == M_EXIF)
	    {
		m_sectionList.remove(i);
		break;
	    }
	}
	m_app0Header = null;
	m_app0HeaderExt = null;
	m_app1Header = null;
	m_format = FileType.UNKNOWN;
    }

    /**
     * Adds a M_COM comment.
     *
     * @param comment the new comment
     */
    public void addComment(String comment)
    {
	m_comments.add(comment);
    }

    /**
     * Removes a M_COM comment.
     *
     * @param idx index of the comment to remove
     */
    public void removeComment(int idx)
    {
	m_comments.remove(idx);
    }

    /**
     * Creates and stores a thumbnail.
     *
     * The thumbnail will have maximal height rows and maximal width cols.
     * The aspect ratio is preserved.  The file is not saved - call
     * {@link #save(boolean)} for that.
     *
     * @param stream input stream containing the JPEG image to create
     *               thumbnail from.
     * @param rows maximal number of rows in the thumbnail.
     * @param cols maximal number of columns in the thumbnail.
     * @param bestRotation if true, rotate thumbnail if it will fit better.
     *
     * @throws IOException if there was an error creating the thumbnail.
     * @throws TagFormatException if the thumbnail tags could not be set.
     * @throws ExifFormatException if there is an error in the EXIF format
     */
    public void makeThumbnailWithMaxDimensions(InputStream stream,
					       int rows, int cols,
					       boolean bestRotation)
	throws IOException, TagFormatException, ExifFormatException
    {
	IntArrayOutputStream out = new IntArrayOutputStream();
	ThumbnailMaker.ThumbnailDetails details
	    = ThumbnailMaker.createThumbnailWithMaxDimensions(stream, out,
							      rows, cols,
							      bestRotation);
	saveThumb(out.toIntArray(), details);

    }

    /**
     * Creates and stores a thumbnail.
     *
     * The thumbnail will have the given width and height.
     * The aspect ratio is preserved by cropping the thumbnail.
     * The file is not saved - call
     * {@link #save(boolean)} for that.
     *
     * @param stream input stream containing the JPEG image to create
     *               thumbnail from.
     * @param rows number of rows in the thumbnail.
     * @param cols number of columns in the thumbnail.
     * @param bestRotation if true, rotate thumbnail if it will fit better.
     *
     * @throws IOException if there was an error creating the thumbnail.
     * @throws TagFormatException if the thumbnail tags could not be set.
     * @throws ExifFormatException if there is an error in the EXIF format
     */
    public void makeThumbnailWithExactDimensions(InputStream stream,
						 int rows, int cols,
						 boolean bestRotation)
	throws IOException, TagFormatException, ExifFormatException
    {
	IntArrayOutputStream out = new IntArrayOutputStream();
	ThumbnailMaker.ThumbnailDetails details
	    = ThumbnailMaker.createThumbnailWithExactDimensions(stream, out,
								rows, cols,
								bestRotation);
	saveThumb(out.toIntArray(), details);
    }

    /**
     * Creates and stores a thumbnail.
     *
     * The thumbnail will have DCF-compliant width and height (160x120).
     * The aspect ratio is preserved by cropping the thumbnail.
     * The file is not saved - call
     * {@link #save(boolean)} for that.
     *
     * @param stream input stream containing the JPEG image to create
     *               thumbnail from.
     * @param bestRotation if true, rotate thumbnail if it will fit better.
     *
     * @throws IOException if there was an error creating the thumbnail.
     * @throws TagFormatException if the thumbnail tags could not be set.
     * @throws ExifFormatException if there is an error in the EXIF format
     */
    public void makeDcfThumbnail(InputStream stream,
				 boolean bestRotation)
	throws IOException, TagFormatException, ExifFormatException
    {
	IntArrayOutputStream out = new IntArrayOutputStream();
	ThumbnailMaker.ThumbnailDetails details
	    = ThumbnailMaker.createDcfThumbnail(stream, out,
						bestRotation);
	saveThumb(out.toIntArray(), details);

    }

    /**
     * Returns a textual representation of the comments, APP0 and APP1
     * headers.
     *
     * Mostly for debugging.
     *
     * @return textual representation of object.
     */
    public String toString()
    {
	StringBuffer buf = new StringBuffer();

	// file type
	if (m_app1Header != null)
	    buf.append("EXIF\n");
	else
	    buf.append("JFIF\n");

	// comments
	buf.append("\nComments:\n");
	for (String str : m_comments)
	    buf.append(str+"\n");
	
	// APP1 header
	buf.append("\nAPP1:\n");
	if (m_app1Header != null)
	    buf.append(m_app1Header.toString());

	return buf.toString();
    }


    /**
     * Loads the static data defined in App1Header.
     *
     * This makes parsing the first EXIF faster, though the time spent
     * overall is of course the same.
     */
    static public void preheat()
    {
	App1Header dummy = new App1Header();
    }

    /**
     * Returns true if the given file has a JFIF- or EXIF-compliant header.
     *
     * The JFIF and EXIF standards stipulate the APP0/APP1 header must
     * follow the SOI marker so this method assume that if it doesn't,
     * the image is not a JPEG.
     */
    static public boolean isJpeg(String filename)
    {
	FileInputStream fstream = null;
	DataInputStream stream = null;
	try
	{
	    fstream = new FileInputStream(filename);
	    stream = new DataInputStream(fstream);
	    int firstByte = stream.readUnsignedByte(); // XFF
	    int secondByte = stream.readUnsignedByte(); // SOI
	    int thirdByte = stream.readUnsignedByte(); // XFF
	    if (firstByte != M_XFF || secondByte != M_SOI
		|| thirdByte != M_XFF)
		return false;
	    int fourthByte = stream.readUnsignedByte();
	    FileType format = FileType.UNKNOWN;
	    if (fourthByte == M_JFIF) format = FileType.JFIF;
	    else if (fourthByte == M_EXIF) format = FileType.EXIF;
	    else return false;
	    int fifthByte = stream.readUnsignedByte(); // size byte 1
	    int sixthByte = stream.readUnsignedByte(); // size byte 2
	    int[] ident = new int[5];
	    for (int i=0; i<ident.length; ++i)
		ident[i] = stream.readUnsignedByte();
	    if (format == FileType.JFIF &&
		ident[0]=='J' && ident[1]=='F' && ident[2]=='I'
		&& ident[3]=='F' && ident[4]==0)
		return true;
	    if (format == FileType.EXIF &&
		ident[0]=='E' && ident[1]=='x' && ident[2]=='i'
		&& ident[3]=='f' && ident[4]==0)
		return true;
	    if (format == FileType.EXIF &&
		ident[0]=='E' && ident[1]=='X' && ident[2]=='I'
		&& ident[3]=='F' && ident[4]==0)
		return true;
	    return false;
	}
	catch (Exception e)
	{
	    return false;
	}
	finally
	{
	    if (stream != null) try{stream.close();} catch (Exception e) {}
	    if (fstream != null) try{fstream.close();} catch (Exception e) {}
	}
    }
    
    /////////////////////////////////////////////////////////////////////
    // package methods

    void save(InputStream in, OutputStream out)
	throws IOException, JpegFormatException,
	ExifFormatException, TagFormatException
    {
	DataInputStream instream = new DataInputStream(in);
	DataOutputStream outstream = new DataOutputStream(out);

	// update DATETIME
	if (m_app1Header != null)
	    m_app1Header.setValue(new TagValue(Tag.DATETIME,
			       new DateTimeTag(new GregorianCalendar())));
	
	// recreate APP0 and APP1 headers
	byte[] app0Bytes = null;
	byte[] app0ExtBytes = null;
	byte[] app1Bytes = null;
	if (m_app0Header != null)
	{
	    int[] bytes_i = m_app0Header.write();
	    app0Bytes = new byte[bytes_i.length];
	    for (int i=0; i<bytes_i.length; ++i)
		app0Bytes[i] = (byte)(bytes_i[i]&0xff);
	}
	if (m_app0HeaderExt != null)
	{
	    int[] bytes_i = m_app0HeaderExt.write();
	    app0ExtBytes = new byte[bytes_i.length];
	    for (int i=0; i<bytes_i.length; ++i)
		app0ExtBytes[i] = (byte)(bytes_i[i]&0xff);
	}
	if (m_app1Header != null)
	{
	    System.out.println("Regen APP1");
	    int[] bytes_i = m_app1Header.write();
	    app1Bytes = new byte[bytes_i.length];
	    for (int i=0; i<bytes_i.length; ++i)
		app1Bytes[i] = (byte)(bytes_i[i]&0xff);
	}
	
	// write file
	boolean moreSections = true;
	boolean app0Written = false;
	boolean app1Written = false;
	int offset = 0; // in input file
	int lastCommentIdx = -1;
	while (moreSections)
	{
	    // find the next marker
	    boolean sofFound = false;
	    int type = 0;
	    boolean first = true;
	    int prevByte = 0;
	    while (!sofFound)
	    {
		try
		{
		    type = instream.readUnsignedByte();
		    offset++;
		    if (!first)
			outstream.write(prevByte);
		    else
			first = false;
			prevByte = type;
		}
		catch (EOFException e)
		{
		    return; // no more tags
		}
		sofFound = type != M_XFF;
	    }
	    if (!sofFound)
		throw new IOException("Invalid JPEG - cannot find next marker");
	    switch (type)
	    {
	    case M_SOI:
		outstream.write(type);
		break;
	    case M_EOI:
		outstream.write(type);
		break;
		
	    case M_SOS:
		outstream.write(type);
		moreSections = false;
		break;
		
	    case M_SOF0:
	    case M_SOF1:
	    case M_SOF2:
	    case M_SOF3:
	    case M_SOF5:
	    case M_SOF6: 
	    case M_SOF7: 
	    case M_SOF9: 
	    case M_SOF10: 
	    case M_SOF11: 
	    case M_SOF13: 
	    case M_SOF14: 
	    case M_SOF15:
	    case M_DHT:
	    case M_DQT:
	    case M_XFF:
	    {
		outstream.write(type);
		Section sec = readSection(instream, type, offset);
		if (sec.data.length >= 2)
		{
		    offset += sec.data.length;
		    outstream.write(sec.asBytes(),0,sec.data.length);
		}
	    }
	    break;
	    case M_COM:
	    {
		outstream.write(type);
		Section sec = readSection(instream, type, offset);
		if (sec.data.length >= 2)
		{
		    offset += sec.data.length;
		    outstream.write(sec.asBytes(),0,sec.data.length);
		    lastCommentIdx++;
		    if (lastCommentIdx == m_numCommentsInFile-1)
		    {
			writeNewComments(outstream);
		    }
		}
	    }
	    break;
	    case M_JFIF:
	    case M_EXIF:
	    {
		if (!app0Written && !app1Written)
		{
		    Section sec = readSection(instream, type, offset);
		    offset += sec.data.length;
		    if (app1Bytes == null)
		    {
			// write JFIF header
			if (app0Bytes != null)
			{
			    outstream.write(M_JFIF);
			    outstream.write(app0Bytes,0,app0Bytes.length);
			    app0Written = true;
			    if (m_numCommentsInFile == 0 && app0ExtBytes==null)
				// as good a place as any
				writeNewComments(outstream); 
			}
			if (app0ExtBytes != null)
			{
			    outstream.write(M_JFIF);
			    outstream.write(app0ExtBytes,0,app0Bytes.length);
			    if (m_numCommentsInFile == 0)
				// as good a place as any
				writeNewComments(outstream);
			}
		    }
		    else  // !(app1Bytes == null)
		    {
			offset += sec.data.length;
			outstream.write(M_EXIF);
			outstream.write(app1Bytes,0,app1Bytes.length);
			if (m_numCommentsInFile == 0)
			    // as good a place as any
			    writeNewComments(outstream);
			app1Written = true;
		    }
		}
	    }
	    break;
	    default:
		outstream.write(type);
		break;
	    }
	    
	}
	
	// do rest of file
	byte[] bytes = new byte[1024];
	while (instream.available() > 0)
	{
	    int n = instream.read(bytes);
	    outstream.write(bytes,0,n);
	}
	
	instream.close();
	outstream.close();
    }
	
    /////////////////////////////////////////////////////////////////////
    // private methods

    private void saveThumb(int[] bytes,
			   ThumbnailMaker.ThumbnailDetails details)
	throws TagFormatException, ExifFormatException, IOException
    {
	if (m_app0HeaderExt != null)
	    m_app0HeaderExt.setThumbnailAsJpeg(bytes);
	else if (m_app0Header != null)
	    m_app0Header.setThumbnailAsJpeg(bytes);
	else if (m_app1Header != null)
	{
	    m_app1Header.setThumbnailBytes(bytes);
	    m_app1Header.setValue(new TagValue(Tag.IFD1_IMAGEWIDTH,
					       details.width));
	    m_app1Header.setValue(new TagValue(Tag.IFD1_IMAGELENGTH,
					       details.height));
	    m_app1Header.setValue(new TagValue(Tag.IFD1_COMPRESSION,
					       Compression.JPEG));
	    m_app1Header.setValue(new TagValue(Tag.IFD1_DATETIME,
				   new DateTimeTag(new GregorianCalendar())));
	}
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

    /**
     * Testing only
     */
    public static void main(String[] args)
    {
	try
	{
	    JpegHeaders headers = new JpegHeaders(args[0]);
	    headers.save(true);
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
    }
}
