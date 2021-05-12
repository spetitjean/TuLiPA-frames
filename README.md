# TuLiPA-frames

This is an extension for the existing [TuLiPA](https://sourcesup.cru.fr/tulipa/) (Tuebingen Linguistic Parsing Architecture), a parser for Lexicalised Tree Adjoining Grammars. 
It is extended with semantic frames paired with the elementary trees. 

Documentation on the parser and the resources is provided in the [Wiki](https://github.com/spetitjean/TuLiPA-frames/wiki).

## Cite
If you use TuLiPA-frames, please cite [this paper](http://www.lrec-conf.org/proceedings/lrec2018/summaries/567.html) for attribution:

Arps, D. & Petitjean, S. (2018), A Parser for LTAG and Frame Semantics. In: N. Calzolari, K. Choukri, C. Cieri, T. Declerck, S. Goggi, K. Hasida, H. Isahara, B. Maegaard, J. Mariani, H. Mazo, A. Moreno, J. Odijk, S. Piperidis & T. Tokunaga, eds, ‘Proceedings of the Eleventh International Conference on Language Resources and Evaluation (LREC 2018)’, European Language Resources Association (ELRA), Paris, France.

## Generalized wrapping

### Predict-wrapping stays the same

Old Complete-wrapping:

Antecedents:
- $[γ,eps_T,i,j,Γ1◦<<f1,f2,Y>>◦Γ2,ws?]$
- $[α,(p·m)⊥,f1,f2,Γ3,yes]$

Side conditions:
- label(α,p·m) = Y
- label(γ,eps) = label(α,p)

Consequent:
- $[α,(p·m)⊥,i,j,Γ1◦Γ3◦Γ2,no]$

### Generalized CW:

Antecedents:
- $[γ,q_T,i,j,Γ1◦<<f1,f2,Y>>◦Γ2,ws?]$
- $[α,m_⊥,f1,f2,Γ3,yes]$

Side conditions:
- $m$ is an integer (i.e. $α(m)$ is the daughter of the root of $α$)
- label(γ,q) = label(α,m)
- label(α,m) = Y

Consequents:
- $[α,m_⊥,i,j,Γ1◦Γ3◦Γ2,no]$

### Finish Generalized CW

A new rule is necessary: After going to the root of the wrapping tree, jump back to the target tree of the wrapping and continue going upwards

Antecedents:
- $[α, eps, TOP, i, j, Γ1, false]$
- $[γ,q_T,i,j,Γ1◦<<f1,f2,Y>>◦Γ2,ws?]$










