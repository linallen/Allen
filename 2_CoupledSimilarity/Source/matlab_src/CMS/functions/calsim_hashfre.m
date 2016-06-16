function Attribute_hash = calsim_hashfre(data,a)
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

end
           
            
            
            
            
            
            
            
            
            
            
