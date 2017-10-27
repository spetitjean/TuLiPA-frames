type MARK = {subst, subst, nadj, foot, anchor, coanchor, flex}
type CAT = {np,n,v,vp,s,adv,pp,p,by,strong,really}
type PHON = {e}

type LABEL!

property mark : MARK

feature cat : CAT
feature phon : PHON
feature arg0 : LABEL
feature arg1 : LABEL
feature i : LABEL
feature e : LABEL

frame-types = {event, psych_state, activity, love, sleep, entity, person}
frame-constraints = { 
	activity -> event,
	psych_state event -> -,
	entity event -> -,
	entity psych_state -> -,
	love -> psych_state,
	sleep -> activity,
	person -> entity
}

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% TREE FRAGMENTS:
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

class Subject
export ?VP ?S ?SubjNP ?SubjMark
declare ?S ?VP ?SubjNP ?SubjMark ?A
{ <syn> {
	node ?S [cat=s] {
		node ?SubjNP (mark=SubjMark) [cat=np, i=?A]
		node ?VP [cat=vp]
	}
  };
  <iface>{[arg0=?A]}
}


class VerbProjection
export ?VP ?V ?F
declare ?VP ?V ?F ?EV
{ <syn> {
	node ?VP [cat=vp, e=?EV] {
		node ?V (mark=anchor) [cat=v]
	}
  };
    <iface>{[e=?EV]}

}

class Object
export ?VP ?V ?ObjNP ?ObjMark
declare ?VP ?V ?ObjNP ?ObjMark ?I
{ <syn> {
	node ?VP [cat=vp] {	
		node ?V [cat=v]
		node ?ObjNP (mark=ObjMark) [cat=np,i=?I]
	}
  };
  <iface>{[arg1=?I]}
}



%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% TREE TEMPLATES:
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%


class alphanx0V
import Subject[] VerbProjection[]
declare ?F ?X
{
  ?SubjMark=subst;
    <iface>{[cat=v, arg0=?X]};	
    <frame>{?F[sleep,  	     		
             actor:?X]}
}


class alphanx0Vnx1_mark
import Subject[] VerbProjection[] Object[]

class alphanx0Vnx1
import alphanx0Vnx1_mark[]
declare ?X ?Y ?F
{
	?SubjMark=subst;
	?ObjMark=subst;
  <iface>{[cat=v, e=?F, arg0=?X, arg1=?Y]};	
  <frame>{?F[love,
             actor:?X,
	     target:?Y]}
}



class propernoun
declare ?NP ?N ?X0 ?X1
{
  <syn>{
    node ?NP [cat=np, i=?X0];
    node ?N (mark=anchor) [cat=n, i=?X1];
    ?NP -> ?N
  };
    <frame>{?X0[person,
	name: ?X1]}
}

class adverb
declare ?X0
{
  <syn>{
	node [cat=vp, e=?X0]{
	  %% I wanted to put a flex node here, but TuLiPA does not like it for some reason
	  node [cat=adv]{ node (mark=flex) [cat=really] }
	  node (mark=foot)[cat=vp]
	  }
  };
  <frame>{?X0[psych-state,
              intensity:strong]}
}

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% TREE FAMILIES:
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

class n0V
{
  alphanx0V[]

}

class n0Vn1
{
  alphanx0Vnx1[]

}


%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% EVALUATION:
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

value n0V
value n0Vn1
value propernoun
value adverb
