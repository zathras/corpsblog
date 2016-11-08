
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

import java.util.*;
import net.sourceforge.jheader.App1Header.*;

/**
 * Comparator that orders according to tag location then tag id (not enum).
 */
public class TagLocationIdComparator implements Comparator<App1Header.Tag>
{
    /**
     * Returns -1, 0 or 1 respectively if o1's tag id is less than,
     * equal to or greater than o2's tag id.
     *
     * @param o1 first tag to compare
     * @param o2 second tag to compare
     * @return -1, 0 or 1.
     */
    public int compare(App1Header.Tag o1, App1Header.Tag o2)
    {
	if (o1.location.ordinal() < o2.location.ordinal()) return -1;	
	if (o1.location.ordinal() > o2.location.ordinal()) return 1;
	
	if (o1.id < o2.id) return -1;
	if (o1.id > o2.id) return 1;
	
	if (o1.offset < o2.offset) return -1;
	if (o1.offset > o2.offset) return 1;

	return 0;
    }
}

