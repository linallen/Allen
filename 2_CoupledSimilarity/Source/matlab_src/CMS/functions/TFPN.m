function [ precision, recall, ri, fscore ] = TFPN(clusters, labels)
% input parameters: 
% clusters: labels by clustering algorithm 
% labels:  true labels  
if nargin==0
   clusters = [1,1,1,0,0,0];
   labels =   [1,1,0,0,0,0];
end

if length(clusters)~= length(labels)
    return;
else
    n=length(clusters);
end
tp=0;fp=0;tn=0;fn=0;
for i=1:n
    for j=i+1:n
        if clusters(i)==clusters(j) & labels(i)==labels(j)
            tp=tp+1;
        elseif clusters(i)==clusters(j) & labels(i)~=labels(j)
            fp=fp+1;
        elseif clusters(i)~=clusters(j) & labels(i)==labels(j)
            fn=fn+1;
        elseif clusters(i)~=clusters(j) & labels(i)~=labels(j)
            tn=tn+1;
        end
    end
end
% [tp fp tn fn tp+fp+tn+fn]
% tp 
% fp
precision = tp/(tp+fp);
recall = tp/(tp+fn);
ri = (tp+tn)/(tp+tn+fp+fn);
fscore = 2*precision*recall/(precision+recall);
end

