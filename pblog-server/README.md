

安装总览

mysql  redis  minIO





## 安装docker



## 安装mysql

```
# docker 中下载 mysql
docker pull mysql:8.0.45

#查看是否下载mysql
docker images

#启动
docker run --name mysql -p 3306:3306 -e MYSQL_ROOT_PASSWORD=123 -d mysql:8.0.45

docker run -d \
  --name mysql8.0.45 \          # 容器名称，便于管理
  -p 3306:3306 \                # 端口映射（宿主机:容器）
  -e MYSQL_ROOT_PASSWORD=123456 \ # root 密码（务必修改为自己的）
  -v mysql_data:/var/lib/mysql \ # 数据卷挂载（防止数据丢失）
  mysql:8.0.45                  # 明确指定镜像版本
  
docker run -d --name mysql -p 3306:3306 -e MYSQL_ROOT_PASSWORD=123 -v mysql_data:/var/lib/mysql mysql:8.0.45 

#设置自启动
docker update --restart=always mysql



```



## 安装canal

### 1.开启mysql binlog配置

```
docker rm 只会删除容器本身，不会删除数据卷
docker rm mysql

docker run -d \
  --name mysql \  # 保持原有容器名
  -p 3306:3306 \   # 保持原有端口
  -e MYSQL_ROOT_PASSWORD=你的MySQL密码 \  # 替换为真实密码
  -v /root/mysql/conf:/etc/mysql/conf.d \  # 挂载binlog配置文件
  -v c3cc630be849d87bd8c2368c38259a2d60b2cda05591127900aaff722fdd77fa:/var/lib/mysql \  # 挂载原有数据卷（复制inspect输出的Name值）
  --restart=always \
  mysql
  
docker run -d \
  --name mysql \
  -p 3306:3306 \
  -e MYSQL_ROOT_PASSWORD=123 \
  -v /root/mysql/conf:/etc/mysql/conf.d \
  -v c3cc630be849d87bd8c2368c38259a2d60b2cda05591127900aaff722fdd77fa:/var/lib/mysql \
  --restart=always \
  mysql
```

### 2.进入 MySQL 容器创建 Canal 账号

```
# 进入MySQL容器
docker exec -it mysql mysql -uroot -p123

# 1. 创建canal账号（授权所有权限，也可按需缩小范围）
CREATE USER 'canal'@'%' IDENTIFIED BY 'canal';
# 2. 授权（必须包含REPLICATION SLAVE/CLIENT，否则无法读取binlog）
GRANT SELECT, REPLICATION SLAVE, REPLICATION CLIENT ON *.* TO 'canal'@'%';
# 3. 刷新权限
FLUSH PRIVILEGES;

# 4. 验证binlog状态（确认已开启）
show variables like '%log_bin%';  # 确保log_bin=ON
```



```
# 实例ID（默认example即可）
canal.instance.id=example
# SlaveID（和MySQL的server-id=1不冲突）
canal.instance.mysql.slaveId=1234

# 核心：替换为你的虚拟机主IP 192.168.150.109
canal.instance.master.address=192.168.150.109:3306
# 先确认MySQL当前的binlog文件名（后续验证）
canal.instance.master.log.file=mysql-bin.000001
canal.instance.master.log.position=0
canal.instance.gtidon=false

# MySQL账号密码（之前创建的canal/canal）
canal.instance.dbUsername=canal
canal.instance.dbPassword=canal
canal.instance.connectionCharset=UTF-8

# 监听test_db库所有表
canal.instance.filter.regex=pblog\\..*
canal.instance.filter.black.regex=mysql\\.slave_.*
```



```
docker stop canal
docker rm canal

docker run -d \
--name canal \
-p 11111:11111 \
-v /root/canal/conf:/home/admin/canal/conf \
--restart=always \
canal/canal-server:v1.1.7
```



## 安装redis

```
docker pull redis


## 创建目录
mkdir -p /opt/redis/conf
mkdir -p /opt/redis/data

## 创建文件
touch /opt/redis/conf/redis.conf

docker run --name redis -p 6379:6379 -d redis:latest

# Docker 创建 Redis 容器命令
docker run \
--restart=always \
--log-opt max-size=100m \
--log-opt max-file=2 \
-p 6379:6379 \
--name redis \
-v /opt/redis/conf/redis.conf:/etc/redis/redis.conf  \
-v /opt/redis/data:/data \
-d redis redis-server /etc/redis/redis.conf \
--appendonly yes \
--requirepass 123 


#设置自启动
docker update --restart=always redis


```



## 安装minIO

```
docker pull minio/minio


mkdir ~/minio-storage  # 在用户主目录下创建一个用于存储的目录
docker volume create minio-data  # 创建一个名为minio-data的Docker卷


docker run -p 9000:9000 -p 40753:40753 --name minio   -e "MINIO_ROOT_USER=root"   -e "MINIO_ROOT_PASSWORD=123456789"   -v minio-data:/data   minio/minio server /data --console-address ":40753"

#设置自启动
docker update --restart=always minio

```

安装完访问http://192.168.150.103:40753即可进入控制台





## 安装elasticsearch

```
docker pull docker.elastic.co/elasticsearch/elasticsearch:8.14.0

docker run -d \
  --name es8 \
  --restart always \
  --ulimit memlock=-1:-1 \
  -p 9200:9200 \
  -p 9300:9300 \
  -e "node.name=es8-node1" \
  -e "cluster.name=es-docker-cluster" \
  -e "discovery.type=single-node" \
  -e "bootstrap.memory_lock=true" \
  -e "ES_JAVA_OPTS=-Xms512m -Xmx512m" \
  -e "xpack.security.enabled=true" \  # 关键1：开启安全认证（原命令是false，需改为true）
  -e "xpack.security.enrollment.enabled=true" \  # 允许生成注册令牌（用于Kibana对接）
  -e "ELASTIC_PASSWORD=你的自定义密码" \  # 关键2：设置elastic用户的初始密码（替换为你想要的密码）
  -e "xpack.security.http.ssl.enabled=false" \  # 可选：关闭HTTPS（测试环境简化，生产建议开启）
  -v es-data:/usr/share/elasticsearch/data \
  docker.elastic.co/elasticsearch/elasticsearch:8.14.0
  
docker run -d \
  --name es8 \
  --restart always \
  --ulimit memlock=-1:-1 \
  -p 9200:9200 \
  -p 9300:9300 \
  -e "node.name=es8-node1" \
  -e "cluster.name=es-docker-cluster" \
  -e "discovery.type=single-node" \
  -e "bootstrap.memory_lock=true" \
  -e "ES_JAVA_OPTS=-Xms512m -Xmx512m" \
  -e "xpack.security.enabled=true" \
  -e "xpack.security.enrollment.enabled=true" \
  -e "ELASTIC_PASSWORD=654321" \
  -e "xpack.security.http.ssl.enabled=false" \
  -v es-data:/usr/share/elasticsearch/data \
  docker.elastic.co/elasticsearch/elasticsearch:8.14.0
  
 # 安装ik分词器
 # 1.先去网上下载ik分析器，并上传到tmp目录下
 https://release.infinilabs.com/analysis-ik/stable/
 
 # 2. 先给 es8 容器创建 ik 插件目录（确保目录存在）
docker exec -it es8 mkdir -p /usr/share/elasticsearch/plugins/ik

# 3. 将虚拟机 /tmp 目录下的 ik 包复制到容器的 ik 目录
docker cp /tmp/elasticsearch-analysis-ik-8.14.0.zip es8:/usr/share/elasticsearch/plugins/ik/


# 4.宿主机执行：以 root 用户进入 es8 容器（关键！root 才有修改权限的权限）
docker exec -it --user root es8 /bin/bash

# 5.解压 ik 包（如果之前没解压成功，重新执行）
unzip elasticsearch-analysis-ik-8.14.0.zip && rm -rf elasticsearch-analysis-ik-8.14.0.zip

# 6.给 ik 插件目录及所有文件赋予 elasticsearch 用户权限
chown -R elasticsearch:elasticsearch /usr/share/elasticsearch/plugins/ik

# 验证权限是否修改成功（可选，查看目录权限）
ls -l /usr/share/elasticsearch/plugins/ik

# 退出 root 身份的容器会话
exit

# 8.重启 ES 容器，加载 ik 分词器插件
docker restart es8

```



ES 8.x 不再允许用 `elastic` 超级管理员账号直接连接 Kibana（安全限制），必须创建 Kibana 专用的服务账号 / 普通账号。

```
# 1. 进入 ES 容器
docker exec -it es8 /bin/bash

# 2. 使用 ES 内置命令创建 Kibana 专用账号（设置密码为你自己的，比如 Kibana@654321）
# 执行后按提示输入 elastic 超级管理员密码，再设置新账号密码
/usr/share/elasticsearch/bin/elasticsearch-users useradd kibana_user -p Kibana@654321 -r kibana_system,superuser

# 3. 退出 ES 容器
exit
```



## 安装kibana

```
# 拉取 8.14.0 版本的 Kibana 镜像（必须和 ES 版本一致）
docker pull kibana:8.14.0

# 启动 Kibana 容器，配置和 ES 的连接及认证
docker run -d \
  --name kibana8 \
  --link es8:elasticsearch  # 关联 ES 容器，让 Kibana 能访问 es8
  -p 5601:5601 \  # 宿主机端口 5601 映射到容器 5601（Kibana 默认端口）
  -e "ELASTICSEARCH_HOSTS=https://es8:9200" \  # 连接 ES 的地址（es8 是容器名）
  -e "ELASTICSEARCH_USERNAME=elastic" \  # ES 认证账号
  -e "ELASTICSEARCH_PASSWORD=你的elastic密码" \  # 替换为你的 ES 密码
  -e "ELASTICSEARCH_SSL_VERIFICATIONMODE=none" \  # 忽略 SSL 证书验证（测试环境）
  kibana:8.14.0
```

```
# 启动 Kibana 容器，配置和 ES 的连接及认证
docker run -d \
  --name kibana8 \
  --link es8:elasticsearch \
  -p 5601:5601 \
  -e "ELASTICSEARCH_HOSTS=http://es8:9200" \
  -e "ELASTICSEARCH_USERNAME=kibana_user" \
  -e "ELASTICSEARCH_PASSWORD=Kibana@654321" \
  kibana:8.14.0
```

