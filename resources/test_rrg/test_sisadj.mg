type CAT  = {CL,CO,RP,NUC,V,ADV,Mary,entered,theRoom,quickly}
type MARK = {subst,flex,star}

property mark : MARK

feature cat : CAT

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




value Mary
value theRoom
value entered
value quickly
