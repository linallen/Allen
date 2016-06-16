clc, clear, close all;

% draw the graph with different a

x=[0,0.1,0.3,0.5,0.7,0.9];
%NMI
y=[0.38,0.55,0.52,0.38,0.38,0.26];  % shuttle
y1=[0.202,0.325,0.43,0.21,0.20,0.20];  % Balloon
y2=[0.72,0.75,0.918,1,0.86,0.84];  % Soybean-s
y3=[0.326,0.511,0.556,0.850,0.8463,0.7732];  % zoo
y4=[0.315,0.4608,0.64,0.71,0.66,0.63]; %soybean-l
y5=[0.388,0.4458,0.5709,0.5922,0.55,0.423]; %vote
%Precision
z =[0.61,0.75,0.71,0.61,0.61,0.60];
z1=[0.49,0.66,0.76,0.62,0.59,0.53];
z2=[0.68,0.73,0.93,1,0.85,0.81];
z3=[0.33,0.51,0.56,0.85,0.84,0.77];
z4=[0.31,0.32,0.35,0.46,0.45,0.42];
z5=[0.7423,0.7856,0.8653,0.8624,0.82,0.7642];
subplot(2,1,1);
plot(x,y,'-r^',x,y1,'-b*',x,y2,'-mo',x,y3,'-cs',x,y4,'-kh',x,y5,'-gd');
title('K-distance clustering with different \alpha on different data sets','FontName','Times New Roman','FontWeight','Bold','FontSize',14);
xlabel('different values of \alpha','FontName','Times New Roman','FontWeight','Bold','FontSize',14);
ylabel('NMI','FontName','Times New Roman','FontWeight','Bold','FontSize',14);
hleg=legend('Shuttle','Balloon','Soybean-s','Zoo','Soybean-l','Voting');
set(hleg,'FontName','Times New Roman','Fontsize',12);

axis([0 0.9 0 1]);
set(gca,'xtick',[0 0.1 0.3 0.5 0.7 0.9]);
set(gca,'ytick',[0 0.2 0.4 0.6 0.8 1]);

set(hleg,'Orientation','horizontal');
subplot(2,1,2);
plot(x,z,'-r^',x,z1,'-b*',x,z2,'-mo',x,z3,'-cs',x,z4,'-kh',x,z5,'-gd');

xlabel('different values of \alpha','FontName','Times New Roman','FontWeight','Bold','FontSize',14);
ylabel('Precision','FontName','Times New Roman','FontWeight','Bold','FontSize',14);
axis([0 0.9 0 1]);
set(gca,'xtick',[0 0.1 0.3 0.5 0.7 0.9]);
set(gca,'ytick',[0 0.2 0.4 0.6 0.8 1]);