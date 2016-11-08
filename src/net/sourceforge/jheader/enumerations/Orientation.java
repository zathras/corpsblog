
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
package net.sourceforge.jheader.enumerations;
import net.sourceforge.jheader.*;

import java.util.*;

public class Orientation extends EnumeratedTag
{
    /** 0th row top, 0th col left */
    public static final long TOP_LEFT = 1; 
    /** 0th row top, 0th col right */
    public static final long TOP_RIGHT = 2; 
    /** 0th row bottom, 0th col right */
    public static final long BOTTOM_RIGHT = 3; 
    /** 0th row bottom, 0th col left */
    public static final long BOTTOM_LEFT = 4; 
    /** 0th row left, 0th col top */
    public static final long LEFT_TOP = 5; 
    /** 0th row right, 0th col top */
    public static final long RIGHT_TOP = 6; 
    /** 0th row right, 0th col bottom */
    public static final long RIGHT_BOTTOM = 7; 
    /** 0th row left, 0th col bottom */
    public static final long LEFT_BOTTOM = 8; 
    
    static private Object[] data = new Object[] {
	TOP_LEFT,   "0th row top, 0th col left",
	TOP_RIGHT,   "0th row top, 0th col right",
	BOTTOM_RIGHT,   "0th row bottom, 0th col right",
	BOTTOM_LEFT,   "0th row bottom, 0th col left",
	LEFT_TOP,   "0th row left, 0th col top",
	RIGHT_TOP,   "0th row right, 0th col top",
	RIGHT_BOTTOM,   "0th row right, 0th col bottom",
	LEFT_BOTTOM,   "0th row left, 0th col bottom",
    };

    static
    {
	populate(Orientation.class, data);
    }

    
    public Orientation(Long value) {super(value);}
    public Orientation(String value) throws TagFormatException {super(value);}
}
