
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

public class Casio2_ImageSize extends EnumeratedTag
{
    public static final long S_640X480 = 0;
    public static final long S_1600X1200 = 4;
    public static final long S_2048X1536 = 5;
    public static final long S_2288X1712 = 20;
    public static final long S_2592X1944 = 21;
    public static final long S_2304X1728 = 22; 
    public static final long S_3008X2008 = 36;
   
    static private Object[] data = new Object[] {
	S_640X480,   "640x480",
	S_1600X1200, "1600x1200",
	S_2048X1536, "2048x1536",
	S_2288X1712, "2288x1712",
	S_2592X1944, "2592x1944",
	S_2304X1728, "2304x1728",
	S_3008X2008, "3008x2008"
    };

    static
    {
	populate(Casio2_ImageSize.class, data);
    }

    
    public Casio2_ImageSize(Long value) {super(value);}
    public Casio2_ImageSize(String value) throws TagFormatException {super(value);}
}
