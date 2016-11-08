
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
import net.sourceforge.jheader.App1Header.Tag;
import net.sourceforge.jheader.App1Header.TagFormat;
import java.lang.reflect.*;

/**
 * Generic class for storing tag values.
 *
 * <p>This class can return the tag as various types with automatic conversion
 * where possible.</p>
 *
 * <p><b>Returning values</b></p>
 * <p>The following table shows what implicit conversions can be made.
 * N indicates native format, I indicates implicitly converted.  For example,
 * if the Tag whose value this stores has a TagFormat of USHORT then
 * it is possible to call getAsRational().  You may only return a value
 * as an EnumeratedTag if the implementingClass in Tag is an
 * instance of EnumeratedTag.  Similarly you may only return a value as
 * DateTimeTag if implementingClass is DateTimeTag.
 * See {@link App1Header.Tag}.
 *
 * <table border="1" cellspacing="0">
 * <tr>
 * <td><b>Tag Format</b></td><td colspan="7"><b>Java Type</b></td>
 * </tr>
 * <tr>
 * <td>&nbsp;</td>
 * <td><b>Integer</b></td><td><b>Long</b></td><td><b>Rational</b></td>
 * <td><b>String</b><td><b>UndefinedTag</b></td></td>
 * <td><b>DateTimeTag</b></td><td><b>EnumeratedTag</b></td>
 * </tr>
 * <tr>
 * <td>BYTE</td>
 * <td>I</td><td>N</td><td>I</td><td>I<td>&nbsp;</td></td>
 * <td>&nbsp;</td><td>N<sup>*</sup></td>
 * </tr>
 * <tr>
 * <td>SBYTE</td>
 * <td>I</td><td>N</td><td>I</td><td>I<td>&nbsp;</td></td>
 * <td>&nbsp;</td><td>N<sup>*</sup></td>
 * </tr>
 * <tr>
 * <td>USHORT</td>
 * <td>I</td><td>N</td><td>I</td><td>I<td>&nbsp;</td></td>
 * <td>&nbsp;</td><td>N<sup>*</sup></td>
 * </tr>
 * <tr>
 * <td>SSHORT</td>
 * <td>I</td><td>N</td><td>I</td><td>I<td>&nbsp;</td></td>
 * <td>&nbsp;</td><td>N<sup>*</sup></td>
 * </tr>
 * <tr>
 * <td>ULONG</td>
 * <td>I</td><td>N</td><td>I</td><td>I<td>&nbsp;</td></td>
 * <td>&nbsp;</td><td>N<sup>*</sup></td>
 * </tr>
 * <tr>
 * <td>SLONG</td>
 * <td>I</td><td>N</td><td>I</td><td>I<td>&nbsp;</td></td>
 * <td>&nbsp;</td><td>N<sup>*</sup></td>
 * </tr>
 * <tr>
 * <td>URATIONAL</td>
 * <td>&nbsp;</td><td>&nbsp;</td><td>N</td><td>I<td>&nbsp;</td></td>
 * <td>&nbsp;</td><td>&nbsp;</td>
 * </tr>
 * <tr>
 * <td>SRATIONAL</td>
 * <td>&nbsp;</td><td>&nbsp;</td><td>N</td><td>I<td>&nbsp;</td></td>
 * <td>&nbsp;</td><td>&nbsp;</td>
 * </tr>
 * <tr>
 * <td>STRING</td>
 * <td>&nbsp;</td><td>&nbsp;</td><td>&nbsp;</td><td>N<td>&nbsp;</td></td>
 * <td>N<sup>*</sup></td><td>&nbsp;</td>
 * </tr>
 * <tr>
 * <td>UNDEFINED</td>
 * <td>&nbsp;</td><td>&nbsp;</td><td>&nbsp;</td><td>I<td>N</td></td>
 * <td>&nbsp;</td><td>N<sup>*</sup></td>
 * </tr>
 * </table>
 * <sup>*</sup> only when an tag's implementingClass is that type.
 * </p>
 *
 * <p><b>Setting values</b></p>
 * <p>Following is a table of types that you should pass for each tag
 * type.  For enumerated tags, you may either pass the appropriate
 * EnumeratedTag class (see the implementingClass in {@link App1Header.Tag})
 * or a Long which conforms to the enumeration.  For date/time tags, you
 * may either pass a DateTimeTag or a String with conforming format.</p>
 *
 * <table border="1" cellspacing="0">
 * <tr><td><b>Tag Format</b></td><td><b>Java Type</b></td></tr>
 * <tr><td>BYTE</td><td>Long or EnumeratedTag</td></tr>
 * <tr><td>SBYTE</td><td>Long or EnumeratedTag</td></tr>
 * <tr><td>USHORT</td><td>Long or EnumeratedTag</td></tr>
 * <tr><td>SSHORT</td><td>Long or EnumeratedTag</td></tr>
 * <tr><td>ULONG</td><td>Long or EnumeratedTag</td></tr>
 * <tr><td>SLONG</td><td>Long or EnumeratedTag</td></tr>
 * <tr><td>URATIONAL</td><td>Rational</td></tr>
 * <tr><td>SRATIONAL</td><td>Rational</td></tr>
 * <tr><td>STRING</td><td>String or DateTimeTag</td></tr>
 * <tr><td>UNDEFINED</td><td>UndefinedTag</td></tr>
 * </table>
 * </p>
 * 
 */
public class TagValue
{
    private static final float BRIGHTNESSVALUE_SV_ISO100 = 5;
    private static final float BRIGHTNESSVALUE_SV_ISO200 = 6;
    private static final float BRIGHTNESSVALUE_SV_ISO400 = 7;
    private static final float BRIGHTNESSVALUE_SV_ISO125 = (float)5.32;
   
    private Object m_value = null;
    private Tag m_tag = null;

    /**
     * Constructor from a tag and a value.
     *
     * @param tag tag whose value to set.
     * @param value tag's value.
     * @throws TagFormatException if value's type is not appropriate
     *                            for the tag (see class documentation for
     *                            table).
     */
    public TagValue(Tag tag, Object value) throws TagFormatException
    {
	m_tag = tag;

	try
	{
	    if (EnumeratedTag.class.isAssignableFrom(tag.implementingClass)
		&& !EnumeratedTag.class.isAssignableFrom(value.getClass()))
	    {
		if (value instanceof Long)
		{
		    Constructor con
			= tag.implementingClass.getConstructor(Long.class);
		    m_value = con.newInstance((Long)value);
		}
		else if (value instanceof Integer)
		{
		    // allows for rather sloppy usage
		    Constructor con
			= tag.implementingClass.getConstructor(Long.class);
		    m_value = con.newInstance(new Long(((Integer)value).intValue()));
		}
		else if (value instanceof UndefinedTag)
		{
		    if (((UndefinedTag)value).getEnum() == null)
			throw new TagFormatException("When constructing an UNDEFINED TagValue for an EnumeratedTag type, must use constructor from EnumeratedTag");
		    m_value = value;
		}
		else
		    throw new TagFormatException("Cannot store tag " + tag
						 + " as " + value.getClass());
	    }
	    else if (DateTimeTag.class.isAssignableFrom(tag.implementingClass)
		&& !DateTimeTag.class.isAssignableFrom(value.getClass()))
	    {
		if (value instanceof String)
		{
		    Constructor con
			= tag.implementingClass.getConstructor(String.class);
		    m_value = con.newInstance((String)value);
		}
		else
		    throw new TagFormatException("Cannot store tag " + tag
		    + " as " + value.getClass());
	    }
	    else
	    {
		if (value instanceof Integer)
		{
		    // allows for rather sloppy usage
		    m_value = new Long(((Integer)value).intValue());
		}
		else
		    m_value = value;
	    }
	}
	catch (NoSuchMethodException e)
	{
	    // this is an error with the static data
	    e.printStackTrace();
	    m_value = null;
	}
	catch (InstantiationException e)
	{
	    // can't happen as LongEnumeratedClass constructor
	    // doesn't throw an exception
	    e.printStackTrace();
	    m_value = null;
	}
	catch (IllegalAccessException e)
	{
	    // can't happen as LongEnumeratedClass constructor
	    // doesn't throw an exception
	    e.printStackTrace();
	    m_value = null;
	}
	catch (InvocationTargetException e)
	{
	    // can't happen as LongEnumeratedClass constructor
	    // doesn't throw an exception
	    e.printStackTrace();
	    m_value = null;
	}
	
	validate();
    }

    /**
     * Return the tag.
     *
     * @return the tag.
     */
    public Tag getTag()
    {
	return m_tag;
    }

    /**
     * Return the value in its native format.
     *
     * @return the value as an Object.
     */
    public Object getAsObject()
    {
	return m_value;
    }

    /**
     * Return the value as a DateTimeTag.
     
     * @return the value as a DateTimeTag.
     * @throws TagFormatException if the implicit conversion cannot be made.
     */
    public DateTimeTag getAsDateTimeTag() throws TagFormatException
    {
	if (m_value instanceof DateTimeTag) return (DateTimeTag)m_value;
	throw new TagFormatException("Value is not a DateTimeTag");
    }
    
    /**
     * Return the value as an ArrayList.
     *
     * The value must be a type other than UNDEFINED or STRING and must
     * have more than one component, otherwise this throws an exception.
     *
     * @return the tag as an ArrayList
     * @throws TagFormatException if the implicit conversion cannot be made.
     */
    public ArrayList getAsArrayList() throws TagFormatException
    {
	if (m_value instanceof EnumeratedTag)
	{
	    ArrayList<Long> list = new ArrayList<Long>();
	    list.add(new Long(((EnumeratedTag)m_value).asLong()));
	    return list;
	}
	if (m_value instanceof UndefinedTag)
	{
	    ArrayList<Integer> list = new ArrayList<Integer>();
	    for (int num : ((UndefinedTag)m_value).getBytes())
		list.add(new Integer(num));
	    return list;
	}
	if (m_value instanceof ArrayList) return (ArrayList)m_value;
	ArrayList<Object> list = new ArrayList<Object>();
	list.add(m_value);
	return list;
    }

    /**
     * Return the tag as an integer.
     *
     * Only works for BYTE, SBYTE, SSHORT, SLONG values.
     *
     * @return the value as an Integer.
     * @throws TagFormatException if the implicit conversion cannot be made.
     */
    public Integer getAsInteger() throws TagFormatException
    {
	if (m_value == null | m_tag == null) return null;
	if (m_value instanceof ArrayList)
	    throw new TagFormatException("Cannot return array as Integer");
	switch (m_tag.format)
	{
	case BYTE:
	case USHORT:
	case SBYTE:
	case SSHORT:
	case SLONG:
	    // typecasting safe for these types
	    if (m_value instanceof EnumeratedTag)
		return (int)(((EnumeratedTag)m_value).asLong());
	    return (int)(((Long)m_value).longValue());
	default:
	    throw new TagFormatException("Cannot return a value of "
					 + m_tag.format + " as an Integer");
	}
    }

    /**
     * Return the tag as a long.
     *
     * Only works for BYTE, SBYTE, SSHORT, SLONG, ULONG values.
     *
     * @return the value as a Long
     * @throws TagFormatException if the implicit conversion cannot be made.
     */
    public Long getAsLong() throws TagFormatException
    {
	if (m_value == null | m_tag == null) return null;
	if (m_value instanceof ArrayList)
	    throw new TagFormatException("Cannot return array as Long");
	switch (m_tag.format)
	{
	case BYTE:
	case USHORT:
	case SBYTE:
	case SSHORT:
	case SLONG:
	case ULONG:
	    if (m_value instanceof EnumeratedTag)
		return ((EnumeratedTag)m_value).asLong();
	    return (Long)m_value;
	default:
	    throw new TagFormatException("Cannot return a value of "
					 + m_tag.format + " as a Long");
	}
    }

    /**
     * Return the tag as a rational.
     *
     * Only works for BYTE, SBYTE, SSHORT, SLONG, ULONG, SRATIONAL,
     * URATIONAL values.
     *
     * @return the value as a Rational.
     * @throws TagFormatException if the implicit conversion cannot be made.
     */
    public Rational getAsRational() throws TagFormatException
    {
	if (m_value == null | m_tag == null) return null;
	if (m_value instanceof ArrayList)
	    throw new TagFormatException("Cannot return array as LongRational");
	switch (m_tag.format)
	{
	case BYTE:
	case USHORT:
	case SBYTE:
	case SSHORT:
	case SLONG:
	case ULONG:
	    if (m_value instanceof EnumeratedTag)
		return new Rational(((EnumeratedTag)m_value).asLong(),1);
	    return new Rational((Long)m_value, 1);
	case SRATIONAL:
	case URATIONAL:
	    return (Rational)m_value;
	default:
	    throw new TagFormatException("Cannot return a value of "
					 + m_tag.format + " as a Rational");
	}
    }

    /**
     * Return the tag as a string.
     *
     * Works for all tag types.
     *
     * @return the value as a String.
     */
    public String getAsString()
    {
	if (m_value == null | m_tag == null) return null;
	if (m_value instanceof ArrayList)
	{
	    StringBuffer buf = new StringBuffer();
	    ArrayList list = (ArrayList)m_value;
	    for (int i=0; i<list.size() && i < 4; ++i)
	    {
		if (i > 0) buf.append(",");
		buf.append(list.get(i).toString());
	    }
	    if (list.size() > 4) buf.append(",...");
	    return buf.toString();
	}
	// each tag with special formatting
	return m_value.toString();
    }

    /**
     * Return the tag as an UndefinedTag.
     *
     * Only works for tags of type UNDEFINED.
     *
     * @return the value as an UndefinedTag.
     * @throws TagFormatException if the implicit conversion cannot be made.
     */
    public UndefinedTag getAsUndefined() throws TagFormatException
    {
	if (m_value == null | m_tag == null) return null;
	if (m_value instanceof ArrayList)
	    throw new TagFormatException("Cannot return array as UndefinedTag");
	switch (m_tag.format)
	{
	case UNDEFINED:
	    return (UndefinedTag)m_value;
	default:
	    throw new TagFormatException("Cannot return a value of "
				 + m_tag.format + " as an UndefinedTag");
	}
    }

    /**
     * Return the value as an enumeration.
     *
     * Only works for tags of type BYTE, SBYTE, SSHORT, USHORT, SLONG, ULONG
     * which have an enumeration defined (see implementingClass in the Tag
     * enum (getTag() to see this).
     *
     * @return the value as an EnumeratedTag.
     * @throws TagFormatException if the implicit conversion cannot be made.
     */
    public EnumeratedTag getAsEnumeration()  throws TagFormatException
    {
	if (m_value instanceof EnumeratedTag) return (EnumeratedTag)m_value;
	if (m_value instanceof ArrayList)
	    throw new TagFormatException("Cannot return array as EnumeratedTag");
	if (m_value instanceof UndefinedTag)
	{
	    UndefinedTag tag = (UndefinedTag)m_value;
	    EnumeratedTag e = tag.getEnum();
	    if (e == null)
		throw new TagFormatException("Cannot return tag " + m_tag
					     + " as enumeration");
	    return e;
	}
	throw new TagFormatException("Cannot return tag as enumerion");
    }

    /**
     * Same as getAsString()
     *
     * @return the value as a String.
     */
    public String toString()
    {
	return getAsString();
    }

    /**
     * Throws an exception if the tag value doesn't match what the tag
     * expects.
     *
     * See class documentation for further details.
     *
     * @throws TagFormatException if the value is not appropriate for the tag.
     */
    private void validate() throws TagFormatException
    {
	if (m_tag == null)
	    throw new TagFormatException("No tag defined in TagValue");
	
	/*switch (m_tag.format)
	{
	case BYTE:
	case USHORT:
	case SBYTE:
	case SSHORT:
	case SLONG:
	case ULONG:
	    if (!(m_value instanceof ArrayList)
		&& !(m_value instanceof Long)
		&& !(m_value instanceof m_tag.format.implementingClass))
		throw new TagFormatException("Cannot store tag " + m_tag
					     + " as " + m_value.getClass());
	    break;
	case URATIONAL:
	case SRATIONAL:
	    if (!(m_value instanceof ArrayList)
		&& !(m_value instanceof Rational))
		throw new TagFormatException("Cannot store tag " + m_tag
					     + " as " + m_value.getClass());
	    break;
	case UNDEFINED:
	    if (!(m_value instanceof UndefinedTag)
		&& !(m_value instanceof m_tag.format.implementingClass))
		throw new TagFormatException("Cannot store tag " + m_tag
					     + " as " + m_value.getClass());
	    break;
	case STRING:
	    if (!(m_value instanceof String)
		&& !(m_value instanceof m_tag.format.implementingClass))
		throw new TagFormatException("Cannot store tag " + m_tag
					     + " as " + m_value.getClass());
	    break;
	    }*/

	try
	{
	    switch (m_tag.format)
	    {
	    case BYTE:
	    case USHORT:
	    case SBYTE:
	    case SSHORT:
	    case SLONG:
	    case ULONG:
	    if (!(m_value instanceof ArrayList))
		getAsLong();
	    break;
	    case SRATIONAL:
	    case URATIONAL:
		if (!(m_value instanceof ArrayList))
		    getAsRational();
		break;
	    case UNDEFINED:
		if (!(m_value instanceof ArrayList))
		    getAsUndefined();
	    case STRING:
		getAsString();
	    break;
	    }
	}
	catch (TagFormatException e)
	{
	    throw new TagFormatException("Cannot set tag " + m_tag + " to "
					 + m_value.getClass());
	}

	if (m_value instanceof ArrayList && m_tag.components >= 0
	    && m_tag.components != ((ArrayList)m_value).size())
	    throw new TagFormatException("Wrong number of components in value "
					 + " for tag " + m_tag);
	    
	if (!(m_value instanceof ArrayList)
	    && !(m_value instanceof String)
	    && !(m_value instanceof DateTimeTag)
	    && !(m_value instanceof UndefinedTag)
	    && m_tag.components != 1
	    && m_tag.components != -1) 
	    throw new TagFormatException("Wrong number of components in value "
					 + " for tag " + m_tag);

	if (m_value instanceof UndefinedTag
	    && m_tag.components >= 0
	    && ((UndefinedTag)m_value).getBytes().length
	    != m_tag.components)
	    throw new TagFormatException("Wrong number of components in value "
					 + " for tag " + m_tag);
    }
}
