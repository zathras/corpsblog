
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
import java.lang.reflect.*;
import net.sourceforge.jheader.App1Header.*;
import net.sourceforge.jheader.JpegHeaders.ByteOrder;
import net.sourceforge.jheader.enumerations.*;

class TagOrId implements Comparable<TagOrId>
{
    public Tag tag;
    public int id;
    public TagOrId(Tag tag)
    {
	this.tag = tag;
	id = -1;
    }
    public TagOrId(int id)
    {
	this.id = id;
	tag = Tag.UNKNOWN;
    }
    public boolean isParsed()
    {
	return tag != Tag.UNKNOWN;
    }
    public int compareTo(TagOrId other)
    {
	if (isParsed() && other.isParsed())
	{
	    if (tag.id < other.tag.id) return -1;
	    if (tag.id > other.tag.id) return 1;
	    return 0;
	}
	if (isParsed() && !other.isParsed())
	{
	    if (tag.id < other.id) return -1;
	    if (tag.id > other.id) return 1;
	    return 0;
	}
	if (!isParsed() && other.isParsed())
	{
	    if (id < other.tag.id) return -1;
	    if (id > other.tag.id) return 1;
	    return 0;
	}
	//if (!isParsed() && !other.isParsed())
	{
	    if (id < other.id) return -1;
	    if (id > other.id) return 1;
	    return 0;
	}
	
    }

    public boolean equals(TagOrId other)
    {
	return tag == other.tag && id == other.id;
    }
}

class App1HeaderWriter
{
    private App1Header m_header = null;
    private int m_tiffHdrOffset = 0;
    private TreeMap<TagOrId, Integer> m_tagIdx
	= new TreeMap<TagOrId,Integer>(); // index within arraylist of tags
    private int m_makerNoteIdx = 0;
    private int m_exifOffsetIdx = 0;
    private int m_interopOffsetIdx = 0;
    private int m_ifd1OffsetIdx = 0;
    private int m_jpegIFOffsetIdx = 0;
    private int m_jpegIFByteCountIdx = 0;
    
    /**
     * Construct writer from the App1Header data
     */
    public App1HeaderWriter(App1Header header)
    {
	m_header = header;
    }

    /**
     * Writes header to an array of bytes.
     */
    public int[] write() throws TagFormatException
    {
	ArrayList<Integer> bytes = new ArrayList<Integer>();
	// start writing after the size block - we will prepend this.

	// EXIF header
	/*fromMotorolaOrder(bytes,
	  new int[]{0x45, 0x78, 0x69, 0x66, 0x00, 0x00});*/
	copyBytes(bytes, new int[]{0x45, 0x78, 0x69, 0x66, 0x00, 0x00});
	m_tiffHdrOffset = bytes.size();

	// byte order
	if (m_header.getByteOrder() == ByteOrder.MOTOROLA)
	    copyBytes(bytes, new int[]{0x4d,0x4d});
	else
	    copyBytes(bytes, new int[]{0x49,0x49});
	
	// TIFF header
	fromMotorolaOrder(bytes, new int[]{0x00, 0x2a});

	// IFD0
 	makeULong(bytes, bytes.size()-m_tiffHdrOffset+4, 4);
	writeIFD(bytes, ExifLocation.IFD0);

	// IFD1
	SortedMap<Tag,TagValue> ifd1Tags = m_header.getTags(ExifLocation.IFD1);
	if (ifd1Tags != null && ifd1Tags.size() > 0)
	{
	    ArrayList<Integer> offset_al = new ArrayList<Integer>();
	    makeULong(offset_al, bytes.size()-m_tiffHdrOffset, 4);
	    for (int i=0; i<offset_al.size(); ++i)
		bytes.set(i+m_ifd1OffsetIdx, offset_al.get(i));
	    writeIFD(bytes, ExifLocation.IFD1);
	}
	
	makeULong(bytes, 0, 4);

	// prepend size
	int size = bytes.size()+2;
	ArrayList<Integer> newBytes = new ArrayList<Integer>();
	ByteOrder prevByteOrder = m_header.getByteOrder();
	m_header.setByteOrder(ByteOrder.MOTOROLA);
	makeUShort(newBytes, size, 2);
	m_header.setByteOrder(prevByteOrder);
	newBytes.addAll(bytes);

	// return
	int[] ret = new int[newBytes.size()];
	for (int i=0; i<newBytes.size(); ++i)
	    ret[i] = newBytes.get(i);
	
	return ret;
    }

    private void writeIFD(ArrayList<Integer> bytes, ExifLocation loc)
	throws TagFormatException
    {	
	// write tags
	writeTagBlock(bytes,loc,m_header.getByteOrder(), m_tiffHdrOffset);

	// write subexif and thumbnail block
	TreeMap<Tag, TagValue> locationTags
	    = m_header.getAllParsedTags().get(loc);
	if (loc == ExifLocation.IFD0)
	{
	    if (locationTags.containsKey(Tag.EXIFOFFSET))
	    {
		int subExifOffset = bytes.size()-m_tiffHdrOffset;
		int idx = m_exifOffsetIdx;
		ArrayList<Integer> offset_al = new ArrayList<Integer>(4);
		makeULong(offset_al, subExifOffset, 4);
		for (int i=0; i<4; ++i)
		    bytes.set(i+idx, offset_al.get(i));
		writeSubExif(bytes);
	    }
	} // if (loc == ExifLocation.IFD0)
	else if (loc == ExifLocation.IFD1)
	{
	    int[] thumbnailBytes = m_header.getThumbnailBytes();
	    if (thumbnailBytes != null && thumbnailBytes.length > 0)
	    {
		{
		    ArrayList<Integer> offset_al = new ArrayList<Integer>();
		    makeULong(offset_al, bytes.size()-m_tiffHdrOffset, 4);
		    for (int i=0; i<offset_al.size(); ++i)
			bytes.set(i+m_jpegIFOffsetIdx, offset_al.get(i));
		}
		{
		    ArrayList<Integer> offset_al = new ArrayList<Integer>();
		    makeULong(offset_al, thumbnailBytes.length, 4);
		    for (int i=0; i<offset_al.size(); ++i)
			bytes.set(i+m_jpegIFByteCountIdx, offset_al.get(i));
		}
		copyBytes(bytes, thumbnailBytes);
	    }
	}
    }

    void writeSubExif(ArrayList<Integer> bytes)
	throws TagFormatException
    {
	// write tags
	writeTagBlock(bytes, ExifLocation.SUBEXIF, m_header.getByteOrder(),
		      m_tiffHdrOffset);

	// write makernote and interoperability blocks
	TreeMap<Tag, TagValue> locationTags
	    = m_header.getAllParsedTags().get(ExifLocation.SUBEXIF);
	if (locationTags.containsKey(Tag.MAKERNOTE))
	{
	    int makernoteOffset = bytes.size()-m_tiffHdrOffset;
	    int idx = m_makerNoteIdx;
	    ArrayList<Integer> offset_al = new ArrayList<Integer>(4);
	    makeULong(offset_al, makernoteOffset, 4);
	    for (int i=0; i<4; ++i)
		bytes.set(i+idx, offset_al.get(i));
	    writeMakerNote(bytes);
	}
	if (locationTags.containsKey(Tag.EXIFINTEROPERABILITYOFFSET))
	{
	    int interopOffset = bytes.size()-m_tiffHdrOffset;
	    int idx = m_interopOffsetIdx;
	    ArrayList<Integer> offset_al = new ArrayList<Integer>(4);
	    makeULong(offset_al, interopOffset, 4);
	    for (int i=0; i<4; ++i)
		bytes.set(i+idx, offset_al.get(i));
	    writeInterop(bytes);
	}

    }
    
    void writeInterop(ArrayList<Integer> bytes)
	throws TagFormatException
    {
	// write tags
	writeTagBlock(bytes, ExifLocation.INTEROP, m_header.getByteOrder(),
		      m_tiffHdrOffset);
    }
    
    void writeMakerNote(ArrayList<Integer> bytes)
	throws TagFormatException
    {
	if (m_header.getValue(Tag.MAKERNOTE) == null) return;
	// write tags
	switch (m_header.getCameraType())
	{
	case UNKNOWN:
	    writeUnknownMakerNote(bytes);
	    break;
	case AGFA:
	    writeUnknownMakerNote(bytes);
	    break;
	case EPSON:
	    writeUnknownMakerNote(bytes);
	    break;
	case CANON:
	    writeCanonMakerNote(bytes);
	    break;
	case CASIO1:
	    writeCasio1MakerNote(bytes);
	    break;
	case CASIO2:
	    writeUnknownMakerNote(bytes);
	    break;
	case FUJIFILM:
	    writeFujifilmMakerNote(bytes);
	    break;
	case KYOCERA:
	    writeUnknownMakerNote(bytes);
	    break;
	case MINOLTA1:
	    writeUnknownMakerNote(bytes);
	    break;
	case MINOLTA2:
	    writeUnknownMakerNote(bytes);
	    break;
	case MINOLTA3:
	    writeUnknownMakerNote(bytes);
	    break;
	case MINOLTA4:
	    writeUnknownMakerNote(bytes);
	    break;
	case MINOLTA5:
	    writeMinolta5MakerNote(bytes);
	    break;
	case NIKON1:
	    writeNikon1MakerNote(bytes);
	    break;
	case NIKON2:
	    writeNikon2MakerNote(bytes);
	    break;
	case OLYMPUS:
	    writeOlympusMakerNote(bytes);
	    break;
	case PANASONIC:
	    writeUnknownMakerNote(bytes);
	    break;
	case PENTAX1:
	    writeUnknownMakerNote(bytes);
	    break;
	case PENTAX2:
	    writeUnknownMakerNote(bytes);
	    break;
	case RICOH:
	    writeUnknownMakerNote(bytes);
	    break;
	case SONY:
	    writeUnknownMakerNote(bytes);
	    break;
	default:
	    // only happens with code misconfiguration
	    throw new TagFormatException("Unknown camera type");
	}
    }

    private void writeUnknownMakerNote(ArrayList<Integer> bytes)
	throws TagFormatException
    {
	copyBytes(bytes, m_header.getMakerNoteBytes());
    }
    
    private void writeCanonMakerNote(ArrayList<Integer> bytes)
	throws TagFormatException
    {
	writeTagBlock(bytes, ExifLocation.MAKERNOTE_CANON,
		      m_header.getByteOrder(), m_tiffHdrOffset);
	
    }

    private void writeCasio1MakerNote(ArrayList<Integer> bytes)
	throws TagFormatException
    {
	if (m_header.getCameraType() == CameraType.CASIO2)
	    copyBytes(bytes, m_header.getValue(Tag.MAKERNOTE).getAsUndefined().getBytes());
	else
	    writeTagBlock(bytes, ExifLocation.MAKERNOTE_CASIO,
			  m_header.getByteOrder(), m_tiffHdrOffset);
    }

    private void writeFujifilmMakerNote(ArrayList<Integer> bytes)
	throws TagFormatException
    {
	int makerNoteOffset = bytes.size();
	ByteOrder prevByteOrder = m_header.getByteOrder();
	m_header.setByteOrder(ByteOrder.INTEL);

	copyBytes(bytes, m_header.getMakerNoteHeader());
	writeTagBlock(bytes, ExifLocation.MAKERNOTE_FUJIFILM,
		      m_header.getByteOrder(), makerNoteOffset);
	
	m_header.setByteOrder(prevByteOrder);
    }

    private void writeMinolta5MakerNote(ArrayList<Integer> bytes)
	throws TagFormatException
    {
	writeTagBlock(bytes, ExifLocation.MAKERNOTE_OLYMPUS,
		      m_header.getByteOrder(), m_tiffHdrOffset);
    }

    private void writeNikon1MakerNote(ArrayList<Integer> bytes)
	throws TagFormatException
    {
	writeTagBlock(bytes, ExifLocation.MAKERNOTE_NIKON,
		      m_header.getByteOrder(), m_tiffHdrOffset);
    }

    private void writeNikon2MakerNote(ArrayList<Integer> bytes)
	throws TagFormatException
    {
	writeTagBlock(bytes, ExifLocation.MAKERNOTE_NIKOND1,
		      m_header.getByteOrder(), m_tiffHdrOffset);
    }

    private void writeOlympusMakerNote(ArrayList<Integer> bytes)
	throws TagFormatException
    {
	copyBytes(bytes, m_header.getMakerNoteHeader());
	writeTagBlock(bytes, ExifLocation.MAKERNOTE_OLYMPUS,
		      m_header.getByteOrder(), m_tiffHdrOffset);
    }

    private void writeTagBlock(ArrayList<Integer> bytes, ExifLocation loc,
		       ByteOrder byteOrder, int tiffHdrOffset)
	throws TagFormatException
    {
	ByteOrder prevByteOrder = m_header.getByteOrder();
	m_header.setByteOrder(byteOrder);

	SortedMap<Tag, TagValue> parsedTags = m_header.getTags(loc);
	TreeMap<Integer,UndefinedTag> unparsedTags
	    = m_header.getAllUnparsedTags().get(loc);
	int numEntries = parsedTags.size();
	if (unparsedTags != null) numEntries += unparsedTags.size();
	
	// write number of entries
	makeUShort(bytes, numEntries, 2);

	Iterator<Tag> parsedIt = parsedTags.keySet().iterator();
	Iterator<Integer> unparsedIt = unparsedTags==null ? null :  unparsedTags.keySet().iterator();
	Tag nextParsed
	    = (parsedIt.hasNext()) ? parsedIt.next() : Tag.UNKNOWN;
	int nextUnparsed
	    = (unparsedIt!=null && unparsedIt.hasNext()) ? unparsedIt.next() : -1;

	// loop through parsed and unparsed tags in id order.
	// for those whose size <= 4, write the value.  For others,
	// save away the location within the array and write a dummy
	// offset which we will replace with the real value later
	while (nextParsed != Tag.UNKNOWN || nextUnparsed != -1)
	{
	    // get next tag - parsed or unparsed next?
	    Tag thisParsed = Tag.UNKNOWN;
	    int thisUnparsed = -1;
	    if (nextParsed != Tag.UNKNOWN && (nextUnparsed==-1 || nextParsed.id < nextUnparsed))
	    {
		thisParsed = nextParsed;
		nextParsed
		    = (parsedIt.hasNext()) ? parsedIt.next() : Tag.UNKNOWN;
	    }
	    else
	    {
		thisUnparsed = nextUnparsed;
		nextUnparsed
		    = (unparsedIt!=null&&unparsedIt.hasNext()) ? unparsedIt.next() : -1;
	    }

	    if (thisParsed != Tag.UNKNOWN)
	    {
		// write parsed tag next
		TagValue value = parsedTags.get(thisParsed);
		int numComponents;
		if (value.getAsObject() instanceof ArrayList)
		    numComponents = value.getAsArrayList().size();
		else if (value.getAsObject() instanceof String)
		    numComponents = value.getAsString().length();
		else if (value.getAsObject() instanceof UndefinedTag)
		    numComponents = value.getAsUndefined().getBytes().length;
		else if (value.getAsObject() instanceof DateTimeTag)
		    numComponents = value.getAsString().length();
		else
		    numComponents = 1;
		/*if (thisParsed.format == TagFormat.STRING
		    && thisParsed.components != 1
		    && value.getAsString().charAt(value.getAsString().length()-1) != (char)0)
		    numComponents++; // we append a null to the value as well */
		// fix number of components for string - will will pad
		// or truncate when writing the actual string.
		if (thisParsed.format == TagFormat.STRING)
		{
		    if (thisParsed.components != -1)
			numComponents = thisParsed.components;
		    else
		    {
			if (value.getAsString().length() == 0 || value.getAsString().charAt(value.getAsString().length()-1) != (char)0)
			    numComponents++;
		    }
		}

		makeUShort(bytes, thisParsed.id, 2);
		makeUShort(bytes, thisParsed.format.id, 2);
		makeULong(bytes, numComponents, 4);
		if (thisParsed == Tag.EXIFOFFSET)
		    m_exifOffsetIdx = bytes.size();
		else if (thisParsed == Tag.EXIFINTEROPERABILITYOFFSET)
		    m_interopOffsetIdx = bytes.size();
		else if (thisParsed == Tag.MAKERNOTE)
		    m_makerNoteIdx = bytes.size();
		else if (thisParsed == Tag.IFD1_JPEGIFOFFSET)
		    m_jpegIFOffsetIdx = bytes.size();
		else if (thisParsed == Tag.IFD1_JPEGIFBYTECOUNT)
		    m_jpegIFByteCountIdx = bytes.size();
		if (numComponents*thisParsed.format.size <= 4)
		{
		    if (m_header.getMacroTags().contains(thisParsed.id))
			makeMacroTag(bytes, loc, thisParsed.id, true);
		    else
		    {
			makeTagValue(bytes, value, true);
		    }
		}
		else
		{
		    m_tagIdx.put(new TagOrId(thisParsed), bytes.size());
		    makeULong(bytes, 0, 4); // dummy offset - fill in later
		}
	    }
	    else
	    {
		// write unparsed next
	        int[] value = unparsedTags.get(thisUnparsed).getBytes();
		makeUShort(bytes, thisUnparsed, 2);
		//makeUShort(bytes, TagFormat.UNDEFINED.id, 2);
		//makeUShort(bytes, value.length, 2);
		if (value.length <= 10)
		{
		    copyBytes(bytes,value);
		}
		else
		{
		    for (int i=0; i<10; ++i)
			bytes.add(value[i]);
		    m_tagIdx.put(new TagOrId(thisUnparsed), bytes.size()-4);
		    //makeULong(bytes, 0, 4); // dummy offset - fill in later
		}
		
	    }
	}

	// write a dummy for offset to next IFD
	if (loc == ExifLocation.IFD0) m_ifd1OffsetIdx = bytes.size();
	makeULong(bytes, 0, 4);
	
	// write values whose size > 4
	for (TagOrId tagOrId : m_tagIdx.keySet())
	{
	    if (tagOrId.tag == Tag.MAKERNOTE) continue; // handle later
	    
	    // write offset into correct position in directory
	    int idx = m_tagIdx.get(tagOrId);
	    int offset = bytes.size() - tiffHdrOffset;
	    ArrayList<Integer> offset_al = new ArrayList<Integer>(4);
	    makeULong(offset_al, offset, 4);
	    for (int i=0; i<4; ++i)
		bytes.set(i+idx, offset_al.get(i));

	    // write value
	    if (tagOrId.isParsed())
	    {
		if (m_header.getMacroTags().contains(tagOrId.tag.id))
		    makeMacroTag(bytes,loc,tagOrId.tag.id,true);
		else
		{
		    makeTagValue(bytes, parsedTags.get(tagOrId.tag), false);
		}
	    }
	    else
	    {
		int[] bytes1 = unparsedTags.get(tagOrId.id).getBytes();
		for (int i=8; i<bytes1.length; ++i)
		    bytes.add(bytes1[i]);
		//copyBytes(bytes, unparsedTags.get(tagOrId.id).getBytes());
	    }
	}
	
	m_header.setByteOrder(prevByteOrder);

	m_tagIdx.clear();
    }

    private void makeMacroTag(ArrayList<Integer> bytes, ExifLocation loc,
			      int tagId, boolean inline)
	throws TagFormatException
    {
	SortedMap<Tag, TagValue> parsedTags = m_header.getTags(loc);
	HashMap<Integer, TreeSet<Tag> > locationMacroTags
	    = m_header.getTagLocationToMacroTagToTagSet().get(loc);
	if (locationMacroTags == null) return;
	TreeSet<Tag> childTags = locationMacroTags.get(tagId);
	if (childTags == null) return;
	int written = 0;
	for (Tag tag : childTags)
	{
	    // we assume we have the full byte range defined in schema
	    TagValue value = parsedTags.get(tag);
	    if (value == null)
	    {
		int[] valueBytes = new int[tag.format.size*tag.components];
		for (int i=0; i<valueBytes.length; ++i)
		    valueBytes[i] = 0;
		copyBytes(bytes, valueBytes);
		written += valueBytes.length;
	    }
	    else
		written += makeTagValue(bytes, value, false);
	}
	if (inline && written < 4)
	    pad(bytes, 4-written);
	
    }

    // returns number of byte written
    private int makeTagValue(ArrayList<Integer> bytes, TagValue value,
			      boolean inline) throws TagFormatException
    {
	int written = 0;
	switch (value.getTag().format)
	{
	case BYTE:
	    if (value.getAsObject() instanceof ArrayList)
	    {
		for (Object o : value.getAsArrayList())
		    makeByte(bytes, (short)((Long)o).longValue(),1);
		if (inline && value.getAsArrayList().size() < 4)
		    pad(bytes, 4-value.getAsArrayList().size());
		written = inline ? 4 : value.getAsArrayList().size();
	    }
	    else
	    {
		makeByte(bytes, (short)value.getAsLong().longValue(),
			 inline?4:1);
		written = inline ? 4 : 1;
	    }
	    break;
	case SBYTE:
	    if (value.getAsObject() instanceof ArrayList)
	    {
		for (Object o : value.getAsArrayList())
		    makeSByte(bytes, (byte)((Long)o).longValue(),1);
		if (inline && value.getAsArrayList().size() < 4)
		    pad(bytes, 4-value.getAsArrayList().size());
		written = inline ? 4 : value.getAsArrayList().size();
	    }
	    else
	    {
		makeSByte(bytes, (byte)value.getAsLong().longValue(),
			 inline?4:1);
		written = inline ? 4 : 1;
	    }
	    break;
	case USHORT:
	    if (value.getAsObject() instanceof ArrayList)
	    {
		for (Object o : value.getAsArrayList())
		    makeUShort(bytes, (int)((Long)o).longValue(),1);
		if (inline && value.getAsArrayList().size() < 4)
		    pad(bytes, 4-value.getAsArrayList().size()*2);
		written = inline ? 4 : value.getAsArrayList().size() * 2;
	    }
	    else
	    {
		makeUShort(bytes, (int)value.getAsLong().longValue(),
			   inline?4:2);
		written = inline ? 4 : 2;
	    }
	    break;
	case SSHORT:
	    if (value.getAsObject() instanceof ArrayList)
	    {
		for (Object o : value.getAsArrayList())
		    makeSShort(bytes, (short)((Long)o).longValue(),1);
		if (inline && value.getAsArrayList().size() < 4)
		    pad(bytes, 4-value.getAsArrayList().size()*2);
		written = inline ? 4 : value.getAsArrayList().size() * 2;
	    }
	    else
	    {
		makeSShort(bytes, (short)value.getAsLong().longValue(),
			   inline?4:2);
		written = inline ? 4 : 2;
	    }
	    break;
	case ULONG:
	    if (value.getAsObject() instanceof ArrayList)
	    {
		for (Object o : value.getAsArrayList())
		    makeULong(bytes, (long)((Long)o).longValue(),1);
		written = value.getAsArrayList().size() * 4;
	    }
	    else
	    {
		makeULong(bytes, (long)value.getAsLong().longValue(), 4);
		written = 4;
	    }
	    break;
	case SLONG:
	    if (value.getAsObject() instanceof ArrayList)
	    {
		for (Object o : value.getAsArrayList())
		    makeSLong(bytes, (int)((Long)o).longValue(),1);
		written = value.getAsArrayList().size() * 4;
	    }
	    else
	    {
		makeSLong(bytes, (int)value.getAsLong().longValue(), 4);
		written = 4;
	    }
	    break;
	case URATIONAL:
	    if (value.getAsObject() instanceof ArrayList)
	    {
		for (Object o : value.getAsArrayList())
		{
		    makeULong(bytes, ((Rational)o).numerator,4);
		    makeULong(bytes, ((Rational)o).denominator,4);
		}
		written = value.getAsArrayList().size() * 8;
	    }
	    else
	    {
		makeULong(bytes, value.getAsRational().numerator,4);
		makeULong(bytes, value.getAsRational().denominator,4);
		written = 8;
	    }
	    break;
	case SRATIONAL:
	    if (value.getAsObject() instanceof ArrayList)
	    {
		for (Object o : value.getAsArrayList())
		{
		    makeSLong(bytes, (int)((Rational)o).numerator,4);
		    makeSLong(bytes, (int)((Rational)o).denominator,4);
		}
		written = value.getAsArrayList().size() * 8;
	    }
	    else
	    {
		makeSLong(bytes, (int)value.getAsRational().numerator,4);
		makeSLong(bytes, (int)value.getAsRational().denominator,4);
		written = 4;
	    }
	    break;
	case UNDEFINED:
	{
	    int[] ubytes = value.getAsUndefined().getBytes();
	    copyBytes(bytes, ubytes);
	    written = ubytes.length;
	    if (inline && ubytes.length < 4)
	    {
		pad(bytes, 4-ubytes.length);
		written += 4-ubytes.length;
	    }
	    
	}
	    break;
	case STRING:
	{
	    String str = new String(value.getAsString());
	    /*if (value.getTag().components != 1
		&& str.charAt(str.length()-1) != (char)0)
		str += (char)0;*/
	    if (value.getTag().components != -1)
	    {
		if (str.length() > 0 && str.charAt(str.length()-1) == (char)0)
		    str = str.substring(0,str.length()-1);
		if (value.getTag().components-1 < str.length())
		    str = str.substring(0,value.getTag().components-1);
		else if (value.getTag().components-1 > str.length())
		{
		    for (int i=str.length(); i<value.getTag().components-1;
			 ++i)
			str += " ";
		}
		str += (char)0;
	    }
	    else
	    {
		if (str.length() == 0 || str.charAt(str.length()-1) != (char)0)
		    str += (char)0;
	    }
	    int[] ubytes = new int[str.length()];
	    for (int i=0; i<ubytes.length; ++i)
		ubytes[i] = (int)str.charAt(i) & 0xff;
	    copyBytes(bytes, ubytes);
	    written = ubytes.length;
	    if (inline && str.length() < 4)
	    {
		pad(bytes, 4-str.length());
		written += 4-str.length();
	    }
	}
	    break;
	default:
	    // only happens if code is misconfigured
	    throw new TagFormatException("Unrecognized tag format");
	}
	return written;
    }

    private void makeULong(ArrayList<Integer> bytes, long num, int len)
    {
	writeInt(bytes,num,4);
	pad(bytes,len-4);
    }
    
    private void makeSLong(ArrayList<Integer> bytes, int num, int len)
    {
	writeInt(bytes,num,4);
	pad(bytes,len-4);
    }
    
    private void makeUShort(ArrayList<Integer> bytes, int num, int len)
    {
	writeInt(bytes,num,2);
	pad(bytes,len-2);
    }
    
    private void makeSShort(ArrayList<Integer> bytes, short num, int len)
    {
	writeInt(bytes,num,2);
	pad(bytes,len-2);
    }
    
    private void makeByte(ArrayList<Integer> bytes, short num,int len)
    {
	writeInt(bytes,num,1);
	pad(bytes,len-1);
    }
    
    private void makeSByte(ArrayList<Integer> bytes, byte num, int len)
    {
	writeInt(bytes,num,1);
	pad(bytes,len-1);
    }

    private void makeString(ArrayList<Integer> bytes, String str, int len)
    {
	for (int i=0; i<str.length(); ++i)
	    bytes.add(((int)str.charAt(i))&0xff);
    }
    
    private void writeInt(ArrayList<Integer> bytes, long num, int len)
    {
	int[] mbytes = new int[len];
	if (len == 4)
	{
	    mbytes[3] = (int)(num) & 0xff;
	    mbytes[2] = (int)(num>>8) & 0xff;
	    mbytes[1] = (int)(num>>16) & 0xff;
	    mbytes[0] = (int)(num>>24) & 0xff;
	}
	else if (len == 2)
	{
	    mbytes[1] = (int)(num) & 0xff;
	    mbytes[0] = (int)(num>>8) & 0xff;
	}
	else if (len == 1)
	{
	    mbytes[0] = (int)(num) & 0xff;
	}
	fromMotorolaOrder(bytes, mbytes);
    }

    private void pad (ArrayList<Integer> bytes, int len)
    {
	for (int i=0; i<len; ++i)
	    bytes.add(0);
    }

    private void fromMotorolaOrder(ArrayList<Integer> to, int[] from)
    {
	if (m_header.getByteOrder() == ByteOrder.MOTOROLA)
	{
	    for (int i=0; i<from.length; ++i)
		to.add(from[i]);
	}
	else
	{
	    for (int i=0; i<from.length; ++i)
		to.add(from[from.length-1-i]);
	}
    }

    private void fromIntelOrder(ArrayList<Integer> to, int[] from)
    {
	if (m_header.getByteOrder() == ByteOrder.INTEL)
	{
	    for (int i=0; i<from.length; ++i)
		to.add(from[i]);
	}
	else
	{
	    for (int i=0; i<from.length; ++i)
		to.add(from[from.length-1-i]);
	}
    }

    private void copyBytes(ArrayList<Integer> to, int[] from)
    {
	for (int i=0; i<from.length; ++i)
	    to.add(from[i]);
    }

}
