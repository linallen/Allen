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

% ------read zoo ----------
% fileID='./datasets/zoo.csv' 
% [A,B]=readzoo(fileID);
% data=A;
% label=B;
% k=7;

%-----read shuttle-------
% fileID='./datasets/shuttle.csv' 
% fileIDlabel='./datasets/shuttle_label.csv';
% A=readfromcsv(fileID);
% B=readfromcsv(fileIDlabel);
% data=A;
% label=B;
% k=2;

%---------read soybean-s-----------
% 
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

%matrix=calsim_hash(data,a);

resultID=[fileID,'.mat'];
matrixID=[fileID,'_sim.mat'];
load(resultID,'result');
load(matrixID,'matrix');

size(matrix)

dis_matrix=(1./matrix)-1;

start=cputime;
kdis_label=kdistance(dis_matrix,k);
NMI_kdis=NMI(kdis_label',label')
[kdis_precision, kdis_recall, ri, fscore]=TFPN(kdis_label',label');
k_distance_precision=kdis_precision
kdis_time=cputime-start

%%------test cms spectral clustering----------
% start=cputime;
% flag_spec=SpectralClustering_Normalized(double(matrix),k);
% NMI_spec=NMI(flag_spec',label')
% [precision, recall, ri, fscore]=TFPN(flag_spec,label');
% spec_precision=precision