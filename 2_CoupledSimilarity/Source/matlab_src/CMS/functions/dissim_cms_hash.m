function dissim=dissim_cms_hash(Attribute_hash,vec1,vec2)

% vec1=[1,2];
% vec2=[2,3];
% %data=[1,1,1;2,1,2;1,2,1;1,2,2;2,2,1;1,1,2];
% %data=[1,1,1;1,2,1;2,1,1;2,2,2;3,3,3;3,3,4]; %%car data

dim=size(vec1);
n=dim(1);
d=dim(2);

sim=0;

for i=1:d
    if vec1(i)==vec2(i)
        sim_temp=1;
    else
        keyname=[num2str(i),'_',num2str(vec1(i)),'to',num2str(i),'_',num2str(vec2(i))];
        keyname_r=[num2str(i),'_',num2str(vec2(i)),'to',num2str(i),'_',num2str(vec1(i))];
        if all(ismember(Attribute_hash.keys,keyname)==0)
            keyname_r;
            sim_temp=cell2mat(values(Attribute_hash,{keyname_r})); 
        else
            keyname;
            sim_temp=cell2mat(values(Attribute_hash,{keyname}));
        end
    end
    sim=sim+sim_temp;
end
dissim=1-sim/d;


end