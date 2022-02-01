package org.charly.plugin.utils

import net.mamoe.mirai.console.data.AutoSavePluginData
import net.mamoe.mirai.console.data.value

object RecordData : AutoSavePluginData("RecordData") {
    val record: MutableMap<Long, MutableList<String>> by value()
}

