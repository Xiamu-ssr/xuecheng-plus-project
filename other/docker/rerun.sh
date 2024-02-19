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

# 执行同目录下的docker-compose.yml
#docker-compose -f ./docker-compose.yml up -d
