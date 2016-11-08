
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

public class Canon_ExposureMode extends EnumeratedTag
{
    public static final long EASY_SHOOTING = 0;
    public static final long PROGRAM = 1;
    public static final long TV_PRIORITY = 2;
    public static final long AV_PRIORITY = 3;
    public static final long MANUAL = 4;
    public static final long A_DEP = 5;
    
    static private Object[] data = new Object[] {
	EASY_SHOOTING, "Easy Shooting",
	PROGRAM, "Program",
	TV_PRIORITY, "Tv priority",
	AV_PRIORITY, "Av priority",
	MANUAL, "Manual",
	A_DEP, "A-DEP"
    };

    static
    {
	populate(Canon_ExposureMode.class, data);
    }

    
    public Canon_ExposureMode(Long value) {super(value);}
    public Canon_ExposureMode(String value) throws TagFormatException {super(value);}
}
