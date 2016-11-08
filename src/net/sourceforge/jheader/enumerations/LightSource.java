
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

public class LightSource extends EnumeratedTag
{
    public static final long UNKNOWN = 0;
    public static final long DAYLIGHT = 1;
    public static final long FLUORESCENT = 2;
    public static final long TUNGSTEN = 3;
    public static final long FLASH = 10;
    public static final long STANDARD_LIGHT_A = 17;
    public static final long STANDARD_LIGHT_B = 18;
    public static final long STANDARD_LIGHT_C = 19;
    public static final long D55 = 20;
    public static final long D65 = 21;
    public static final long D75 = 22;
    public static final long OTHER = 255;
    
    static private Object[] data = new Object[] {
	UNKNOWN, "Unknown",
	DAYLIGHT, "Daylight",
	FLUORESCENT, "Fluorescent",
	TUNGSTEN, "Tungsten",
	FLASH, "Flash",
	STANDARD_LIGHT_A, "Standard Light A",
	STANDARD_LIGHT_B, "Standard Light B",
	STANDARD_LIGHT_C, "Standard Light C",
	D55, "D55",
	D65, "D65",
	D75, "D75",
	OTHER, "Other",
    };

    static
    {
	populate(LightSource.class, data);
    }

    
    public LightSource(Long value) {super(value);}
    public LightSource(String value) throws TagFormatException {super(value);}
}
