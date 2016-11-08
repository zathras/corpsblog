
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

public class Fujifilm_PictureMode extends EnumeratedTag
{
    public static final long AUTO = 0;
    public static final long PORTRAIT = 1;
    public static final long LANDSCAPE = 2;
    public static final long SPORTS = 4;
    public static final long NIGHT = 5;
    public static final long PROGRAM_AE = 6;
    public static final long APERTURE_PRIOR_AE = 256;
    public static final long SHUTTER_PRIOR_AE = 512;
    public static final long MANUAL = 768;
    
    static private Object[] data = new Object[] {
	AUTO, "Auto",
	PORTRAIT, "Portrait Scene",
	LANDSCAPE, "Landscape Scene",
	SPORTS, "Sports Scene",
	NIGHT, "Night Scene",
	PROGRAM_AE, "Program AE",
	APERTURE_PRIOR_AE, "Aperture Priority AEW",
	SHUTTER_PRIOR_AE, "Shutter Priority AE",
	MANUAL, "Manual Exposure"
    };

    static
    {
	populate(Fujifilm_PictureMode.class, data);
    }

    
    public Fujifilm_PictureMode(Long value) {super(value);}
    public Fujifilm_PictureMode(String value) throws TagFormatException {super(value);}
}
