
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

public class Nikon_WhiteBalance extends EnumeratedTag
{
    public static final long AUTO = 0;
    public static final long PRESET = 1;
    public static final long DAYLIGHT = 2;
    public static final long INCANDESCENCE = 3;
    public static final long FLUORESCENCE = 4;
    public static final long CLOUDY = 5;
    public static final long SPEEDLIGHT = 6;

    static private Object[] data = new Object[] {
	AUTO, "Auto",
	PRESET, "Preset",
	DAYLIGHT, "Daylight",
	INCANDESCENCE, "Incandescence",
	FLUORESCENCE, "Fluorescence",
	CLOUDY, "Cloudy",
	SPEEDLIGHT, "SpeedLight"
    };

    static
    {
	populate(Nikon_WhiteBalance.class, data);
    }

    
    public Nikon_WhiteBalance(Long value) {super(value);}
    public Nikon_WhiteBalance(String value) throws TagFormatException {super(value);}
}
