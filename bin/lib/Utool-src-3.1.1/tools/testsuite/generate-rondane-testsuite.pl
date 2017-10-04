use IO::Socket;


sub convert {
    my $id = shift;
    my $usr = shift;

    # encode entities
    $usr =~ s[%.*][]g;
    $usr =~ s[\n][ ]g;
    $usr =~ s[&][&amp;]sg;
    $usr =~ s["][&quot;]sg;  #"
    $usr =~ s['][&apos;]sg; #' 
    $usr =~ s[<][&lt;]sg;
    $usr =~ s[>][&gt;]sg;


    my $socket = IO::Socket::INET->new("localhost:2802")
    or die $!;
    
    print $socket "<utool cmd='convert' output-codec='domgraph-codegen'>\n";
    print $socket "  <usr codec='domcon-oz' string='$usr' />\n";
    print $socket "</utool>\n";
    $socket->shutdown(1);

    my $answer = join(' ', <$socket>);
    close $socket;

#	      print STDERR "Vorher: -$answer-\n";

    $answer =~ s/^.*makeGraph1/public void makeGraph$id/s;
    $answer =~ s/\}.*$/ \}/s;
    $answer =~ s/&quot;/\"/g;

#	      print STDERR "Nachher: -$answer-\n";
#	      exit(0);

    return $answer;
}



opendir(DIR, ".") || die "$!";
#@mrsfiles = grep { /\.mrs\.pl$/ } readdir(DIR);
@mrsfiles = grep { /\.clls$/ } readdir(DIR);
closedir DIR;


print <<'END';
/*
 * @(#)RondaneGraphs.java created 26.06.2006
 * 
 * Copyright (c) 2006 Alexander Koller
 *  
 */

package de.saar.chorus.domgraph.regression.rondane;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import de.saar.chorus.domgraph.graph.*;

public class RondaneGraphs {
    Map<Integer,Method> methods;
    
    public RondaneGraphs() {
        Class c = RondaneGraphs.class;
        Method[] allMethods = c.getDeclaredMethods();
        
        methods = new HashMap<Integer,Method>();
        for( Method m : allMethods ) {
            if( m.getName().startsWith("makeGraph")) {
                String name = m.getName().substring(9);
                Integer id = Integer.parseInt(name);
                methods.put(id, m);
            }
        }
    }
    
    public Set<Integer> getIds() {
        return methods.keySet();
    }
    
    public void getGraph(int id, DomGraph graph, NodeLabels labels) throws Exception {
        if( methods.containsKey(id)) {
            methods.get(id).invoke(this, graph, labels);
        } else {
            throw new Exception("Couldn't find a graph with id " + id);
        }
    }
END


for $filename (@mrsfiles) {
    print STDERR "Processing $filename ... \n";
    ($sentid) = ($filename =~ /^(\d+)/);

    open MRSFILE, $filename or die "$!";
    $code = convert($sentid, join('', <MRSFILE>));
    close MRSFILE;

    print "$code\n";
}

print <<'EOP';
} // class
EOP


    
