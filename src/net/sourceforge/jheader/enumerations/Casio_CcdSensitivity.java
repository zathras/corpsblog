
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

public class Casio_CcdSensitivity extends EnumeratedTag
{
    /** This is for QV3000 */
    public static final long QV3000_NORMAL = 64;
    /** This is for QV3000 */
    public static final long PLUS_ONE = 125;
    /** This is for QV3000 */
    public static final long PLUS_TWO = 250;
    /** This is for QV3000 */
    public static final long PLUS_THREE = 254;
    /** This is for QV8000/2000 */
    public static final long QV8000_NORMAL = 80;
    /** This is for QV8000/2000 */
    public static final long HIGH = 100;
    
    static private Object[] data = new Object[] {
	QV3000_NORMAL, "Normal",
	PLUS_ONE, "+1.0",
	PLUS_TWO, "+2.0",
	PLUS_THREE, "+3.0",
	QV8000_NORMAL, "Normal",
	HIGH, "High",
    };

    static
    {
	populate(Casio_CcdSensitivity.class, data);
    }

    
    public Casio_CcdSensitivity(Long value) {super(value);}
    public Casio_CcdSensitivity(String value) throws TagFormatException {super(value);}
}
