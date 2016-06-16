clc, clear, close all;
addpath(genpath('functions'));

%% -----read balloons------
fileID='./datasets/balloons.csv'
fileIDlabel='./datasets/balloons_label.csv';
A=readfile(fileID);
B=readfile(fileIDlabel);
data=subs_elm(A);
label=subs_elm(B);
k=2;

size(data)
a=0.5;

%% ------------test kmodes with CMS-----
start=cputime;
kmode_cms_label=kmodes_cms(data,k);
[precision, recall, ri, fscore]=TFPN(kmode_cms_label',label');
kmode_precision=precision
NMI_kmode=NMI(kmode_cms_label,label')
kmodes_time=cputime-start

%% ----------test spectral clustering-------
%uncomment similarity matrix first
start=cputime;
flag_spec=SpectralClustering_Normalized(double(matrix),k);
NMI_spec=NMI(flag_spec',label')
[precision, recall, ri, fscore]=TFPN(flag_spec,label');
spec_precision=precision
