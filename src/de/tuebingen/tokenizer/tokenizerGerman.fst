% FSA for tokenizing German
% erases punctuation

% keywords: epsilon space final start

% start
start 0

% final
final 1 2 4 6
 
% transitions
0 1 ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyzäöüÄÖÜß
1 1 0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyzäöüÄÖÜß-(){}[]
1 3 ._:/~ 
3 3 ._:/~ 
3 1 0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyzäöüÄÖÜß

0 4 0123456789 
4 4 0123456789
4 5 ,
4 1 -
5 4 0123456789
4 6 .

0 2 space epsilon
0 2 ,.!?; epsilon
0 2 _-"'/:&(){}[]#+*~$

