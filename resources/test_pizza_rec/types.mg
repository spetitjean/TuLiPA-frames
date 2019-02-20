%% This file defines the types used both in the syntax and the semantic metagrammars
%% It is imported by both files (include types.mg)
%% To generate the type hierarchy, use the option --more when compiling

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

frame-types = {event, activity, eat, entity, person, food, pizza, psych-state}
frame-constraints = { 
	activity -> event,
	entity event -> -,
	eat -> activity,
	person -> entity,
	food -> entity,
	food person -> -,
	pizza -> food
}
