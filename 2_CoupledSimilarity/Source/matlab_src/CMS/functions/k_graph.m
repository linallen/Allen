function adjacent_matrix=k_graph(data,k)
dim=size(data);
n=dim(1);
adjacent_matrix=zeros(n,n);
for i=1:n
    [num,val]=sort(data(:,i));
    yend=num(end-k:end-1);
    bend=val(end-k:end-1);
    for j=1:k
        if(adjacent_matrix(bend(j))<yend(j))
            adjacent_matrix(bend(j),i)=yend(j);
            adjacent_matrix(i,bend(j))=yend(j);
        end
    end
end 
mm=1;
for i=1:n
    for j=i:n
        if(adjacent_matrix(i,j)==0)
        else
            adjacent_list(mm,1)=i;
            adjacent_list(mm,2)=j;
            adjacent_list(mm,3)=adjacent_matrix(i,j);
            mm=mm+1;
        end
    end
end

xlswrite('a.xlsx',adjacent_list);


    
    
