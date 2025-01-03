# 智能质检系统设计

- 仅用于个人测试

## 框架

- snowy开源框架

## 启动流程

### 安装相关程序

- node.js==22.12.0
- java==17.0.13
- mysql==9.0.1
- redis-server

#### 使用nvm安装npm，并安装依赖
[nvm](https://github.com/coreybutler/nvm-windows/releases/tag/1.2.2)

```cmd
# nvm管理npm版本
nvm install 22.12.0
nvm use 22.12.0

# npm设置淘宝镜像源
npm config set registry https://registry.npmmirror.com
cd snowy-admin-web
npm install
```

#### 用IDEA安装Java：azul-17.0.13

#### 安装mysql
[MySQL-Community](https://dev.mysql.com/downloads/mysql/`)

#### 安装redis
[redis](https://github.com/microsoftarchive/redis/releases)

* 切换到redis文件夹执行`redis-server.exe redis.windows.conf`

### SQL执行

1. 新建数据库snowy
   - root
   - 123456
2. 执行两个sql文件
   - `snowy-web-app/src/main/resources/_sql/snowy_mysql.sql`
   - `snowy-plugin/snowy-plugin-inspection/src/main/resources/_sql/inspection_mysql.sql`


