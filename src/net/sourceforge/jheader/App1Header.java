
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
import java.io.IOException;
import java.lang.reflect.*;
import net.sourceforge.jheader.JpegHeaders.ByteOrder;
import net.sourceforge.jheader.enumerations.*;

class TagOffsetComparator implements Comparator<App1Header.Tag>
{
    public int compare(App1Header.Tag o1, App1Header.Tag o2)
    {
	if (o1.offset < o2.offset) return -1;
	if (o1.offset > o2.offset) return 1;
	return 0;
    }
}

/**
 * <p>Information parsed from an APP1 block in a JPEG file.</p>
 *
 * <p>An EXIF file always begins with an APP1 block.  A JFIF file does not
 * contain one but rather an APP0 block.</p>
 */
public class App1Header
{
    /**
     * Data format for a tag.
     */
    public enum TagFormat
    {
	BYTE      ( 1, 1),
	STRING    ( 2, 1),
	USHORT    ( 3, 2),
	ULONG     ( 4, 4),
	URATIONAL ( 5, 8),
	SBYTE     ( 6, 1),
	UNDEFINED ( 7, 1),
	SSHORT    ( 8, 2),
	SLONG     ( 9, 4),
	SRATIONAL (10, 8)/*,
	SINGLE    (11, 4),
	DOUBLE    (12, 8)*/;

	/**
	 * Identifier in the file.
	 */
	public final int id;

	/**
	 * Size of one component.
	 */
	public final int size;
	
	TagFormat(int id, int size)
	{
	    this.id = id;
	    this.size = size;
	}
    }

    /**
     * Blocks of tags in an EXIF file.
     *
     * <p>IFD0 is the first image file directory and contains the main
     * informative tags.  SUBEXIF is a block within the IFD0 and
     * contains more EXIF tags.  INTEROP is a block within SUBEXIF and
     * contains EXIF 2.1 tags.  IFD1 is the thumbnail image file
     * directory and contains tags describing the thumbnail.  The
     * makernotes are all within the SUBEXIF block and are camera make
     * specific.</p>
     */
    public enum ExifLocation
    {
	UNKNOWN,
	IFD0,
	SUBEXIF,
	INTEROP,
	IFD1,
	MAKERNOTE_OLYMPUS,
	MAKERNOTE_NIKON,
	MAKERNOTE_NIKOND1,
	MAKERNOTE_CASIO,
	MAKERNOTE_CASIO2,
	MAKERNOTE_FUJIFILM,
	MAKERNOTE_CANON
    }

    /**
     * Supported camera types.
     *
     * <p>Note that the makernote format may not be recognized for all
     * these types.  In such cases, the camera make can be determined but
     * the makernote tags are left unparsed.</p>
     */
    public enum CameraType
    {
	UNKNOWN ("Unknown", ExifLocation.UNKNOWN),
	AGFA ("Agfa", ExifLocation.SUBEXIF),
	EPSON ("Epson", ExifLocation.SUBEXIF),
	CANON ("Canon", ExifLocation.MAKERNOTE_CANON),
	CASIO1 ("Casio Type 1", ExifLocation.MAKERNOTE_CASIO),
	CASIO2 ("Casio Type 2", ExifLocation.SUBEXIF),
	FUJIFILM ("Fujifilm", ExifLocation.MAKERNOTE_FUJIFILM),
	KYOCERA ("Fujifilm", ExifLocation.SUBEXIF),
	MINOLTA1 ("Konica/Minolta Type 1", ExifLocation.SUBEXIF),
	MINOLTA2 ("Konica/Minolta Type 2", ExifLocation.SUBEXIF),
	MINOLTA3 ("Konica/Minolta Type 3", ExifLocation.SUBEXIF),
	MINOLTA4 ("Konica/Minolta Type 4", ExifLocation.SUBEXIF),
	MINOLTA5 ("Konica/Minolta Type 5", ExifLocation.MAKERNOTE_OLYMPUS),
	NIKON1 ("Nikon Type 1", ExifLocation.MAKERNOTE_NIKON),
	NIKON2 ("Nikon Type 2 or 3", ExifLocation.MAKERNOTE_NIKOND1),
	OLYMPUS ("Olympus", ExifLocation.MAKERNOTE_OLYMPUS),
	PANASONIC ("Panasonic", ExifLocation.SUBEXIF),
	PENTAX1 ("Pentax/Asahi Type 1", ExifLocation.SUBEXIF),
	PENTAX2 ("Pentax/Asahi Type 2", ExifLocation.SUBEXIF), // casio type 2
	RICOH ("Ricoh", ExifLocation.SUBEXIF),
	SONY ("Sony", ExifLocation.SUBEXIF);
	
	public final String name;
	public final ExifLocation makerNoteLocation;

	CameraType(String n, ExifLocation l)
	{
	    name = n;
	    makerNoteLocation = l;
	}
    }
    
    /**
     * Register of all recognized EXIF and MAKERNOTE tags.
     */
    public enum Tag
    {
	UNKNOWN      (0x0000,"Image Description",ExifLocation.UNKNOWN,TagFormat.UNDEFINED,-1,0,"",UndefinedTag.class,false),

	// IFD0
	IMAGEDESCRIPTION      (0x010e,"Image Description",ExifLocation.IFD0,TagFormat.STRING,-1,0,"",String.class,true),
	MAKE		      (0x010f,"Make",ExifLocation.IFD0,TagFormat.STRING,-1,0,"",String.class,true),
	MODEL		      (0x0110,"Model",ExifLocation.IFD0,TagFormat.STRING,-1,0,"",String.class,true),
	ORIENTATION	      (0x0112,"Orientation",ExifLocation.IFD0,TagFormat.USHORT,1,0,"",Orientation.class,true),
	XRESOLUTION	      (0x011a,"X Resolution",ExifLocation.IFD0,TagFormat.URATIONAL,1,0,"",Rational.class,true),
	YRESOLUTION	      (0x011b,"Y Resolution",ExifLocation.IFD0,TagFormat.URATIONAL,1,0,"",Rational.class,true),
	RESOLUTIONUNIT	      (0x0128,"Resolution Units",ExifLocation.IFD0,TagFormat.USHORT,1,0,"",ResolutionUnit.class,true),
	SOFTWARE	      (0x0131,"Software",ExifLocation.IFD0,TagFormat.STRING,-1,0,"",String.class,true),
	DATETIME	      (0x0132,"Date/Time Modified",ExifLocation.IFD0,TagFormat.STRING,20,0,"",DateTimeTag.class,true),
	WHITEPOINT	      (0x013e,"White Point",ExifLocation.IFD0,TagFormat.URATIONAL,2,0,"",Rational.class,true),
	PRIMARYCHROMATICITIES (0x013f,"Primary Chromacities",ExifLocation.IFD0,TagFormat.URATIONAL,6,0,"",Rational.class,true),
	YCBCRCOEFFICIENTS     (0x0211,"YCbCr Coefficients",ExifLocation.IFD0,TagFormat.URATIONAL,3,0,"",Rational.class,true),
	YCBCRPOSITIONING      (0x0213,"YCbCr Positions",ExifLocation.IFD0,TagFormat.USHORT,1,0,"",YCbCrPositioning.class,true),
	REFERENCEBLACKWHITE   (0x0214,"Reference Black White",ExifLocation.IFD0,TagFormat.URATIONAL,6,0,"",Rational.class,true),
	COPYRIGHT	      (0x8298,"Copyright",ExifLocation.IFD0,TagFormat.STRING,-1,0,"",String.class,true),
	EXIFOFFSET	      (0x8769,"EXIF Offset",ExifLocation.IFD0,TagFormat.ULONG,1,0,"",Long.class,false),

	// MISC
	NEWSUBFILETYPE           (0x00fe,"New Subfile Type",ExifLocation.IFD0,TagFormat.ULONG,1,0,"",Long.class,true),
	SUBFILETYPE		 (0x00ff,"Subfile Type",ExifLocation.IFD0,TagFormat.USHORT,1,0,"",Long.class,true),
	TRANSFERFUNCTION	 (0x012d,"Transfer Function",ExifLocation.IFD0,TagFormat.USHORT,3,0,"",Long.class,true),
	ARTIST			 (0x013b,"Artist",ExifLocation.IFD0,TagFormat.STRING,-1,0,"",String.class,true),
	PREDICTOR		 (0x013d,"Predictor",ExifLocation.IFD0,TagFormat.USHORT,1,0,"",Long.class,true),
	TILEWIDTH		 (0x0142,"Tile Width",ExifLocation.IFD0,TagFormat.USHORT,1,0,"",Long.class,true),
	TILELENGTH		 (0x0143,"Tile Length",ExifLocation.IFD0,TagFormat.USHORT,1,0,"",Long.class,true),
	TILEOFFSETS		 (0x0144,"Tile Offsets",ExifLocation.IFD0,TagFormat.ULONG,-1,0,"",Long.class,true),
	TILEBYTECOUNTS		 (0x0145,"Tile Byte Counts",ExifLocation.IFD0,TagFormat.USHORT,-1,0,"",Long.class,true),
	SUBIFDS			 (0x014a,"Sub IFDs",ExifLocation.IFD0,TagFormat.ULONG,-1,0,"",Long.class,true),
	JPEGTABLES		 (0x015b,"JPEG Tables",ExifLocation.IFD0,TagFormat.UNDEFINED,-1,0,"",UndefinedTag.class,true),
	CFAREPEATPATTERNDIM	 (0x828d,"CFA Repeat Pattern Dim",ExifLocation.IFD0,TagFormat.USHORT,2,0,"",Long.class,true),
	CFAPATTERN		 (0x828e,"CFA Pattern",ExifLocation.IFD0,TagFormat.BYTE,-1,0,"",Long.class,true),
	BATTERYLEVEL		 (0x828f,"Battery Level",ExifLocation.IFD0,TagFormat.URATIONAL,1,0,"",Rational.class,true),
	IPTCNAA		         (0x83bb,"IPTC/NAA",ExifLocation.IFD0,TagFormat.ULONG,-1,0,"",Long.class,true),
	INTERCOLORPROFILE	 (0x8773,"Inter Color Profile",ExifLocation.IFD0,TagFormat.UNDEFINED,-1,0,"",UndefinedTag.class,true),
	SPECTRALSENSITIVITY	 (0x8824,"Spectral Sensitivity",ExifLocation.IFD0,TagFormat.STRING,-1,0,"",String.class,true),
	GPSINFO			 (0x8825,"GPS Info",ExifLocation.IFD0,TagFormat.ULONG,1,0,"",Long.class,true),
	OECF			 (0x8828,"OECF",ExifLocation.IFD0,TagFormat.UNDEFINED,-1,0,"",UndefinedTag.class,true),
	INTERLACE		 (0x8829,"Interlace",ExifLocation.IFD0,TagFormat.USHORT,1,0,"",Long.class,true),
	TIMEZONEOFFSET		 (0x882a,"Time Zone Offset",ExifLocation.IFD0,TagFormat.SSHORT,1,0,"",Long.class,true),
	SELFTIMERMODE		 (0x882b,"Self Timer Mode",ExifLocation.IFD0,TagFormat.USHORT,1,0,"",Long.class,true),
	FLASHENERGY	         (0x920b,"Flash Energy",ExifLocation.IFD0,TagFormat.URATIONAL,1,0,"",Rational.class,true),
	SPATIALFREQUENCYRESPONSE (0x920c,"Spatial Frequency Response",ExifLocation.IFD0,TagFormat.UNDEFINED,-1,0,"",UndefinedTag.class,true),
	NOISE			 (0x920d,"Noise",ExifLocation.IFD0,TagFormat.UNDEFINED,-1,0,"",UndefinedTag.class,true),
	IMAGENUMBER 		 (0x9211,"Image Number",ExifLocation.IFD0,TagFormat.ULONG,1,0,"",Long.class,true),
	SECURITYCLASSIFICATION	 (0x9212,"Security Classification",ExifLocation.IFD0,TagFormat.STRING,1,0,"",String.class,true),
	IMAGEHISTORY		 (0x9213,"Image History",ExifLocation.IFD0,TagFormat.STRING,-1,0,"",String.class,true),
	SUBJECTLOCATION		 (0x9214,"Subject Location",ExifLocation.IFD0,TagFormat.USHORT,4,0,"",Long.class,true),
	EXPOSUREINDEX2		 (0x9215,"Exposure Index",ExifLocation.IFD0,TagFormat.URATIONAL,1,0,"",Rational.class,true),
	TIFFEPSTANDARDID	 (0x9216,"TIFF/EPStandardID",ExifLocation.IFD0,TagFormat.BYTE,4,0,"",Long.class,true),
	FLASHENERGY2		 (0xa20b,"Flash Energy",ExifLocation.IFD0,TagFormat.URATIONAL,1,0,"",Rational.class,true),
	SPATIALFREQUENCYRESPONSE2 (0xa20c,"Spatial FrequencyResponse",ExifLocation.IFD0,TagFormat.USHORT,1,0,"",Long.class,true),
	SUBJECTLOCATION2	 (0xa214,"Subject Location",ExifLocation.IFD0,TagFormat.USHORT,1,0,"",Long.class,true),

	// SUBEXIF
	EXPOSURETIME               (0x829a,"Exposure Time",ExifLocation.SUBEXIF,TagFormat.URATIONAL,1,0,"",Rational.class,true),
	FNUMBER			   (0x829d,"F Number",ExifLocation.SUBEXIF,TagFormat.URATIONAL,1,0,"",Rational.class,true),
	EXPOSUREPROGRAM		   (0x8822,"Exposure Program",ExifLocation.SUBEXIF,TagFormat.USHORT,1,0,"",ExposureProgram.class,true),
	ISOSPEEDRATINGS		   (0x8827,"ISO Speed Ratings",ExifLocation.SUBEXIF,TagFormat.USHORT,1,0,"",Long.class,true), // doc says 2 components!
	EXIFVERSION		   (0x9000,"EXIF Version",ExifLocation.SUBEXIF,TagFormat.UNDEFINED,4,0,"",UndefinedTag.class,true),
	DATETIMEORIGINAL	   (0x9003,"Date/Time Taken",ExifLocation.SUBEXIF,TagFormat.STRING,20,0,"",DateTimeTag.class,true),
	DATETIMEDIGITIZED	   (0x9004,"Date/Time Digitized",ExifLocation.SUBEXIF,TagFormat.STRING,20,0,"",DateTimeTag.class,true),
	COMPONENTSCONFIGURATION	   (0x9101,"Components Configuration",ExifLocation.SUBEXIF,TagFormat.UNDEFINED,-1,0,"",UndefinedTag.class,true),
	COMPRESSEDBITSPERPIXEL	   (0x9102,"Compressed Bits Per Pixel",ExifLocation.SUBEXIF,TagFormat.URATIONAL,1,0,"",Rational.class,true),
	SHUTTERSPEEDVALUE	   (0x9201,"Shutter Speed Value",ExifLocation.SUBEXIF,TagFormat.SRATIONAL,1,0,"seconds",Rational.class,true),
	APERTUREVALUE		   (0x9202,"Aperture Value",ExifLocation.SUBEXIF,TagFormat.URATIONAL,1,0,"apex",Rational.class,true),
	BRIGHTNESSVALUE		   (0x9203,"Brightness Value",ExifLocation.SUBEXIF,TagFormat.SRATIONAL,1,0,"apex",Rational.class,true),
	EXPOSUREBIASVALUE	   (0x9204,"Exposure Value",ExifLocation.SUBEXIF,TagFormat.SRATIONAL,1,0,"apex",Rational.class,true),
	MAXAPERTUREVALUE	   (0x9205,"Max. Aperture Value",ExifLocation.SUBEXIF,TagFormat.URATIONAL,1,0,"",Rational.class,true),
	SUBJECTDISTANCE		   (0x9206,"Subject Distance",ExifLocation.SUBEXIF,TagFormat.URATIONAL,1,0,"metres",Rational.class,true),
	METERINGMODE		   (0x9207,"MeteringMode",ExifLocation.SUBEXIF,TagFormat.USHORT,1,0,"",MeteringMode.class,true),
	LIGHTSOURCE		   (0x9208,"Light Source",ExifLocation.SUBEXIF,TagFormat.USHORT,1,0,"",LightSource.class,true),
	FLASH			   (0x9209,"Flash",ExifLocation.SUBEXIF,TagFormat.USHORT,1,0,"",Flash.class,true),
	FOCALLENGTH		   (0x920a,"Focal Length",ExifLocation.SUBEXIF,TagFormat.URATIONAL,1,0,"",Rational.class,true),
	MAKERNOTE		   (0x927c,"Maker Note",ExifLocation.SUBEXIF,TagFormat.UNDEFINED,-1,0,"",UndefinedTag.class,false),
	USERCOMMENT		   (0x9286,"User Comment",ExifLocation.SUBEXIF,TagFormat.UNDEFINED,-1,0,"",UndefinedTag.class,true),
	//USERCOMMENT		   (0x9286,"User Comment",ExifLocation.SUBEXIF,TagFormat.UNDEFINED,-1,0,"",String.class,true), // was this
	SUBSECTIME		   (0x9290,"Subsecond Time",ExifLocation.SUBEXIF,TagFormat.STRING,-1,0,"",String.class,true),
	SUBSECTIMEORIGINAL	   (0x9291,"Subsecond Time Taken",ExifLocation.SUBEXIF,TagFormat.STRING,-1,0,"",String.class,true),
	SUBSECTIMEDIGITIZED	   (0x9292,"Subsecond Time Digitized",ExifLocation.SUBEXIF,TagFormat.STRING,-1,0,"",String.class,true),
	FLASHPIXVERSION		   (0xa000,"Flash Pix Version",ExifLocation.SUBEXIF,TagFormat.UNDEFINED,4,0,"",UndefinedTag.class,true),
	COLORSPACE		   (0xa001,"Color Space",ExifLocation.SUBEXIF,TagFormat.USHORT,1,0,"",ColorSpace.class,true),
	EXIFIMAGEWIDTH		   (0xa002,"EXIF Image Width",ExifLocation.SUBEXIF,TagFormat.ULONG,1,0,"",Long.class,true),
	EXIFIMAGEHEIGHT		   (0xa003,"EXIF Image Height",ExifLocation.SUBEXIF,TagFormat.ULONG,1,0,"",Long.class,true),
	RELATEDSOUNDFILE	   (0xa004,"Related Sound File",ExifLocation.SUBEXIF,TagFormat.STRING,-1,0,"",String.class,true),
	EXIFINTEROPERABILITYOFFSET (0xa005,"EXIF Interoporability Offset",ExifLocation.SUBEXIF,TagFormat.ULONG,1,0,"",Long.class,false),
	FOCALPLANEXRESOLUTION	   (0xa20e,"Focal Plane X Resolution",ExifLocation.SUBEXIF,TagFormat.URATIONAL,1,0,"",Rational.class,true),
	FOCALPLANEYRESOLUTION	   (0xa20f,"Focal Plane Y Resolution",ExifLocation.SUBEXIF,TagFormat.URATIONAL,1,0,"",Rational.class,true),
	FOCALPLANERESOLUTIONUNIT   (0xa210,"Focal Plane Resolution Units",ExifLocation.SUBEXIF,TagFormat.USHORT,1,0,"",Long.class,true),
	EXPOSUREINDEX		   (0xa215,"Exposure Index",ExifLocation.SUBEXIF,TagFormat.URATIONAL,1,0,"",Rational.class,true),
	SENSINGMETHOD		   (0xa217,"Sensing Method",ExifLocation.SUBEXIF,TagFormat.USHORT,1,0,"",SensingMethod.class,true),
	//FILESOURCE		   (0xa300,"File Source",ExifLocation.SUBEXIF,TagFormat.UNDEFINED,1,0,"",FileSource.class,true),
	FILESOURCE		   (0xa300,"File Source",ExifLocation.SUBEXIF,TagFormat.UNDEFINED,1,0,"",UndefinedTag.class,true),
	SCENETYPE		   (0xa301,"Scene Type",ExifLocation.SUBEXIF,TagFormat.UNDEFINED,1,0,"",SceneType.class,true),
	CFAPATTERN_HRPT            (0xa302,"CFA Pattern - H. Repeat",ExifLocation.SUBEXIF,TagFormat.USHORT,2,0,"",Long.class,true),
	CFAPATTERN_VRPT            (0xa302,"CFA Pattern - V. Repeat",ExifLocation.SUBEXIF,TagFormat.USHORT,2,2,"",Long.class,true),
	CFAPATTERN_VALUETL         (0xa302,"CFA Pattern - TL Value",ExifLocation.SUBEXIF,TagFormat.BYTE,1,4,"",CfaPattern.class,true),
	CFAPATTERN_VALUETR         (0xa302,"CFA Pattern - TR Value",ExifLocation.SUBEXIF,TagFormat.BYTE,1,5,"",CfaPattern.class,true),
	CFAPATTERN_VALUEBL         (0xa302,"CFA Pattern - BL Value",ExifLocation.SUBEXIF,TagFormat.BYTE,1,6,"",CfaPattern.class,true),
	CFAPATTERN_VALUEBR         (0xa302,"CFA Pattern - BR Value",ExifLocation.SUBEXIF,TagFormat.BYTE,1,7,"",CfaPattern.class,true),
	CUSTOMRENDERED             (0xa401,"Custom Rendered",ExifLocation.SUBEXIF,TagFormat.USHORT,1,0,"",CustomRendered.class,true),
	EXPOSUREMODE               (0xa402,"Exposure Mode",ExifLocation.SUBEXIF,TagFormat.USHORT,1,0,"",ExposureMode.class,true),
	WHITEBALANCE               (0xa403,"White Balance",ExifLocation.SUBEXIF,TagFormat.USHORT,1,0,"",WhiteBalance.class,true),
	DIGITALZOOM                (0xa404,"Digital Zoom",ExifLocation.SUBEXIF,TagFormat.URATIONAL,1,0,"",Rational.class,true),
	FOCALLENGTH35MM            (0xa405,"Focal Length in 35mm Film",ExifLocation.SUBEXIF,TagFormat.USHORT,1,0,"",Long.class,true),
	SCENECAPTURETYPE           (0xa406,"Scene Capture Type",ExifLocation.SUBEXIF,TagFormat.USHORT,1,0,"",SceneCaptureType.class,true),
	GAINCONTROL                (0xa407,"Scene Capture Type",ExifLocation.SUBEXIF,TagFormat.USHORT,1,0,"",GainControl.class,true),
	CONTRAST                   (0xa408,"Contrast",ExifLocation.SUBEXIF,TagFormat.USHORT,1,0,"",Contrast.class,true),
	SATURATION                 (0xa409,"Saturation",ExifLocation.SUBEXIF,TagFormat.USHORT,1,0,"",Saturation.class,true),
	SHARPNESS                  (0xa40A,"Sharpness",ExifLocation.SUBEXIF,TagFormat.USHORT,1,0,"",Sharpness.class,true),
	DEVICESETTINGDESCRIPTION   (0xa40B,"Device Setting Description",ExifLocation.SUBEXIF,TagFormat.UNDEFINED,-1,0,"",UndefinedTag.class,true),
	SUBJECTDISTANCERANGE       (0xa40C,"Subject Distance Range",ExifLocation.SUBEXIF,TagFormat.USHORT,1,0,"",Long.class,true),
	
	// INTEROPERABILITY
	INTEROPERABILITYINDEX   (0x0001,"Interoperability Index",ExifLocation.INTEROP,TagFormat.STRING,4,0,"",String.class,false),
	INTEROPERABILITYVERSION (0x0002,"Interoperability Version",ExifLocation.INTEROP,TagFormat.UNDEFINED,4,0,"",UndefinedTag.class,true),
	RELATEDIMAGEFILEFORMAT  (0x1000,"Related Image File Format",ExifLocation.INTEROP,TagFormat.STRING,-1,0,"",String.class,true),
	RELATEDIMAGEWIDTH       (0x1001,"Related Image Width",ExifLocation.INTEROP,TagFormat.ULONG,1,0,"",Long.class,true),
	RELATEDIMAGELENGTH      (0x1001,"Related Image Length",ExifLocation.INTEROP,TagFormat.ULONG,1,0,"",Long.class,true),

	// IFD1
	IFD1_IMAGEWIDTH                (0x0100,"Thumb Image Width",ExifLocation.IFD1,TagFormat.ULONG,1,0,"",Long.class,true),
	IFD1_IMAGELENGTH               (0x0101,"Thumb Image Length",ExifLocation.IFD1,TagFormat.ULONG,1,0,"",Long.class,true),
	IFD1_BITSPERSAMPLE             (0x0102,"Thumb Bits Per Sample",ExifLocation.IFD1,TagFormat.USHORT,3,0,"",Long.class,true),
	IFD1_COMPRESSION               (0x0103,"Thumb Compression",ExifLocation.IFD1,TagFormat.USHORT,1,0,"",Compression.class,true),
	IFD1_PHOTOMETRICINTERPRETATION (0x0106,"Thumb Photometric Interpretation",ExifLocation.IFD1,TagFormat.USHORT,1,0,"",Long.class,true),
	IFD1_MAKE		       (0x010f,"Make (Thumb)",ExifLocation.IFD1,TagFormat.STRING,-1,0,"",String.class,true),
	IFD1_MODEL		       (0x0110,"Model (Thumb)",ExifLocation.IFD1,TagFormat.STRING,-1,0,"",String.class,true),
	IFD1_STRIPOFFSETS              (0x0111,"Thumb Strip Offsets",ExifLocation.IFD1,TagFormat.ULONG,-1,0,"",Long.class,true),
	IFD1_ORIENTATION	       (0x0112,"Thumb Orientation",ExifLocation.IFD1,TagFormat.USHORT,1,0,"",Orientation.class,true),
	IFD1_SAMPLESPERPIXEL	       (0x0115,"Thumb Samples PerPixel",ExifLocation.IFD1,TagFormat.USHORT,1,0,"",Long.class,true),
	IFD1_ROWSPERSTRIP	       (0x0116,"Thumb Rows Per Strip",ExifLocation.IFD1,TagFormat.ULONG,1,0,"",Long.class,true),
	IFD1_STRIPBYTECONUNTS	       (0x0117,"Thumb Strip Byte Conunts",ExifLocation.IFD1,TagFormat.ULONG,-1,0,"",Long.class,true),
	IFD1_XRESOLUTION	       (0x011a,"Thumb X Resolution",ExifLocation.IFD1,TagFormat.URATIONAL,1,0,"",Rational.class,true),
	IFD1_YRESOLUTION	       (0x011b,"Thumb Y Resolution",ExifLocation.IFD1,TagFormat.URATIONAL,1,0,"",Rational.class,true),
	IFD1_PLANARCONFIGURATION       (0x011c,"Thumb Planar Configuration",ExifLocation.IFD1,TagFormat.USHORT,1,0,"",Long.class,true),
	IFD1_RESOLUTIONUNIT	       (0x0128,"Thumb Resolution Unit",ExifLocation.IFD1,TagFormat.USHORT,1,0,"",ResolutionUnit.class,true),
	IFD1_DATETIME	               (0x0132,"Date/Time Thumb Modified",ExifLocation.IFD1,TagFormat.STRING,20,0,"",DateTimeTag.class,true),
	IFD1_JPEGIFOFFSET	       (0x0201,"Thumb Jpeg IF Offset",ExifLocation.IFD1,TagFormat.ULONG,1,0,"",Long.class,false),
	IFD1_JPEGIFBYTECOUNT	       (0x0202,"Thumb Jpeg IF Byte Count",ExifLocation.IFD1,TagFormat.ULONG,1,0,"",Long.class,false),
	IFD1_YCBCRCOEFFICIENTS	       (0x0211,"Thumb YCbCr Coefficients",ExifLocation.IFD1,TagFormat.URATIONAL,3,0,"",Rational.class,true),
	IFD1_YCBCRSUBSAMPLING	       (0x0212,"Thumb YCbCr SubSampling",ExifLocation.IFD1,TagFormat.USHORT,2,0,"",Long.class,true),
	IFD1_YCBCRPOSITIONING	       (0x0213,"Thumb YCbCr Positioning",ExifLocation.IFD1,TagFormat.USHORT,1,0,"",YCbCrPositioning.class,true),
	IFD1_REFERENCEBLACKWHITE       (0x0214,"Thumb Reference Black White",ExifLocation.IFD1,TagFormat.URATIONAL,6,0,"",Rational.class,true),

	// OLYMPUS
	OLYMPUS_SPECIALMODE     (0x0200,"Special Mode",ExifLocation.MAKERNOTE_OLYMPUS,TagFormat.ULONG,1,0,"",Olympus_SpecialMode.class,true),
	OLYMPUS_JPEGQUAL	(0x0201,"Jpeg Quality",ExifLocation.MAKERNOTE_OLYMPUS,TagFormat.USHORT,1,0,"",Olympus_JpegQual.class,true),
	OLYMPUS_MACRO		(0x0202,"Macro",ExifLocation.MAKERNOTE_OLYMPUS,TagFormat.USHORT,1,0,"",Olympus_Macro.class,true),
	OLYMPUS_UNKNOWN1        (0x0203,"Unknown",ExifLocation.MAKERNOTE_OLYMPUS,TagFormat.USHORT,1,0,"",Long.class,false),
	OLYMPUS_DIGIZOOM        (0x0204,"Digi Zoom",ExifLocation.MAKERNOTE_OLYMPUS,TagFormat.URATIONAL,1,0,"",Rational.class,true),
	OLYMPUS_UNKNOWN2        (0x0205,"Unknown",ExifLocation.MAKERNOTE_OLYMPUS,TagFormat.URATIONAL,1,0,"",Rational.class,false),
	OLYMPUS_UNKNOWN3        (0x0206,"Unknown",ExifLocation.MAKERNOTE_OLYMPUS,TagFormat.USHORT,6,0,"",Long.class,false),
	OLYMPUS_SOFTWARERELEASE (0x0207,"Software Release",ExifLocation.MAKERNOTE_OLYMPUS,TagFormat.STRING,5,0,"",String.class,true),
	OLYMPUS_PICTINFO	(0x0208,"Picture Info",ExifLocation.MAKERNOTE_OLYMPUS,TagFormat.STRING,52,0,"",String.class,true),
	OLYMPUS_CAMERAID	(0x0209,"Camera ID",ExifLocation.MAKERNOTE_OLYMPUS,TagFormat.UNDEFINED,32,0,"",UndefinedTag.class,true),
	OLYMPUS_DATADUMP	(0x0f00,"Data Dump",ExifLocation.MAKERNOTE_OLYMPUS,TagFormat.ULONG,30,0,"",Long.class,true),

	// NIKON
	NIKON_UNKNOWN1           (0x0002,"Unknown",ExifLocation.MAKERNOTE_NIKON,TagFormat.STRING,6,0,"",String.class,false),
	NIKON_QUALITY		 (0x0003,"Quality",ExifLocation.MAKERNOTE_NIKON,TagFormat.USHORT,1,0,"",Nikon_Quality.class,true),
	NIKON_COLORMODE          (0x0004,"Color Mode",ExifLocation.MAKERNOTE_NIKON,TagFormat.USHORT,1,0,"",Nikon_ColorMode.class,true),
	NIKON_IMAGEADJUSTMENT    (0x0005,"Image Adjustment",ExifLocation.MAKERNOTE_NIKON,TagFormat.USHORT,1,0,"",Nikon_ImageAdjustment.class,true),
	NIKON_CCDSENSITIVITY	 (0x0006,"CCD Sensitivity",ExifLocation.MAKERNOTE_NIKON,TagFormat.USHORT,1,0,"",Nikon_CcdSensitivity.class,true),
	NIKON_WHITEBALANCE	 (0x0007,"White Balance",ExifLocation.MAKERNOTE_NIKON,TagFormat.USHORT,1,0,"",Nikon_WhiteBalance.class,true),
	NIKON_FOCUS		 (0x0008,"Focus",ExifLocation.MAKERNOTE_NIKON,TagFormat.URATIONAL,1,0,"",Rational.class,true),
	NIKON_UNKNOWN2           (0x0009,"Unknown",ExifLocation.MAKERNOTE_NIKON,TagFormat.STRING,20,0,"",String.class,false),
	NIKON_DIGITALZOOM	 (0x000a,"Digital Zoom",ExifLocation.MAKERNOTE_NIKON,TagFormat.URATIONAL,1,0,"",Rational.class,true),
	NIKON_CONVERTER	         (0x000b,"Converter",ExifLocation.MAKERNOTE_NIKON,TagFormat.USHORT,1,0,"",Long.class,true),
	NIKON_UNKNOWN3           (0x0f00,"Unknown",ExifLocation.MAKERNOTE_NIKON,TagFormat.ULONG,-1,0,"",Long.class,false),

	// NIKOND1
	NIKOND1_UNKNOWN1              (0x0001,"Unknown",ExifLocation.MAKERNOTE_NIKOND1,TagFormat.UNDEFINED,4,0,"",UndefinedTag.class,false),
	NIKOND1_ISOSETTING	     (0x0002,"ISO Setting",ExifLocation.MAKERNOTE_NIKOND1,TagFormat.USHORT,2,0,"",Long.class,true),
	NIKOND1_COLORMODE	     (0x0003,"Color Mode",ExifLocation.MAKERNOTE_NIKOND1,TagFormat.STRING,-1,0,"",String.class,true),
	NIKOND1_QUALITY		     (0x0004,"Quality",ExifLocation.MAKERNOTE_NIKOND1,TagFormat.STRING,-1,0,"",String.class,true),
	NIKOND1_WHITEBALANCE	     (0x0005,"White Balance",ExifLocation.MAKERNOTE_NIKOND1,TagFormat.STRING,-1,0,"",String.class,true),
	NIKOND1_IMAGESHARPENING       (0x0006,"Image Sharpening",ExifLocation.MAKERNOTE_NIKOND1,TagFormat.STRING,-1,0,"",String.class,true),
	NIKOND1_FOCUSMODE	     (0x0007,"Focus Mode",ExifLocation.MAKERNOTE_NIKOND1,TagFormat.STRING,-1,0,"",String.class,true),
	NIKOND1_FLASHSETTING	     (0x0008,"Flash Setting",ExifLocation.MAKERNOTE_NIKOND1,TagFormat.STRING,-1,0,"",String.class,true),
	NIKOND1_UNKNOWN2              (0x000a,"Unknown",ExifLocation.MAKERNOTE_NIKOND1,TagFormat.URATIONAL,4,0,"",Rational.class,false),
	NIKOND1_ISOSELECTION	     (0x000f,"ISO Selection",ExifLocation.MAKERNOTE_NIKOND1,TagFormat.STRING,-1,0,"",String.class,true),
	NIKOND1_IMAGEADJUSTMENT       (0x0080,"Image Adjustment",ExifLocation.MAKERNOTE_NIKOND1,TagFormat.STRING,-1,0,"",String.class,true),
	NIKOND1_ADAPTER		     (0x0082,"Adapter",ExifLocation.MAKERNOTE_NIKOND1,TagFormat.STRING,-1,0,"",String.class,true),
	NIKOND1_MANUALFOCUSDISTANCE   (0x0085,"Manual Focus Distance",ExifLocation.MAKERNOTE_NIKOND1,TagFormat.URATIONAL,1,0,"",Rational.class,true),
	NIKOND1_DIGITALZOOM	     (0x0086,"Digital Zoom",ExifLocation.MAKERNOTE_NIKOND1,TagFormat.URATIONAL,1,0,"",Rational.class,true),
	NIKOND1_AFFOCUSPOSITION       (0x0088,"AF Focus Position",ExifLocation.MAKERNOTE_NIKOND1,TagFormat.UNDEFINED,4,0,"",UndefinedTag.class,true),
	NIKOND1_DATADUMP	             (0x0010,"Data Dump",ExifLocation.MAKERNOTE_NIKOND1,TagFormat.UNDEFINED,174,0,"",UndefinedTag.class,false),

	// CASIO TYPE 1
	CASIO_RECORDINGMODE   (0x0001,"Recording Mode",ExifLocation.MAKERNOTE_CASIO,TagFormat.USHORT,1,0,"",Casio_RecordingMode.class,true),
	CASIO_QUALITY	      (0x0002,"Quality",ExifLocation.MAKERNOTE_CASIO,TagFormat.USHORT,1,0,"",Casio_Quality.class,true),
	CASIO_FOCUSINGMODE    (0x0003,"Focusing Mode",ExifLocation.MAKERNOTE_CASIO,TagFormat.USHORT,1,0,"",Casio_FocussingMode.class,true),
	CASIO_FLASHMODE       (0x0004,"Flash Mode",ExifLocation.MAKERNOTE_CASIO,TagFormat.USHORT,1,0,"",Casio_FlashMode.class,true),
	CASIO_FLASHINTENSITY  (0x0005,"Flash Intensity",ExifLocation.MAKERNOTE_CASIO,TagFormat.USHORT,1,0,"",Casio_FlashIntensity.class,true),
	CASIO_OBJECTDISTANCE  (0x0006,"Object distance",ExifLocation.MAKERNOTE_CASIO,TagFormat.ULONG,1,0,"",Long.class,true),
	CASIO_WHITEBALANCE    (0x0007,"White Balance",ExifLocation.MAKERNOTE_CASIO,TagFormat.USHORT,1,0,"",Casio_WhiteBalance.class,true),
	CASIO_UNKNOWN1        (0x0008,"Unknown",ExifLocation.MAKERNOTE_CASIO,TagFormat.USHORT,1,0,"",Long.class,false),
	CASIO_UNKNOWN2        (0x0009,"Unknown",ExifLocation.MAKERNOTE_CASIO,TagFormat.USHORT,1,0,"",Long.class,false),
	CASIO_DIGITALZOOM     (0x000a,"Digital Zoom",ExifLocation.MAKERNOTE_CASIO,TagFormat.ULONG,1,0,"",Casio_DigitalZoom.class,true),
	CASIO_SHARPNESS	      (0x000b,"Sharpness",ExifLocation.MAKERNOTE_CASIO,TagFormat.USHORT,1,0,"",Casio_Sharpness.class,true),
	CASIO_CONTRAST	      (0x000c,"Contrast",ExifLocation.MAKERNOTE_CASIO,TagFormat.USHORT,1,0,"",Casio_Contrast.class,true),
	CASIO_SATURATION      (0x000d,"Saturation",ExifLocation.MAKERNOTE_CASIO,TagFormat.USHORT,1,0,"",Casio_Saturation.class,true),
	CASIO_UNKNOWN3        (0x000e,"Unknown",ExifLocation.MAKERNOTE_CASIO,TagFormat.USHORT,1,0,"",Long.class,false),
	CASIO_UNKNOWN4        (0x000f,"Unknown",ExifLocation.MAKERNOTE_CASIO,TagFormat.USHORT,1,0,"",Long.class,false),
	CASIO_UNKNOWN5        (0x0010,"Unknown",ExifLocation.MAKERNOTE_CASIO,TagFormat.USHORT,1,0,"",Long.class,false),
	CASIO_UNKNOWN6        (0x0011,"Unknown",ExifLocation.MAKERNOTE_CASIO,TagFormat.USHORT,1,0,"",Long.class,false),
	CASIO_UNKNOWN7        (0x0012,"Unknown",ExifLocation.MAKERNOTE_CASIO,TagFormat.USHORT,1,0,"",Long.class,false),
	CASIO_UNKNOWN8        (0x0013,"Unknown",ExifLocation.MAKERNOTE_CASIO,TagFormat.USHORT,1,0,"",Long.class,false),
	CASIO_CCDSENSITIVITY  (0x0014,"CCD Sensitivity",ExifLocation.MAKERNOTE_CASIO,TagFormat.USHORT,1,0,"",Casio_CcdSensitivity.class,true),

	// CASIO TYPE 2
	CASIO2_PREVIEWTHUMBNAILDIMENSIONS (0x0002,"Preview Thumbnail Dimensions",ExifLocation.MAKERNOTE_CASIO2,TagFormat.USHORT,2,0,"",ArrayList.class,true),
	CASIO2_PREVIEWTHUMBNAILSIZE       (0x0003,"Preview Thumbnail Size",ExifLocation.MAKERNOTE_CASIO2,TagFormat.ULONG,1,0,"",Long.class,false),
	CASIO2_PREVIEWTHUMBNAILOFFSET     (0x0004,"Preview Thumbnail Offset",ExifLocation.MAKERNOTE_CASIO2,TagFormat.ULONG,1,0,"",Long.class,false),
	CASIO2_QUALITYMODE                (0x0008,"Quality Mode",ExifLocation.MAKERNOTE_CASIO2,TagFormat.USHORT,1,0,"",Casio2_QualityMode.class,true),
	CASIO2_IMAGESIZE                  (0x0009,"Image Size",ExifLocation.MAKERNOTE_CASIO2,TagFormat.USHORT,1,0,"",Casio2_ImageSize.class,true),
	CASIO2_FOCUSMODE                  (0x000D,"Focus Mode",ExifLocation.MAKERNOTE_CASIO2,TagFormat.USHORT,1,0,"",Casio2_FocusMode.class,true),
	CASIO2_ISOSENSITIVITY             (0x0014,"ISO Sensitivity",ExifLocation.MAKERNOTE_CASIO2,TagFormat.USHORT,1,0,"",Casio2_IsoSensitivity.class,true),
	CASIO2_WHITEBALANCE               (0x0019,"White Balance",ExifLocation.MAKERNOTE_CASIO2,TagFormat.USHORT,1,0,"",Casio2_WhiteBalance.class,true),
	CASIO2_FOCALLENGTH                (0x001D,"Focal Length",ExifLocation.MAKERNOTE_CASIO2,TagFormat.ULONG,1,0,"",Long.class,true),
	CASIO2_SATURATION                 (0x001F,"Saturation",ExifLocation.MAKERNOTE_CASIO2,TagFormat.USHORT,1,0,"",Casio2_Saturation.class,true),
	CASIO2_CONTRAST                   (0x0020,"Contrast",ExifLocation.MAKERNOTE_CASIO2,TagFormat.USHORT,1,0,"",Casio2_Contrast.class,true),
	CASIO2_SHARPNESS                  (0x0021,"Sharpness",ExifLocation.MAKERNOTE_CASIO2,TagFormat.USHORT,1,0,"",Casio2_Sharpness.class,true),
	CASIO2_PIM                        (0x0E00,"Print Image Matching Info",ExifLocation.MAKERNOTE_CASIO2,TagFormat.UNDEFINED,-1,0,"",UndefinedTag.class,true),
	CASIO2_CASIOPREVIEWTHUMBNAIL      (0x2000,"Casio Preview Thumbnail",ExifLocation.MAKERNOTE_CASIO2,TagFormat.UNDEFINED,-1,0,"",UndefinedTag.class,false),
	CASIO2_WHITEBALANCEBIAS           (0x2011,"White Balance Bias",ExifLocation.MAKERNOTE_CASIO2,TagFormat.USHORT,2,0,"",Long.class,true),
	CASIO2_WHITEBALANCE2              (0x2012,"White Balance",ExifLocation.MAKERNOTE_CASIO2,TagFormat.USHORT,1,0,"",Casio2_WhiteBalance2.class,true),
	CASIO2_OBJECTDISTANCE             (0x2022,"Object Distance",ExifLocation.MAKERNOTE_CASIO2,TagFormat.USHORT,1,0,"mm",Long.class,true),
	CASIO2_FLASHDISTANCE              (0x2034,"Flash Distance",ExifLocation.MAKERNOTE_CASIO2,TagFormat.USHORT,1,0,"",Long.class,true),
	CASIO2_RECORDMODE                 (0x3000,"Record Mode",ExifLocation.MAKERNOTE_CASIO2,TagFormat.USHORT,1,0,"",Casio2_RecordMode.class,true),
	CASIO2_SELTIMER                   (0x3001,"Self Timer",ExifLocation.MAKERNOTE_CASIO2,TagFormat.USHORT,1,0,"",Casio2_SelfTimer.class,true),
	CASIO2_QUALITY                    (0x3002,"Quality",ExifLocation.MAKERNOTE_CASIO2,TagFormat.USHORT,1,0,"",Long.class,true),
	CASIO2_FOCUSMODE2                 (0x3003,"Focus Mode",ExifLocation.MAKERNOTE_CASIO2,TagFormat.USHORT,1,0,"",Long.class,true),
	CASIO2_TIMEZONE                   (0x3006,"Focus Mode",ExifLocation.MAKERNOTE_CASIO2,TagFormat.STRING,1,0,"",String.class,true),
	CASIO2_BESTSHOTMODE               (0x3007,"Best Shot Mode",ExifLocation.MAKERNOTE_CASIO2,TagFormat.USHORT,1,0,"",Casio2_BestShotMode.class,true),
	CASIO2_CCDISOSENSITIVITY          (0x3014,"CCD ISO Sensitivity",ExifLocation.MAKERNOTE_CASIO2,TagFormat.USHORT,1,0,"",Long.class,true),
	CASIO2_COLORMODE                  (0x3015,"Color Mode",ExifLocation.MAKERNOTE_CASIO2,TagFormat.USHORT,1,0,"",Casio2_ColorMode.class,true),
	CASIO2_ENHANCEMENT                (0x3016,"Enhancement",ExifLocation.MAKERNOTE_CASIO2,TagFormat.USHORT,1,0,"",Casio2_Enhancement.class,true),
	CASIO2_FILTER                     (0x3017,"Filter",ExifLocation.MAKERNOTE_CASIO2,TagFormat.USHORT,1,0,"",Casio2_Filter.class,true),
	
	// CANON
	CANON_UNKNOWN1              (0x0000,"Unknown",ExifLocation.MAKERNOTE_CANON,TagFormat.USHORT,6,0,"",Long.class,false),
	CANON_MACROMODE             (0x0001,"Macro Mode",ExifLocation.MAKERNOTE_CANON,TagFormat.USHORT,1,2,"",Canon_MacroMode.class,true),
	CANON_SELFTIMERLENGTH       (0x0001,"Self Timer Length",ExifLocation.MAKERNOTE_CANON,TagFormat.USHORT,1,4,"10ths of a second",Long.class,true),
	CANON_UNKNOWN3              (0x0001,"Unknown1",ExifLocation.MAKERNOTE_CANON,TagFormat.USHORT,1,6,"",Long.class,false),
	CANON_FLASHMODE             (0x0001,"Flash Mode",ExifLocation.MAKERNOTE_CANON,TagFormat.USHORT,1,8,"",Canon_FlashMode.class,true),
	CANON_CONTINUOUSDRIVEMODE   (0x0001,"Continuous Drive Mode",ExifLocation.MAKERNOTE_CANON,TagFormat.USHORT,1,10,"",Canon_ContinuousDriveMode.class,true),
	CANON_UNKNOWN4              (0x0001,"Unknown2",ExifLocation.MAKERNOTE_CANON,TagFormat.USHORT,1,12,"",Long.class,false),
	CANON_FOCUSMODE             (0x0001,"Focus Mode",ExifLocation.MAKERNOTE_CANON,TagFormat.USHORT,1,14,"",Canon_FocusMode.class,true),
	CANON_UNKNOWN5              (0x0001,"Unknown3",ExifLocation.MAKERNOTE_CANON,TagFormat.USHORT,1,16,"",Long.class,false),
	CANON_UNKNOWN6              (0x0001,"Unknown4",ExifLocation.MAKERNOTE_CANON,TagFormat.USHORT,1,18,"",Long.class,false),
	CANON_IMAGESIZE             (0x0001,"Image Size",ExifLocation.MAKERNOTE_CANON,TagFormat.USHORT,1,20,"",Canon_ImageSize.class,true),
	CANON_EASYSHOOTINGMODE      (0x0001,"Easy Shooting Mode",ExifLocation.MAKERNOTE_CANON,TagFormat.USHORT,1,22,"",Canon_EasyShootingMode.class,true),
	CANON_UNKNOWN7              (0x0001,"Unknown5",ExifLocation.MAKERNOTE_CANON,TagFormat.USHORT,1,24,"",Long.class,false),
	CANON_CONTRAST              (0x0001,"Contrast",ExifLocation.MAKERNOTE_CANON,TagFormat.USHORT,1,26,"",Canon_Contrast.class,true),
	CANON_SATURATION            (0x0001,"Saturation",ExifLocation.MAKERNOTE_CANON,TagFormat.USHORT,1,28,"",Canon_Saturation.class,true),
	CANON_SHARPNESS             (0x0001,"Sharpness",ExifLocation.MAKERNOTE_CANON,TagFormat.USHORT,1,30,"",Canon_Sharpness.class,true),
	CANON_ISO                   (0x0001,"ISO",ExifLocation.MAKERNOTE_CANON,TagFormat.USHORT,1,32,"",Canon_Iso.class,true),
	CANON_METERINGMODE          (0x0001,"MeteringMode",ExifLocation.MAKERNOTE_CANON,TagFormat.USHORT,1,34,"",Canon_MeteringMode.class,true),
	CANON_UNKNOWN8              (0x0001,"Unknown6",ExifLocation.MAKERNOTE_CANON,TagFormat.USHORT,1,36,"",Long.class,false),
	CANON_AFPOINTSELECTED       (0x0001,"AF Point Selected",ExifLocation.MAKERNOTE_CANON,TagFormat.USHORT,1,38,"",Canon_AfSelectionPoint.class,true),
	CANON_EXPOSUREMODE          (0x0001,"ExposureMode",ExifLocation.MAKERNOTE_CANON,TagFormat.USHORT,1,40,"",Canon_ExposureMode.class,true),
	CANON_UNKNOWN9              (0x0001,"Unknown7",ExifLocation.MAKERNOTE_CANON,TagFormat.USHORT,1,42,"",Long.class,false),
	CANON_UNKNOWN10             (0x0001,"Unknown8",ExifLocation.MAKERNOTE_CANON,TagFormat.USHORT,1,44,"",Long.class,false),
	CANON_LONGFOCALLENGTH       (0x0001,"Long Focal Length",ExifLocation.MAKERNOTE_CANON,TagFormat.USHORT,1,46,"focal units",Long.class,true),
	CANON_SHORTFOCALLENGTH      (0x0001,"Short Focal Length",ExifLocation.MAKERNOTE_CANON,TagFormat.USHORT,1,48,"",Long.class,true),
	CANON_FOCALUNITSPERMM       (0x0001,"Focal Units Per MM",ExifLocation.MAKERNOTE_CANON,TagFormat.USHORT,1,50,"",Long.class,true),
	CANON_UNKNOWN11             (0x0001,"Unknown9",ExifLocation.MAKERNOTE_CANON,TagFormat.USHORT,1,52,"",Long.class,false),
	CANON_UNKNOWN12             (0x0001,"Unknown10",ExifLocation.MAKERNOTE_CANON,TagFormat.USHORT,1,54,"",Long.class,false),
	CANON_UNKNOWN13             (0x0001,"Unknown11",ExifLocation.MAKERNOTE_CANON,TagFormat.USHORT,1,56,"",Long.class,false),
	CANON_FLASHDETAILS          (0x0001,"FlashDetails",ExifLocation.MAKERNOTE_CANON,TagFormat.USHORT,1,58,"",Canon_FlashDetails.class,true),
	CANON_UNKNOWN14             (0x0001,"Unknown12",ExifLocation.MAKERNOTE_CANON,TagFormat.USHORT,1,60,"",Long.class,true),
	CANON_UNKNOWN15             (0x0001,"Unknown13",ExifLocation.MAKERNOTE_CANON,TagFormat.USHORT,1,62,"",Long.class,true),
	CANON_FOCUSMODEG1           (0x0001,"Focus Mode",ExifLocation.MAKERNOTE_CANON,TagFormat.USHORT,1,64,"",Canon_FocusModeG1.class,true),
	
	CANON_UNKNOWN2              (0x0003,"Unknown",ExifLocation.MAKERNOTE_CANON,TagFormat.USHORT,4,0,"",Long.class,false),

	CANON_UNKNOWN16          (0x0004,"Unknown14",ExifLocation.MAKERNOTE_CANON,TagFormat.USHORT,1,2,"",Long.class,false),
	CANON_UNKNOWN17          (0x0004,"Unknown15",ExifLocation.MAKERNOTE_CANON,TagFormat.USHORT,1,4,"",Long.class,false),
	CANON_UNKNOWN18          (0x0004,"Unknown16",ExifLocation.MAKERNOTE_CANON,TagFormat.USHORT,1,6,"",Long.class,false),
	CANON_UNKNOWN19          (0x0004,"Unknown17",ExifLocation.MAKERNOTE_CANON,TagFormat.USHORT,1,8,"",Long.class,false),
	CANON_UNKNOWN20          (0x0004,"Unknown18",ExifLocation.MAKERNOTE_CANON,TagFormat.USHORT,1,10,"",Long.class,false),
	CANON_UNKNOWN21          (0x0004,"Unknown19",ExifLocation.MAKERNOTE_CANON,TagFormat.USHORT,1,12,"",Long.class,false),
	CANON_WHITEBALANCE       (0x0004,"White Balance",ExifLocation.MAKERNOTE_CANON,TagFormat.USHORT,1,14,"",Canon_WhiteBalance.class,true),
	CANON_UNKNOWN22          (0x0004,"Unknown20",ExifLocation.MAKERNOTE_CANON,TagFormat.USHORT,1,16,"",Long.class,false),
	CANON_SEQUENCENUMBER     (0x0004,"Sequence Number",ExifLocation.MAKERNOTE_CANON,TagFormat.USHORT,1,18,"",Long.class,true),
	CANON_UNKNOWN23          (0x0004,"Unknown21",ExifLocation.MAKERNOTE_CANON,TagFormat.USHORT,1,20,"",Long.class,false),
	CANON_UNKNOWN24          (0x0004,"Unknown22",ExifLocation.MAKERNOTE_CANON,TagFormat.USHORT,1,22,"",Long.class,false),
	CANON_UNKNOWN25          (0x0004,"Unknown23",ExifLocation.MAKERNOTE_CANON,TagFormat.USHORT,1,24,"",Long.class,false),
	CANON_UNKNOWN26          (0x0004,"Unknown24",ExifLocation.MAKERNOTE_CANON,TagFormat.USHORT,1,26,"",Long.class,false),
	CANON_AFPOINTUSED        (0x0004,"AF Point Used",ExifLocation.MAKERNOTE_CANON,TagFormat.USHORT,1,28,"",Long.class,true), // Break up somehow 
	CANON_FLASHBIAS          (0x0004,"Flash Bias",ExifLocation.MAKERNOTE_CANON,TagFormat.USHORT,1,30,"",Canon_FlashBias.class,true),
	CANON_UNKNOWN27          (0x0004,"Unknown25",ExifLocation.MAKERNOTE_CANON,TagFormat.USHORT,1,32,"",Long.class,false),
	CANON_UNKNOWN28          (0x0004,"Unknown26",ExifLocation.MAKERNOTE_CANON,TagFormat.USHORT,1,34,"",Long.class,false),
	CANON_UNKNOWN29          (0x0004,"Unknown27",ExifLocation.MAKERNOTE_CANON,TagFormat.USHORT,1,36,"",Long.class,false),
	CANON_SUBJECTDISTANCE    (0x0004,"Subject Distance",ExifLocation.MAKERNOTE_CANON,TagFormat.USHORT,1,38,"",Long.class,true),

	CANON_IMAGETYPE          (0x0006,"Image Type",ExifLocation.MAKERNOTE_CANON,TagFormat.STRING,32,0,"",String.class,true),
	CANON_FIRMWAREVERSION	 (0x0007,"Firmware Version",ExifLocation.MAKERNOTE_CANON,TagFormat.STRING,24,0,"",String.class,true),
	CANON_IMAGENUMBER	 (0x0008,"Image Number",ExifLocation.MAKERNOTE_CANON,TagFormat.ULONG,1,0,"",Long.class,true),
	CANON_OWNERNAME		 (0x0009,"Owner Name",ExifLocation.MAKERNOTE_CANON,TagFormat.STRING,32,0,"",String.class,true),
	CANON_UNKNOWN30          (0x000a,"Unknown",ExifLocation.MAKERNOTE_CANON,TagFormat.USHORT,-1,0,"",Long.class,false),
	CANON_CAMERASERIALNUMBER (0x000c,"Camera Serial Number",ExifLocation.MAKERNOTE_CANON,TagFormat.ULONG,1,0,"",Long.class,true),
	CANON_UNKNOWN31          (0x000d,"Unknown",ExifLocation.MAKERNOTE_CANON,TagFormat.USHORT,-1,0,"",Long.class,false),

	CANON_LONGEXPOSURENOISEREDUCTION  (0x000f,"Long Exposure Noise Reduction",ExifLocation.MAKERNOTE_CANON,TagFormat.USHORT,-1,2,"",Canon_LongExposureNoiseReduction.class,true),
	CANON_SHUTTERAELOCKBUTTONS        (0x000f,"Shutter/AE Lock Buttons",ExifLocation.MAKERNOTE_CANON,TagFormat.USHORT,-1,4,"",Canon_ShutterAeLockButtons.class,true),
	CANON_MIRRORLOCKUP                (0x000f,"Mirror Lockup",ExifLocation.MAKERNOTE_CANON,TagFormat.USHORT,-1,6,"",Canon_MirrorLockup.class,true),
	CANON_TVAVEXPOSURELEVEL           (0x000f,"Tv/Av and Exposure Level",ExifLocation.MAKERNOTE_CANON,TagFormat.USHORT,-1,8,"",Canon_TvAvExposureLevel.class,true),
	CANON_AFASSISTLIGHT               (0x000f,"AF Assist Light",ExifLocation.MAKERNOTE_CANON,TagFormat.USHORT,-1,10,"",Canon_AfAssistLight.class,true),
	CANON_SHUTTERSPEEDINAVMODE	  (0x000f,"Shutter Speed in Av Mode",ExifLocation.MAKERNOTE_CANON,TagFormat.USHORT,-1,12,"",Canon_ShutterSpeedInAvMode.class,true),
	CANON_AEBSEQUENCEAUTOCANCELLATION (0x000f,"AEB Sequence/Auto Cancellation",ExifLocation.MAKERNOTE_CANON,TagFormat.USHORT,-1,14,"",Canon_AebSequenceAutoCancellation.class,true),
	CANON_SHUTTERCURTAINSYNC          (0x000f,"Shutter Curtain Sync",ExifLocation.MAKERNOTE_CANON,TagFormat.USHORT,-1,16,"",Canon_ShutterCurtainSync.class,true),
	CANON_LENSAFSTOPBUTTONFNSWITCH	  (0x000f,"Lens AF Stop Button Fn Switch",ExifLocation.MAKERNOTE_CANON,TagFormat.USHORT,-1,18,"",Canon_LensAfButtonFnSwitch.class,true),
	CANON_AUTOREDUCTIONOFFILLFLASH	  (0x000f,"Auto Reduction of Fill Flash",ExifLocation.MAKERNOTE_CANON,TagFormat.USHORT,-1,20,"",Canon_AutoReductionOfFillFlash.class,true),	
	CANON_MENUBUTTONRETURNPOSITION    (0x000f,"Menu Button Return Position",ExifLocation.MAKERNOTE_CANON,TagFormat.USHORT,-1,22,"",Canon_MenuButtonReturnPosition.class,true),
	CANON_SETBUTTONFUNCWHENSHOOTING	  (0x000f,"Set Button Func. When Shooting",ExifLocation.MAKERNOTE_CANON,TagFormat.USHORT,-1,24,"",Canon_SetButtonFuncWhenShooting.class,true),
	CANON_SENSORCLEANING	          (0x000f,"Sensor Cleaning",ExifLocation.MAKERNOTE_CANON,TagFormat.USHORT,-1,26,"",Canon_SensorCleaning.class,true),

	// FUJIFILM
	FUJIFILM_VERSION          (0x0000,"Version",ExifLocation.MAKERNOTE_FUJIFILM,TagFormat.UNDEFINED,4,0,"",UndefinedTag.class,true),
	FUJIFILM_QUALITY	  (0x1000,"Quality",ExifLocation.MAKERNOTE_FUJIFILM,TagFormat.STRING,8,0,"",String.class,true),
	FUJIFILM_SHARPNESS	  (0x1001,"Sharpness",ExifLocation.MAKERNOTE_FUJIFILM,TagFormat.USHORT,1,0,"",Fujifilm_Sharpness.class,true),
	FUJIFILM_WHITEBALANCE	  (0x1002,"White Balance",ExifLocation.MAKERNOTE_FUJIFILM,TagFormat.USHORT,1,0,"",Fujifilm_WhiteBalance.class,true),
	FUJIFILM_COLOR		  (0x1003,"Color",ExifLocation.MAKERNOTE_FUJIFILM,TagFormat.USHORT,1,0,"",Fujifilm_Color.class,true),
	FUJIFILM_TONE		  (0x1004,"Tone",ExifLocation.MAKERNOTE_FUJIFILM,TagFormat.USHORT,1,0,"",Fujifilm_Tone.class,true),
	FUJIFILM_FLASHMODE	  (0x1010,"Flash Mode",ExifLocation.MAKERNOTE_FUJIFILM,TagFormat.USHORT,1,0,"", Fujifilm_FlashMode.class,true),
	FUJIFILM_FLASHSTRENGTH	  (0x1011,"Flash Strength",ExifLocation.MAKERNOTE_FUJIFILM,TagFormat.SRATIONAL,1,0,"",Rational.class,true),
	FUJIFILM_MACRO		  (0x1020,"Macro",ExifLocation.MAKERNOTE_FUJIFILM,TagFormat.USHORT,1,0,"",Fujifilm_Macro.class,true),
	FUJIFILM_FOCUSMODE	  (0x1021,"Focus Mode",ExifLocation.MAKERNOTE_FUJIFILM,TagFormat.USHORT,1,0,"",Fujifilm_FocusMode.class,true),
	FUJIFILM_SLOWSYNC	  (0x1030,"Slow Sync.",ExifLocation.MAKERNOTE_FUJIFILM,TagFormat.USHORT,1,0,"",Fujifilm_SlowSync.class,true),
	FUJIFILM_PICTUREMODE	  (0x1031,"Picture Mode",ExifLocation.MAKERNOTE_FUJIFILM,TagFormat.USHORT,1,0,"",Fujifilm_PictureMode.class,true),
	FUJIFILM_UNKNOWN1         (0x1032,"Unknown",ExifLocation.MAKERNOTE_FUJIFILM,TagFormat.USHORT,1,0,"",Long.class,true),
	FUJIFILM_CONTTAKEBRACKET  (0x1100,"ContTake/Bracket",ExifLocation.MAKERNOTE_FUJIFILM,TagFormat.USHORT,1,0,"",Fujifilm_ContTake.class,true),
	FUJIFILM_UNKNOWN2         (0x1200,"Unknown",ExifLocation.MAKERNOTE_FUJIFILM,TagFormat.USHORT,1,0,"",Long.class,true),
	FUJIFILM_BLURWARNING	  (0x1300,"Blur Warning",ExifLocation.MAKERNOTE_FUJIFILM,TagFormat.USHORT,1,0,"",Fujifilm_BlurWarning.class,true),
	FUJIFILM_FOCUSWARNING	  (0x1301,"Focus Warning",ExifLocation.MAKERNOTE_FUJIFILM,TagFormat.USHORT,1,0,"",Fujifilm_FocusWarning.class,true),
	FUJIFILM_AEWARNING	  (0x1302,"AE Warning",ExifLocation.MAKERNOTE_FUJIFILM,TagFormat.USHORT,1,0,"",Fujifilm_AeWarning.class,true);
	
	/**
	 * Identifier as it appears in the file.
	 */
	public final int           id;

	/**
	 * Textual name of the tag for displaying.
	 */
	public final String        name;

	/**
	 * Where the tag is expected to appear in the APP1 header.
	 */
	public final ExifLocation  location;

	/**
	 * Tag data type.
	 */
	public final TagFormat     format;

	/**
	 * Number of expected components (or -1 for unknown or any.
	 *
	 * The number of bytes is the tag format size times this number.
	 */
	public final int           components;

	/**
	 * For tags which contain multiple heterogenous values (ie macrotags),
	 * the offset within the macrotag where this value appears.
	 */
	public final int           offset;

	/**
	 * Units, where appropriate.
	 */
	public final String        units;

	/**
	 * Class as which the tag is stored internally (and can always be
	 * returned as).
	 */
	public final Class         implementingClass;

	/**
	 * Informative only.  Whether or not the tag is editable.
	 *
	 * Offset tags are markied as not editable.  This library maintains
	 * them automatically.
	 */
	public final boolean       editable;
	
	Tag(int id, String name, ExifLocation location, TagFormat format,
	    int components, int offset, String units, Class implementingClass,
	    boolean editable)
	{
	    this.id         = id;
	    this.name       = name;
	    this.location   = location;
	    this.components = components;
	    this.format     = format;
	    this.offset     = offset;
	    this.units      = units;
	    this.implementingClass = implementingClass;
	    this.editable   = editable;
	}
    }

    private static HashSetFromArray<Integer> s_macroTags
    = new HashSetFromArray<Integer>(new Integer[]{
	Tag.CFAPATTERN_HRPT.id,
	Tag.CANON_MACROMODE.id,
	Tag.CANON_WHITEBALANCE.id,
	Tag.CANON_LONGEXPOSURENOISEREDUCTION.id
    });

    private static HashMap<Integer, TagFormat> s_tagFormatIdToEnum
	= new HashMap<Integer, TagFormat>();

    private static HashMap<ExifLocation,TreeMap<Integer, Tag> >
	s_tagLocationToIdToEnum
	= new HashMap<ExifLocation,TreeMap<Integer, Tag> >();

    private static HashMap<ExifLocation,HashMap<Integer, TreeSet<Tag> > >
        s_tagLocationToMacroTagToTagSet
        = new HashMap<ExifLocation,HashMap<Integer, TreeSet<Tag> > >();
    
    private int[] m_thumbnailBytes = null;
    
    static
    {
	// build map of tag id to Tag
 	TreeMap<Integer, Tag> locationTags = null;
	ExifLocation lastLocation = ExifLocation.UNKNOWN;
	for (Tag tag : Tag.values())
	{
	    if (locationTags == null || tag.location != lastLocation)
	    {
		locationTags = new TreeMap<Integer, Tag>();
		s_tagLocationToIdToEnum.put(tag.location, locationTags);
		lastLocation = tag.location;
	    }
	    locationTags.put(tag.id, tag);
	}

	// build map of tag format id to TagFormat
	for (TagFormat tag : TagFormat.values())
	    s_tagFormatIdToEnum.put(tag.id, tag);

	// build map of tag id to subtags set
	HashMap<Integer, TreeSet<Tag> > locationMacroTags = null;
	lastLocation = ExifLocation.UNKNOWN;
	for (Tag tag : Tag.values())
	{
	    if (locationMacroTags == null || tag.location != lastLocation)
	    {
		locationMacroTags = new HashMap<Integer, TreeSet<Tag> >();
		s_tagLocationToMacroTagToTagSet.put(tag.location,
						    locationMacroTags);
		lastLocation = tag.location;
	    }
	    if (s_macroTags.contains(tag.id))
	    {
		TreeSet<Tag> set = locationMacroTags.get(tag.id);
		if (set == null)
		{
		    set = new TreeSet<Tag>(new TagOffsetComparator());
		    locationMacroTags.put(tag.id, set);
		}
		set.add(tag);
	    }
	}
    }
    
    private ByteOrder m_byteOrder = ByteOrder.MOTOROLA;
    private EnumMap<ExifLocation, TreeMap<Tag,TagValue> > m_parsedTags
        = new EnumMap<ExifLocation, TreeMap<Tag,TagValue> >(ExifLocation.class);
    // note: for unparsed tags, UndefinedTag includes format and num components
    private EnumMap<ExifLocation, TreeMap<Integer,UndefinedTag> > m_unparsedTags
	= new EnumMap<ExifLocation, TreeMap<Integer,UndefinedTag> >(ExifLocation.class);

    /*
     * Several cameras can share the same makernote format.  This is the header
     * from it.
     */
    private int[] m_makerNoteHeader = null;

    /*
     * Several cameras can share the same makernote format.  This is the header
     * from it.
     */
    private int[] m_makerNoteBytes = null;

    /*
     * When known, eg "Olympus", "Nikon" etc.
     *
     * Note that several cameras share the same maker note format, thus
     * for example Pentax and Olympus both use ExifLocation.MAKERMOTE_OLYMPUS
     * but have different headers (see m_makerNoteHeader).
     */
    private CameraType m_cameraType = CameraType.UNKNOWN;

    /**
     * Create a new empty APP1 header .
     *
     * Creates empty IFD0, IFD1 and SUBEXIF sections.  Note it is not
     * possible to create makernote or interoperability sections from
     * scratch.  That is, only standard EXIF 2.0 can be created.
     */
    App1Header()
    {
	m_parsedTags.put(ExifLocation.IFD0, new TreeMap<Tag,TagValue>());
	m_parsedTags.put(ExifLocation.IFD1, new TreeMap<Tag,TagValue>());
	m_parsedTags.put(ExifLocation.SUBEXIF, new TreeMap<Tag,TagValue>());
    }

    /**
     * Constructor from complete APP1 header.
     *
     * Parses the data and stores internally.
     *
     * @param data bytes as read from the JPEG file, excluding marker itself.
     */
    App1Header(int[] data)
	throws IOException, ExifFormatException, TagFormatException
    {
	App1HeaderReader reader = new App1HeaderReader(this, data);

	// make sure the bare minimum of sections is present so that
	// tags can be added to them.
	TreeMap<Tag,TagValue> ifd0Tags = m_parsedTags.get(ExifLocation.IFD0);
	TreeMap<Tag,TagValue> ifd1Tags = m_parsedTags.get(ExifLocation.IFD1);
	TreeMap<Tag,TagValue> subExifTags
	    = m_parsedTags.get(ExifLocation.SUBEXIF);
	if (ifd0Tags == null)
	    m_parsedTags.put(ExifLocation.IFD0, new TreeMap<Tag,TagValue>());
	if (ifd1Tags == null)
	    m_parsedTags.put(ExifLocation.IFD1, new TreeMap<Tag,TagValue>());
	if (subExifTags == null)
	    m_parsedTags.put(ExifLocation.SUBEXIF,
			     new TreeMap<Tag,TagValue>());
    }

    /**
     * Returns the JPEG thumbnail if one is defined.
     *
     * @return the thumbnail bytes, as they appear in the file.
     */
    public int[] getThumbnailBytes()
    {
	return m_thumbnailBytes;
    }

    /**
     * Sets the JPEG thumbnail.
     *
     * @param bytes the thumbnail bytes, which must be JPEG encoded
     * and must not contains APP0 or APP1 markers.
     */
    public void setThumbnailBytes(int[] bytes)
	throws TagFormatException, ExifFormatException
    {
	m_thumbnailBytes = bytes;
	if (getValue(Tag.IFD1_JPEGIFOFFSET) == null)
	    setValue(new TagValue(Tag.IFD1_JPEGIFOFFSET, new Long(0)));
	if (getValue(Tag.IFD1_JPEGIFBYTECOUNT) == null)
	    setValue(new TagValue(Tag.IFD1_JPEGIFBYTECOUNT, new Long(0)));
    }

    /**
     * Returns the byte ordering within the tag.
     *
     * Callers shouldn't need to do anything with this value as it is
     * all handled internally.
     *
     * @return the byte order.
     */
    public ByteOrder getByteOrder()
    {
	return m_byteOrder;
    }

    /**
     * Returns the camera type.
     *
     * @return the camera type, which may be UNKNOWN.
     */
    public CameraType getCameraType()
    {
	return m_cameraType;
    }

    /**
     * Returns all known tag/value pairs found in the given part of the
     * header.
     *
     * @param location location within the EXIF header.
     * @return all tags found in the given part of the EXIF header,
     *         sorted in order of tag id, or null if none are found.
     */
    public SortedMap<Tag,TagValue> getTags(ExifLocation location)
    {
	return m_parsedTags.get(location);
    }
    
    /**
     * Returns all known tag/value pairs found in all parts of the
     * header.
     *
     * @return all pairs, in order of location then tag id, or an empty set if none found.
     */
    public SortedMap<Tag,TagValue> getTags()
    {
	return getTags(false);
    }
    
    /**
     * Returns all known tag/value pairs found in all parts of the
     * header.
     *
     * @param skipUneditable if true, do not return tags not marked as
     *                       editable.
     * @return all pairs, in order of tag id, or an empty set if none found.
     */
    public SortedMap<Tag,TagValue> getTags(boolean skipUneditable)
    {
	TreeMap<Tag,TagValue> pairs = new TreeMap<Tag,TagValue>(new TagLocationIdComparator());
	for (ExifLocation location : m_parsedTags.keySet())
	{
	    TreeMap<Tag,TagValue> tags = m_parsedTags.get(location);
	    for (Tag tag : tags.keySet())
	    {
		if (!skipUneditable || tag.editable)
		    pairs.put(tag, tags.get(tag));
	    }
	}
	return pairs;
    }

    /**
     * Returns all possible tags that may appear in the given location.
     *
     * @param loc location within the EXIF header.
     * @return all available tags for the given part of an EXIF, or
     *         an empty set but not null.
     */
    public static SortedSet<Tag> getAvailableTags(ExifLocation loc)
    {
	TreeSet<Tag> avail = new TreeSet<Tag>(new TagIdComparator());
	TreeMap<Integer,Tag> tags = s_tagLocationToIdToEnum.get(loc);
	if (tags == null) return avail;
	for (Integer id : tags.keySet())
	    avail.add(tags.get(id));
	return avail;
    }

    /**
     * Returns all possible tags for the given camera type.
     *
     * Returns all IFD0, IFD1 and SUBEXIF tags along with any appropriate
     * makernote tags.
     *
     * @param camera camera type to return tags for.
     * @return set of all tags possible for the given camera type,
     *         sorted in order of tag id.
     */
    public static SortedSet<Tag> getAvailableTags(CameraType camera)
    {
	TreeSet<Tag> avail = new TreeSet<Tag>(new TagIdComparator());
	avail.addAll(getAvailableTags(ExifLocation.IFD0));
	avail.addAll(getAvailableTags(ExifLocation.IFD1));
	avail.addAll(getAvailableTags(ExifLocation.SUBEXIF));
	if (camera.makerNoteLocation != ExifLocation.UNKNOWN
	    && camera.makerNoteLocation != ExifLocation.SUBEXIF)
	    avail.addAll(getAvailableTags(camera.makerNoteLocation));
	return avail;
    }

    /**
     * Returns the value for the given tag or null if it is not set.
     *
     * @param tag the tag whose value to return.
     * @return the value for the tag or null if it is not present.
     */
    public TagValue getValue(Tag tag)
    {
	TreeMap<Tag,TagValue> locTags = m_parsedTags.get(tag.location);
	if (locTags == null) return null;
	return locTags.get(tag);
    }

    /**
     * Removes the named tag from the header.
     *
     * @param tag the tag to remove
     */
    public void removeTag(Tag tag)
    {
	TreeMap<Tag,TagValue> locTags = m_parsedTags.get(tag.location);
	if (locTags != null) locTags.remove(tag);
    }

    /**
     * Sets the value for a given tag.
     *
     * <p>You can only set tags for parts of the EXIF header that exist.
     * For example if there is no Olympus makernote you cannot set an
     * Olympus makernote tag.  Attempting to do so throws an
     * ExifFormatException.</p>
     *
     * <p>Does not update the file.  Call {@link JpegHeaders#save(boolean)}
     * for that.</p>
     *
     * @param value the value to set the tag to.
     *
     * @throws TagFormatException if there is a formatting error in the tag
     *                            value (eg it is an inappropriate type for
     *                            the tag).
     * @throws ExifFormatException if there is an error in the EXIF format.
     */
    public void setValue(TagValue value)
	throws TagFormatException, ExifFormatException
    {
	TreeMap<Tag,TagValue> locTags
	    = m_parsedTags.get(value.getTag().location);
	if (locTags == null)
	    throw new ExifFormatException("Cannot set " + value.getTag().location
					  + " tags in this EXIF file");
	locTags.put(value.getTag(),value);
	makeOffsets(value.getTag());
    }

    /**
     * Sets a value for a given tag, parsing it from a string value first.
     *
     * <p>If the tag is an enumerated type you can pass either the numeric
     * or string (case-insensitive) value.</p>
     *
     * You can't set any tags whose number of components is greater than one
     * using this method, unless it is a STRING tag.  The exception is
     * UndefinedTags which are enumerated (ie implementingClass is a subclass
     * of EnumeratedTag).  In this case you can pass the textual name
     * of the value but not the numeric value.</p>
     *
     * <p>You can only set tags for parts of the EXIF header that exist.
     * For example if there is no Olympus makernote you cannot set an
     * Olympus makernote tag.  Attempting to do so throws an
     * ExifFormatException.</p>
     *
     * <p>Note that UNDEFINED type values are assumed to be of the form
     & 0xnn,0xnn,..., not unicode strings.  If you wish to set an UNDEFINED
     * tag to a unicode string value, construct an UndefinedTag using a
     * String, construct a TagValue from this and call setValue(TagValue).</p>
     *
     * @param tag the tag whose value to set.
     * @param value the value, as a string, to set the tag to.
     *
     * @throws TagFormatException if there is a formatting error in the tag
     *                            value (eg it is an inappropriate type for
     *                            the tag), or if there is an error parsing
     *                            the string.
     * @throws ExifFormatException if there is an error in the EXIF format.
     */
    public void setValue(Tag tag, String value)
	throws TagFormatException, ExifFormatException
    {
	TagValue tv = null;
	try
	{
	    boolean isNumber = false;
	    try
	    {
		Double.parseDouble(value);
		isNumber = true;
	    }
	    catch (Exception e) {}
	    if (EnumeratedTag.class.isAssignableFrom(tag.implementingClass))
	    {
		if (tag.format == TagFormat.UNDEFINED)
		{
		    if (isNumber)
			throw new TagFormatException("When setting an UNDEFINED EnumeratedTag type, must use textual value");
		    Constructor con
			= tag.implementingClass.getConstructor(String.class);
		    EnumeratedTag etag = (EnumeratedTag) con.newInstance(new String(value));
		    UndefinedTag utag = new UndefinedTag(etag,
							 m_byteOrder,
							 tag.components);
		    tv = new TagValue(tag, utag);
			
		}
		else
		{
		    if (isNumber)
		    {
			Constructor con
			    = tag.implementingClass.getConstructor(Long.class);
			tv = new TagValue(tag, con.newInstance(new Long(value)));
		    }
		    else
		    {
			Constructor con
			    = tag.implementingClass.getConstructor(String.class);
			tv = new TagValue(tag, con.newInstance(value));
		    }
		}
	    }
	    else
	    {
		switch (tag.format)
		{
		case BYTE:
		case SBYTE:
		case USHORT:
		case SSHORT:
		case ULONG:
		case SLONG:
		    tv = new TagValue(tag, new Long(value));
		    break;
		case SRATIONAL:
		case URATIONAL:
		{
		    String[] args = value.split("/");
		    if (args.length != 2)
			throw new TagFormatException(value
						     + " is not a rational");
		    tv = new TagValue(tag, new Rational(new Long(args[0]),
							new Long(args[1])));
		}
		break;
		case UNDEFINED:
		{
		    String[] args = value.split(",");
		    int[] bytes = new int[args.length];
		    for (String arg : args)
		    {
			int n;
			if (arg.toLowerCase().startsWith("0x"))
			    n = Integer.parseInt(arg.substring(2),16);
			else
			    n = Integer.parseInt(arg);
			tv = new TagValue(tag, new UndefinedTag(bytes));
		    }
		}
		break;
		case STRING:
		{
		    if (DateTimeTag.class.isAssignableFrom(tag.implementingClass))
			tv = new TagValue(tag, new DateTimeTag(value));
		    else
			tv = new TagValue(tag, value);
		}
		break;
		}
	    }
	}
	catch (NumberFormatException e)
	{
	    throw new TagFormatException("Error converting " + value
					 + " to number");
	}
	catch (NoSuchMethodException e)
	{
	    // this is an error with the static data
	    e.printStackTrace();
	    throw new ExifFormatException("Misconfiguration in static data");
	}
	catch (InstantiationException e)
	{
	    // can't happen as LongEnumeratedClass constructor
	    // doesn't throw an exception
	    e.printStackTrace();
	    throw new ExifFormatException("Misconfiguration in static data");
	}
	catch (IllegalAccessException e)
	{
	    // can't happen as LongEnumeratedClass constructor
	    // doesn't throw an exception
	    e.printStackTrace();
	    throw new ExifFormatException("Misconfiguration in static data");
	}
	catch (InvocationTargetException e)
	{
	    // can't happen as LongEnumeratedClass constructor
	    // doesn't throw an exception
	    e.printStackTrace();
	    throw new ExifFormatException("Misconfiguration in static data");
	}
	TreeMap<Tag,TagValue> locTags = m_parsedTags.get(tag.location);
	if (locTags == null)
	    throw new ExifFormatException("Cannot set " + tag.location
					  + " tags in this EXIF file");
	System.out.println("Creating tag " + tag);
	locTags.put(tag,tv);
	makeOffsets(tag);
    }

    private void makeOffsets(Tag tag)
	throws TagFormatException, ExifFormatException
    {
	if (tag == Tag.EXIFOFFSET || tag == Tag.EXIFINTEROPERABILITYOFFSET)
	    return;
	if (tag.location == ExifLocation.SUBEXIF)
	{
	    if (getValue(Tag.EXIFOFFSET) == null)
		setValue(new TagValue(Tag.EXIFOFFSET, new Long(0)));
	}
	else if (tag.location == ExifLocation.INTEROP)
	{
	    if (getValue(Tag.EXIFINTEROPERABILITYOFFSET) == null)
		setValue(new TagValue(Tag.EXIFINTEROPERABILITYOFFSET,
				      new Long(0)));
	}
    }

    /**
     * Returns a textual represenation of this object (really for debugging).
     *
     * @return textual representation of object.
     */
    public String toString()
    {
	StringBuffer buf = new StringBuffer();
	System.out.println("Parsed Tags:");
	boolean first = true;
	for (ExifLocation loc : getAllParsedTags().keySet())
	{
	    if (first)
		first = false;
	    else
		buf.append("\n");

	    buf.append(loc + ":\n");
	    TreeMap<Tag,TagValue> tags = getAllParsedTags().get(loc);
	    for (Tag tag : tags.keySet())
	    {
		TagValue value = tags.get(tag);
		if (!tag.name.equals("Unknown"))
		{
		    if (tag.editable)
			buf.append(tag.name + " = " + value.toString()+"\n");
		    else
			buf.append(tag.name + " = <data>\n");
		}
	    }
	}

	return buf.toString();
    }

    /**
     * Returns the raw, unparsed makernote bytes or null if there is
     * no makernote section.
     *
     * @return the raw bytes as they appeared in the file.
     */
    public int[] getMakerNoteBytes()
    {
	return m_makerNoteBytes;
    }

    ///////////////////////////////////////////////////////////////////
    // package methods

    /**
     * Returns all available macrotags as a set of ids.
     *
     * Macro tags are pieces of information stored together in a single
     * EXIF tag.  For example, Canon cameras store a number of pseudo-tags
     * inside a single EXIF tag.  They are placed at known offsets within
     * the tag value.
     *
     * @return set of all macro tags
     */
    static Set<Integer> getMacroTags()
    {
	return s_macroTags;
    }

    /**
     * Returns a map of tag format id to TagFormat enum.
     *
     * @return a map.
     */
    static HashMap<Integer, TagFormat> getTagFormatIdToEnum()
    {
	return s_tagFormatIdToEnum;
    }

    /**
     * Returns a map of tag location id to a map of tag id to Tag enum.
     *
     * @return a map.
     */
    static HashMap<ExifLocation,TreeMap<Integer, Tag> > getTagLocationToIdToEnum()
    {
	return s_tagLocationToIdToEnum;
    }

    /**
     * Returns a map of tag location id to a map of macro tag to a set of
     * child Tags.
     *
     * @return a map.
     */
    static HashMap<ExifLocation,HashMap<Integer, TreeSet<Tag> > > getTagLocationToMacroTagToTagSet()
    {
	return s_tagLocationToMacroTagToTagSet;
    }

    /**
     * Sets the byte order for the EXIF header.
     *
     * @param order the new byte order.
     */
    void setByteOrder(ByteOrder order)
    {
	m_byteOrder = order;
    }

    /**
     * Returns all parsed tag values.
     *
     * @return a map.
     */
    EnumMap<ExifLocation, TreeMap<Tag,TagValue> > getAllParsedTags()
    {
	return m_parsedTags;
    }

    /**
     * Returns all unparsed tag values, ie those whose tag id is unrecognized
     * or is recognized but has an unexpected format.
     *
     * @return a map.
     */
    EnumMap<ExifLocation, TreeMap<Integer,UndefinedTag> > getAllUnparsedTags()
    {
	return m_unparsedTags;
    }

    /**
     * Sets the camera type.
     *
     * @param type the new camera type
     */
    void setCameraType(CameraType type)
    {
	m_cameraType = type;
    }

    /**
     * Sets the makernote header.
     *
     * @param bytes the header as an array of unsigned bytes.
     */
    void setMakerNoteHeader(int[] bytes)
    {
	m_makerNoteHeader = bytes;
    }

    /**
     * Sets the makernote bytes (as they would appear in the file).
     *
     * @param bytes the makernote as an array of unsigned bytes.
     */
    void setMakerNoteBytes(int[] bytes)
    {
	m_makerNoteBytes = bytes;
    }

    /**
     * Returns the makernote bytes as it appears in the file.
     *
     * @return the makernote as an array of unsigned bytes.
     */
    int[] getMakerNoteHeader()
    {
	return m_makerNoteHeader;
    }

    /**
     * Writes the APP1 header to an array of bytes
     *
     * @return the bytes as unsigned ints, as they should appear in the file,
     *         excluding the APP1 marker itself.
     */
    int[] write() throws TagFormatException
    {
	App1HeaderWriter writer = new App1HeaderWriter(this);
	return writer.write();
    }
}
