
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
import java.text.*;

/**
 * <p>Class for returning and setting EXIF date/time tag values.</p>
 *
 * <p>In EXIF, date/time tags are STRING type but follow an expected syntax.
 * This library always returns them as DateTimeTag objects, which simplifies
 * parsing out the components and setting them.</p>
 */
public class DateTimeTag
{
    /**
     * The original string as read (in case parsing fails) or formatted from
     * passed date/time
     */
    private String m_string = "";

    /**
     * The parsed date/time.
     */
    private Calendar m_calendar;
    
    /**
     * Construct from a string of the form YYYY:MM:DD HH:MM:SS.
     *
     * @throws TagFormatException if the string could not be parsed.
     */
    public DateTimeTag(String dateTime) throws TagFormatException
    {
	m_string = dateTime;
	SimpleDateFormat format = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
	try
	{
	    Date date = format.parse(dateTime);
	    m_calendar = new GregorianCalendar();
	    m_calendar.setTime(date);
	}
	catch (ParseException e)
	{
	    throw new TagFormatException(dateTime
					 + " is not a recognized date/time");
	}
    }

    /**
     * Construct from a calendar
     *
     * @param cal calendar.  This class stores a reference to it, not a copy.
     */
    public DateTimeTag(Calendar cal)
    {
	m_calendar = cal;
	SimpleDateFormat format = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
	m_string = format.format(m_calendar.getTime());
    }

    /**
     * Construct from a date and time
     *
     * @param year eg 2005
     * @param mon eg Calendar.JANUARY
     * @param day 1-31
     * @param hour 0-23
     * @param min 0-59
     * @param sec 0-59
     */
    public DateTimeTag(int year, int mon, int day, int hour, int min, int sec)
    {
	m_calendar = new GregorianCalendar(year,mon,day,hour,min,sec);
	SimpleDateFormat format = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
	m_string = format.format(m_calendar.getTime());
    }

    /**
     * Returns the string representation of this object (which is how
     * it will be written to the JPEG).
     *
     * @return string representation of date/time value.
     */
    public String asString()
    {
	return m_string;
    }

    /**
     * Returns this date/time object as a Calendar.
     *
     * This allows setting and retrieving of the various date/time components.
     *
     * @return date/time as a calendar.
     */
    public Calendar asCalendar()
    {
	return m_calendar;
    }

    /**
     * Same as asString();
     *
     * @return string representation of date/time value.
     */
    public String toString()
    {
	return m_string;
    }

}
