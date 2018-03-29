%% To be compiled with the synframe compiler
%% xmg compile synframe frames.mg --force
%% Or online: http://xmg.phil.hhu.de/index.php/upload/workbench

%% To also generate the type hierarchy (in file more.mac)
%% xmg compile synframe frames.mg --force --more

include types.mg

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
