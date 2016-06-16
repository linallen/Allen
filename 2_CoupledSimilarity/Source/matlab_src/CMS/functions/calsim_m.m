function matrix = calsim_m(data,a)
dim=size(data);
n=dim(1);
d=dim(2);
matrix=ones(n,n);
for i=2:n
    for k=i:n
        intra_sim=0;
        inter_sim=0;
        sim1=0;
        for j=1:d %calculate the intra-similarity
            if data(i-1,j)==data(k,j)
                intra_sim=1;
                inter_sim=1;
            else
                x=cal_feq(data(i-1,j),data(:,j));
                y=cal_feq(data(k,j),data(:,j));
                intra_sim=(log(x+1)*log(y+1))/(log(x+1)+log(y+1)+(log(x+1)*log(y+1)));
                inter=0;
                if(a==0)
                    inter=1;
                else
                    for l=1:d
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
                            ICPmin=0;
                            ICPmax=0;
                            if isempty(v3)%万一人家没有交集呢
                            else
                                for z=1:length(v3)
                                    u0=find(data(:,l)==v3(z));
                                    ICP1=length(intersect(u0,u1))/length(u1);
                                    ICP2=length(intersect(u0,u2))/length(u2);
                                    ICPmin=ICPmin+min(ICP1,ICP2);
                                    ICPmax=ICPmax+max(ICP1,ICP2);
                                    
                                   % minicp1=minicp1+min(ICP1,ICP2);
%                                     ICP1(z)=length(intersect(u0,u1))/length(u1);
%                                     ICP2(z)=length(intersect(u0,u2))/length(u2);
                                end
                                minicp1=ICPmax/(2*ICPmax-ICPmin);
%                                 minicp1=dot(ICP1,ICP2)/(sqrt(dot(ICP1,ICP1)*dot(ICP2,ICP2)));
                                %minicp1=dot(ICP1,ICP2);
                                %minicp1=minicp1/(length(v3));%可除可不除，其实不除效果更好些，但是为了符合理论推导，还是除了吧
                            end
                        end
                        inter=inter+ minicp1/(d-1);
                    end
                end
                inter_sim=inter;
            end
            sim=(a^2+1)*intra_sim*inter_sim/((a^2*intra_sim)+inter_sim);
            sim1=sim1+sim;
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
               
            
            
            
            
            
            
            
            
            
            
