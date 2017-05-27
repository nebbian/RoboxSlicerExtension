#!/usr/bin/perl -i
#
# Post-processing script for changing slic3r Gcode into something more resembling Cura,
# which is more compatible with the Robox post processor.

use strict;
use warnings;

my $layerCount = 0;
my @lines;

my $inputFile = $ARGV[0];
my $tempFile = "$inputFile.tmp";

print $inputFile;

open(INPUT, "< $inputFile")         or die "can't open $inputFile: $!";
open(NEW, "> $tempFile")         or die "can't open $tempFile: $!";

while (<INPUT>) {
	if (/^(;LAYER:)/) {
		$layerCount++;
	}
}

seek INPUT, 0, 0;

my $MIN_EXTRUSION_LENGTH = 0.3;		# Minimum extrusion length before we will allow a retraction
my $MIN_TRAVEL_DISTANCE = 0.01;  # Minimum distance of travel before we actually take the command seriously

my $oldHint = '';
my $hint = '';
my $currentX = 0.00;
my $currentY = 0.00;
my $currentZ = 0.00;
my $commandDistance = 0;
my $outputZ = 0;
my $layerChange = 0;
my $currentSpeed = 0;
my $retractCount = 0;
my $travelMoveLastFileSize = 0;
my $lastMoveWasTravel = 0;
my $lastMoveWasRetract = 0;
my $extrusionAfterRetraction = 0;

while (<INPUT>) {

	if (/(X([\-0-9\.]+)\s+Y([\-0-9\.]+))/){
		my $newX = $2;
		my $newY = $3;
		
		$commandDistance = sqrt((($newX - $currentX)**2) + (($newY - $currentY)**2));
	}

	if (/^(M204\s+S(\d+))/) {
		printf NEW "M201 X%d Y%d Z%d E2000\n", $2, $2, $2;
	} elsif (/^(M190\s)/) {
		# Remove bed temperature settings
	} elsif (/^(M104\s)/) {
		# Remove nozzle temperature settings
	} elsif (/^(M109\s)/) {
		# Remove set temperature and wait
	} elsif (/^(G21\s)/) {
		# Remove set units to mm
	} elsif (/^(G90\s)/) {
		# Remove use absolute coordinates
	} elsif (/^(M83\s)/) {
		# Remove use relative distances for extrusion
	} elsif (/^(G1\s+Z([0-9\.]+)\s+)/){
		# Layer change code
		$currentZ = $2;
		$outputZ = 1;
		$layerChange = 1;
		$oldHint = "";
	} elsif (/^(;LAYER:0)/) {
		# Output the layer count
		printf NEW ";Layer count: %d\n", $layerCount;
		print NEW $_;
		$extrusionAfterRetraction = 0;
	} elsif (/^(;LAYER:)/) {
		# Output the layer number
		print NEW $_;
		#$extrusionAfterRetraction = 0;
	} elsif (/^(G1\s+E([\-0-9\.]+)\s+F([0-9]+))/){
		#retraction/unretraction
		
		my $extrusion = $2;
		my $feedrate = $3;
		# Don't print travel moves before a retraction
		if(($lastMoveWasTravel > 0) && ($extrusion < 0)){
			seek(NEW, $travelMoveLastFileSize, 0);
		}

		# Ensure that we remove the first retract/unretract pair
		# Ensure that we leave enough room for slowly closing a nozzle before retracting
		if($extrusionAfterRetraction > $MIN_EXTRUSION_LENGTH){
			printf NEW "G1 F%s E%s\n", $feedrate, $extrusion;

			$retractCount ++;
		
			if($extrusion > 0){
				$extrusionAfterRetraction = 0;
			}
			$lastMoveWasRetract = 2;
		}

	} elsif (/^(G1\s+F([\-0-9]+))/){
		$currentSpeed = $2;
	} elsif (/^(G1\s+X([\-0-9\.]+)\s+Y([\-0-9\.]+)\sF([0-9]+))/){
		# Show travel moves as G0
		
		# Don't repeat travel moves
		if($lastMoveWasTravel > 0){
			seek(NEW, $travelMoveLastFileSize, 0);
		}
		else {
			$travelMoveLastFileSize = tell(NEW);
		}
		
		if($commandDistance < $MIN_TRAVEL_DISTANCE){
			# Ignore, as this distance is too small for the postprocessor to handle
		}
		else {
		
			if(($lastMoveWasRetract == 0) && ($outputZ == 0)){
				if($outputZ == 1){
					printf NEW "G1 F%s X%s Y%s Z%s\n", $4, $2, $3, $currentZ;
					$outputZ = 0;
					$extrusionAfterRetraction = 0;
				}
				else {
					printf NEW "G1 F%s X%s Y%s E0.00\n", $4, $2, $3;
					$lastMoveWasTravel = 2;
				}
			}
			else {

				if($outputZ == 1){
					printf NEW "G0 F%s X%s Y%s Z%s\n", $4, $2, $3, $currentZ;
					$outputZ = 0;
				}
				else {
					printf NEW "G0 F%s X%s Y%s\n", $4, $2, $3;
				}
				$lastMoveWasTravel = 2;
		
				# Reset current extrusion length if we've just travelled, ignore if travel is part of retraction'
				if($lastMoveWasRetract == 0){
					$extrusionAfterRetraction = 0;
				}
			}
		}
	} elsif (/^(G1\s+X([\-0-9\.]+)\s+Y([\-0-9\.]+)\s+E([\-0-9\.]+)\s+;\s+(\w+))/){
		# Output hints as to what is going on
		if($5 eq "brim") { $hint = "SKIRT";}
		if($5 eq "perimeter") { $hint = "WALL-OUTER"; }
		if($5 eq "infill") { $hint = "SKIN"; }

		if($hint ne $oldHint){
			print NEW ";TYPE:$hint\n";
			$oldHint = $hint;
		}
		
		if(($2 == $currentX) && ($3 == $currentY)){
			# Don't output zero distance moves
		}
		else {
		
			if($currentSpeed == 0){
				printf NEW "G1 X%s Y%s E%s \n", $2, $3, $4;
			}
			else {
				printf NEW "G1 F%s X%s Y%s E%s \n", $currentSpeed, $2, $3, $4;
				$currentSpeed = 0;
			}
			$extrusionAfterRetraction += $4;
		}		
	} else {
		print NEW $_;
	}
	
	# Save the current position
	if (/(X([\-0-9\.]+)\s+Y([\-0-9\.]+))/){
		$currentX = $2;
		$currentY = $3;
	}

	if($lastMoveWasTravel > 0){
		$lastMoveWasTravel --;
	}
	if($lastMoveWasRetract > 0){
		$lastMoveWasRetract --;
	}
}


close(INPUT)                  or die "can't close $inputFile: $!";
close(NEW)                  or die "can't close $tempFile: $!";
rename($inputFile, "$inputFile.orig")   or die "can't rename $inputFile to $inputFile.orig: $!";
rename($tempFile, $inputFile)          or die "can't rename $tempFile to $inputFile: $!";


