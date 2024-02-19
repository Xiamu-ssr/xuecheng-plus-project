#!/bin/bash

# 获取所有名字以xuecheng-plus开头的容器的ID
container_ids=$(docker ps -a --filter "name=xuecheng-plus" -q)

# 如果找到了容器，那么暂停并删除它们
if [ ! -z "$container_ids" ]; then
    echo "Stopping and removing containers: $container_ids"
    docker stop $container_ids
    docker rm $container_ids
else
    echo "No containers with name starting with 'xuecheng-plus' found."
fi

# 将jar移动到宿主机
mkdir /root/springcloud

cp -r ./xuecheng-plus-gateway/target/xuecheng-plus-gateway.jar													/root/springcloud/xuecheng-plus-gateway.jar
cp -r ./xuecheng-plus-learning/xuecheng-plus-learning-api/target/xuecheng-plus-learning-api.jar 				/root/springcloud/xuecheng-plus-learning-api.jar
cp -r ./xuecheng-plus-orders/xuecheng-plus-orders-api/target/xuecheng-plus-orders-api.jar 						/root/springcloud/xuecheng-plus-orders-api.jar
cp -r ./xuecheng-plus-content/xuecheng-plus-content-api/target/xuecheng-plus-content-api.jar 					/root/springcloud/xuecheng-plus-content-api.jar
cp -r ./xuecheng-plus-media/xuecheng-plus-media-api/target/xuecheng-plus-media-api.jar							/root/springcloud/xuecheng-plus-media-api.jar
cp -r ./xuecheng-plus-auth/target/xuecheng-plus-auth.jar														/root/springcloud/xuecheng-plus-auth.jar
cp -r ./xuecheng-plus-search/target/xuecheng-plus-search.jar													/root/springcloud/xuecheng-plus-search.jar
cp -r ./xuecheng-plus-system/xuecheng-plus-system-api/target/xuecheng-plus-system-api.jar						/root/springcloud/xuecheng-plus-system-api.jar
