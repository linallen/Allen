clc, clear, close all;
addpath(genpath('functions'));
%-----read balloons------
% fileID='./datasets/balloons.csv'
% fileIDlabel='./datasets/balloons_label.csv';
% A=readfile(fileID);
% B=readfile(fileIDlabel);
% data=subs_elm(A);
% label=subs_elm(B);
% k=2;

%------read test ----------
% fileID='./datasets/test.csv' 
% [A,B]=readzoo(fileID);
% data=A;
% label=B;
% k=2;

%------read zoo ----------
fileID='./datasets/zoo.csv' 
[A,B]=readzoo(fileID);
data=A;
label=B;
k=7;

%-----read shuttle-------
% fileID='./datasets/shuttle.csv' 
% fileIDlabel='./datasets/shuttle_label.csv';
% A=readfromcsv(fileID);
% B=readfromcsv(fileIDlabel);
% data=A;
% label=B;
% k=2;

%---------read soybean-s-----------
% % 
% fileID='./datasets/soybean-s.csv'
% [A,B]=readzoo(fileID);
% data=A;
% label=B;
% k=4;

%---------read soybean-l-----------
% 
% fileID='./datasets/soybean-l.csv'
% [A,B]=readzoo(fileID);%spectral NMI 0.8328  
% data=A;
% label=B;
% k=19;

%---------congress_vote--------
% fileID='./datasets/congress_vote.csv'
% [A,B]=readzoo(fileID);
% data=A;
% label=B;
% k=2;

%--------read car_evaluation-----
% fileID='./datasets/car_evaluation.csv';
% [A,B]=readzoo(fileID);
% data=A;
% label=B;
% k=4;

size(data)
a=0.5;

%% ------------test kmodes with CMS-----
% start=cputime;
% kmode_cms_label=kmodes_cms(data,k);
% [precision, recall, ri, fscore]=TFPN(kmode_cms_label',label');
% kmode_precision=precision
% NMI_kmode=NMI(kmode_cms_label,label')
% kmodes_time=cputime-start

%% ------------test kmodes with COS-----
% start=cputime;
% kmode_cos_label=kmodes_cos(data,k);
% [precision, recall, ri, fscore]=TFPN(kmode_cos_label',label');
% kmode_precision=precision
% NMI_kmode=NMI(kmode_cos_label,label')
% kmodes_time=cputime-start

%% ------------test kmodes with OF-----
% start=cputime;
% kmode_of_label=kmodes_of(data,k);
% [precision, recall, ri, fscore]=TFPN(kmode_of_label',label');
% kmode_precision=precision
% NMI_kmode=NMI(kmode_of_label,label')
% kmodes_time=cputime-start;

%% --------test kmodes with OL-----
% flag_kmodes=kmodes(data,k);
% NMI_kmode_old=NMI(flag_kmodes,label')
% [precision, recall, ri, fscore]=TFPN(flag_kmodes,label');
% kmodes_precision=precision

%% ---------------- similarity matrix by different similarity measure ---------------
%start=cputime
%matrix=calsim_old(data); % OL similarity measure
%matrix=calsim_of; %OL similarity measure matrix; 
%matrix=calsim_wang(data);   % COS similarity measure matrix
matrix=calsim_hash(data,a); % CMS of hash table 
%calsim_time = cputime - start 

%% ----------test spectral clustering-------
%uncomment similarity matrix first

start=cputime;
flag_spec=SpectralClustering_Normalized(double(matrix),k);
NMI_spec=NMI(flag_spec',label')
[precision, recall, ri, fscore]=TFPN(flag_spec,label');
spec_precision=precision

%% --------test k-distance------
%uncomment similarity matrix first

% start=cputime;
% dis_matrix=(1./matrix)-1; 
% kdis_label=kdistance(dis_matrix,k);
% [precision, recall, ri, fscore]=TFPN(kdis_label',label');
% NMI_kdis=NMI(kdis_label',label');
% k_distance_precision=precision
% k_distance_NMI=NMI_kdis
% kdis_time=start-cputime