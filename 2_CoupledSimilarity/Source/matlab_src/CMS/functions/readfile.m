function data=readfile(fileID)
%fileID='balloons.csv'
T=readtable(fileID);  % read file to table,会自动把第一行忽略,所以要加上表头
data= table2array(T); %convert table to array(实际上是cell)
