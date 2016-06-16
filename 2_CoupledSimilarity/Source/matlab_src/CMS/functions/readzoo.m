function [zoomatrix,zoolabel]=readzoo(file)
%file='zoo.csv';
fid = fopen(file);
C = textscan(fid, '%s');
str_cell = regexp(C{1}, ',', 'split');
cols = length(str_cell{1});
rows = size(str_cell,1);
data = cell(rows,cols);
for i=1:rows
    for j=1:cols
        data{i,j} = str_cell{i}{j};
    end
end
data2=data(:,1:cols);

matrix = subs_elm(data2);


zoomatrix=matrix(:,1:(cols-1));
zoolabel=matrix(:,cols);
