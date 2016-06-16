clear; clc;
SimGraph = [12,15,0,0;12,1,20,0;0,30,1,1;0,40,1,1];
 Type =1;
k=2;
[C, L, U] = SpectralClustering(SimGraph, 3, 1)
