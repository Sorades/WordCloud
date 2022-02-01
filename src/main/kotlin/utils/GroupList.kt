package org.charly.plugin.utils

import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.ValueDescription
import net.mamoe.mirai.console.data.value
import net.mamoe.mirai.contact.Group

object GroupList:AutoSavePluginConfig("GroupList") {
    @ValueDescription("开启词云群名单")
    val groupList:MutableList<Long> by value()


    fun Group.enable():Boolean{
        return this.id in groupList
    }
}