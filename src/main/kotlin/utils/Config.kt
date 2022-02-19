package org.charly.plugin.utils

import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.ValueDescription
import net.mamoe.mirai.console.data.value
import net.mamoe.mirai.contact.Group
import java.awt.Color

object Config : AutoSavePluginConfig("Config") {
    @ValueDescription("开启词云群名单")
    val groupList: MutableList<Long> by value()

    fun Group.enable(): Boolean {
        return this.id in groupList
    }

    @ValueDescription("词云管理员")
    val admin:Long by value()
    @ValueDescription("管理员今日词云查询指令")
    val adminCommand:String by value("cy")

    @ValueDescription("群消息屏蔽关键词")
    val blockKey: List<String> by value(listOf("cy","词云", "语音消息", "不支持的消息", "视频", "@", "?xml", "https:", "http:"))

    @ValueDescription("自动清理和备份的时间设定")
    val dailyTaskExecTime: String by value("22:00:00")

    @ValueDescription("遮罩图片，找不到则默认600*600的矩形")
    val maskPicName: String by value("1.png")

    @ValueDescription("字体名称，需和文件对应，找不到则默认楷体")
    val fontName: String by value("字体.ttf")

    @ValueDescription("词语最短长度")
    val minWordLength: Int by value(2)

    @ValueDescription("词频")
    val wordFreToReturn: Int by value(600)

    @ValueDescription("边界大小")
    val padding: Int by value(2)

    @ValueDescription("词云字体颜色，填十六进制")
    val colors: List<Int> by value(
        listOf(
            0xfbd023,
            0xcde11d,
            0x90d743,
            0x27ad81,
            0x3e4c8a,
            0x3e4c8a,
            0x46085c
        )
    )

    @ValueDescription("词云背景颜色, 默认白色0xffffff")
    val backColor:Int by value(0xffffff)

    @ValueDescription("词云梯度方式,0表示Linear, 1表示Sqrt")
    val fontScalar:Int by value(0)

    @ValueDescription("最小字体")
    val fontMin:Int by value(6)

    @ValueDescription("最大字体")
    val fontMax:Int by value(80)

    @ValueDescription("昨日词云触发指令正则表达式")
    val commandRegex:String by value("词云|cy")
}