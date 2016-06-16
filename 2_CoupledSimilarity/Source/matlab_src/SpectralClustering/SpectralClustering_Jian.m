function IDX = SpectralClustering_Jian(W, k)
%function [ IDX, V, D, Q, L ] = SpectralClustering_Normalized(W, k)
% spectral clustering algorithm
% input: adjacency matrix W; number of cluster k 
% return: cluster indicator vectors as columns in IDX; unnormalized
% Laplacian L; degree matrix D; eigenvectors matrix Q; eigenvalues diagnol matrix V;

% Degree matrix
D = diag(sum(W));

% Normalized Laplacian
L = D - W;
L = D^(-1/2)*L*D^(-1/2);

% compute the eigenvectors corresponding to the k smallest eigenvalues
% diagonal matrix V is NcutL's k smallest magnitude eigenvalues 
% matrix Q whose columns are the corresponding eigenvectors.
opt = struct('issym', true, 'isreal', true);
[U, V] = eigs(L, D, k, 'SM', opt);
for i=1:size(U,1)
    r = (norm(U(i,:),2))^(1/2);
    for j=1:size(U,2)        
        T(i,j) = U(i,j)/r;
    end
end

% use the k-means algorithm to cluster V row-wise
% IDX will be a n-by-1 matrix containing the cluster number for each data point
IDX = kmeans(T, k,'emptyaction','singleton');
end
