## 使用说明
* 将要加固的应用放到input文件夹，执行脚本Jiagu_input.bat进行加固，加固后的应用将输出到output文件夹

## keystore替换
* 1.如果需要修改keystore，可以替换keystore文件夹的test.jks文件
* 2.修改Jiagu_input.bat，修改"java -jar pack.jar -apk %%i -key keystore/xxx.jks -kp test123 -alias test -ap test123"。kp后面接keystore password，alias后面接密钥别名，ak后面接alias password