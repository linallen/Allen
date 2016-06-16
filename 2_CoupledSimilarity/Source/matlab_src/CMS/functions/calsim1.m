function matrix = calsim1(data,a)
dim=size(data);
n=dim(1);
d=dim(2);
matrix=ones(n,n);

for i=2:n
    for k=i:n
        intra_sim=0;
        inter_sim=0;
        for j=1:d %calculate the intra-similarity
            if data(i-1,j)==data(k,j)
                intra=1;
            else
                %x=feq(data(i-1,j));
                %y=feq(data(k,j));
                x=cal_feq(data(i-1,j),data(:,j));
                y=cal_feq(data(k,j),data(:,j));
                intra=(log(x+1)*log(y+1))/(log(x+1)+log(y+1)+(log(x+1)*log(y+1)));
            end
            inter=0;
            for l=1:d
                minicp=0;
                minicp1=0;
               % if j==k
                if j==l
                else
                    u1=find(data(:,j)==data(i-1,j));
                    u2=find(data(:,j)==data(i,j));
                    v1=data(u1,l);
                    v2=data(u2,l);
                    v3=intersect(v1,v2); 
                    minicp1=0;
                    if isempty(v3)%万一人家没有交集呢
                    else
                        for z=1:length(v3)
                            u0=find(data(:,l)==v3(z));
                            %ICP1=length(intersect(u0,u1))/length(u1);
                            %ICP2=length(intersect(u0,u2))/length(u2);
                           % minicp1=minicp1+min(ICP1,ICP2);
                            ICP1(z)=length(intersect(u0,u1))/length(u1);
                            ICP2(z)=length(intersect(u0,u2))/length(u2);
                        end
                        %minicp1=dot(ICP1,ICP2)/(sqrt(dot(ICP1,ICP1)*dot(ICP2,ICP2)));
                        minicp1=dot(ICP1,ICP2);
                        %minicp1=minicp1/(length(v3));%可除可不除，其实不除效果更好些，但是为了符合理论推导，还是除了吧
                    end
                end
                minicp=minicp+ minicp1;
            end
            inter=inter+minicp/(d-1);
            intra_sim=intra_sim+intra; 
            inter_sim=inter_sim+inter;
        
        end
        intra_sim=intra_sim/d;
        inter_sim= inter_sim/d;
        if(intra_sim==1)
            sim=1;
        else
            sim=(a^2+1)*intra_sim*inter_sim/((a^2*intra_sim)+inter_sim);
            %sim=intra_sim*inter_sim;
        matrix(i-1,k)=sim;
        matrix(k,i-1)=sim;
        end
    end
end

end
function feq=cal_feq(vector,num)
M=find(vector==num);
feq=length(M);
end
               
            
            
            
            
            
            
            
            
            
            
