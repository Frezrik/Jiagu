# APK加固
* 支持Android5.0及其以上版本(已在5.1、7.1、8.1、10.0、11.0真机测试)
* 支持多dex加固
* 采用不落地加载dex方案

## 一.模块说明
### 工具开发调试使用
* app: 需要加固的源程序代码
* jiagu: 用于生成壳dex
* pack: java执行程序，用于打包加固app
### Release工具
* JigguTool: 加固工具打包，如果只需要使用加固功能，直接用这个目录的工具即可

## 二.脚本说明
### 工具开发调试使用
* Jiagu_app.bat：编译打包app，打包出来的应用用于加固测试
* Jiagu_build.bat：编译生成加固工具
* Jiagu_input.bat：对input目录下的apk进行加固
### Release工具
* Jiagu_update.bat：将Jiagu_build编译出来的工具更新到JiaguTool目录

## 三.使用说明
### 方法一(工具开发调试使用)：
* 1.执行脚本Jiagu_build.bat生成加固工具
* 2.将要加固的应用放到input文件夹，执行脚本Jiagu_input.bat进行加固，加固后的应用将输出到output文件夹
### 方法二(Release工具)：
* 加固环境会打包到JiaguTool，可以直接使用jiaguTool进行应用加固
* 如果修改了加固工具代码，可执行Jiagu_updata.bat重新打包加固工具

## 四.加固原理
### 1.加固工具处理
* 获取app的application名
* 修改app的AndroidManifest.xml的Application为dex壳的Application
* 壳dex和源dex拼接成一个dex，格式如下：
    * 壳dex + (1字节application名长度 + app的application名 + 4字节源dex大小 + 源dex) + 4字节源dex2大小 + [源dex2] + 4字节源dex3大小 + [源dex3] + ... + 4字节壳dex大小
    * 小括号的数据前512字节进行AES加密
    * 中括号的数据的dex头(也就是前112位进行异或)
* 打包签名app

### 2.壳dex处理
* 解密出app的application名和dex
* 加载app的dex
    * Android8.0以下版本采用call libart的OpenMemory函数实现内存加载dex
    * Android8.0及以上采用系统提供的InMemoryDexClassLoader实现内存加载dex
* 壳application替换为app的application

