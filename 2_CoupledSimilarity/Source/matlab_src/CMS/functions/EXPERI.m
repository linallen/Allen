clc, clear, close all;
%NMI FOR SC

y=[0.98,		0.97,		0.7509,	0.7125;
    0.43,		0.92,		0.75,	0.7;
    0.35,		0.8495,		0.608,	0.4027;
    0.98,		0.83,		0.6199,	0.4095;];
yy=y';
%AC FOR SC
y1=[0.98,		0.964,		0.636,	0.6;
    0.75,		0.91,		0.64,	0.6;
   0.467,		0.718,		0.6,	0.42;
    0.98,		0.70,		0.58,	0.41;];
yy1=y1';
x=[1,2,3,4];
name={'Balloon','Soybean-s','Zoo','Soybean-l'};

figure;
subplot(2,1,1);
bar(x,yy1,'grouped');
title('Spectral clustering comparisons with AC and NMI','FontName','Times New Roman','FontWeight','Bold','FontSize',12);
set(gca,'XTickLabel',name,'FontName','Times New Roman','FontWeight','Bold','Fontsize',12);
ylabel('Accuracy','FontName','Times New Roman','FontWeight','Bold','FontSize',12);
grid on;


subplot(2,1,2);
bar(x,yy,'grouped');
ylabel('Normalized Mutual Information','FontName','Times New Roman','FontWeight','Bold','FontSize',12);
set(gca,'XTickLabel',name,'FontName','Times New Roman','FontWeight','Bold','Fontsize',12);

%location=[left,bottom,width,height];
hleg=legend('SC-CMS','SC-COS','SC-OF','SC-OL');

set(hleg,'Orientation','horizontal');
%set(hleg,'Interpreter','none')

set(hleg,'Location','BestOutside');
set(hleg,'FontName','Times New Roman','Fontsize',10);
grid on;