function distance= caldis(data)
dim=size(data);
n=dim(1);
d=dim(2);
distance=zeros(n,n);
for i=2:n
    for k=i:n
        sim=0;
        for j=1:d
            if data(i-1,j)==data(k,j)
                sim=sim+0;
            else
                sim=sim+1;
            end
        end
        distance(i-1,k)=sim;
        distance(k,i-1)=sim;
    end
end
end