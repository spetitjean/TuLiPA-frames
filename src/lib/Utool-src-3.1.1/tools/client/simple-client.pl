use IO::Socket;


# open connection
$socket = IO::Socket::INET->new("localhost:2802")
    or die $!;


while(<>) {
    print $socket $_;
}
$socket->shutdown(1);

while(<$socket>) {
    print;
}

$socket->close();
