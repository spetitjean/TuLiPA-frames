type MARK = {subst, subst, nadj, foot, anchor, coanchor, flex}
type CAT = {np,n,v,vp,s,adv,pp,p,by,strong}
type PHON = {e}
type name = {John}

type LABEL!

property mark : MARK

feature cat : CAT
feature phon : PHON
feature arg0 : LABEL
feature arg1 : LABEL
feature i : LABEL
feature e : LABEL

frame-types = {event, activity, eat, entity, person, dish, pizza}
frame-constraints = { 
	activity -> event,
	entity event -> -,
	eat -> activity,
	person -> entity,
	dish -> entity,
	dish person -> -,
	pizza -> dish
}

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% FRAMES:
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

class FramePizza
declare ?X0
{
  <frame>{
    ?X0[pizza]
  };
  <iface>{
    [i=?X0]
  }
}


class FrameJohn
declare ?X0
{
  <frame>{
    ?X0[person,
      name: John]
  };
  <iface>{
    [i=?X0]
  }
}

class FrameEat
declare ?X0
{
  <frame>{
    ?X0[eat,
      target: [food]]
  };
  <iface>{
    [e=?X0]
  }
}

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% EVALUATION:
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

value FramePizza
value FrameJohn
value FrameEat
