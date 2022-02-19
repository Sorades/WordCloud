# MiraiWordCloud

这是一个基于 [Kumo](https://github.com/kennycason/kumo) 的词云插件

### 使用方法：

将插件置于plugin目录下，启动控制台。

第一次启动将生成默认配置文件，此时先关闭控制台，打开位于config文件夹下的配置文件，将需要使用词云的群添加至groupList

在群聊环境中使用命令（可在配置文件中修改）：cy / 词云，即可手动获取由昨日数据生成的词云

<img src="https://gitee.com/LeoSora/pic-go/raw/master/img/20220219153919.png" alt="image-20220219153920338" style="zoom:67%;" />

安装插件后，机器人会在 22:00:00 自动发送每日词云，时间同样可在配置文件修改

<img src="https://gitee.com/LeoSora/pic-go/raw/master/img/IMG_20220219_154027.jpg" alt="image-20220219154051676" style="zoom: 50%;" />

如果要使用遮罩或修改字体，将遮罩图片和字体文件放置于data文件夹下，并修改配置文件中的对应名称即可