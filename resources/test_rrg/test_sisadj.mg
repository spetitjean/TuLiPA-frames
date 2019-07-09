# declaring the set of possible node labels (categories)
type CAT  = {CL,CO,RP,NUC,V,ADV,Mary,entered,theRoom,quickly}

# declaring the set of possible node marks (node types)
type MARK = {subst,flex,star,ddaughter}

property mark : MARK

feature cat : CAT

# classes theRoom and Mary describe trees with indention and bracketing. No variables are involved.
# classes entered and quickly describe trees by declaring node variables and constraining the node ordering 
# by precedence and domincnace constraints.

class Mary
{ <syn>{
	node [cat=RP]{
	     node (mark=flex) [cat=Mary]
	}
    }
}

class theRoom
{ <syn>{
	node [cat=RP]{
	     node (mark=flex) [cat=theRoom]
	}			
    }
}

class entered
declare ?Cl ?Co ?Nuc ?Ve ?ANCH ?RPS ?RPO
{ <syn>{
  node ?Cl [cat=CL];
  node ?Co [cat=CO];
  node ?Nuc [cat=NUC];
  node ?Ve [cat=V];
  node ?ANCH (mark=flex)[cat=entered];
  node ?RPS (mark=subst)[cat=RP];
  node ?RPO (mark=subst)[cat=RP];
  ?Cl -> ?Co; 
  ?Co -> ?RPS; ?Co -> ?Nuc; ?Co -> ?RPO; ?RPS >> ?Nuc; ?Nuc >> ?RPO;
  ?Nuc -> ?Ve; ?Ve -> ?ANCH  
  }
}

class quickly
declare ?Co ?Adv ?Anch
{ <syn>{
  node ?Co (mark=star)[cat=CO];
  node ?Adv [cat=ADV];
  node ?Anch (mark=flex)[cat=quickly];
  ?Co -> ?Adv; ?Adv -> ?Anch
  }
}


# all described classes are evaluated. No classes are reused, no metagrammatical factorization happens.
value Mary
value theRoom
value entered
value quickly
