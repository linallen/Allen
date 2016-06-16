function Attribute_hash = calsim_hashcos(data)
% data=[1,2;2,3;3,4;1,2;];
% %data=[1,1,1;2,1,2;1,2,1;1,2,2;2,2,1;1,1,2];
% %data=[1,1,1;1,2,1;2,1,1;2,2,2;3,3,3;3,3,4]; %%car data
% a=0.5;

% fileID='soybean-s.csv'%k=4!!NMI calsim_m -0.6694 0.5526 calsim 0.5217
% [A,B]=readzoo(fileID);%spectral NMI 0.8328  
% data=A;
% label=B;
% data=data(:,2:4);
a=0.5;
dim=size(data);
n=dim(1);
d=dim(2);
matrix=ones(n,n);
fre_Hash=cal_freHash(data);
%format 1_2 means attribute 1 value 2
%conditional probability between attribute values 
 % format: 1_2to2_3, 1_v1 means value 2 of attribute 1 

keyset={'ini'};
valueset=[1];
ICP_hash=containers.Map(keyset,valueset);
for i=1:d
    veci=unique(data(:,i));
    for j=i:d
        if i==j
        else
            vecj=unique(data(:,j));
            for ii=1:length(veci)
                for jj=1:length(vecj)
                    keyname=[num2str(i),'_',num2str(veci(ii)),'to',num2str(j),'_',num2str(vecj(jj))];
                    valuetemp=intersect( find(data(:,i)==veci(ii)),find(data(:,j)==vecj(jj)));
                    length(valuetemp);
                    ICP_hash(keyname)=length(valuetemp);
                end
            end
        end
    end
end
remove(ICP_hash,{'ini'});


keyset_inter={'ini'};
valueset_inter=[1];
Inter_hash=containers.Map(keyset_inter,valueset_inter);
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
                intra_sim=(x*y/(x+y+x*y));
                inter_sim=0;
                Intra_hash(keyname)=intra_sim;

                %%calculate inter similarity
                inter=0;
                for l=1:d
                    minicp=0;
                    if l==i

                    else
                        u1=find(data(:,i)==v1);
                        u2=find(data(:,i)==v2);
                        u3=intersect(data(u1,l),data(u2,l));
                        
                        if ~isempty(u3)
                            ICPmin=0;
                            ICPmax=0;
                            for k=1:length(u3)
                                keyname_v1=[num2str(l),'_',num2str(u3(k)),'to',num2str(i),'_',num2str(v1)];
                                keyname_v1_r=[num2str(i),'_',num2str(v1),'to',num2str(l),'_',num2str(u3(k))];
                                keyname_v2=[num2str(l),'_',num2str(u3(k)),'to',num2str(i),'_',num2str(v2)];
                                keyname_v2_r=[num2str(i),'_',num2str(v2),'to',num2str(l),'_',num2str(u3(k))];
                                if all((ismember(ICP_hash.keys,keyname_v1))==0)
                                    keyname_v1;
                                    ICP_hash.keys;
                                    ICP1=cell2mat(values(ICP_hash,{keyname_v1_r}))/length(u1);
                                else
                                    ICP1=cell2mat(values(ICP_hash,{keyname_v1}))/length(u1);
                                end
                                if all((ismember(ICP_hash.keys,keyname_v2))==0)
                                    ICP2=cell2mat(values(ICP_hash,{keyname_v2_r}))/length(u2);
                                else
                                    ICP2=cell2mat(values(ICP_hash,{keyname_v2}))/length(u2);
                                end
                                ICPmin=ICPmin+min(ICP1,ICP2);
                                ICPmax=ICPmax+max(ICP1,ICP2);

                            end
                            minicp=ICPmin;
                        end
                    end
                inter=inter+minicp;
                end
                inter_sim=inter/(d-1);
 %               if inter_sim==0
  %                  attribute_sim=intra_sim;
  %              else
                    %attribute_sim=(a^2+1)*intra_sim*inter_sim/((a^2*intra_sim)+inter_sim);
                    attribute_sim=(1-a)*intra_sim+a*inter_sim;

    %            end
                Inter_hash(keyname)=inter_sim;
                Attribute_hash(keyname)=attribute_sim;

            end
        end
        
    end    
end
remove(Inter_hash,{'ini'});
remove(Intra_hash,{'ini'});
remove(Attribute_hash,{'ini'});
Attribute_hash.keys;
inter_matrix=ones(n,n);
intra_matrix=ones(n,n);
for i=1:n
    for j=(i+1):n
        sim=0;
        sim_inter=0;
        sim_intra=0;
        for k=1:d
            if data(i,k)==data(j,k)
                sim_temp=1;
                inter_temp=1;
                intra_temp=1;
            else
                keyname=[num2str(k),'_',num2str(data(i,k)),'to',num2str(k),'_',num2str(data(j,k))];
                keyname_r=[num2str(k),'_',num2str(data(j,k)),'to',num2str(k),'_',num2str(data(i,k))];

                if all(ismember(Attribute_hash.keys,keyname)==0)
                    keyname_r;
                    sim_temp=cell2mat(values(Attribute_hash,{keyname_r}));
                    inter_temp=cell2mat(values(Inter_hash,{keyname_r}));
                    intra_temp=cell2mat(values(Intra_hash,{keyname_r}));
                else
                    keyname;
                    sim_temp=cell2mat(values(Attribute_hash,{keyname}));
                    inter_temp=cell2mat(values(Inter_hash,{keyname}));
                    intra_temp=cell2mat(values(Intra_hash,{keyname}));
                
                end
            end
            sim=sim+sim_temp;
            sim_inter=sim_inter+inter_temp;
            sim_intra=sim_intra+intra_temp;
        end
        matrix(i,j)=sim/d;
        matrix(j,i)=sim/d;
        inter_matrix(i,j)=sim_inter/d;
        inter_matrix(j,i)=sim_inter/d;
        intra_matrix(i,j)=sim_intra/d;
        intra_matrix(j,i)=sim_intra/d;
    end
end

end
           
            
            
            
            
            
            
            
            
            
            
