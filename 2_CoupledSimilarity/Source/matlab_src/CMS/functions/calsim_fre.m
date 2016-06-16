function matrix = calsim_fre(data,a)
% data=[1,2;2,3;3,4;1,2;];
% %data=[1,1,1;2,1,2;1,2,1;1,2,2;2,2,1;1,1,2];
% %data=[1,1,1;1,2,1;2,1,1;2,2,2;3,3,3;3,3,4]; %%car data
% a=0.5;

% fileID='soybean-s.csv'%k=4!!NMI calsim_m -0.6694 0.5526 calsim 0.5217
% [A,B]=readzoo(fileID);%spectral NMI 0.8328  
% data=A;
% label=B;
% data=data(:,2:4);

dim=size(data);
n=dim(1);
d=dim(2);
matrix=ones(n,n);
fre_Hash=cal_freHash(data);
%format 1_2 means attribute 1 value 2
%conditional probability between attribute values 
 % format: 1_2to2_3, 1_v1 means value 2 of attribute 1 


keyset_inter={'ini'};
valueset_inter=[1];
Intra_hash=containers.Map(keyset_inter,valueset_inter);
Attribute_hash=containers.Map(keyset_inter,valueset_inter);

for i=1:d
    vec=unique(data(:,i));
    for j=1:length(vec)
        for jj=(j+1):length(vec)
            v1=vec(j);
            v2=vec(jj);      
            keyname_intra1=[num2str(i),'_',num2str(vec(j))];
            keyname_intra2=[num2str(i),'_',num2str(vec(jj))];
            keyname=[num2str(i),'_',num2str(vec(j)),'to',num2str(i),'_',num2str(vec(jj))];
            fre_Hash.keys;
           if v1==v2
               Attribute_hash(keyname)=1;
           else
                %%calculate intra similarity
                x=cell2mat(values(fre_Hash,{keyname_intra1}));
                y=cell2mat(values(fre_Hash,{keyname_intra2}));
                intra_sim=1/(1+log(n/x)*log(n/y));
              %  intra_sim=(log(x+1)*log(y+1))/(log(x+1)+log(y+1)+(log(x+1)*log(y+1)));
                Intra_hash(keyname)=intra_sim;
                attribute_sim=intra_sim;
           end
                Attribute_hash(keyname)=attribute_sim;
        end
    end
end
remove(Intra_hash,{'ini'});
remove(Attribute_hash,{'ini'});
Attribute_hash.keys;
intra_matrix=ones(n,n);
for i=1:n
    for j=(i+1):n
        sim=0;
        sim_intra=0;
        for k=1:d
            if data(i,k)==data(j,k)
                sim_temp=1;
                intra_temp=1;
            else
                keyname=[num2str(k),'_',num2str(data(i,k)),'to',num2str(k),'_',num2str(data(j,k))];
                keyname_r=[num2str(k),'_',num2str(data(j,k)),'to',num2str(k),'_',num2str(data(i,k))];

                if all(ismember(Attribute_hash.keys,keyname)==0)
                    keyname_r;
                    sim_temp=cell2mat(values(Attribute_hash,{keyname_r}));
                    intra_temp=cell2mat(values(Intra_hash,{keyname_r}));
                else
                    keyname;
                    sim_temp=cell2mat(values(Attribute_hash,{keyname})); 
                    intra_temp=cell2mat(values(Intra_hash,{keyname}));       
                end
            end
            sim=sim+sim_temp;
            sim_intra=sim_intra+intra_temp;
        end
        matrix(i,j)=sim/d;
        matrix(j,i)=sim/d;
        intra_matrix(i,j)=sim_intra/d;
        intra_matrix(j,i)=sim_intra/d;
    end
end
end
           
            
            
            
            
            
            
            
            
            
            
