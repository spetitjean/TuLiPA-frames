
use strict;
use Getopt::Std;
use IO::Socket;

use vars qw[
  $usage
  $command
  %options
  $incodec
  $outcodec
  $socket
  $input
  $answer
];

$usage = << "EOF";
usage: utool classify -I <codec> 
   or: utool solvable -I <codec>
   or: utool solve -I <codec> -O <codec> 
   or: utool display -I <codec>
EOF

$command = shift
  or die $usage;

$command =~ /^classify|solve|solvable|display$/
  or die $usage;

# parse command line options
getopts('I:O:', \%options);

$incodec = $options{'I'}
  or die $usage;

$outcodec = $options{'O'};

foreach my $file (@ARGV) {

  # open connection
  $socket = IO::Socket::INET->new("localhost:2802")
    or die $!;

  open(INPUT, $file) or die "Couldn't open $file: $!\n";

  # read input
  $input = join('', <INPUT>);

  close(INPUT);

  # encode entities
  $input =~ s[%.*][]g;
  $input =~ s[\n][ ]g;
  $input =~ s[&][&amp;]sg;
  $input =~ s["][&quot;]sg;
  $input =~ s['][&apos;]sg;
  $input =~ s[<][&lt;]sg;
  $input =~ s[>][&gt;]sg;

  # send stuff to server
  if (defined $outcodec) {
    print $socket "<utool cmd='$command' output-codec='$outcodec'>\n";
  } else {
    print $socket "<utool cmd='$command'>";
  }
  print $socket "<usr name='$file' codec='$incodec' string='$input'/>";
  print $socket "</utool>";

  $socket->shutdown(1);

  while( <$socket> ) {
      $answer .= $_;
  }

  if ($command eq "classify") {
    my ($code) = ($answer =~ /code='(\d+)'/);
    
    print "$file\t$code\n";
  }
  elsif ($command eq "solvable") {
    my ($solvable) = ($answer =~ /solvable='(.*?)'/);
    
    print "$file\t$solvable\n";
  }
  elsif ($command eq "solve") {
    while ($answer =~ /<solution string='(.*?)'\s*\/>/sg) {
      my $solution = $1;

      $solution =~ s/&apos;/\'/g;

      print "$solution\n";
    }    
  }

  # close socket
  $socket->close(); 
}
