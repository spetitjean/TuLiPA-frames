%% To be compiled with the mph compiler
%% xmg compile mph morphs.mg

type CAT = {v, sv, sn, sp, p, adj}
type GEN = {m, f}
type FAM = {
  IntransitifActif
}

feature morph: string
feature lemma: string
feature cat  : CAT
feature fam  : FA
feature gen  : GEN

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% VERBS
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

class MorphEats
{
  <morpho> {
    morph <- "eats";
    lemma <- "eat";
    cat   <- v;
    mode <- ind;
    pers <- 3;
    num <- sg
   }
}

class MorphPizza
{
  <morpho> {
    morph <- "pizza";
    lemma <- "pizza";
    cat   <- n
   }
}

class MorphJohn
{
  <morpho> {
    morph <- "John";
    lemma <- "john";
    cat   <- n
   }
}



%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% VALUATION
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

value MorphEats
value MorphPizza
value MorphJohn
