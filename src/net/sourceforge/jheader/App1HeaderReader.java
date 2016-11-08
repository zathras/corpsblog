
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
import java.io.IOException;
import net.sourceforge.jheader.App1Header.*;
import net.sourceforge.jheader.JpegHeaders.ByteOrder;
import net.sourceforge.jheader.enumerations.*;

class TagValueDetails
{
    public long offset;
    public int numBytes;
    public TagValueDetails(long o, int n) {offset=0; numBytes=n;}
}

/**
 * Information parsed from an APP1 marker in a JPEG file
 */
class App1HeaderReader
{
    /**
     * Number of bytes from start of file to TIFF header.
     *
     * Used for computing offsets, which are always from TIFF header.
     */
    private int m_tiffHdrOffset = 0;

    /**
     * Current offset in APP1 header currently reading.
     */
    private int m_dataIdx;
    private int m_makerNoteIdx;

    private App1Header m_header = null;
    
    /**
     * Constructor from complete APP1 header.
     *
     * Parses the data and stores internally
     */
    public App1HeaderReader(App1Header header, int[] data)
	throws IOException, ExifFormatException, TagFormatException
    {
	try
	{
	    m_header = header;
	    
	    if (data.length < 6)
		throw new IOException("Error in JPEG - corrupt EXIF section");

	    m_dataIdx = 2;
	    // read exif header (6 bytes)
	    SubIntArray exifHeader
		= new SubIntArray(data, m_dataIdx, 6, m_header.getByteOrder(),
				  ByteOrder.MOTOROLA);
	    if (!exifHeader.equals(new int[]{0x45, 0x78, 0x69, 0x66, 0x00, 0x00}))
		throw new IOException("Error in JPEG - incorrect EXIF header");
	    m_dataIdx += 6;
	    
	    // check byte order
	    m_tiffHdrOffset = m_dataIdx;
	    if (data[m_dataIdx] == 0x4d && data[m_dataIdx+1] == 0x4d)
		m_header.setByteOrder(ByteOrder.MOTOROLA);
	    else if (data[m_dataIdx] == 0x49 && data[m_dataIdx+1] == 0x49)
		m_header.setByteOrder(ByteOrder.INTEL);
	    else
		throw new IOException("Error in JPEG - unknown EXIF byte order");
	    m_dataIdx += 2;

	    SubIntArray tiffHeader
		= new SubIntArray(data, m_dataIdx, 2, m_header.getByteOrder(),
				  ByteOrder.MOTOROLA);
	    m_dataIdx += 2;
	    if (!tiffHeader.equals(new int[]{0x00, 0x2a}))
		throw new IOException("Error in JPEG - incorrect TIFF header");
	    
	    int ifd0Offset = readInt(data, m_dataIdx, 4);
	    m_dataIdx += 4;
	    
	    // parse IFD0 block (main image)
	    long nextOffset = parseIFD(data, ExifLocation.IFD0);
	    
	    if (nextOffset > 0)
	    {
		// parse IFD1 block (thumbnail)
		m_dataIdx = (int)(m_tiffHdrOffset + nextOffset);
		parseIFD(data, ExifLocation.IFD1);
	    }
	}
	catch (ArrayIndexOutOfBoundsException e)
	{
	    e.printStackTrace();
	    throw new ExifFormatException("Error in EXIF format");
	}
    }

    /**
     * Parsed an IFD and returns the offset to the next IFD
     */
    private long parseIFD(int[] data, ExifLocation location)
	throws ExifFormatException, TagFormatException
    {
	int nextOffset = parseTagBlock(data,location, m_header.getByteOrder(),
				       m_tiffHdrOffset);

	// parse subexif and thumbnail blocks
	TreeMap<Tag, TagValue> locationTags
	    = m_header.getAllParsedTags().get(location);
	long subExifOffset = 0;
	long thumbnailOffset = 0;
	long thumbnailSize = 0;
	if (locationTags != null)
	{
	    if (locationTags.containsKey(Tag.EXIFOFFSET))
		subExifOffset
		    = locationTags.get(Tag.EXIFOFFSET).getAsLong();
	    if (locationTags.containsKey(Tag.IFD1_JPEGIFOFFSET))
		thumbnailOffset
		    = locationTags.get(Tag.IFD1_JPEGIFOFFSET).getAsLong();
	    if (locationTags.containsKey(Tag.IFD1_JPEGIFBYTECOUNT))
		thumbnailSize
		    = locationTags.get(Tag.IFD1_JPEGIFBYTECOUNT).getAsLong();
	}
	if (location == ExifLocation.IFD0)
	{
	    if (subExifOffset>0)
	    {
		m_dataIdx = (int)(subExifOffset + m_tiffHdrOffset);
		parseSubExif(data);
		subExifOffset = 0;
	    }
	} // if (location == ExifLocation.IFD0)
	else if (location == ExifLocation.IFD1)
	{
	    if (thumbnailOffset>0 && thumbnailSize > 0)
	    {
		m_dataIdx = (int)(thumbnailOffset + m_tiffHdrOffset);
		m_header.setThumbnailBytes(copyBytes(data, m_dataIdx,
						     (int)thumbnailSize));
		thumbnailOffset = 0;
	    }
	}
	
	// skip to end of IFD
	if (nextOffset > 0)
	    m_dataIdx = (int)(nextOffset + m_tiffHdrOffset);
	
	return nextOffset;
    }

    private void parseSubExif(int[] data)
	throws ExifFormatException, TagFormatException
    {
	parseTagBlock(data,ExifLocation.SUBEXIF, m_header.getByteOrder(),
		      m_tiffHdrOffset);

	// parse EXIF interoperability and makernote blocks
	TreeMap<Tag, TagValue> locationTags
	    = m_header.getAllParsedTags().get(ExifLocation.SUBEXIF);
	//if (locationTags.containsKey(Tag.EXIFINTEROPERABILITYOFFSET)) // why?
	{
	    if (locationTags != null)
	    {
		if (locationTags.containsKey(Tag.EXIFINTEROPERABILITYOFFSET))
		{
		    long interopOffset = locationTags.get(Tag.EXIFINTEROPERABILITYOFFSET).getAsLong();
		    m_dataIdx = (int)(interopOffset + m_tiffHdrOffset);
		    parseInterop(data);
		    interopOffset = 0;
		}
		if (locationTags.containsKey(Tag.MAKERNOTE))
		{
		    int[] makerNoteBytes
			= locationTags.get(Tag.MAKERNOTE).getAsUndefined().getBytes();
		    m_header.setMakerNoteBytes(makerNoteBytes);
		    
		    int prevDataIdx = m_dataIdx;
		    m_dataIdx = m_makerNoteIdx;
		    parseMakerNote(data);
		    m_dataIdx = prevDataIdx;
		}
	    }
	}
    }

    private void parseInterop(int[] data)
	throws ExifFormatException, TagFormatException
    {
	parseTagBlock(data,ExifLocation.INTEROP, m_header.getByteOrder(),
		      m_tiffHdrOffset);
    }

    private void parseMakerNote(int[] data)
	throws ExifFormatException, TagFormatException
    {
	// peek at header to see what makernote type we have
	SubIntArray header = null;
	if (data.length >= 6)
	    header = new SubIntArray(data, m_dataIdx, 6);
	else if (data.length >= 5)
	    header = new SubIntArray(data, m_dataIdx, 5);
	else if (data.length >= 4)
	    header = new SubIntArray(data, m_dataIdx, 4);
	else if (data.length >= 3)
	    header = new SubIntArray(data, m_dataIdx, 3);
	else if (data.length >= 2)
	    header = new SubIntArray(data, m_dataIdx, 2);
	else
	    header = new SubIntArray(data, m_dataIdx, 0);
	
	if (header.equals(
	    new int[]{(int)'A',(int)'G',(int)'F',(int)'A',0x00,0x01}, 6))
	    parseAgfaMakerNote(data);
	else if (header.equals(
	    new int[]{(int)'E',(int)'P',(int)'S',(int)'O',(int)'N',0x00, 6}))
	    parseEpsonMakerNote(data);
	else if (header.equals(
	    new int[]{(int)'K',(int)'Y',(int)'O',(int)'C',(int)'E',(int)'R'},
			       6))
	    parseFujifilmMakerNote(data);
	else if (header.equals(new int[]{(int)'M',(int)'L',(int)'Y'}, 3))
	    parseMinoltaMakerNote(data);
	else if (header.equals(new int[]{(int)'K',(int)'C'}, 2))
	    parseMinoltaMakerNote(data);
	else if (header.equals(
	    new int[]{(int)'+',(int)'M',(int)'+',(int)'M',(int)'+',(int)'M'},
	    6))
	    parseMinoltaMakerNote(data);
	else if (header.equals(
	    new int[]{(int)'M',(int)'I',(int)'N',(int)'O',(int)'L'}, 5))
	    parseMinoltaMakerNote(data);
	else if (header.equals(
	    new int[]{(int)'N',(int)'i',(int)'k',(int)'o',(int)'n',0x00}, 6))
	    parseNikonMakerNote(data);
	else if (header.equals(
	    new int[]{(int)'O',(int)'L',(int)'Y',(int)'M',(int)'P',0x00}, 6))
	    parseOlympusMakerNote(data);
	else if (header.equals(
	    new int[]{(int)'P',(int)'a',(int)'n',(int)'a',(int)'s',(int)'o'},
	    6))
	    parsePanasonicMakerNote(data);
	else if (header.equals(
	    new int[]{(int)'P',(int)'a',(int)'n',(int)'a',(int)'s',(int)'o'}, 6))
	    parsePanasonicMakerNote(data);
	else if (header.equals(new int[]{(int)'M',(int)'K',(int)'E',(int)'D'},
			       4))
	    parsePanasonicMakerNote(data);
	else if (header.equals(new int[]{(int)'A',(int)'O', (int)'C'}, 3))
	    parsePentaxMakerNote(data);
	else if (header.equals(new int[]{(int)'R',(int)'e', (int)'v'}, 3))
	    parseRicohMakerNote(data);
	else if (header.equals(new int[]{(int)'R',(int)'v'}, 2))
	    parseRicohMakerNote(data);
	else if (header.equals(
	    new int[]{(int)'S',(int)'O',(int)'N',(int)'Y',(int)' ',(int)'C'},
	    6))
	    parseSonyMakerNote(data);
	else if (header.equals(
	    new int[]{(int)'S',(int)'O',(int)'N',(int)'Y',(int)' ',(int)'D'},
	    6))
	    parseSonyMakerNote(data);
	else if (header.equals(
	    new int[]{(int)'K',(int)'Y',(int)'O',(int)'C',(int)'E',(int)'R'},
	    6))
	    parseKyoceraMakerNote(data);
	else if (isCanonMakerNote(data))
	    parseCanonMakerNote(data);
	else if (isNikonD1MakerNote(data))
	    parseNikonMakerNote(data);
	else if (isCasioMakerNote(data))
	    parseCasioMakerNote(data);
	else if (isMinolta5MakerNote(data))
	    parseMinoltaMakerNote(data);
    }

    private void parseAgfaMakerNote(int[] data)
	throws TagFormatException, ExifFormatException
    {
	// process header
	m_header.setMakerNoteHeader(new int[]
	    {(int)'A',(int)'G',(int)'F',(int)'A',0x00,0x01});
	m_header.setCameraType(CameraType.AGFA);
	m_dataIdx += 6;

	// process IFD
	parseOlympusTags(data);
    }
    
    private void parseEpsonMakerNote(int[] data)
	throws TagFormatException, ExifFormatException
    {
	// process header
	m_header.setMakerNoteHeader(new int[]
	    {(int)'E',(int)'P',(int)'S',(int)'O',(int)'N',0x00,0x01,0x00});
	m_header.setCameraType(CameraType.EPSON);
	m_dataIdx += 8;

	// process IFD
	parseOlympusTags(data);
    }
    
    private void parseFujifilmMakerNote(int[] data)
	throws TagFormatException, ExifFormatException
    {
	ByteOrder prevByteorder = m_header.getByteOrder();
	m_header.setByteOrder(ByteOrder.INTEL);

	// process header
	m_header.setMakerNoteHeader(new int[]
	    {(int)'F',(int)'U',(int)'J',(int)'I',(int)'F',(int)'I',(int)'L',(int)'M'});
	m_header.setCameraType(CameraType.FUJIFILM);
	int offset = readInt(data, m_dataIdx, 4);
	m_dataIdx += offset;

	// get makernote offset
	TreeMap<Tag, TagValue> locationTags
	    = m_header.getAllParsedTags().get(ExifLocation.SUBEXIF);
	int makerNoteOffset
	    = (int)locationTags.get(Tag.MAKERNOTE).getAsLong().longValue();

	// parse tags
	parseTagBlock(data, ExifLocation.MAKERNOTE_FUJIFILM, ByteOrder.INTEL,
		      makerNoteOffset);
	
	m_header.setByteOrder(prevByteorder);
    }

    private void parseMinoltaMakerNote(int[] data)
	throws TagFormatException, ExifFormatException
    {
	SubIntArray header = null;
	if (data.length >= 8)
	    header = new SubIntArray(data, m_dataIdx, 8);
	else if (data.length >= 5)
	    header = new SubIntArray(data, m_dataIdx, 5);
	else if (data.length >= 3)
	    header = new SubIntArray(data, m_dataIdx, 3);
	else if (data.length >= 2)
	    header = new SubIntArray(data, m_dataIdx, 2);
	else
	    header = new SubIntArray(data, m_dataIdx, 0);
	
	if (header.equals(new int[]{(int)'M',(int)'L',(int)'Y'}, 3))
	{
	    m_header.setCameraType(CameraType.MINOLTA1);
	    m_header.setMakerNoteHeader(header.toArray(3));
	}
	else if (header.equals(new int[]{(int)'K',(int)'C'}, 2))
	{
	    m_header.setCameraType(CameraType.MINOLTA2);
	    m_header.setMakerNoteHeader(header.toArray(2));
	}
	else if (header.equals(
			       new int[]{(int)'+',(int)'M',(int)'+',(int)'M',(int)'+',(int)'M',(int)'+',(int)'M'}, 8))
	{
	    m_header.setCameraType(CameraType.MINOLTA3);
	    m_header.setMakerNoteHeader(header.toArray(8));
	}
	else if (header.equals(
			       new int[]{(int)'M',(int)'I',(int)'N',(int)'O',(int)'L'}, 5))
	{
	    m_header.setCameraType(CameraType.MINOLTA4);
	    m_header.setMakerNoteHeader(header.toArray(5));
	}
	else
	{
	    m_header.setCameraType(CameraType.MINOLTA5);
	    parseOlympusTags(data);
	}
    }
    
    private void parseNikonMakerNote(int[] data)
	throws TagFormatException, ExifFormatException
    {
	SubIntArray header = null;
	if (data.length >= 10)
	    header = new SubIntArray(data, m_dataIdx, 10);
	else if (data.length >= 8)
	    header = new SubIntArray(data, m_dataIdx, 8);
	else
	    header = new SubIntArray(data, m_dataIdx, 0);
	if (data.length >= 10)
	    header = new SubIntArray(data, m_dataIdx, 10);
	if (data.length >= 8)
	    header = new SubIntArray(data, m_dataIdx, 8);
	if (header.equals(new int[]{(int)'N',(int)'i',(int)'k',(int)'o',(int)'n',0x00,0x01,0x00}, 8))
	{
	    m_header.setCameraType(CameraType.NIKON1);
	    m_header.setMakerNoteHeader(header.toArray(8));
	    m_dataIdx += 8;
	    parseNikonTags(data);
	}
	else if (header.equals(new int[]{(int)'N',(int)'i',(int)'k',(int)'o',(int)'n',0x00,0x02,0x10,0x00,0x00}, 10))
	{
	    m_header.setCameraType(CameraType.NIKON2);
	    m_header.setMakerNoteHeader(header.toArray(10));
	    m_dataIdx += 10;
	    parseNikonD1Tags(data);
	} 
	else if (header.equals(new int[]{(int)'N',(int)'i',(int)'k',(int)'o',(int)'n',0x00,0x02,0x00,0x00,0x00}, 10))
	{
	    m_header.setCameraType(CameraType.NIKON2);
	    m_header.setMakerNoteHeader(header.toArray(10));
	    m_dataIdx += 8;
	    parseNikonD1Tags(data);
	}
	else
	{
	    m_header.setCameraType(CameraType.NIKON2);
	    m_header.setMakerNoteHeader(null);
	    parseNikonD1Tags(data);
	}
   }
    
    private void parseOlympusMakerNote(int[] data)
	throws TagFormatException, ExifFormatException
    {
	m_header.setMakerNoteHeader(copyBytes(data,m_dataIdx,8));
	m_header.setCameraType(CameraType.OLYMPUS);
	m_dataIdx += 8;

	parseOlympusTags(data);
    }
    
    private void parsePanasonicMakerNote(int[] data)
	throws TagFormatException, ExifFormatException
    {
	SubIntArray header = null;
	if (data.length >= 12)
	    header = new SubIntArray(data, m_dataIdx, 12);
	else if (data.length >= 4)
	    header = new SubIntArray(data, m_dataIdx, 4);
	else
	    header = new SubIntArray(data, m_dataIdx, 0);

	if (header.equals(new int[]{(int)'P',(int)'a',(int)'n',(int)'a',(int)'s',(int)'o',(int)'n',(int)'i',(int)'c',0x00,0x00,0x00}, 12))
	{
	    m_header.setCameraType(CameraType.PANASONIC); // type 1
	    m_header.setMakerNoteHeader(header.toArray(12));
	    m_dataIdx += 12;
	}
	else if (header.equals(new int[]{(int)'M',(int)'K',(int)'E',(int)'D'}, 4))
	{
	    m_header.setCameraType(CameraType.PANASONIC); // type 2
	    m_header.setMakerNoteHeader(header.toArray(4));
	    m_dataIdx += 4;
	}

	// our docs for the tags don't include types - need an example
    }
    
    private void parsePentaxMakerNote(int[] data)
	throws TagFormatException, ExifFormatException
    {
	SubIntArray header = null;
	if (data.length >= 4)
	    header = new SubIntArray(data, m_dataIdx, 4);
	else
	    header = new SubIntArray(data, m_dataIdx, 0);

	if (header.equals(new int[]{(int)'A',(int)'O',(int)'C',0x00,0x00,0x00}, 4))
	{
	    m_header.setCameraType(CameraType.PENTAX2);
	    m_header.setMakerNoteHeader(header.toArray(4));
	    m_dataIdx += 4;

	    // casio type 2 which we have partial details for
	}
	else
	{
	    m_header.setCameraType(CameraType.PENTAX1);
	    m_header.setMakerNoteHeader(null);
	    
	    // casio type 1 which we have partial details for
	}

    }
    
    private void parseRicohMakerNote(int[] data)
	throws TagFormatException, ExifFormatException
    {
	m_header.setCameraType(CameraType.RICOH);
    }
    
    private void parseSonyMakerNote(int[] data)
	throws TagFormatException, ExifFormatException
    {
	m_header.setCameraType(CameraType.SONY);
    }
    
    private void parseKyoceraMakerNote(int[] data)
	throws TagFormatException, ExifFormatException
    {
	m_header.setCameraType(CameraType.KYOCERA);
    }
    
    private void parseCasioMakerNote(int[] data)
	throws TagFormatException, ExifFormatException
    {
	SubIntArray header = null;
	if (data.length >= 6)
	    header = new SubIntArray(data, m_dataIdx, 6);
	else
	    header = new SubIntArray(data, m_dataIdx, 0);

	int dataIdx = m_dataIdx;
	if (header.equals(new int[]{(int)'Q',(int)'V',(int)'C',0x00,0x00,0x00}, 6))
	{
	    m_header.setCameraType(CameraType.CASIO2);
	    m_dataIdx += 6;
	    parseCasioType2Tags(data);
	}
	else
	{
	    m_header.setCameraType(CameraType.CASIO1);
	    parseCasioType1Tags(data);
	}
    }
    
    private void parseCanonMakerNote(int[] data)
	throws TagFormatException, ExifFormatException
    {
	m_header.setCameraType(CameraType.CANON);
	parseTagBlock(data, ExifLocation.MAKERNOTE_CANON,
		      m_header.getByteOrder(), m_tiffHdrOffset);
    }
    
    private void parseOlympusTags(int[] data)
	throws TagFormatException, ExifFormatException
    {
	parseTagBlock(data, ExifLocation.MAKERNOTE_OLYMPUS, m_header.getByteOrder(),
		      m_tiffHdrOffset);
    }

    private void parseNikonTags(int[] data)
	throws TagFormatException, ExifFormatException
    {
	parseTagBlock(data, ExifLocation.MAKERNOTE_NIKON, m_header.getByteOrder(),
		      m_tiffHdrOffset);
    }

    private void parseNikonD1Tags(int[] data)
	throws TagFormatException, ExifFormatException
    {
	parseTagBlock(data, ExifLocation.MAKERNOTE_NIKOND1, m_header.getByteOrder(),
		      m_tiffHdrOffset);
    }

    private void parseCasioType1Tags(int[] data)
	throws TagFormatException, ExifFormatException
    {
	parseTagBlock(data, ExifLocation.MAKERNOTE_CASIO, m_header.getByteOrder(),
		      m_tiffHdrOffset);
    }

    private void parseCasioType2Tags(int[] data)
	throws TagFormatException, ExifFormatException
    {
	parseTagBlock(data, ExifLocation.MAKERNOTE_CASIO2, m_header.getByteOrder(),
		      m_tiffHdrOffset);
    }

    private boolean isNikonD1MakerNote(int[] data)
    {
	int dataIdx = m_dataIdx;
	int numEntries = readInt(data, dataIdx, 2);
	dataIdx += 2;
	int tagId = readInt(data, dataIdx, 2); dataIdx += 2;
	int fmtId = readInt(data, dataIdx, 2); dataIdx += 2;
	int numComponents = readInt(data, dataIdx, 4); dataIdx += 4;
	if (tagId == 0x0001 && fmtId == 7 && numComponents == 4)
	    return true;
	return false;
    }
    
    private boolean isCanonMakerNote(int[] data)
    {
	try
	{
	    int dataIdx = m_dataIdx;
	    int numEntries = readInt(data, dataIdx, 2);
	    dataIdx += 2;
	    int tagId = readInt(data, dataIdx, 2); dataIdx += 2;
	    int fmtId = readInt(data, dataIdx, 2); dataIdx += 2;
	    int numComponents = readInt(data, dataIdx, 4); dataIdx += 4;
	    if (tagId == 0x0001 && fmtId == 3)
	    {
		return true;
	    }
	    return false;
	}
	catch (ArrayIndexOutOfBoundsException e)
	{
	    e.printStackTrace();
	    return false;
	}
    }

    /*
     * This is the hardest one (along with Minolta type 5).  That's why we
     * check for all the others first.
     */
    private boolean isCasioMakerNote(int[] data)
    {
	try
	{
	    SubIntArray header = null;
	    if (data.length >= 6)
		header = new SubIntArray(data, m_dataIdx, 6);
	    else
		header = new SubIntArray(data, m_dataIdx, 0);
	    
	    int dataIdx = m_dataIdx;
	    if (header.equals(new int[]{(int)'Q',(int)'V',(int)'C',0x00,0x00,0x00}, 6))
		return true; // type 2
	    
	    // try type 1
	    int numEntries = readInt(data, dataIdx, 2);
	    dataIdx += 2;
	    HashSet<Integer> ids = new HashSet<Integer>();
	    for (int i=0; i<numEntries; ++i)
	    {
		int tagId = readInt(data, dataIdx, 2); dataIdx += 2;
		int fmtId = readInt(data, dataIdx, 2); dataIdx += 2;
		int numComponents = readInt(data, dataIdx, 4); dataIdx += 4;
		TagFormat format = m_header.getTagFormatIdToEnum().get(fmtId);
		if (format == null) return false;
		SubIntArray value = new SubIntArray(data, dataIdx, format.size * numComponents);
		dataIdx += format.size * numComponents;
		ids.add(tagId);
	    }
	    if ((ids.contains(1) || ids.contains(2) || ids.contains(3))
		&& !ids.contains(0xe00) && !ids.contains(0x1000) && !ids.contains(0x1001))
		return true; // to differentiate from pentax
	    return false;
	}
	catch (ArrayIndexOutOfBoundsException e)
	{
	    e.printStackTrace();
	    return false;
	}
    }
    
    /*
     * This is the hardest one (along with Minolta type 5).  That's why we
     * check for all the others first.
     */
    private boolean isPentaxMakerNote(int[] data)
    {
	try
	{
	    SubIntArray header = null;
	    if (data.length >= 4)
		header = new SubIntArray(data, m_dataIdx, 4);
	    else
		header = new SubIntArray(data, m_dataIdx, 0);
	    int dataIdx = m_dataIdx;
	    if (header.equals(new int[]{(int)'A',(int)'O',(int)'C',0x00}, 4))
		return true; // type 2
	    
	    // try type 1
	    int numEntries = readInt(data, dataIdx, 2);
	    dataIdx += 2;
	    HashSet<Integer> ids = new HashSet<Integer>();
	    for (int i=0; i<numEntries; ++i)
	    {
		int tagId = readInt(data, dataIdx, 2); dataIdx += 2;
		int fmtId = readInt(data, dataIdx, 2); dataIdx += 2;
		int numComponents = readInt(data, dataIdx, 4); dataIdx += 4;
		TagFormat format = m_header.getTagFormatIdToEnum().get(fmtId);
		if (format == null) return false;
		SubIntArray value = new SubIntArray(data, dataIdx, format.size * numComponents);
		dataIdx += format.size * numComponents;
		ids.add(tagId);
	    }
	    if ((ids.contains(1) || ids.contains(2) || ids.contains(3))
		&& (ids.contains(0xe00) || ids.contains(0x1000) || ids.contains(0x1001)))
		return true; // to differentiate from casio
	    return false;
	}
	catch (ArrayIndexOutOfBoundsException e)
	{
	    e.printStackTrace();
	    return false;
	}
    }
    
    /*
     * This is the hardest one (along with Minolta type 5).  That's why we
     * check for all the others first.
     */
    private boolean isMinolta5MakerNote(int[] data)
    {
	try
	{
	    int dataIdx = m_dataIdx;
	    int numEntries = readInt(data, dataIdx, 2);
	    dataIdx += 2;
	    int tagId = readInt(data, dataIdx, 2); dataIdx += 2;
	    int fmtId = readInt(data, dataIdx, 2); dataIdx += 2;
	    int numComponents = readInt(data, dataIdx, 4); dataIdx += 4;
	    if ((tagId == 0x0200 && fmtId == 4 && numComponents == 3)
		|| (tagId == 0x0201 && fmtId == 3 && numComponents == 1)
		|| (tagId == 0x0201 && fmtId == 3 && numComponents == 1))
		return true;
	    // we assume that one of the first three tags will be present.
	    // maybe it won't.
	    return false;
	}
	catch (ArrayIndexOutOfBoundsException e)
	{
	    e.printStackTrace();
	    return false;
	}
    }
    
    /**
     * Shared by parseIFD(int[],ExifLocation), parseSubExif(int[]),
     * parseMakerNote(int[]).
     *
     * Return offset to next IFD for IFD blocks, 0 otherwise.
     */
    private int parseTagBlock(int[] data, ExifLocation location,
			      ByteOrder byteOrder, int tiffHdrOffset)
	throws ExifFormatException, TagFormatException
    {
	ByteOrder prevByteOrder = m_header.getByteOrder();
	m_header.setByteOrder(byteOrder);
	
	// read number of entries
	int numEntries = readInt(data, m_dataIdx, 2);
	m_dataIdx += 2;

	// create empty sets of parsed and unparsed values for this location
	TreeMap<Tag,TagValue> parsedValues = m_header.getAllParsedTags().get(location);
	if (parsedValues == null)
	{
	    parsedValues = new TreeMap<Tag,TagValue>(new TagIdComparator());
	    m_header.getAllParsedTags().put(location, parsedValues);
	}
	TreeMap<Integer,UndefinedTag> unparsedValues
	    = m_header.getAllUnparsedTags().get(location);
	if (unparsedValues == null)
	{
	    unparsedValues = new TreeMap<Integer,UndefinedTag>();
	    m_header.getAllUnparsedTags().put(location, unparsedValues);
	}

 	TreeMap<Integer, Tag> locationTags
	    = m_header.getTagLocationToIdToEnum().get(location);
	if (locationTags == null)	
	{
	    // this means we are missing static data.
	    throw new ExifFormatException("No tags defined for location "
					  + location);
	}
	
	// these temporarily store offsets.  Once we've reached that byte
	// we can enter it into parsedValues
	TreeMap<Tag,TagValueDetails> tagToOffset
	    = new TreeMap<Tag,TagValueDetails>(new TagIdComparator());
	TreeMap<Long,Tag> offsetToTag = new TreeMap<Long,Tag>();
	TreeMap<Integer,TagValueDetails> tagIdToOffset // for unparsed tags
	    = new TreeMap<Integer,TagValueDetails>();
	TreeMap<Long,Integer> offsetToTagId = new TreeMap<Long,Integer>();
	
	// read each entry
	for (int i=0; i<numEntries; ++i)
	{
	    // read tag bytes
	    int tagId = readInt(data, m_dataIdx, 2); m_dataIdx += 2;
	    int fmtId = readInt(data, m_dataIdx, 2); m_dataIdx += 2;
	    int numComponents = readInt(data, m_dataIdx, 4); m_dataIdx += 4;
	    SubIntArray valueOrOffset = new SubIntArray(data, m_dataIdx, 4); m_dataIdx += 4;
	    Tag tag = locationTags.get(tagId);
	    if (tag == Tag.MAKERNOTE) m_makerNoteIdx = m_dataIdx - 4;
	    TagFormat format = m_header.getTagFormatIdToEnum().get(fmtId);
	    if (format == null)
		throw new ExifFormatException("Unknown tag format " + fmtId);

	    // is it a macro tag that fits inline?
	    if (m_header.getMacroTags().contains(tagId) && numComponents*format.size<=4)
	    {
		parseMacroTag(parsedValues, data, location, tagId, format, numComponents, valueOrOffset);
	    }
	    else
	    {
		// regular tag
		boolean parsedTagFound = false;
		boolean convertLongToShort = false;
		if (tag != null && format != null && format == tag.format
		&& (tag.components == -1 || tag.components == numComponents))
		{
		    // tag found and in the expected place
		    parsedTagFound = true;
		}
		else if ((tag==Tag.EXIFIMAGEWIDTH || tag==Tag.EXIFIMAGEHEIGHT
			  || tag==Tag.RELATEDIMAGEWIDTH
			  || tag==Tag.RELATEDIMAGELENGTH
			  || tag==Tag.IFD1_IMAGEWIDTH
			  || tag==Tag.IFD1_IMAGELENGTH)
			 && format == TagFormat.USHORT)
		{
		    parsedTagFound = true;
		    convertLongToShort = true;
		}
		else if (m_header.getMacroTags().contains(tagId))
		{
		    parsedTagFound = true;
		}
		/*if (!parsedTagFound)
		    System.err.println("Warning: tag "
				       + HexUtils.toHex((long)tagId)
				       + " format " + fmtId + " components "
				       + numComponents
				       + " not recognized in "
				       + location);*/
		    
		if (parsedTagFound)
		{
		    if (numComponents * tag.format.size <= 4)
		    {
			// tag value is in the bytes read
			parsedValues.put(tag, makeTagValue(tag, valueOrOffset,
				   numComponents, convertLongToShort));
		    }
		    else
		    {
			// tag value is at some offset
			Long offset = makeULong(valueOrOffset,0,4);
			tagToOffset.put(tag, new TagValueDetails(offset,
								 format.size*numComponents));
			offsetToTag.put(offset, tag);
		    }
		} // if (parsedTagFound)
		else
		{
		    // somethig wrong - either unrecognized tag id, wrong
		    // format or not in the expected part of the EXIF.
		    // Flag as unknown
		    if (numComponents * format.size <= 4)
		    {
			// tag value is in the bytes read
			unparsedValues.put(tagId,
					   new UndefinedTag(copyBytes(data,m_dataIdx-10,10)));
		    }
		    else
		    {
			Long offset = makeULong(valueOrOffset,0,4);
			tagIdToOffset.put(tagId,
					  new TagValueDetails(offset,
							      numComponents*format.size));
			offsetToTagId.put(offset, tagId);
			// store temporarily - we will replace last four bytes
			// with data later.
			unparsedValues.put(tagId,
					   new UndefinedTag(copyBytes(data,m_dataIdx-10,10)));
		    }
		} // !if (parsedTagFound)
	    } // !if (m_header.getMacroTags().contains(tagId)
	    
	}

	int nextOffset = readInt(data, m_dataIdx, 4); m_dataIdx += 4;

	// parse offsetted tags
	Iterator<Long> ptit = offsetToTag.keySet().iterator();
	Iterator<Long> utit = offsetToTagId.keySet().iterator();
	long nextParsedOffset = ptit.hasNext() ? ptit.next() : 0;
	long nextUnparsedOffset = utit.hasNext() ? utit.next() : 0;
	for (;;)
	{
	    if (nextParsedOffset>0
		&& (nextParsedOffset<nextUnparsedOffset
		    ||nextUnparsedOffset==0))
	    {
		// skip to nextParsedOffset
		m_dataIdx = (int)(nextParsedOffset + tiffHdrOffset);

		// parse value
		Tag tag = offsetToTag.get(nextParsedOffset);
		TagValueDetails details = tagToOffset.get(tag);
		int numComponents = details.numBytes/tag.format.size;
		SubIntArray value = new SubIntArray(data, m_dataIdx,
						    details.numBytes);
		if (tag == Tag.MAKERNOTE)
		{
		    m_makerNoteIdx = m_dataIdx;
		}
		m_dataIdx += details.numBytes;

		if (m_header.getMacroTags().contains(tag.id))
		{
		    parseMacroTag(parsedValues, data, location, tag.id, tag.format, numComponents, value);
		}
		else
		{
		    parsedValues.put(tag, makeTagValue(tag, value,
					      numComponents, false));
		}
		
		// go to next parsed offset ready for next iteration
		nextParsedOffset = ptit.hasNext() ? ptit.next() : 0;
	    }
	    else if (nextUnparsedOffset>0
		     && (nextUnparsedOffset<nextParsedOffset
			 ||nextParsedOffset==0))
	    {
		// skip to nextParsedOffset
		m_dataIdx = (int)(nextUnparsedOffset + tiffHdrOffset);

		// parse value
		Integer tagId = offsetToTagId.get(nextUnparsedOffset);
		TagValueDetails details = tagIdToOffset.get(tagId);
		SubIntArray value = new SubIntArray(data, m_dataIdx,
						    details.numBytes);
		m_dataIdx += details.numBytes;
		int[] unparsedTag = new int[details.numBytes+6];
		UndefinedTag unparsedValue = unparsedValues.get(tagId);
		for (int i=0; i<6; ++i)
		    unparsedTag[i] = unparsedValue.getBytes()[i];
		for (int i=0; i<details.numBytes; ++i)
		    unparsedTag[6+i] = value.get(i);		
 		unparsedValues.put(tagId, new UndefinedTag(unparsedTag));
				
		// go to next parsed offset ready for next iteration
		nextUnparsedOffset = utit.hasNext() ? utit.next() : 0;
	    }
	    else break; 
	    
	}

	m_header.setByteOrder(prevByteOrder);
	return nextOffset;
    }

    private void parseMacroTag(TreeMap<Tag,TagValue> parsedValues,
			       int[] data, ExifLocation location,
			       int tagId, TagFormat format, int numComponents,
			       SubIntArray value)
	throws TagFormatException
    {
	HashMap<Integer, TreeSet<Tag> > locationMacroTags
	    = m_header.getTagLocationToMacroTagToTagSet().get(location);
	if (locationMacroTags == null) return;
	TreeSet<Tag> childTags = locationMacroTags.get(tagId);
	if (childTags == null) return;
	for (Tag tag : childTags)
	{
	    /*if (data.length-tag.offset < tag.components*tag.format.size)
		throw new TagFormatException("Unexpected end of tag parsing "
		+ tag);*/
	    //int[] tagData = copyBytes(data,tag.offset,
	    //		      tag.components*tag.format.size);
	    if (tag.offset+tag.components*tag.format.size >= value.length())
		break;
	    SubIntArray tagData
		= new SubIntArray(value,tag.offset,
				  tag.components*tag.format.size);
	    parsedValues.put(tag, makeTagValue(tag, tagData, tag.components,
					       false));
	}
    }
    
    private int[] copyBytes(int[] data, int o, int n)
    {
	int[] newData = new int[n];
	for (int i=0; i<n; ++i)
	    newData[i] = data[i+o];
	return newData;
    }

    private int readInt(int[] data, int off, int len)
    {
	SubIntArray offsetBytes = new SubIntArray(data, off, len,
						  m_header.getByteOrder(),
						  ByteOrder.MOTOROLA);
	int offset = 0;
	int shift = 0;
	for (int i=len-1; i>=0; --i)
	{
	    offset |= offsetBytes.get(i) << shift;
	    shift += 8;
	}
	return offset;
    }

    /**
     * returns a TagValue object given a tag, tag value in bytes and
     * number of components.
     *
     * Expects bytes to be in whatever order the file is written with
     */
    private TagValue makeTagValue(Tag tag, SubIntArray bytes,
				  int numComponents,
				  boolean convertLongToShort)
	throws TagFormatException
    {
	Object value = null;

	switch (tag.format)
	{
	case SBYTE:
	    if (numComponents == 1)
		value = makeTagValue(tag,
				     new Long(makeSByte(bytes,0,1)));
	    else
	    {
		ArrayList<Long> list = new ArrayList<Long>();
		for (int i=0; i<numComponents; ++i)
		    list.add(new Long(makeSByte(bytes,
						i*numComponents,1)));
		value = list;
	    }
	    break;
	case BYTE:
	    if (numComponents == 1)
		value = makeTagValue(tag,
				     new Long(makeByte(bytes,0,1)));
	    else
	    {
		ArrayList<Long> list = new ArrayList<Long>();
		for (int i=0; i<numComponents; ++i)
		    list.add(new Long(makeByte(bytes,
					       i*numComponents,1)));
		value = list;
	    }
	    break;
	case SSHORT:
	    if (numComponents == 1)
		value = makeTagValue(tag,
				     new Long(makeSShort(bytes,0,2)));
	    else
	    {
		ArrayList<Long> list = new ArrayList<Long>();
		for (int i=0; i<numComponents; ++i)
		    list.add(new Long(makeSShort(bytes, i*2,2)));
		value = list;
	    }
	    break;
	case USHORT:
	    if (numComponents == 1)
		value = makeTagValue(tag,
				     new Long(makeUShort(bytes,0,2)));
	    else
	    {
		ArrayList<Long> list = new ArrayList<Long>();
		for (int i=0; i<numComponents; ++i)
		    list.add(new Long(makeUShort(bytes, i*2,2)));
		value = list;
	    }
	    break;
	case SLONG:
	    if (numComponents == 1)
		value = makeTagValue(tag,
				     new Long(makeSLong(bytes,0,4)));
	    else
	    {
		ArrayList<Long> list = new ArrayList<Long>();
		for (int i=0; i<numComponents; ++i)
		    list.add(new Long(makeSLong(bytes, i*4,4)));
		value = list;
	    }
	    break;
	case ULONG:
	    if (numComponents == 1)
	    {
		// XXX wrong way round?
		if (convertLongToShort)
		    value = makeTagValue(tag,
					 new Long(makeUShort(bytes,0,2)));
		else
		{
		    value = makeTagValue(tag,
					     new Long(makeULong(bytes,0,4)));
		}
	    }
	    else
	    {
		ArrayList<Long> list = new ArrayList<Long>();
		for (int i=0; i<numComponents; ++i)
		    list.add(new Long(makeULong(bytes, i*4,4)));
		value = list;
		}
	    break;
	case STRING:
	    value = makeString(bytes,0,bytes.length());
	    break;
	case UNDEFINED:
	    int[] bytes1 = new int[numComponents];
	    for (int i=0; i<numComponents; ++i)
		bytes1[i] = bytes.get(i);
	    //value = new UndefinedTag(bytes1);
	    value = makeUndefinedTagValue(tag, bytes1);
	    break;
	case SRATIONAL:
	    if (numComponents == 1)
		value
		    = new Rational(new Long(makeSLong(bytes,0,4)),
				   new Long(makeSLong(bytes,4,4)));
	    else
	    {
		ArrayList<Rational> list = new ArrayList<Rational>();
		for (int i=0; i<numComponents; ++i)
		    list.add(new Rational(new Long(makeSLong(bytes,i*8,4)),
					  new Long(makeSLong(bytes,i*8+4,4))));
		value = list;
	    }
	    break;
	case URATIONAL:
	    if (numComponents == 1)
		value = new Rational(new Long(makeULong(bytes,0,4)),
				     new Long(makeULong(bytes,4,4)));
	    else
	    {
		ArrayList<Rational> list = new ArrayList<Rational>();
		for (int i=0; i<numComponents; ++i)
		    list.add(new Rational(new Long(makeULong(bytes,i*8,4)),
					  new Long(makeULong(bytes,i*8+4,4))));
		value = list;
	    }
	    break;
	default:
	    value = new UndefinedTag(bytes.toArray());
	    break;
	}
	return new TagValue(tag, value);
    }

    private Object makeTagValue(Tag tag, Long value)
    {
	try
	{
	    if (EnumeratedTag.class.isAssignableFrom(tag.implementingClass))
	    {
		return EnumeratedTag.instantiate(tag.implementingClass, value);
	    }
	    return value;
	}
	catch (Exception e)
	{
	    // this would be an error with the static data
	    e.printStackTrace();
	    return null;
	}
    }

    private Object makeUndefinedTagValue(Tag tag, int[] bytes)
    {
	try
	{
	    if (EnumeratedTag.class.isAssignableFrom(tag.implementingClass))
	    {
		long lvalue;
		if (bytes.length == 0)
		    lvalue = 0;
		else if (bytes.length == 1)
		    lvalue = bytes[0]&0xff;
		else
		{
		    if (m_header.getByteOrder() == ByteOrder.MOTOROLA)
			lvalue = ((bytes[0]&0xff) << 8) | (bytes[1]&0xff);
		    else
			lvalue = ((bytes[1]&0xff) << 8) | (bytes[0]&0xff);
		}
		EnumeratedTag etag
		    = EnumeratedTag.instantiate(tag.implementingClass, lvalue);
		return new UndefinedTag(etag, m_header.getByteOrder(),
					bytes.length);
	    }
	    else
		return new UndefinedTag(bytes);
	}
	catch (Exception e)
	{
	    // this is an error with the static data
	    e.printStackTrace();
	    return null;
	}
    }

    private long makeULong(SubIntArray bytes, int o, int n)
    {
	SubIntArray mbytes
	    = new SubIntArray(bytes, o, n, m_header.getByteOrder(),
			      ByteOrder.MOTOROLA);
	long value = 0;
	if (bytes.length() > 3) value |= (mbytes.get(3)&0xff);
	if (bytes.length() > 2) value += (mbytes.get(2)&0xff) << 8;
	if (bytes.length() > 1) value += (mbytes.get(1)&0xff) << 16;
	if (bytes.length() > 0) value += (mbytes.get(0)&0xff) << 24;
	return value;
    }
    
    private int makeSLong(SubIntArray bytes, int o, int n)
    {
	SubIntArray mbytes
	    = new SubIntArray(bytes, o, n, m_header.getByteOrder(),
			      ByteOrder.MOTOROLA);
	int value = 0;
	if (bytes.length() > 3) value |= (mbytes.get(3)&0xff);
	if (bytes.length() > 2) value |= (mbytes.get(2)&0xff) << 8;
	if (bytes.length() > 1) value |= (mbytes.get(1)&0xff) << 16;
	if (bytes.length() > 0) value |= (mbytes.get(0)&0xff) << 24;
	return value;
    }

    private int makeUShort(SubIntArray bytes, int o, int n)
    {
	SubIntArray mbytes
	    = new SubIntArray(bytes, o, n, m_header.getByteOrder(),
			      ByteOrder.MOTOROLA);
	int value = 0;
	if (bytes.length() > 1) value |= (mbytes.get(1)&0xff);
	if (bytes.length() > 0) value |= (mbytes.get(0)&0xff) << 8;
	return value;
    }

    private short makeSShort(SubIntArray bytes, int o, int n)
    {
	SubIntArray mbytes
	    = new SubIntArray(bytes, o, n, m_header.getByteOrder(),
			      ByteOrder.MOTOROLA);
	short value = 0;
	if (bytes.length() > 1) value |= (mbytes.get(1)&0xff);
	if (bytes.length() > 0) value |= (mbytes.get(0)&0xff)  << 8;
	return value;
    }

    private short makeByte(SubIntArray bytes, int o, int n)
    {
	SubIntArray mbytes
	    = new SubIntArray(bytes, o, n, m_header.getByteOrder(),
			      ByteOrder.MOTOROLA);
	short value = 0;
	if (bytes.length() > 0) value |= mbytes.get(0)&0xff;
	return value;
    }

    private byte makeSByte(SubIntArray bytes, int o, int n)
    {
	SubIntArray mbytes
	    = new SubIntArray(bytes, o, n, m_header.getByteOrder(),
			      ByteOrder.MOTOROLA);
	byte value = 0;
	if (bytes.length() > 0) value |= mbytes.get(0)&0xff;
	return value;
    }

    private String makeString(SubIntArray bytes, int o, int n)
    {
	StringBuffer buf = new StringBuffer();
	for (int i=o; i<n+o; ++i)
	{
	    //if (!(i == n+o-1 && bytes[i] == 0))
		buf.append((char)(bytes.get(i)&0xff));
	}
	String str = buf.toString();
	int lastNonZero = -1;
	for (int i=str.length()-1; i>=0; --i)
	    if (str.charAt(i) != (char)0)
	    {
		lastNonZero = i;
		break;
	    }
	if (lastNonZero == -1) return "";
	if (lastNonZero < str.length()-1)
	    return str.substring(0,lastNonZero+1);
	return str;
    }

}
