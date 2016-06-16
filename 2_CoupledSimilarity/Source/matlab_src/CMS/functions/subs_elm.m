function [matrix] = subs_elm( data )

%   transform data(cell) to a new cell whose elements are integers 

[rows,cols] = size(data);
elems = {};
class(elems);
m=0;
for i=1:rows
    for j=1:cols
        if ~ismember(data{i,j},elems)
            m=m+1;
            elems{m} = data{i,j};
        end
    end
end
for i=1:rows
    for j=1:cols
        for k=1:m
            if strcmp(data{i,j},elems{k})
            	data{i,j} = k;
            end
        end
    end
end
matrix=cell2mat(data);
% data = char(data)