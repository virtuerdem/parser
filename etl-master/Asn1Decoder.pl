#!/usr/local/bin/perl
#
# Description:
# Parses ASN.1 STS files to comma-separated files
#
# Revision: PA8
# Author: etoriva
#
# Usage: STSASN1decode.pl <STS folder name>
#
# The script will create a csv folder under the STS folder,
# unless the -nc (or --nocsv) parameter is specified. Then
# existing csv folder is used, and data appended to existing
# files.
#
# Reference: POD 5/190 83-CNZ 230 49 PA2
#
# ASN.1 structure (not fully compatible with 3GPP):
#
# DEFINITIONS AUTOMATIC TAGS::=BEGIN
# MeasDataCollection::=SEQUENCE
# {
# 	measFileHeader MeasFileHeader
# 	measData SEQUENCE OF MeasData
# 	measFileFooter MeasFileFooter
# }
# MeasFileHeader::=SEQUENCE
# {
# 	fileFormatVersion INTEGER,
# 	senderName UTF8String (SIZE(0..400)),
# 	senderType SenderType,
# 	VendorName PrintableString (SIZE (0..32)),
# 	collectionBeginTime TimeStamp,
# 	...
# }
# SenderType::= PrintableString (SIZE (0..8))
# TimeStamp::= GeneralizedTime
# MeasData::= SEQUENCE
# {
# 	nEId NEId,
# 	measInfo SEQUENCE OF MeasInfo
# }
# NEId::= SEQUENCE
# {
# 	nEUserName PrintableString(SIZE(0..64)),
# 	nEDistinguishedName PrintableString (SIZE (0..400))
# }
# MeasInfo::= SEQUENCE
# {
# 	measStartTime TimeStamp,
# 	granularityPeriod INTEGER,
# 	measTypes SEQUENCE OF MeasType,
# 	measValues SEQUENCE OF MeasValue
# }
# MeasType::= PrintableString (SIZE (1..32))
# MeasValue::= SEQUENCE
# {
# 	measObjInstId MeasObjInstId,
# 	measResults SEQUENCE OF MeasResult,
# 	suspectFlag BOOLEAN DEFAULT FALSE
# }
# MeasObjInstId::= PrintableString (SIZE (1..64))
# MeasResult::= CHOICE
# {
# 	iValue INTEGER (0..4294967295),
# 	rValue REAL,
# 	noValue NULL,
# 	...
# }
# MeasFileFooter::= TimeStamp
# END
# 

# History
# PA8 - Removed dependencies on opendir et.al. Specified permission for csv
#       directory, and changed to use stat to get file size.
# PA7 - Corrected ETA measurements
# PA6 - Adapted to allow for both definite length and indefinite length
# PA5 - Added option not to delete and create csv folder
# PA4 - Added ETA measurements
# PA3 - Changed Hour_UTC to Time_UTC
# PA2 - Added PERLEN and EXCHID to make it more like Statics
# PA1 - First release
#

# TODO
#  - Try to speed up processing. Today much faster with definite length
#  - Save file handles as hash, check hashes instead of files
#

### start of main ###
use strict;

# Constants
use constant CUNIVERSAL 		=> 0;
use constant CAPPLICATION 		=> 1;
use constant CCONTEXTSPEC 		=> 2;
use constant CPRIVATE	 		=> 3;
use constant CPRIMITIVE 		=> 0;
use constant CCONSTRUCTED 		=> 1;
use constant CINDEFLENGTH 		=> 128;
#use constant CCSVDIR	 		=> "csv";
use constant DEBUG		 		=> 0;
use constant DEBUGDEEP	 		=> 0;				# Often not needed

# Declare variables
#my $INFILE;				# File handler
my $file_global;		# All octets in file, global variable
my $filesize_global;	# The size of the file, global variable
my $filename_global;	# The size of the file, global variable
my $idx_global;			# Current index in the file, global variable
my @files;				# Array of files
my $filename;
my $filecnt;
my $dh;					# Directory handle
my $dirname;			# Directory name
my $foo;

my $starttime;
my $processtime;
my $sec;
my $min;
my $hour;

my $createcsv;			# Command line parameter flag
my $printhelp;			# Command line parameter flag

# Check to see if it's an old Perl running...
warn "Warning!\nThis version of Perl (" . $] . ") may be too old for the script to work properly.\n\n" if $] lt '5.012';

# Check command line parameters and establish working directory
checkargv(\$dirname, \$createcsv, \$printhelp);
if ($printhelp) {
	printhelp ();
	exit;
}

# Decode each file
$starttime = time;
$filecnt = 0;

# Change directory, create CSV directory, and collect file names
if (-d $dirname) {
	chdir ($dirname);
	@files = getFiles();
	#setupCSVdirectory(CCSVDIR) if $createcsv;

	foreach $filename (@files) {
		print "Decoding file " . ++$filecnt . " of " . @files . ": $filename...";
		if ($filecnt > 1) {
			$processtime = ((time - $starttime) / ($filecnt - 1) * (@files - $filecnt + 1));
			$hour = int ($processtime / 3600);
			$min = int (($processtime - $hour * 3600) / 60);
			$sec = $processtime - $hour * 3600 - $min * 60;
			printf (" (ETA: %02d:%02d:%02d)\n", $hour, $min, $sec) if $filecnt > 1;
		} else {
			printf (" (ETA: thinking)\n") if $filecnt == 1;
		}

		$filename_global = $filename;
		open (INFILE, $filename) || die ("Error: Could not open file $filename\nCode: $!\n");
		$filesize_global = (stat($filename))[7];
		binmode INFILE;
		read (INFILE, $file_global, $filesize_global);

		decodeASN1toCSV();

		close INFILE;
	}
} else {

	$filename_global = $dirname;
	open (INFILE, $dirname) || die ("Error: Could not open file $filename\nCode: $!\n");
	$filesize_global = (stat($dirname))[7];
	binmode INFILE;
	read (INFILE, $file_global, $filesize_global);

	decodeASN1toCSV();

	close INFILE;
	#die "Error: $dirname is not a directory.\n";
}

$processtime = (time - $starttime);
$hour = int ($processtime / 3600);
$min = int (($processtime - $hour * 3600) / 60);
$sec = $processtime - $hour * 3600 - $min * 60;
print "Completed in ";
printf ("%02d:%02d:%02d\n", $hour, $min, $sec);

exit (0);

##################################################
##################################################
################## end of main ###################
##################################################
##################################################


################
# Sub-routines #
################

###################
# decodeASN1toCSV #
###################
sub decodeASN1toCSV {
	my $senderName;
	my $collectionBeginTime;
	my $class;				# Identifier class
	my $pc;					# Identifier: primitive/contructed
	my $tag;				# Identifier tag
	my $measStartTime;
	my $granularityPeriod;
	my @counters;			# STS counter names
	my $measObjInstId;		# OT + object id
	my @countervalues;		# STS counter names
	my $suspectFlag;
	my $collectionEndTime;
	my @oneOTandBRP;		# Array of array with one OT during one BRP

	# Length of tags when definite length is used
	my $measDataCollectionLength;
	my $measDataSeqLength;
	my $measDataLength;
	my $measInfoSeqLength;
	my $measInfoLength;
	my $measValuesSeqLength;
	my $measValueLength;

	# Go through the entire ASN.1 structure
	# Important:
	#  getMeasDataCollection_taglength must be called first, sets $idx_global
	$measDataCollectionLength = getMeasDataCollection_taglength();
	$measDataCollectionLength += $idx_global if $measDataCollectionLength >= 0;
	getMeasFileHeader(\$senderName, \$collectionBeginTime);
	print "Debug.. senderName = $senderName\n\tcollectionBeginTime = $collectionBeginTime\n" if DEBUG or DEBUGDEEP;
	$measDataSeqLength = getMeasDataSeq_taglength();
	$measDataSeqLength += $idx_global if $measDataSeqLength >= 0;
	while (!sequenceEnded($measDataSeqLength)) {
		$measDataLength = getMeasData_taglength();
		$measDataLength += $idx_global if $measDataLength >= 0;
		getNEId();
		$measInfoSeqLength = getMeasInfoSeq_taglength();
		$measInfoSeqLength += $idx_global if $measInfoSeqLength >= 0;
		while (!sequenceEnded($measInfoSeqLength)) {
			$measInfoLength = getMeasInfo_taglength();
			$measInfoLength += $idx_global if $measInfoLength >= 0;
			$measStartTime = getASN1value(\$class, \$pc, \$tag);
			$measStartTime = fixTimeStamp($measStartTime);				# Remove Z is UTC
			$granularityPeriod = value2integer(getASN1value(\$class, \$pc, \$tag));
			if ($granularityPeriod != 300 && $granularityPeriod != 900 && $granularityPeriod != 3600) {
				warningmsg("incorrect BRP", $granularityPeriod);
			}
			$granularityPeriod /= 60;
			print "Debug.. BRP = $granularityPeriod\n\tmeasStartTime = $measStartTime\n" if DEBUG or DEBUGDEEP;
			getMeasTypes(\@counters);
			if (DEBUG or DEBUGDEEP) {
				print "Debug.. Counters:\n\t";
				foreach my $countername (@counters) {
					print "$countername ";
				}
				print "\n";
			}
			$measValuesSeqLength = getMeasValuesSeq_taglength();
			$measValuesSeqLength += $idx_global if $measValuesSeqLength >= 0;
			while (!sequenceEnded($measValuesSeqLength)) {
				$measValueLength = getMeasValue_taglength();
				$measValueLength += $idx_global if $measValueLength >= 0;
				$measObjInstId = getASN1value(\$class, \$pc, \$tag);
				print "Debug.. measObjInstId = $measObjInstId\n" if DEBUG or DEBUGDEEP;
				push (@countervalues, $measObjInstId);			# Save OT and object id
				getMeasResult(\@countervalues);
				if (DEBUG or DEBUGDEEP) {
					print "Debug.. Counter values:\n\t";
					foreach my $countervalue (@countervalues) {
						print "$countervalue ";
					}
					print "\n";
				}
				$suspectFlag = value2integer(getASN1value(\$class, \$pc, \$tag));
				print "Debug.. suspectFlag = $suspectFlag\n" if DEBUG or DEBUGDEEP;

				warningmsg("measValue has incorrect length, or ended prematurely", 1) if not sequenceEnded($measValueLength);
				if ($measValueLength < 0) {
					nextoctet(); nextoctet();		# EoC measValue h'0000 (indefinite length)
				}

				# Save counters in an array for the OT
				push (@oneOTandBRP, [ @countervalues ]);

				undef @countervalues;
			}

			warningmsg("measValues (seq) has incorrect length, or ended prematurely", 2) if not sequenceEnded($measValuesSeqLength);
			if ($measValuesSeqLength < 0) {
				nextoctet(); nextoctet();		# EoC measValue h'0000 (indefinite length)
			}

			warningmsg("MeasInfo has incorrect length, or ended prematurely", 2) if not sequenceEnded($measInfoLength);
			if ($measInfoLength < 0) {
				nextoctet(); nextoctet();		# EoC measValue h'0000 (indefinite length)
			}
		
			saveDataToFile(\@counters, \@oneOTandBRP, $measStartTime, $granularityPeriod, $senderName);

			undef @counters;
			undef @oneOTandBRP;
		}

		warningmsg("MeasInfo (seq) has incorrect length, or ended prematurely", 2) if not sequenceEnded($measInfoSeqLength);
		if ($measInfoSeqLength < 0) {
			nextoctet(); nextoctet();		# EoC measValue h'0000 (indefinite length)
		}

		warningmsg("MeasData has incorrect length, or ended prematurely", 2) if not sequenceEnded($measDataLength);
		if ($measDataLength < 0) {
			nextoctet(); nextoctet();		# EoC measValue h'0000 (indefinite length)
		}
	}

	warningmsg("MeasData (seq) has incorrect length, or ended prematurely", 2) if not sequenceEnded($measDataSeqLength);
	if ($measDataSeqLength < 0) {
		nextoctet(); nextoctet();		# EoC measValue h'0000 (indefinite length)
	}
	getMeasFileFooter(\$collectionEndTime);
	print "Debug.. collectionEndTime = $collectionEndTime\n" if DEBUG or DEBUGDEEP;
	warningmsg("MeasDataCollection has incorrect length, or ended prematurely", 2) if not sequenceEnded($measDataCollectionLength);
	if ($measDataCollectionLength < 0) {
		nextoctet(); nextoctet();		# EoC measValue h'0000 (indefinite length)
	}

}

###################################
# getMeasDataCollection_taglength #
###################################
# Read MeasDataCollection start - tag and length
# Return length
#
sub getMeasDataCollection_taglength {
	my $octet;
	my $subname = "getMeasDataCollection_taglength";
	
	$idx_global = -1;

	# b001x xxxx (h'30)
	$octet = nextoctet();
	errormsg ($subname, 1) if (getclass($octet) != CUNIVERSAL || getpc($octet) != CCONSTRUCTED);
	warningmsg ($subname, 2) if (gettag($octet) != 16);

	return taglength($subname);
}

#####################
# getMeasFileHeader #
#####################
sub getMeasFileHeader {
	my ($senderName_ref, $collectionBeginTime_ref) = @_;
	my $class;				# Identifier class
	my $pc;					# Identifier: primitive/contructed
	my $tag;				# Identifier tag
	my $octet;
	my $taglen;
	my $fileFormatVersion;
	my $senderType;
	my $vendorName;
	my $subname = "getMeasFileHeader";

	# MeasFileHeader start
	# b101x xxxx (h'A0)
	# tag=0, bxxx0 0000 (h'A0)
	$octet = nextoctet();
	errormsg ($subname, 1) if (getclass($octet) != CCONTEXTSPEC || getpc($octet) != CCONSTRUCTED);
	warningmsg ($subname, 2) if (gettag($octet) != 0);

	# Length
	$taglen = taglength($subname);
	$taglen += $idx_global if $taglen >= 0;

	# MeasFileHeader data...
	# fileFormatVersion has to be 1
	$fileFormatVersion = ord (getASN1value(\$class, \$pc, \$tag));
	warningmsg ($subname, 4) if ($fileFormatVersion != 1);

	# The exchange
	$$senderName_ref = getASN1value(\$class, \$pc, \$tag);

	# senderType not used for STS
	$senderType = getASN1value(\$class, \$pc, \$tag);
	warningmsg ($subname, 5) if ($senderType ne "");

	# vendorName is "Ericsson"
	$vendorName = getASN1value(\$class, \$pc, \$tag);
	warningmsg ($subname, 6) if ($vendorName ne "Ericsson");

	# collectionBeginTime
	$$collectionBeginTime_ref = getASN1value(\$class, \$pc, \$tag);

	# MeasFileHeader stop
	errormsg ($subname, 7) if not sequenceEnded($taglen);
	if ($taglen < 0) {
		nextoctet(); nextoctet();		# EoC measValue h'0000 (indefinite length)
	}
}


############################
# getMeasDataSeq_taglength #
############################
# Read MeasData sequence start - tag and length,
# will eventually end with h'0000
#
sub getMeasDataSeq_taglength {
	my $octet;
	my $subname = "getMeasDataSeq_taglength";
	
	# b101x xxxx (h'A1)
	# bxxx0 0001 (h'A1)
	$octet = nextoctet();
	errormsg ($subname, 1) if (getclass($octet) != CCONTEXTSPEC || getpc($octet) != CCONSTRUCTED);
	warningmsg ($subname, 2) if (gettag($octet) != 1);

	return taglength($subname);
}

############################
# getMeasData_taglength #
############################
# Read MeasData start - tag and length,
# will eventually end with h'0000
#
sub getMeasData_taglength {
	my $octet;
	my $subname = "getMeasData_taglength";
	
	# b001x xxxx (h'30)
	$octet = nextoctet();
	errormsg ($subname, 1) if (getclass($octet) != CUNIVERSAL || getpc($octet) != CCONSTRUCTED);
	warningmsg ($subname, 2) if (gettag($octet) != 16);

#	# Indefinite length, h'80
#	$octet = nextoctet();
#	errormsg ($subname, 3) if (bin2dec(octet2bin($octet)) != CINDEFLENGTH);

	return taglength($subname);
}

###########
# getNEId #
###########
sub getNEId {
	my $class;				# Identifier class
	my $pc;					# Identifier: primitive/contructed
	my $tag;				# Identifier tag
	my $octet;
	my $taglen;
	my $neUserName;
	my $neDistinguishedName;
	my $subname = "getNEId";

	# NEId start
	# b101x xxxx (h'A0)
	# tag=0, bxxx0 0000 (h'A0)
	$octet = nextoctet();
	errormsg ($subname, 1) if (getclass($octet) != CCONTEXTSPEC || getpc($octet) != CCONSTRUCTED);
	warningmsg ($subname, 2) if (gettag($octet) != 0);

	# Length
	$taglen = taglength($subname);
	$taglen += $idx_global if $taglen >= 0;

	# MeasFileHeader data...
	# neUserName is an empty string
	$neUserName = getASN1value(\$class, \$pc, \$tag);
	warningmsg ($subname, 4) if ($neUserName ne "");

	# neDistinguishedName is an empty string
	$neDistinguishedName = getASN1value(\$class, \$pc, \$tag);
	warningmsg ($subname, 5) if ($neDistinguishedName ne "");
	
	# NEId stop
	errormsg ($subname, 6) if not sequenceEnded($taglen);
	if ($taglen < 0) {
		nextoctet(); nextoctet();		# EoC measValue h'0000 (indefinite length)
	}
}

############################
# getMeasInfoSeq_taglength #
############################
# Read MeasInfo sequence start - tag and length,
# will eventually end with h'0000
#
sub getMeasInfoSeq_taglength {
	my $octet;
	my $subname = "getMeasInfoSeq_taglength";

	# b101x xxxx (h'A1)
	# bxxx0 0001 (h'A1)
	$octet = nextoctet();
	errormsg ($subname, 1) if (getclass($octet) != CCONTEXTSPEC || getpc($octet) != CCONSTRUCTED);
	warningmsg ($subname, 2) if (gettag($octet) != 1);

	return taglength($subname);
}

############################
# getMeasInfo_taglength #
############################
# Read MeasInfo start - tag and length,
# will eventually end with h'0000
#
sub getMeasInfo_taglength {
	my $octet;
	my $subname = "getMeasInfo_taglength";
	
	# b001x xxxx (h'30)
	$octet = nextoctet();
	errormsg ($subname, 1) if (getclass($octet) != CUNIVERSAL || getpc($octet) != CCONSTRUCTED);
	warningmsg ($subname, 2) if (gettag($octet) != 16);

	return taglength($subname);
}

################
# getMeasTypes #
################
# Read counter names
#
sub getMeasTypes {
	my ($counters_ref) = @_;
	my $class;				# Identifier class
	my $pc;					# Identifier: primitive/contructed
	my $tag;				# Identifier tag
	my $octet;
	my $taglen;
	my $subname = "getMeasTypes";
	my $foo;
	
	# b101x xxxx (h'A2)
	# bxxx0 0010 (h'A2)
	$octet = nextoctet();
	errormsg ($subname, 1) if (getclass($octet) != CCONTEXTSPEC || getpc($octet) != CCONSTRUCTED);
	warningmsg ($subname, 2) if (gettag($octet) != 2);

	$taglen = taglength($subname);
	$taglen += $idx_global if $taglen >= 0;

	while (!sequenceEnded($taglen)) {
		push (@{$counters_ref}, getASN1value(\$class, \$pc, \$tag));
	}
	if ($taglen < 0) {
		nextoctet(); nextoctet();		# EoC measValue h'0000 (indefinite length)
	}
}

##############################
# getMeasValuesSeq_taglength #
##############################
# Read MeasValues sequence start - tag and length,
# will eventually end with h'0000
#
sub getMeasValuesSeq_taglength {
	my $octet;
	my $subname = "getMeasValuesSeq_taglength";

	# b101x xxxx (h'A3)
	# bxxx0 0011 (h'A3)
	$octet = nextoctet();
	errormsg ($subname, 1) if (getclass($octet) != CCONTEXTSPEC || getpc($octet) != CCONSTRUCTED);
	warningmsg ($subname, 2) if (gettag($octet) != 3);

	return taglength($subname);
}

##########################
# getMeasValue_taglength #
##########################
# Read MeasValue start - tag and length,
# will eventually end with h'0000
#
sub getMeasValue_taglength {
	my $octet;
	my $subname = "getMeasValue_taglength";
	
	# b001x xxxx (h'30)
	$octet = nextoctet();
	errormsg ($subname, 1) if (getclass($octet) != CUNIVERSAL || getpc($octet) != CCONSTRUCTED);
	warningmsg ($subname, 2) if (gettag($octet) != 16);

	return taglength($subname);
}

#################
# getMeasResult #
#################
# Read counter values
#
sub getMeasResult {
	my ($countervalues_ref) = @_;
	my $class;				# Identifier class
	my $pc;					# Identifier: primitive/contructed
	my $tag;				# Identifier tag
	my $octet;
	my $taglen;
	my $value;
	my $subname = "getMeasResult";
	my $foo;
	
	# b101x xxxx (h'A1)
	# bxxx0 0001 (h'A1)
	$octet = nextoctet();
	errormsg ($subname, 1) if (getclass($octet) != CCONTEXTSPEC || getpc($octet) != CCONSTRUCTED);
	warningmsg ($subname, 2) if (gettag($octet) != 1);

	$taglen = taglength($subname);
	$taglen += $idx_global if $taglen >= 0;

	while (!sequenceEnded($taglen)) {
		$value = getASN1value(\$class, \$pc, \$tag);
		SWITCH: for ($tag) {	# A switch/case statment...
			/0/		&& do {
				push (@{$countervalues_ref}, value2integer($value));
				last; };
			/1/		&& do {
				warningmsg ("REAL values not implemented in $subname", 4);
				push (@{$countervalues_ref}, -1);
				last; };
			/2/		&& do {
				push (@{$countervalues_ref}, "");
				last; };
			# Default action
				warningmsg ("Tag $tag not allowed, $subname", 4);
				push (@{$countervalues_ref}, "");
		} # SWITCH..
	}
	if ($taglen < 0) {
		nextoctet(); nextoctet();		# EoC measValue h'0000 (indefinite length)
	}
}

#####################
# getMeasFileFooter #
#####################
sub getMeasFileFooter {
	my ($collectionEndTime_ref) = @_;
	my $class;				# Identifier class
	my $pc;					# Identifier: primitive/contructed
	my $tag;				# Identifier tag
	my $octet;
	my $subname = "getMeasFileFooter";

	$$collectionEndTime_ref = getASN1value(\$class, \$pc, \$tag);
}

##################
# saveDataToFile #
##################
sub saveDataToFile {
	my ($counters_ref, $oneOTandBRP_ref, $measStartTime, $granularityPeriod, $senderName) = @_;
	my @objinfo;			# [0] = object type, [1] = object id
	my $OTFILE;
	my $filename;

	# Check if file exists. If yes, append counters.
	# If no, create file, add header and counters
	@objinfo = split ('\.', $$oneOTandBRP_ref[0][0]);
	#$filename = CCSVDIR . "/" . $objinfo[0] . ".csv";
	my $replacefrom = ".asn1";
	my $replaceto = "_";
	$filename_global =~ s/$replacefrom/$replaceto/;

	$filename = $filename_global . $objinfo[0] . ".csv";
	print "*Parsed: $filename\n" if DEBUG or DEBUGDEEP;
	if (-e $filename) {
		open ($OTFILE, ">>$filename"); 
	} else {
		open ($OTFILE, ">$filename"); 
		print $OTFILE "Date_UTC,Time_UTC,Object_id,Perlen,Exchid";
		foreach my $countername (@$counters_ref) {
			print $OTFILE ",$countername";
		}
		print $OTFILE "\n";
	}

	for my $i ( 0 .. $#{$oneOTandBRP_ref} ) {
		# $oneOTandBRP[$i][0] = objecttype.objectid, split the string
		@objinfo = split ('\.', $$oneOTandBRP_ref[$i][0]);
		print $OTFILE substr ($measStartTime, 0, 4) . "-" . substr ($measStartTime, 4, 2) . "-" . 
				substr ($measStartTime, 6, 2) . ",";				# Date
		print $OTFILE substr ($measStartTime, 8, 2) . ":" . 
				substr ($measStartTime, 10, 4) . ",";				# Time
		print $OTFILE "$objinfo[1],";								# Object ID
		print $OTFILE "$granularityPeriod,$senderName";				# PERLEN and EXCHID
		for my $j ( 1 .. $#{$$oneOTandBRP_ref[$i]} ) {
			print $OTFILE ",$$oneOTandBRP_ref[$i][$j]";
		}
		print $OTFILE "\n";
	}
	close ($OTFILE);
}



################
# getASN1value #
################
sub getASN1value {
	my ($class_ref, $pc_ref, $tag_ref) = @_;
	my $octet;
	my $len;
	my $value;
	my $subname = "getMeasFileFooter";
	
	$octet = nextoctet();
	
	$$class_ref = getclass($octet);
	$$pc_ref = getpc($octet);
	$$tag_ref = gettag($octet);
	
	# Get length
	$len = taglength($subname);
	
	# Get value
	$value = getvalue($len);
	print "Debug deep.. class/pc/tag = $$class_ref/$$pc_ref/$$tag_ref, length = $len ($idx_global), value = $value\n" if DEBUGDEEP;

	return $value;
}

############
# getclass #
############
# Bit 6..7
# 0: Universal
# 1: Application
# 2: Context-specific
# 3: Private
sub getclass {
	return bin2dec(substr (octet2bin(shift), 0, 2));
}

#########
# getpc #
#########
# Bit 5
# 0: Primitive
# 1: Contructed
sub getpc {
	return substr (octet2bin(shift), 2, 1);
}

##########
# gettag #
##########
# Bits 0..4, unless value is h'1f, then
# tag > 30 - see X.690 8.1.2.4
sub gettag {
	(my $octet) = @_;
	my $value;
	
	$value = bin2dec(substr (octet2bin($octet), 3, 5));
	
	if ($value == 0x1f) {					# Tag number greater than 30
		# See X.690 8.1.2.4
		$value = "";						# Reset the value

		# Get tag octets and convert to binary. Use bits 1..7 as long as bit 8 = 1
		$octet = nextoctet();
		while (substr (octet2bin($octet), 0, 1) ne "0") {
			$value .= substr (octet2bin($octet), 1, 7);
			$octet = nextoctet();
		}
		$value = bin2dec($value);
	}

	return $value;
}

#############
# taglength #
#############
# Return number of octets of the tag.
# Return -1 if tag is of indefinite form
#
sub taglength {
	my ($subname) = @_;

	my $octet;
	my $taglen;
	my $lengthoctets;
	
	$taglen = 0;

	# Definite form
	#   Short form, see X.690 8.1.3.4
	#     B8 = 0
	#     B1..7 = length
	#   Long form, see X.690 8.1.3.5
	#     B8 = 1
	#     B1..7 = # of subsequent length octets (h'FF not used)
	# Indefinite form
	#   See X.690 8.1.3.6
	#     B8 = 1
	#     B1..7 = 0
	#     Content octets terminated by end-of-contents octets (h'00 h'00)
	$octet = nextoctet();
	if (bin2dec(octet2bin($octet)) == CINDEFLENGTH) {		# Indefinite form
		$taglen = -1;
	} elsif (bin2dec(octet2bin($octet)) < CINDEFLENGTH) {	# Definite form, short form
		$taglen = bin2dec(octet2bin($octet));
	} else {												# Definite form, long form
		$lengthoctets = bin2dec(octet2bin($octet)) - CINDEFLENGTH;
		while ($lengthoctets > 0) {
			$lengthoctets--;
			$octet = nextoctet();
			$taglen += bin2dec(octet2bin($octet)) * (256 ** $lengthoctets);
		}
	}
	return $taglen;
}

############
# getvalue #
############
sub getvalue {
	my ($len) = @_;
	my $value = "";

	if ($len < 0) {			# Indefinite length
		while (bin2dec(twooctets2bin(peekoctets(2))) != 0) {
			$value = $value . nextoctet();
		}
		nextoctet(); nextoctet();		# EoC h'0000
	} else {				# Definite length
		while ($len > 0) {
			$value = $value . nextoctet();
			$len = $len - 1;
		}
	}	

	return $value;
}

#################
# value2integer #
#################
sub value2integer {
	my ($octets) = @_;
	my $value = "";
	my $twoscomplement = "";

	# Convert to binary
	for (my $i = 0; $i < length ($octets); $i++) {
		$value .= octet2bin(substr ($octets, $i, 1));
	}
	# Convert to decimal
	if (substr ($value, 0, 1) eq "1") {				# Negative value
		$value = bin2dec($value) - 2**(8 * length ($octets));
	} else {										# Positive value
		$value = bin2dec($value);
	}

	return $value;
}

#################
# sequenceEnded #
#################
sub sequenceEnded {
	my ($seqLength) = @_;

	if ($seqLength < 0) {			# Indefinite length, next octets should be h'00 00
		return (bin2dec(twooctets2bin(peekoctets(2))) == 0);
	}
	if ($seqLength >= 0) {			# Definite length
		return ($idx_global == $seqLength);
	}
	return 0;						# Not yet ended
}

################
# fixTimeStamp #
################
sub fixTimeStamp {
	my ($timestamp) = @_;

	if (substr ($timestamp, -1) eq "Z") {
		$timestamp = substr ($timestamp, 0, length ($timestamp) - 1);
	}
	if ($timestamp =~ /.*[+,\-]/) {
		$timestamp = substr ($timestamp, 0, 12) . substr ($timestamp, length ($timestamp) - 5);
	} else {
		$timestamp = substr ($timestamp, 0, 12);
	}

	return $timestamp;
}

#############
# nextoctet #
#############
# Increases file index by 1
sub nextoctet {
	$idx_global += 1;
	if ($idx_global >= $filesize_global) {
		die ("End of file reached, index = $idx_global\n");
	}
	return substr ($file_global, $idx_global, 1);
}

###############
# peekoctets #
###############
# Returns coming octets without increasing file index
sub peekoctets {
	my ($numoctets) = @_;
	if ($idx_global + $numoctets >= $filesize_global) {
		warn ("End of file may be reached, index = " . $idx_global + $numoctets . "\n");
	}
	return substr ($file_global, $idx_global + 1, $numoctets);
}

#############
# octet2bin #
#############
sub octet2bin {
	my ($octet) = @_;
	return substr (unpack ("B16", pack ("n", ord ($octet))), 8, 8);
}

#############
# twooctets2bin #
#############
sub twooctets2bin {
	my ($octets) = @_;
	return octet2bin(substr($octets, 0, 1)) . octet2bin(substr($octets, 1, 1));
}

###########
# bin2dec #
###########
sub bin2dec {
	return oct ("0b" . shift);
}

############
# getFiles #
############
sub getFiles {
	my @files;
	my $i;

	@files = <*>;						# Get all files and folders
	$i = 0;
	while ($i < scalar @files) {
		if (-f $files[$i]) {
			$i++;						# Keep files
		} else {
			splice @files, $i, 1;		# Remove non-files from array
		}
	}

	return @files;
}

#####################
# setupCSVdirectory #
#####################
sub setupCSVdirectory {
	(my $dirname) = @_;

	if (-d $dirname) {
		# csv directory exists
		die ("Error: Directory $dirname has .csv files. Move or remove the files.\n") if folderhascsvfiles($dirname);
	} elsif (-e $dirname) {
		# csv file exists but not directory
		die ("Error: Cannot create directory $dirname since a file with that name already exists.\n");
	} else {
		# csv directory does not exist
		mkdir ($dirname, 0777) || die ("Error: Could not create directory $dirname\nCode: $!\n");
	}
}

#####################
# folderhascsvfiles #
#####################
sub folderhascsvfiles {
	my $dirname = shift;
	my @foo;
	@foo = <$dirname/*.csv>;
	return scalar @foo;
}

############
# errormsg #
############
sub errormsg {
	my ($errortext, $errorcode) = @_;

	die ("Error: ASN1 file may be incorrect ($errortext, error: $errorcode, index: $idx_global).");
}

##############
# warningmsg #
##############
sub warningmsg {
	my ($errortext, $errorcode) = @_;

	warn ("Warning: ASN1 file may be incorrect ($errortext, error: $errorcode, index: $idx_global).");
}


##################################################
# checkargv
#
# Checks the command line for flags etc.
#
sub checkargv {
  ($dirname, 								# Folder name with ASN1 files
  $createcsv,								# Flag to create csv folder
  $printhelp) = @_;
  
  my $flag;									# Temporary flag variable

  # Default values
  $printhelp = 0;							# Do not print help
  $createcsv = 1;							# Create csv folder
  $dirname = ".";

  my $i = 0;
  while ($ARGV[$i]) {						# Step through each argument on the command line
  	if (substr ($ARGV[$i], 0, 1) eq '-') {	# Check the flags
  		$flag = substr ($ARGV[$i], 1);		# Take whatever is after the first '-'

  		if 	($flag eq 'h' || $flag eq '-help') { $printhelp = !0; }
  		elsif 	($flag eq 'nc' || $flag eq '-nocsv') { $createcsv = 0; }
  		else {	# Doesn't recognize the flag
  			print "Error! Unknown flag: $flag\n\n";
  			print $0, " -h prints the help.\n";
  			exit;
  		}
  	} else {
		$dirname = $ARGV[$i];				# The folder name, the one without a '-' first
  	}
  	$i++;
  }
  if ($i == 0) { $printhelp = !0; }			# No arguments given, print help
  
  return;
}
##################################################


##################################################
# printhelp
#
# Prints help.
#
sub printhelp {
  print "Syntax:\n";
  print "\t", $0, " [flags] [ASN1 folder name]\n\n";

  print "Flags:\n";
  print "  -h,  --help\tThis help text\n";
  print "  -nc, --nocsv\tDo not create the csv folder (it already exists)\n";
  print "\n";
  print "If no folder name is specified, the current folder is used.\n";
  print "\n";
}
##################################################
