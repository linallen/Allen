clc, clear, close all;
%NMI FOR SC

y=[0.6,		0.98,		0.88,	0.71	;
   0.43,		0.78,		0.76,	0.68;
  0.43,		0.79,		0.72,	0.70;];
yy=y';
%AC FOR SC
y1=[0.7,		0.98,		0.94,	0.54;
    0.66,		0.66,		0.80,	0.52;
   0.66,		0.6,		0.70,	0.46;];
yy1=y1';
x=[1,2,3,4];
name={'Balloon','Soybean-s','Zoo','Soybean-l'};

figure;
subplot(2,1,1);
bar(x,yy1,'grouped');
%title('K-means clustering comparisons with AC and NMI','FontName','Times New Roman','FontWeight','Bold','FontSize',12);
set(gca,'XTickLabel',name,'FontName','Times New Roman','FontWeight','Bold','Fontsize',12);
ylabel('Accuracy','FontName','Times New Roman','FontWeight','Bold','FontSize',12);
grid on;


subplot(2,1,2);
bar(x,yy,'grouped');
ylabel('Normalized Mutual Information','FontName','Times New Roman','FontWeight','Bold','FontSize',12);
set(gca,'XTickLabel',name,'FontName','Times New Roman','FontWeight','Bold','Fontsize',12);
xlabel('Four UCI data sets','FontName','Times New Roman','FontWeight','Bold','FontSize',12)
%location=[left,bottom,width,height];
hleg=legend('SC-CMS','SC-OF','SC-OL');

set(hleg,'Orientation','horizontal');
%set(hleg,'Interpreter','none')

set(hleg,'Location','BestOutside');
set(hleg,'FontName','Times New Roman','Fontsize',10);
grid on;