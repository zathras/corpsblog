
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

public class Flash extends EnumeratedTag
{
    public static final long FLASH_DID_NOT_FIRE = 0x00;
    public static final long FLASH_FIRED = 0x01;
    public static final long STROBE_RETURN_LIGHT_NOT_DETECTED = 0x05;
    public static final long STROBE_RETURN_LIGHT_DETECTED = 0x07;
    public static final long FLASH_FIRED_COMPULSORY_FLASH_MODE = 0x09;
    public static final long FLASH_FIRED_COMPULSORY_FLASH_MODE_RETURN_LIGHT_NOT_DETECTED = 0x0D;
    public static final long FLASH_FIRED_COMPULSORY_FLASH_MODE_RETURN_LIGHT_DETECTED = 0x0F;
    public static final long FLASH_DID_NOT_FIRE_COMPULSORY_FLASH_MODE = 0x10;
    public static final long FLASH_DID_NOT_FIRE_AUTO_MODE = 0x18;
    public static final long FLASH_FIRED_AUTO_MODE = 0x19;
    public static final long FLASH_FIRED_AUTO_MODE_RETURN_LIGHT_NOT_DETECTED = 0x1D;
    public static final long FLASH_FIRED_AUTO_MODE_RETURN_LIGHT_DETECTED = 0x1F;
    public static final long NO_FLASH_FUNCTION = 0x20;
    public static final long FLASH_FIRED_RED_EYE_REDUCTION_MODE = 0x41;
    public static final long FLASH_FIRED_RED_EYE_REDUCTION_MODE_RETURN_LIGHT_NOT_DETECTED = 0x45;
    public static final long FLASH_FIRED_RED_EYE_REDUCTION_MODE_RETURN_LIGHT_DETECTED = 0x47;
    public static final long FLASH_FIRED_COMPULSORY_FLASH_MODE_RED_EYE_REDUCTION_MODE = 0x49;
    public static final long FLASH_FIRED_COMPULSORY_FLASH_MODE_RED_EYE_REDUCTION_MODE_RETURN_LIGHT_NOT_DETECTED = 0x4D;
    public static final long FLASH_FIRED_COMPULSORY_FLASH_MODE_RED_EYE_REDUCTION_MODE_RETURN_LIGHT_DETECTED = 0x4F;
    public static final long FLASH_FIRED_AUTO_MODE_RED_EYE_REDUCTION_MODE = 0x59;
    public static final long FLASH_FIRED_AUTO_MODE_RETURN_LIGHT_NOT_DETECTED_RED_EYE_REDUCTION_MODE = 0x5D;
    public static final long FLASH_FIRED_AUTO_MODE_RETURN_LIGHT_DETECTED_RED_EYE_REDUCTION_MODE = 0x5F;
    
    static private Object[] data = new Object[] {
FLASH_DID_NOT_FIRE,                                                                 "Did not fire",							     
FLASH_FIRED,									    "Fired",							     
STROBE_RETURN_LIGHT_NOT_DETECTED,						    "Strobe rtn light not detected",				     
STROBE_RETURN_LIGHT_DETECTED,							    "Strobe rtn light detected",					     
FLASH_FIRED_COMPULSORY_FLASH_MODE,						    "Fired, compulsory mode",					     
FLASH_FIRED_COMPULSORY_FLASH_MODE_RETURN_LIGHT_NOT_DETECTED,			    "Fired, compulsory mode, rtn light not detected",		     
FLASH_FIRED_COMPULSORY_FLASH_MODE_RETURN_LIGHT_DETECTED,			    "Fired, compulsory mode, rtn light detected",			     
FLASH_DID_NOT_FIRE_COMPULSORY_FLASH_MODE,					    "Did not fire, compulsory mode",				     
FLASH_DID_NOT_FIRE_AUTO_MODE,							    "Did not fire, auto mode",					     
FLASH_FIRED_AUTO_MODE,								    "Fired, auto mode",						     
FLASH_FIRED_AUTO_MODE_RETURN_LIGHT_NOT_DETECTED,				    "Fired, auto mode, rtn light not detected",			     
FLASH_FIRED_AUTO_MODE_RETURN_LIGHT_DETECTED,					    "Fired, auto mode, rtn light detected",				     
NO_FLASH_FUNCTION,								    "No flash function",						     
FLASH_FIRED_RED_EYE_REDUCTION_MODE,						    "Fired, red-eye reduction",					     
FLASH_FIRED_RED_EYE_REDUCTION_MODE_RETURN_LIGHT_NOT_DETECTED,			    "Fired, red-eye reduction, rtn light not detected",		     
FLASH_FIRED_RED_EYE_REDUCTION_MODE_RETURN_LIGHT_DETECTED,			    "Fired, red-eye reduction, rtn light detected",			     
FLASH_FIRED_COMPULSORY_FLASH_MODE_RED_EYE_REDUCTION_MODE,			    "Fired, compulsory mode, red-eye reduction",			     
FLASH_FIRED_COMPULSORY_FLASH_MODE_RED_EYE_REDUCTION_MODE_RETURN_LIGHT_NOT_DETECTED, "Fired, compulsory mode, red-eye reduction, rtn light not detected", 
FLASH_FIRED_COMPULSORY_FLASH_MODE_RED_EYE_REDUCTION_MODE_RETURN_LIGHT_DETECTED,	    "Fired, compulsory mode, red-eye reduction, rtn light detected",     
FLASH_FIRED_AUTO_MODE_RED_EYE_REDUCTION_MODE,					    "Fired, auto mode, red-eye reduction",				     
FLASH_FIRED_AUTO_MODE_RETURN_LIGHT_NOT_DETECTED_RED_EYE_REDUCTION_MODE,		    "Fired, auto mode, rtn light not detected, red-eye reduction",	     
FLASH_FIRED_AUTO_MODE_RETURN_LIGHT_DETECTED_RED_EYE_REDUCTION_MODE,		    "Fired, auto mode, rtn light detected, red-eye reduction"            
    };

    static
    {
	populate(Flash.class, data);
    }

    
    public Flash(Long value) {super(value);}
    public Flash(String value) throws TagFormatException {super(value);}
}
