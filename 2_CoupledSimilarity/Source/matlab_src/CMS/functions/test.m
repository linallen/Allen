clc, clear, close all;
fileID='balloons.csv';
A=readfile(fileID);
data = data_pre(A);
a=1;
matrix=calsim(data,a);
k=4;
matrix;
adjacent=k_graph(matrix,k)