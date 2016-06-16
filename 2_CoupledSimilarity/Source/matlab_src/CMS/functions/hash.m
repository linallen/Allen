
keyset={'1to1'};
valueset=[0.5];
hashObj=containers.Map(keyset,valueset);
hashObj('2to2')=[1]; %add key value to hash table
values(hashObj,{'2to2'});
a=values(hashObj);
class(a)  %cell
cell2mat(a)

data=[1,2;2,3;3,4;1,2;];
dim=size(data);
n=dim(1);
d=dim(2);
matrix=ones(n,n);
fre_Hash=cal_freHash(data);
%format 1_2 means attribute 1 value 2
%conditional probability between attribute values 
 % format: 1_2to2_3, 1_v1 means value 2 of attribute 1 
k=1;
keyset={['1','_',num2str(data(1,1)),'to','1','_',num2str(data(1,2))]};
occu_num=intersect(find(data(:,1)==data(1,1)),find(data(:,2)==data(1,2)));
valueset=[length(occu_num)];
ICP_hash=containers.Map(keyset,valueset);
for i=1:n
    for j=2:d
        key_i=data(i,j-1);
        key_j=data(i,j);
        keyname=[num2str(i),'_',num2str(key_i),'to',num2str(i),'_',num2str(key_j)];
        ICP_hash.keys
        ismember(keyset,keyname)
        if all(ismember(keyset,keyname)==0)
            valuetemp=intersect(find(data(:,j-1)==data(i,j-1)),find(data(:,j)==data(i,j)));
            ICP_hash(keyname)= length(valuetemp);     
            k=k+1;
        end
        
    end

end

