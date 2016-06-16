function matrix = calsim_old(data)
dim=size(data);
n=dim(1);
d=dim(2);
matrix=ones(n,n);
for i=2:n
    for k=i:n
        sim=0;
        for j=1:d
            if data(i-1,j)==data(k,j)
                sim=sim+1;
            else
                sim=sim+0;
            end
        end
        matrix(i-1,k)=sim/d;
        matrix(k,i-1)=sim/d;
    end
end
end
