%% To be compiled with the mph compiler
%% xmg compile mph morphs.mg --force
%% Or online: http://xmg.phil.hhu.de/index.php/upload/workbench

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

class MorphLoves
{
  <morpho> {
    morph <- "loves";
    lemma <- "love";
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

class MorphMary
{
  <morpho> {
    morph <- "Mary";
    lemma <- "mary";
    cat   <- n
   }
}

class MorphSleeps
{
  <morpho> {
    morph <- "sleeps";
    lemma <- "sleep";
    cat   <- v;
    mode <- ind;
    pers <- 3;
    num <- sg
   }
}


%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% VALUATION
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

value MorphLoves
value MorphPizza
value MorphJohn
value MorphMary
value MorphSleeps
