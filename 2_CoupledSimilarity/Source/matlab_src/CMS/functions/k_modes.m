%-------with our similarity/distance matrix
function flag= k_modes(data,k,a)
%get the dimension of the data matrix
dim = size(data);
n=dim(1);
d=dim(2);
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
    %fprev = length(find(abs(data(i,:)-mModes(1,:))>0));
    fprev=loss(data,i,mModes,1,a);
    vShip(i)=1;
    for s=2:k
        %f= length(find(abs(data(i,:)-mModes(s,:))>0));
        f=loss(data,i,mModes,s,a);
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
    %estimate vship given the estimate of mModes
    for i =1:n
        %fprev = length(find(abs(data(i,:)-mModes(1,:))>0));
        fprev=loss(data,i,mModes,1,a);
        vShip(i)=1;
        for s =2:k
            %f =length(find(abs(data(i,:)-mModes(s,:))>0));
            f=loss(data,i,mModes,s,a);
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
function fprev = loss(data,i,mModes,s,a)%instead of f=length(find(abs(data(i,:)-mModes(s,:))>0));
dim=size(data);
n=dim(1);
d=dim(2);

intra_sim=0;
inter_sim=0;
for j=1:d %calculate the intra-similarity
    if data(i,j)==mModes(s,j)
            intra=1;
    else
        x=cal_feq(data(i,j),data(:,j));
        y=cal_feq(mModes(s,j),data(:,j));
        intra=(log(x+1)*log(y+1))/(log(x+1)+log(y+1)+(log(x+1)*log(y+1)));
    end
    intra_sim=intra_sim+intra;   
    inter=0;
    for l=1:d
        minicp=0;
        minicp1=0;
        if j==l
        else
            u1=find(data(:,j)==data(i,j));
            u2=find(data(:,j)==mModes(s,j));
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
                    if length(u2)==0
                        length(u2)
                        break;
                    end
                    ICP1(z)=length(intersect(u0,u1))/length(u1);
                    ICP2(z)=length(intersect(u0,u2))/length(u2);
                end
                minicp1=dot(ICP1,ICP2);
                %minicp1=dot(ICP1,ICP2)/(sqrt(dot(ICP1,ICP1)*dot(ICP2,ICP2)));
                %minicp1=minicp1/(length(v3));%可除可不除，其实不除效果更好些，但是为了符合理论推导，还是除了吧
            end
        end
        minicp=minicp+minicp1;
    end
    inter=inter+minicp;
    inter_sim=inter_sim+inter/(d-1);
end
intra_sim=intra_sim/(d);
inter_sim=inter_sim/(d);
if(intra_sim==1)
    sim=1;
else
    sim=(a^2+1)*intra_sim*inter_sim/((a^2*intra_sim)+inter_sim);    
end
fprev=1/sim-1;
end
    
function feq=cal_feq(vector,num)
M=find(vector==num);
feq=length(M);
end
            
            
            
            
            
            
            
            
            
            
            
            

