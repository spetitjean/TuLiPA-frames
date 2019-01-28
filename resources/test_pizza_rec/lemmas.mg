%% To be compiled with the lex compiler
%% xmg compile lex lemmas.mg --force
%% Or online: http://xmg.phil.hhu.de/index.php/upload/workbench

type CAT = {s, v, vp, np, n, adj}
type FAM = {
   n0V,
   n0Vn1,
   propernoun,
   commonnoun,
   adverb
}
type SEM = {
   FrameEat,
   FrameJohn,
   FramePizza
}

feature entry: string
feature gloss: string
feature cat  : CAT
feature fam  : FAM

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% VERBS
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
class LemmaEat
{
  <lemma> {
    entry <- "eat";
    sem   <- FrameEat; 
    cat   <- v;
    fam   <- n0Vn1
   }
}

class LemmaJohn
{
  <lemma> {
    entry <- "john";
    sem   <- FrameJohn;
    cat   <- n;
    fam   <- propernoun
  }
}

class LemmaPizza
{
  <lemma> {
    entry <- "pizza";
    sem   <- FramePizza;
    cat   <- n;
    fam   <- commonnoun
   }
}

class LemmaReally
{
  <lemma> {
    entry <- "really";
    sem   <- FrameReally;
    cat   <- adv;
    fam   <- adverb
   }
}



value LemmaEat
value LemmaJohn
value LemmaPizza
value LemmaReally

