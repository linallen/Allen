%convert categorical variable cell to numeric matrix
function matrix=data_pre(data)
[n,d]=size(data);
% n=dim(1);
% d=dim(2);
U={};
k=0;
matrix = zeros(n,d);
for j=1:d
    for i=1:n
        if(ismember(U,data(i,j)))
        else
            k=k+1;
            U(k)= data(i,j);
            M=find(strcmp(data(:,j),U(k)));
            matrix(M,j)= k;
        end
    end
end

        
            
            
        
            
            
            
         
            
        
        


