#!/usr/bin/perl -w
## Wikipedia Atricle Link Count
## Input: directory path to the extracted articles from Wikipedia dump in JSON format with "text" and "url" fields
## Output: Two column file: <curid> <context of the link>

use strict;
use warnings;
use utf8;

use File::Basename;
use File::Find;
use JSON;
use Lingua::Sentence;
use Path::Iterator::Rule;

my $sentenceSplitter = Lingua::Sentence->new("en");

my ($dirPath) = $ARGV[0];
my ($outDirPath) = $ARGV[1];
my $rule = Path::Iterator::Rule->new;

if (not defined $outDirPath) {
  die "Usage $0 <dir-path-extracted-wiki-with-json> <extracted-dir-path>\n";
}

=for comment
### to access all files in a directory and subdirectory
# Approach 1: (unable to traverse in sub-directories)
foreach my $inFile (glob("$dirPath/*")) {
    printf "%s\n", $inFile;
}
# Approach 2:
for my $inFile ( $rule->all( $dirPath ) ) {
    if(!-d $inFile) {
        printf "%s\n", $inFile;
    }
}

exit;
=cut

for my $inFile ( $rule->all( $dirPath )) {
    if(!-d $inFile) {
        printf "%s\n", $inFile;
        open(my $in, $inFile) || die "Can not open $inFile\n";
        my $filename = basename($inFile);
        my $outFile =$outDirPath.$filename;
        open(my $out, ">$outFile") || die "Can not open $outFile\n";
        while(my $line = <$in>)
        {
            chomp($line);
            my %parsedJSON = %{decode_json($line)};

            my $articleContent = $parsedJSON{'text'};
            my $articleURL = $parsedJSON{'url'};
            $articleURL =~ s/https\:\/\/en.wikipedia.org\/wiki\?curid=//g;
            printf "curid: %s\n", $articleURL;

            ### if want to print curid in the output file as well, uncomment following line
            # print $out $articleURL,"\n";

            $articleContent =~ s/\\n\\n/\\n/g;
            my @text = $sentenceSplitter->split_array($articleContent);

            my $hrefCount = 0;

            #process each line, check for reference and capture context
            for(my $i=0; $i<=$#text;$i++)
            {
                (my @refs) = $text[$i] =~ m/<a href=\"(.+?)\">/g;
                if($#refs >= 0) #at least one match
                {
                    my $context = $text[$i];
                    if($i > 0 ){$context = $text[$i-1]." ".$context;}
                    if($i < $#text){$context = $context." ".$text[$i+1];} 
                    $context =~ s/<a href.*?\">/ /g;
                    $context =~ s/<\/a>//g;
                    $context =~ s/\s+/ /g;
                    foreach(@refs)
                    {   binmode($out, ":utf8");
                        print $out lc($_),"\t",$context,"\n";
                        $hrefCount++;
                    }
                }   
            }
            # printf "%d\n", $hrefCount;
        }
        close($in);
        close($out);
    }
}    
