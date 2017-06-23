#!/usr/bin/perl -i
#
# Post-processing script for changing slic3r Gcode into something more resembling Cura,
# which is more compatible with the Robox post processor.

use strict;
use warnings;
use Math::Round;

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

my $MIN_EXTRUSION_LENGTH = 0.0;		# Minimum extrusion length before we will allow a retraction
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
my $totalExtrusion = 0;
my $retracted = 0;


my $commandX = 'false';
my $commandY = 'false';
my $commandZ = 'false';
my $commandE = 'false';
my $commandSpeed = 'false';
my $comment = 'false';

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
	} elsif (/^(G1\s+)/){ 
			
		my $outputCommand = "";		
	
		# Grab all possible commands
		if(/(X([\-0-9\.]+)\s)/) {$commandX = $2} else {$commandX = 'false'}
		if(/(Y([\-0-9\.]+)\s)/) {$commandY = $2} else {$commandY = 'false'}
		if(/(Z([\-0-9\.]+)\s)/) {$commandZ = $2} else {$commandZ = 'false'}
		if(/(E([\-0-9\.]+)\s)/) {$commandE = $2} else {$commandE = 'false'}
		if(/(F([\-0-9\.]+)\s)/) {$commandSpeed = round($2)} else {$commandSpeed = 'false'}
		if(/;\s+(.+)$/) {$comment = $1} else {$comment = 'false'}
	
		# Output hints
		if($comment eq "brim") { $hint = "SKIRT";}
		if($comment eq "skirt") { $hint = "SKIRT";}
		if($comment eq "perimeter") { $hint = "WALL-OUTER"; }
		if($comment eq "infill") { $hint = "SKIN"; }
		if($comment eq "support material") { $hint = "SUPPORT"; }
		if($comment eq "support material interface") { $hint = "SUPPORT"; }

		if($hint ne $oldHint){
			print NEW ";TYPE:$hint\n";
			$oldHint = $hint;
			# last;	# Break out of the if statement
		}
		
		# Figure out if this is a travel move or not
		if($commandE ne 'false'){
			$outputCommand = 'G1';	# Printing move
		}
		else {
			$outputCommand = 'G0';	# Travel move
		}
	
		### Build command for output
		
		# Output speed if present
		if($commandSpeed ne 'false'){
			$currentSpeed = $commandSpeed;
		}

		# Remember the Z position if required
		if($commandZ ne 'false'){
			
			$currentZ = $commandZ;
			$oldHint = "";
		}
		
		# Output the X and Y position if required
		if(($commandX ne 'false') && ($commandY ne 'false')){
			$outputCommand .= sprintf " F%s X%s Y%s Z%s", $currentSpeed, $commandX, $commandY, $currentZ;
		}
		
		# 
		if($commandE ne 'false'){
			#Find retract/unretract
			if(($commandX eq 'false') && ($commandY eq 'false')){
				# Retract/unretract
				
				# Print extrusion if not straight after the first layer change
				if($totalExtrusion > 0){
					$outputCommand .= sprintf " E%s", $commandE;
				}
			}
			else {
				# Normal print move
				$outputCommand .= sprintf " E%s", $commandE;
			}
			$totalExtrusion += $commandE;
			
		}
		
		
		## Send the command to the file
		if(length($outputCommand) > 2){
			printf NEW "%s\n", $outputCommand;
		}	
	} elsif (/^(;LAYER:0)/) {
		# Output the layer count
		printf NEW ";Layer count: %d\n", $layerCount;
		print NEW $_;
		$extrusionAfterRetraction = 0;
	} elsif (/^(;LAYER:)/) {
		# Output the layer number
		print NEW $_;
		#$extrusionAfterRetraction = 0;	
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


