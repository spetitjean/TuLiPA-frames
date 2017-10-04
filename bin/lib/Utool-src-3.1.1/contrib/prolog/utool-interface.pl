%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%%   $RCSfile: utool-interface.pl,v $
%%  $Revision: 1.1 $
%%      $Date: 2006/09/18 08:22:44 $
%%     Author: Stefan Mueller (Stefan.Mueller@cl.uni-bremen.de)
%%    Purpose: Communication with the scope resolver/USR display utool
%%   Language: Prolog
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

:- use_module(library(xml)).

% Command =
%     solve        Solve an underspecified description.
%     solvable     Check solvability without enumerating solutions.
%     convert      Convert underspecified description from one format to another.
%     classify     Check whether a description belongs to special classes.
%     display      Start the Underspecification Workbench GUI.
%     server       Start Utool in server mode.
%     help         Display help on a command.

% Name = String to be displayed in the GUI

% InCodec = mrs-prolog, ...

% InString = 'psoa(h1,e2,
%   [ rel('prop-or-ques_m',h1,
%         [ attrval('ARG0',e2),
%           attrval('MARG',h3),
%           attrval('PSV',u4),
%           attrval('TPC',u5) ]),
%     rel('_every_q',h6,
%         [ attrval('ARG0',x7),
%           attrval('RSTR',h9),
%           attrval('BODY',h8) ]),
%     rel('_dog_n_1',h10,
%         [ attrval('ARG0',x7) ]),
%     rel('_bark_v_1',h11,
%         [ attrval('ARG0',e2),
%           attrval('ARG1',x7) ]) ],
%   hcons([ qeq(h3,h11), qeq(h9,h10) ]))'


utool_request(Command,Name,InCodec,InString,OutCodec,Result,ErrorMessage) :-
  Hostname=localhost,
  Port=2802,
  socket('AF_INET',Socket),
  catch(socket_connect(Socket,'AF_INET'(Hostname,Port),UStream),
        system_error('socket_connect/3: ''connect'' failed: Connection refused'),
        (format(user_error,'~N**Warning: Connection refused to port ~w of host ~w.~nUtool not running in server mode?~nTrying to start utool. Calling "utool server" ...~n~n',[Port,Hostname]),
            catch((exec('utool server',[null,null,null],_Pid),
                   % wait some time till java is ready for socket connections
                   sleep(1),
                   socket_connect(Socket,'AF_INET'(Hostname,Port),UStream)),
                   system_error('socket_connect/3: ''connect'' failed: Connection refused'),
                   (format(user_error,'~N**Warning: Starting utool and reconnecting failed.~n',[]),
                       fail)))),

  (OutCodec = '' ->
%   format(user_error,'<utool cmd="~w">~n',[Command]),
    format(UStream,   '<utool cmd="~w">~n',[Command])
  ; format(UStream,   '<utool cmd="~w" output-codec="~w">~n',[Command,OutCodec])
  ),

  name(InAtom,InString),
%  format(user_error,'<usr codec="~w" string="~w"/>~n',[InCodec,InAtom]),
  ( Name = '' -> 
    format(UStream,   '<usr           codec="~w" string="~w"/>~n',[InCodec,InAtom])
  ; format(UStream,   '<usr name="~w" codec="~w" string="~w"/>~n',[Name,InCodec,InAtom])
  ),


  % optionally we can pass a set of equivalences, if the file `equivalences.xml' exists
  % in the grammar directory.
  ( absolute_file_name('equivalences.xml',
			_,
			[access(read),file_errors(fail)]) ->
  
      open('equivalences.xml',read,Equi),
      read_chars(Equi,[],EquivalencesString),
      close(Equi),
      transform_xml_characters(EquivalencesString,EquivalencesT_XML),
      format(UStream,   '<eliminate equations="~s" />~n',[EquivalencesT_XML])
  ; true
  ),
%   format(UStream,   '<eliminate equations="&lt;equivalences style=&quot;Chinese-B-Gram&quot;&gt;
%                                            &lt;permutesWithEverything label=&quot;proper_q&quot;  hole=&quot;1&quot; /&gt;;
% 	                                   &lt;permutesWithEverything label=&quot;pronoun_q&quot; hole=&quot;1&quot; /&gt;;
%                                            &lt;/equivalences&gt;" />~n',[]),
  
%  format(user_error,'</utool>~n',[]),flush_output(user_error),
  format(UStream,   '</utool>~n',[]),

  flush_output(UStream),
  get_results(UStream,Command,Result,ErrorMessage),
  close(UStream).


get_results(Stream,Command,Result,ExplanationString) :-
        read_chars(Stream,[],Chars),
        xml_parse(Chars,XML),
        %xml_pp(XML),
        
        ( xml_subterm( XML, element(error, Attributes, _Content)) ->
            member(explanation=ExplanationString,Attributes),
            %format(user_error,"~N* ERROR: ~s~n",[ExplanationString]),
            Result = 0
        ;
            ExplanationString = "",
            (Command = solve ->
                ( xml_subterm( XML, element(solution, [string=SolutionString], _Content) ),
                    name(Solution,SolutionString),
                    write(Solution),nl,
                    fail
                ; true )
            
            ; Command = solvable ->
                xml_subterm( XML, element(result, Attributes, _Content) ),
                                % This just prints the number of scopings.
                                % These numbers are provided like the number of residues.
                ( member(solvable="true",Attributes) ->
                    member(count=CountString,Attributes),
                    name(Result,CountString)
                ; format(user_error,"~N* ERROR: The MRS does not scope!~n",[])
                )
            ; % Command = display -> we do not want to return anything
                true
            )
        ).

read_chars(Stream,In,Out) :-
        read_line(Stream,Line),
        (Line \== end_of_file ->
            append(In,Line,New),
            read_chars(Stream,New,Out)
        ; In = Out ).


transform_xml_characters([],[]).
transform_xml_characters([Ci|Ti],Transformed) :-
  xml:character_entity( Ci1,Ci),!,
  transform_xml_characters(Ti,To),
  append([38|Ci1],[59],TransformedCi),
  append(TransformedCi,To,Transformed).

transform_xml_characters([Ci|Ti],[Ci|To]) :-
  transform_xml_characters(Ti,To).

        
