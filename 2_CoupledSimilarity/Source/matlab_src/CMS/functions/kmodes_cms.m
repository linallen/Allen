function flag= kmodes_cms(data,k)
%get the dimension of the data matrix
% data=[1,2;2,3;3,4;1,2;];
% k=2;
dim = size(data);
n=dim(1);
d=dim(2);
a=0.5;
Attribute_hash = calsim_hashtabel(data,a);
Attribute_hash.keys;
%Declaration
%Memberships , vShip [ i ]= j means x_i is in the jth cluster
vShip= zeros(1,n);
mModes=zeros(k,d);%Mode of each cluster
Lprev = 0; L =0;%Loss funtion values
%initialize modes
vrand = zeros(1,k);
vrand(1)=floor(n*rand+1);
mModes(1,:) = data(vrand(1),:);
for i =2:k
    bTag=0;
    while bTag==0
        bTag =1;
        j = floor(n*rand+1);
        for s=1:(i-1)
            if j==vrand(s)
                bTag =0;
            end
        end
    end 
    vrand(i)=j;
    mModes(i,:)=data(vrand(i),:);
end
clear vrand;

%estimate vShip given the initial mModes
for i =1:n
    %%  fprev = dissimilarity(data(i,:), nModes(1,:));

    fprev = dissim_cms_hash(Attribute_hash,data(i,:), mModes(1,:));
%    fprev = length(find(abs(data(i,:)-mModes(1,:))>0));
    vShip(i)=1;
    for s=2:k
        f = dissim_cms_hash(Attribute_hash,data(i,:), mModes(s,:));
        %f= length(find(abs(data(i,:)-mModes(s,:))>0));
        if fprev>f
            fprev=f;
            vShip(i)=s;
        end
    end
    L =L+fprev;
end
%iteration phase,estimate vship,estimate mModes
Lprev = n*d;
while abs(Lprev-L)>0
    Lprev=L;
    L=0;
    %estimate mModes given the revised vship
    for s=1:k
        index = find(vShip==s);
       if length(index)>0 % i add the condition
        for j = 1:d
            A = sort(data(index,j));
            [b,m,nn]=unique(A);
            nL = length(m);
            nMax=m(1);
            mModes(s,j)=b(1);
            for i =2:nL
                if (m(i)-m(i-1))>nMax
                    nMax = m(i)-m(i-1);
                    mModes(s,j)=b(i);
                end
            end
        end
       end
    end
    %estimate vship given the estimate of mModes
    for i =1:n
       %  fprev = dissimilarity(data(i,:), nModes(1,:));
        fprev = length(find(abs(data(i,:)-mModes(1,:))>0));
        vShip(i)=1;
        for s =2:k
           %  fprev = dissimilarity(data(i,:), nModes(s,:));
            f =length(find(abs(data(i,:)-mModes(s,:))>0));
            if fprev>f
                fprev =f;
                vShip(i)=s;
            end
        end
        L =L+ fprev;
    end
    Lprev;
    L;
end
flag = vShip;

end

