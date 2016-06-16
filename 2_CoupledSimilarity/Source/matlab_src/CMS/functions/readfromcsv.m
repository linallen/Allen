function matrix=readfromcsv(fileID)
%file = 'shuttle.csv';
%fileID='king';
file=fileID;
fid = fopen(file);
C = textscan(fid, '%s');
str_cell = regexp(C{1}, ';', 'split');
cols = length(str_cell{1});
rows = size(str_cell,1);
data = cell(rows,cols);
for i=1:rows
    for j=1:cols
        data{i,j} = str_cell{i}{j};
    end
end
data;
% 
matrix= subs_elm(data);
