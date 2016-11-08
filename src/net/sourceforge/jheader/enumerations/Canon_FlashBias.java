
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

public class Canon_FlashBias extends EnumeratedTag
{
    public static final long EV_MINUS_2 = 0xffc0;
    public static final long EV_MINUS_1_67 = 0xffcc;
    public static final long EV_MINUS_1_50 = 0xffd0;
    public static final long EV_MINUS_1_33 = 0xffd4;
    public static final long EV_MINUS_1 = 0xffe0;
    public static final long EV_MINUS_0_67 = 0xffec;
    public static final long EV_MINUS_0_50 = 0xfff0;
    public static final long EV_MINUS_0_33 = 0xfff4;
    public static final long EV_0 = 0x0000;
    public static final long EV_PLUS_0_33 = 0x000c;
    public static final long EV_PLUS_0_50 = 0x0010;
    public static final long EV_PLUS_0_67 = 0x0014;
    public static final long EV_PLUS_1 = 0x0020;
    public static final long EV_PLUS_1_33 = 0x002c;
    public static final long EV_PLUS_1_50 = 0x0030;
    public static final long EV_PLUS_1_67 = 0x0034;
    public static final long EV_PLUS_2 = 0x0040;
    
    static private Object[] data = new Object[] {
	EV_MINUS_2,    "-2 EV",
	EV_MINUS_1_67, "-1.67 EV",
	EV_MINUS_1_50, "-1.50 EV",
	EV_MINUS_1_33, "-1.33 EV",
	EV_MINUS_1,    "-1 EV",
	EV_MINUS_0_67, "-0.67 EV",
	EV_MINUS_0_50, "-0.50 EV",
	EV_MINUS_0_33, "-0.33 EV",
	EV_0,          "0 EV",
	EV_PLUS_0_33,  "0.33 EV",
	EV_PLUS_0_50,  "0.50 EV",
	EV_PLUS_0_67,  "0.67 EV",
	EV_PLUS_1,     "1 EV",
	EV_PLUS_1_33,  "1.33 EV",
	EV_PLUS_1_50,  "1.50 EV",
	EV_PLUS_1_67,  "1.67 eV",
	EV_PLUS_2,     "2 EV"
    };

    static
    {
	populate(Canon_FlashBias.class, data);
    }

    
    public Canon_FlashBias(Long value) {super(value);}
    public Canon_FlashBias(String value) throws TagFormatException {super(value);}
}
