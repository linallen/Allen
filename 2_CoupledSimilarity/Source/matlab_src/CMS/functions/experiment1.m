clc, clear, close all;
y=[0.7,0.6596,0.6596,0.97,0.467,0.97;
    0.98,0.6656,0.5952,0.9623,0.7183,0.7067;
    0.9477,0.8602,0.7058,0.6608,0.5989,0.5856;
    0.7022,0.6804,0.7016,0.5105,0.4027,0.4095;];
y1=[0.6012,0.4325,0.4325,0.98,0.3,0.98;
    0.89,0.78,0.79,0.87,0.84,0.82;
    0.8769,0.7759,0.7226,0.643,0.608,0.6199;
    0.5419,0.5255,0.4566,0.48,0.42,0.41;];
x=[1,2,3,4];
name={'Balloon','Soybean-s','Zoo','Soybean-l'};

figure;
subplot(2,1,1);
bar(x,y,'grouped');
title('Clustering comparisons with AC and NMI','FontName','Times New Roman','FontWeight','Bold','FontSize',12);
set(gca,'XTickLabel',name,'FontName','Times New Roman','FontWeight','Bold','Fontsize',12);
ylabel('Accuracy','FontName','Times New Roman','FontWeight','Bold','FontSize',12);
grid on;


subplot(2,1,2);
bar(x,y1,'grouped');
ylabel('Normalized Mutual Information','FontName','Times New Roman','FontWeight','Bold','FontSize',12);
set(gca,'XTickLabel',name,'FontName','Times New Roman','FontWeight','Bold','Fontsize',12);

%location=[left,bottom,width,height];
hleg=legend('KM-CMS','KM-OF','KM-OL','SC-CMS','SC-OF','SC-OL');

set(hleg,'Orientation','horizontal');
%set(hleg,'Interpreter','none')

set(hleg,'Location','BestOutside');
set(hleg,'FontName','Times New Roman','Fontsize',10);
grid on;