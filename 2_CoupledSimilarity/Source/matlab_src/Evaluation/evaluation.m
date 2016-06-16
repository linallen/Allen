function [AC,MIhat] = evaluation(res,gnd)
% res is the clustering result; gnd is the label information
res = bestMap(gnd,res);
AC = length(find(gnd == res))/length(gnd);
MIhat = MutualInfo(gnd,res);
end
