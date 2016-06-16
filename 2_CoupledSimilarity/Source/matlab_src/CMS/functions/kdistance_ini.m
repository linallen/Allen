function label=kdistance_ini(dis_matrix,k)
%input:dis_matrix is the ditance matrix,k is the cluster number
%output:cluter label 

% data=[1,1,1;1,2,1;2,1,1;2,2,2;3,3,3;3,3,4];
% matrix=calsim_hash(data,1);
% dis_matrix=(1./matrix)-1;
% k=2;
%the label is cluter label, index is the node
%% initialize the point of closure randomly
n=length(dis_matrix);
node_index=[1:n];
label=zeros(n,1);
cluster_set={};
seed=[];
%% choose randomly
% 
% rnd=ceil(rand*n);
% for i=1:k
%     rnd=ceil(rand*n);
%     seed(i)=rnd;
%     cluster_set{i}=rnd;
%     label(rnd)=i;
% end
%% initialize the point of closure by distance
maxdis=max(max(dis_matrix));
ff=1;
row=[];
column=[];
while ff<=k
    if ff==1
        [row,column]=find(dis_matrix==maxdis); 
        if length(row)>1
            leng=length(row);
            rnd=ceil(rand*leng);
            row=row(rnd);
            column=column(rnd);
        end
        seed=[row,column];
        cluster_set{1}=row;
        cluster_set{2}=column;
        label(row)=1;
        label(column)=2;
        ff=ff+2;
 
    else
        row_long=dis_matrix(row,:);
        diff=setdiff([1:n],seed);
        row_long_new=row_long(diff);

        max_row=max(row_long_new);
        [~,row]=find(row_long==max_row);
        if length(row)>1
            leng=length(row);
            rnd=ceil(rand*leng);
            row=row(rnd);
        end
        cluster_set{ff}=row;
        label(row)=ff;
        seed=[seed,row];
        ff=ff+1 ; 
    end
end
   


% label;
% cluster_set{1};
% cluster_set{2};
% seed;

%% assign each node to the nearest closure

abc=k;
iretation_time=0;
while abc~=0
%while iretation_time~=6
abc=k;
for i=1:n
    if all(ismember(seed,i)==0)
    point=i;
    min_dis=100000;
    for j=1:k
        dis_matrix(point,cluster_set{j});
        [dis,flag]=min(dis_matrix(point,cluster_set{j}));% distance to cluster j
        if dis<min_dis
            min_dis=dis;
            min_k=j;
        end
    end
    if ismember(cluster_set{min_k},i)
    else
        if label(i)==min_k
        else
            if label(i)~=0
                setdiff(cluster_set{label(i)},i);   %remove i from  the former cluster
            end
            %cluster_set{label(i)}(cluster_set{label(i)}==i)=[];
            cluster_set{min_k}=[cluster_set{min_k},i];
            cluster_set{min_k};
            label(i)=min_k;
        end
    end
    end

end
%cluster_set{1};
%cluster_set{2};
label;

%dis_matrix(5,:)=[1,2,3,4,5,6];
%dis_matrix(:,5)=[1,2,3,4,5,6]';
%% calculate the new seed of cluster
% caluculate distance between any two closures
closest_cluster=zeros(k,1);
for i=1:k
    closest_dis=zeros(k,1);
    for j=1:k
        if i==j
        else
            dis_cluster=max(max(dis_matrix(cluster_set{i},cluster_set{j})));
            closest_dis(j)=dis_cluster;
        end
    end 
    [~,index]=max(closest_dis);
    closest_cluster(i)=index;
end
% choose the point of closure with farest distance to the closest closure
for i=1:k
    dis_i_cluster=[];
    
    for j=cluster_set{i}
        cluster=closest_cluster(i);
        dis_i_cluster(j)=min(dis_matrix(j,cluster_set{cluster}));
    end
    [~,new_seed]=max(dis_i_cluster);
    if seed(i)==new_seed
        abc=abc-1;
    else
        seed(i)=new_seed;  
        abc=k;
    end
end
iretation_time=iretation_time+1;
            
save kdis;
    
end
iretation_time
end

