clc, clear, close all;
x=[1,2,3,4,5,6];
%vote data
y=[0.3253,0.4272,0.4102,0.4274,0.423,0.4274];%NMI
y1 =[0.5578,0.7674,0.7574,0.7674,0.7642,0.7674];

% car data
z=[0.338,0.694,0.6795,0.6523,0.6838,0.6584];% 1 is the car
z1=[0.2787,0.4199,0.435,0.4105,0.3913,0.392];

subplot(2,1,1);
plot(x,y,'-ro',x,y1,'-.b^')
title(' K-distance clustering result with different iterations','FontName','Times New Roman','FontWeight','Bold','FontSize',14);
xlabel('The number of iterations of vote data','FontName','Times New Roman','FontWeight','Bold','FontSize',14);
ylabel('NMI or Precision','FontName','Times New Roman','FontWeight','Bold','FontSize',12);
hleg=legend('NMI','P');
set(hleg,'FontName','Times New Roman','Fontsize',11);
axis([1 6 0 1]);
set(gca,'xtick',[1 2 3 4 5 6]);
set(gca,'ytick',[0 0.2 0.4 0.6 0.8 1]);
set(hleg,'Orientation','horizontal');
subplot(2,1,2);
%plot(x,z,'-ro',x,z1,'-.b',x,z2,':',x,z3,'g');
plot(x,z,'-ro',x,z1,'-.b^');
hleg=legend('NMI','P');
set(hleg,'FontName','Times New Roman','Fontsize',11);

xlabel('The number of iterations of car evaluation data','FontName','Times New Roman','FontWeight','Bold','FontSize',14);
ylabel('NMI or Precision','FontName','Times New Roman','FontWeight','Bold','FontSize',12);
axis([1 6 0 1]);
set(gca,'xtick',[1 2 3 4 5 6]);
set(gca,'ytick',[0 0.2 0.4 0.6 0.8 1]);