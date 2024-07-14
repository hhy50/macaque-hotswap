




<p align="center">
    <img alt="logo" src="https://github.com/haiyanghan/macaque-hotswap/blob/master/doc/logo.jpg" width="90" height="80">
</p>
<h1 align="center" style="margin: 30px 0 30px; font-weight: bold;">Macaque-hotswap</h1>
<h4 align="center">一个开源的热部署框架。</h4>
<p align="center">
</p>

---

## 用法:

1. 下载源码自编译 `or` 下载 release包
2. 启动服务端
3. 安装idea插件([macaque-plugin](https://github.com/haiyanghan/macaque-plugin))
4. idea插件里面设置服务端地址和端口
5. 右击Java文件选择服务端进程

## 使用限制

目前已知

- 无法新增方法，新增字段（通过[兼容模式](#兼容模式)解决）
- 无法修改已有方法的签名（通过[兼容模式](#兼容模式)解决）
- 内部类无法替换
- 无法新增类
- 栈顶的方法（即正在运行的方法）无法替换

> 后续会对以上未实现功能进行逐步优化解决

---

## 快速开始

#### 1. 源码编译

```shell
git clone https://github.com/haiyanghan/macaque-hotswap.git
cd macaque-hotswap
./gradlew macaque-server:release
```

编译后的路径位于 `macaque-server/build/distributions/macaque-server-${version}.tar.gz`
> 需要jdk1.8的环境

#### 2. 启动服务端

将打包后的压缩包上传到运行`jvm`的服务器并解压缩

首先需要修改 `runServer` 脚本里的 `JAVA_HOME`
然后执行

```shell
chmod +x runServer.sh
sh runServer.sh
```

> 请尽量使用`root`用户进行启动，如果使用其他用户，则可能会出现找不到相应的jvm进程的情况

#### 3. 安装idea插件

插件项目地址： [macaque-plugin](https://github.com/haiyanghan/macaque-plugin)

鉴于目前还没有上idea的插件市场，您可以从`release`里面下载最新的到本地，从本地进行安装

#### 4. 设置服务端地址和端口

Idea -> File -> Settings -> Tools -> Macaque

<image src="https://github.com/haiyanghan/macaque-hotswap/blob/master/doc/images/macaque-settings.png" style="width: 500px;height: 330px"></image>

> 端口在 runServer脚本里面通过 `--serverPort=2023` 进行设置

#### 5. 使用

右击Java文件 -> 选择进程

1. 选择进程后会弹出选择框，选择编译选项
    - `Rebuild project` 重新编译整个项目
    - `recompile` 增量编译所属模块
    - `Use Compiled` 使用编译好的
2. 编译完成后，通过确认框再次确认

<image src="https://github.com/haiyanghan/macaque-hotswap/blob/master/doc/images/useage.gif"></image>

---

## 运行原理

待补充

## 待开发功能

#### Server端

|         | 功能描述                    | 开发状态 |
|---------|-------------------------|------|
| 兼容模式    | [兼容模式](#兼容模式)           | ×    |
| 版本链     | [版本链](#版本链)             | ×    |
| class文件对比 | [class文件对比](#class文件对比) | ×    |

---

#### 插件端

|       | 功能描述                    | 开发状态 |
|-----------|-----------------------|------|
| 多文件选择     | 一次替换多个文件              | ×    |
| 多服务端设置    | 配置多个服务端               | √    |
| 无服务端模式    | 服务端地址为本地的话, 不需要运行服务端就可以进行热替换 | √    |
| 进程过滤      | 指定表达式过滤服务端的进程         | √    |
| 增强Local模式 | 本地模式下修改Java文件自动热部署    | ×    |


#### 其他idea

|                | 功能描述                                 | 开发状态 |
|--------------------|------------------------------------|------|
| springboot-starter | 通过引入依赖, 使用springweb的方式来代替server端进程 | ×    |

## 功能描述

#### 兼容模式

项目依赖 `Instrumentation Api`中的 `redefineClasses`和`retransformClasses`方法，
这两个方法都限制了新的`class`相比旧的`class`不能有新增的方法和新增的字段，
兼容模式下会对需要替换的class对象加工增强，间接的实现新增方法和新增字段，
这样会导致加工后的`class`和原本预期的`class`字节码不一致等

#### 版本链

每次热替换都会以`class`为单位生成一个版本号, 可以在插件里面选择指定的版本进行回滚

#### class文件对比

本地可以选择和服务端的某个进程中正在运行的`class`（可以基于版本链）进行对比, 对比的粒度可以是字节码或者是反编译后的Java文件

## 联系方式

QQ群: `904726708 `

## 贡献代码

本人个人精力有限，欢迎对该项目感兴趣的小伙伴来贡献代码，贡献范围包括但不限于：

1. 修复bug
2. 代码优化
3. 新的功能
4. 补充注释
5. 补充文档

两种贡献方法：
1. 提交`pull request`
2. 成为仓库成员