#!/bin/bash

# 将jar移动到宿主机
mkdir /home/mumu/springcloud

cp -r ./xuecheng-plus-gateway/target/xuecheng-plus-gateway.jar													/cygdrive/c/Users/mumu/Desktop/springcloud/xuecheng-plus-gateway.jar
cp -r ./xuecheng-plus-learning/xuecheng-plus-learning-api/target/xuecheng-plus-learning-api.jar 				/cygdrive/c/Users/mumu/Desktop/springcloud/xuecheng-plus-learning-api.jar
cp -r ./xuecheng-plus-orders/xuecheng-plus-orders-api/target/xuecheng-plus-orders-api.jar 						/cygdrive/c/Users/mumu/Desktop/springcloud/xuecheng-plus-orders-api.jar
cp -r ./xuecheng-plus-content/xuecheng-plus-content-api/target/xuecheng-plus-content-api.jar 					/cygdrive/c/Users/mumu/Desktop/springcloud/xuecheng-plus-content-api.jar
cp -r ./xuecheng-plus-media/xuecheng-plus-media-api/target/xuecheng-plus-media-api.jar							/cygdrive/c/Users/mumu/Desktop/springcloud/xuecheng-plus-media-api.jar
cp -r ./xuecheng-plus-auth/target/xuecheng-plus-auth.jar														/cygdrive/c/Users/mumu/Desktop/springcloud/xuecheng-plus-auth.jar
cp -r ./xuecheng-plus-search/target/xuecheng-plus-search.jar													/cygdrive/c/Users/mumu/Desktop/springcloud/xuecheng-plus-search.jar
cp -r ./xuecheng-plus-system/xuecheng-plus-system-api/target/xuecheng-plus-system-api.jar						/cygdrive/c/Users/mumu/Desktop/springcloud/xuecheng-plus-system-api.jar

#scp -r "C:\Users\mumu\Desktop\springcloud\*" root@192.168.101.65:/root/XChengOnline/docker/springcloud
scp -r "C:\Users\mumu\Desktop\springcloud\*" root@123.60.168.191:/root/xuecheng-plus-guide/docker/springcloud
