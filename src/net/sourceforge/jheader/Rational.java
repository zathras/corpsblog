
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

/**
 * Stores EXIF signed rational value as a pair of longs.
 */
public class Rational implements Comparable
{
    /**
     * Numerator value.
     */
    public long numerator;

    /**
     * Denominator value.
     */
    public long denominator;

    /**
     * Construct from a numerator and denominator.
     *
     * @param numerator value to set numerator to.
     * @param denominator value to set denominator to.
     */
    public Rational(long numerator, long denominator)
    {
	this.numerator = numerator;
	this.denominator = denominator;
    }

    /**
     * Return the rational as a string
     *
     * @return string represenation as numerator/denominator.
     */
    public String toString()
    {
	return numerator + "/" + denominator;
    }

    /**
     * Return true iff passed object numerically equal to this object.
     *
     * @param o other object to compare.
     * @return true iff passed object numerically equal to this object
     */
    public boolean equals(Object o)
    {
	if (o instanceof Rational)
	{
	    Rational o1 = (Rational)o;
	    return (numerator == o1.numerator && denominator == o1.denominator)
		|| ((double)numerator/denominator
		    == (double)o1.numerator/o1.denominator);
	}
	else if (o instanceof Long)
	{
	    Long o1 = (Long)o;
	    return (numerator == o1 && denominator == 1)
		|| ((double)numerator/denominator
		    == (double)o1.longValue());
	}
	else if (o instanceof Integer)
	{
	    Integer o1 = (Integer)o;
	    return (numerator == o1 && denominator == 1)
		|| ((double)numerator/denominator
		    == (double)o1.intValue());
	}
	else return false;
    }

    /**
     * Returns -1, 0, 1 respectively if this object is numerically less
     * than, equal to or greater than passed object.
     *
     * @param o other object to compare
     * @return -1, 0, 1
     */
    public int compareTo(Object o)
    {
	if (o instanceof Rational)
	{
	    Rational o1 = (Rational)o;
	    double thisDouble = (double)numerator/denominator;
	    double oDouble = (double)o1.numerator/o1.denominator;
	    if (thisDouble < oDouble) return -1;
	    if (thisDouble > oDouble) return 1;
	    return 0;
	}
	else if (o instanceof Long)
	{
	    Long o1 = (Long)o;
	    double thisDouble = (double)numerator/denominator;
	    double oDouble = (double)(o1.longValue());
	    if (thisDouble < oDouble) return -1;
	    if (thisDouble > oDouble) return 1;
	    return 0;
	}
	else if (o instanceof Integer)
	{
	    Integer o1 = (Integer)o;
	    double thisDouble = (double)numerator/denominator;
	    double oDouble = (double)(o1.intValue());
	    if (thisDouble < oDouble) return -1;
	    if (thisDouble > oDouble) return 1;
	    return 0;
	}
	else return -1;
    }
	
}
