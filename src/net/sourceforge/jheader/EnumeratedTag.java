
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
import java.lang.reflect.*;

/**
 * <p>A tag value of type SBYTE, UBYTE, USHORT, SSHORT, ULONG or LONG that has
 * a predefined set of allowable values.</p>
 *
 * <p>Each derived class has a set of constants which represent the allowable
 * values.  The methods asLong() and asString() (also toString()) return the
 * long and String representaitons respectively.</p>
 *
 * <p>When retrieving enumerated types from App1Header using
 * TagValue getValue(tag), calling getAsEnumeration() on the TagValue will
 * return an instance of the appropriate class deriving from EnumeratedTag.
 * You can then call asLong(), asString() or getAll() on this.</p>
 *
 * <p>If you want to set a value with App1Header using void setValue(TagValue),
 * you can do something like:
 * <pre>
 * if (EnumeratedTag.class.isAssignableFrom(tag.implementingClass))
 * {
 *     EnumeratedTag etag = EnumeratedTag.instantiate(tag.implementingClass, myValue);
 * }
 * </pre>
 * where myValue is a Long or a String (you can instantiate fom the numeric
 * or textual value).  If you simply want a list of allowable values, you
 * can do
 * <pre>
 * EnumeratedTag.instantiate(tag.implementingClass, (long)0).getAll(false)
 * </pre>
 * </p>
 */
public class EnumeratedTag
{
    static protected HashMap<Class, TreeMap<Long, String> >
	s_classToLongToString = new HashMap<Class, TreeMap<Long, String> >();
    protected long m_value;

    /**
     * A convenient but not very typesafe way of setting static data
     * for enumerated classes.
     *
     * Array should have the form
     * {longValue1 stringValue1 ... longValuen stringValuen}
     * where the longValues are Long and the stringValues are String.
     */
    static protected void populate(Class enumeratedClass, Object[] data)
    {
	TreeMap<Long, String> map = new TreeMap<Long, String>();
	s_classToLongToString.put(enumeratedClass, map);
	for (int i=0; i<data.length; i+=2)
	    map.put((Long)data[i], (String)data[i+1]);
    }

    /**
     * Construct a tag value from its numerical value.
     */
    protected EnumeratedTag(long value)
    {
	m_value = value;
    }

    /**
     * Construct a tag value from its numerical value.
     */
    protected EnumeratedTag(Long value)
    {
	m_value = value.longValue();
    }

    /**
     * Construct a tag value from its string value.
     *
     * Case insensitive.
     *
     * @throws TagFormatException if the passed string does not correspond
     *                            to an allowable value.
     */
    protected EnumeratedTag(String value) throws TagFormatException
    {
	TreeMap<Long, String> valueToStr = s_classToLongToString.get(getClass());
	if (valueToStr == null) // this would be a configuration error
	    throw new TagFormatException("No values found for tag");
	boolean found = false;
	String value_uc = value.toUpperCase();
	for (Long lvalue : valueToStr.keySet())
	{
	    if (valueToStr.get(lvalue).toUpperCase().equals(value_uc))
	    {
		m_value = lvalue;
		found = true;
		break;
	    }
	}
	if (!found)
	    throw new TagFormatException("No value " + value_uc + " found");
    }

    /**
     * Return the tag's numeric value.
     *
     * @return value as a long.
     */
    public long asLong()
    {
	return m_value;
    }

    /**
     * Return the tag value's textual name.
     *
     * @return value as a string.
     */
    public String asString()
    {
	String value = s_classToLongToString.get(getClass()).get(m_value);
	return value == null ? "Unknown ("+m_value+")" : value;
    }

    /**
     * Return all possible values as a sorted map of numerical value
     * to textual name.
     *
     * Same as getAll(false).
     *
     * @return map of value ids to value names, in numeric order.
     */
    public SortedMap<Long, String> getAll()
    {
	return getAll(false);
    }

    /**
     * Return all possible values as a sorted map of numerical value
     * to textual name.
     *
     * @param strict if the current value of this tag is not in the set
     *               of allowable tags, giving strict=true excludes this value
     *               from the set that is returned.
     * @return map of value ids to value names, in numeric order.
     */
    public SortedMap<Long, String> getAll(boolean strict)
    {
	TreeMap<Long, String> map = s_classToLongToString.get(getClass());
	if (map == null) return null;
	if (map.containsKey(m_value) || strict)
	    return Collections.unmodifiableSortedMap(map);
	TreeMap<Long, String> ret = new TreeMap<Long, String>(map);
	ret.put(m_value, "Unknown ("+m_value+")");
	return ret;
    }

    /**
     * Same as asString().
     *
     * @return tag value as a string.
     */
    public String toString()
    {
	return asString();
    }

    /**
     * Instantiates a base class of EnumeratedTag and returns it.
     *
     * This will throw an exception if theClass does not extend
     * EnumeratedTag.  The signature does not enforce this to simplify
     * calling code.
     *
     * @param theClass the class you wish to instantiate
     * @param value the value that is passed to the constructor of theClass.
     * @return instance of theClass.
     */
    static public EnumeratedTag instantiate(Class theClass, Long value)
	throws NoSuchMethodException, InstantiationException,
	       InvocationTargetException, IllegalAccessException
    {
	Constructor con = theClass.getConstructor(Long.class);
	return (EnumeratedTag)con.newInstance(value);
    }
	
    /**
     * Instantiates a base class of EnumeratedTag and returns it.
     *
     * This will throw an exception if theClass does not extend
     * EnumeratedTag.  The signature does not enforce this to simplify
     * calling code.
     *
     * @param theClass the class you wish to instantiate
     * @param value the value that is passed to the constructor of theClass.
     * @return instance of theClass.
     */
    static public EnumeratedTag instantiate(Class theClass, String value)
	throws NoSuchMethodException, InstantiationException,
	       InvocationTargetException, IllegalAccessException
    {
	Constructor con = theClass.getConstructor(String.class);
	return (EnumeratedTag)con.newInstance(value);
    }
	
}
