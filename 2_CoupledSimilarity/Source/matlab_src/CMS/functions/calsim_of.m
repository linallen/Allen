function matrix = calsim_of(data)
dim=size(data);
n=dim(1);
d=dim(2);
matrix=ones(n,n);
for i=2:n
    for k=i:n
        intra_sim=0;
        sim1=0;
        for j=1:d %calculate the intra-similarity
            if data(i-1,j)==data(k,j)
                intra_sim=1;
            else
                x=cal_feq(data(i-1,j),data(:,j));
                y=cal_feq(data(k,j),data(:,j));
                intra_sim=1/(1+log(n/x)*log(n/y));
              
            end
            sim1=sim1+intra_sim;
        end
        matrix(i-1,k)=sim1/d;
        matrix(k,i-1)=sim1/d;
    end
end
matrix;
end

function feq=cal_feq(vector,num)
M=find(vector==num);
feq=length(M);
end
               
            
            
            
            
            
            
            
            
            
            
