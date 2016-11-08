
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

public class Olympus_SpecialMode extends EnumeratedTag
{
    public static final long NORMAL = 0;
    public static final long UNKNOWN = 1;
    public static final long FAST = 2;
    public static final long PANORAMA = 3;
    public static final long PANORAMA_LEFT_TO_RIGHT = 1;
    public static final long PANORAMA_RIGHT_TO_LEFT = 2;
    public static final long PANORAMA_BOTTOM_TO_TOP = 3;
    public static final long PANORAMA_TOP_TO_BOTTOM = 4;

    static private Object[] data = new Object[] {
	NORMAL, "Normal",
	UNKNOWN, "Unknown",
	FAST, "Fast",
	PANORAMA, "Panoram",
	PANORAMA_LEFT_TO_RIGHT, "Panorama left to right",
	PANORAMA_RIGHT_TO_LEFT, "Panaorama right to left",
	PANORAMA_BOTTOM_TO_TOP, "Panorama bottom to top",
	PANORAMA_TOP_TO_BOTTOM, "Panorama top to bottom",
    };

    static
    {
	populate(Olympus_SpecialMode.class, data);
    }

    
    public Olympus_SpecialMode(Long value) {super(value);}
    public Olympus_SpecialMode(String value) throws TagFormatException {super(value);}
}
