package org.charly.plugin

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.mamoe.mirai.Bot
import net.mamoe.mirai.console.command.descriptor.ExperimentalCommandDescriptors
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import net.mamoe.mirai.contact.Contact.Companion.sendImage
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.event.GlobalEventChannel
import net.mamoe.mirai.event.subscribeGroupMessages
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.utils.ExternalResource.Companion.uploadAsImage
import net.mamoe.mirai.utils.info
import org.charly.plugin.utils.GenerateCloud
import org.charly.plugin.utils.GroupList
import org.charly.plugin.utils.GroupList.enable
import org.charly.plugin.utils.RecordData
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ConsoleExperimentalApi::class, ExperimentalCommandDescriptors::class)
object WordCloud : KotlinPlugin(
    JvmPluginDescription(
        id = "org.charly.plugin.WordCloud",
        name = "Mirai词云插件",
        version = "1.0.0",
    ) {
        author("CharlyWayne")
        info("""基于kumo的词云生成插件""")
    }
) {
    private val clearCache = object : TimerTask() {
        override fun run() {
            if (RecordData.record.isNotEmpty()) {
                RecordData.record.clear()
                WordCloud.logger.info("群消息自动清理")
            }
        }
    }
    private val autoSend = object : TimerTask() {
        override fun run() {
            logger.info("自动发送词云")
            if (RecordData.record.isEmpty()) {
                logger.info("记录为空，无法发送词云")
                return
            }
            Bot.instances.forEach {
                launch {
                    for ((groupId, message) in RecordData.record) {
                        val group: Group? = it.getGroup(groupId)
                        if (message.isEmpty())
                            continue
                        try {
                            group?.sendImage(GenerateCloud.generateCloud(groupId)!!)
                            group?.sendMessage("这是本群今日词云噢（￣︶￣）↗　")
                        } catch (e: RuntimeException) {
                            logger.info("词云图片上传出错")
                        }
                        delay(200)
                    }
                }
            }
        }
    }

    override fun onEnable() {
        init()
        logger.info { "WordCloud Plugin loaded" }
        GlobalEventChannel.subscribeGroupMessages {
            startsWith("/cy") { msg ->
                if (!group.enable() || sender.id != 1546114957L) return@startsWith
                if (msg != "") return@startsWith
                logger.info("发起词云请求")
                try {
                    val imageMessage: Image =
                        GenerateCloud.generateCloud(subject.id)?.uploadAsImage(subject) ?: return@startsWith
                    subject.sendMessage(imageMessage)
                } catch (e: RuntimeException) {
                    WordCloud.logger.error(e)
                    WordCloud.logger.error("图片上传失败, 请检查网络")
                }
            }
        }
        GlobalEventChannel.subscribeGroupMessages {
            startsWith("") { msg ->
                if (!group.enable()) return@startsWith
                val blockVal = listOf("/cy", "语音消息", "不支持的消息", "视频", "https", "@", "?xml")
                for (v in blockVal) {
                    if (msg.contains(v))
                        return@startsWith
                }

                val content = msg.replace("[动画表情]", "")
                    .replace("[图片]", "")
                    .replace("[表情]", "")
                    .replace("/", "")

                if (content.isEmpty()) return@startsWith

                try {
                    if (RecordData.record.containsKey(subject.id)) {
                        RecordData.record[subject.id]!!.add(content)
                    } else {
                        RecordData.record[subject.id] = mutableListOf(content)
                    }
                    logger.debug("store ok")
                } catch (e: Exception) {
                    logger.error("消息存储失败")
                }
            }
        }
    }

    override fun onDisable() {
        logger.info { "WordCloud 插件已卸载" }
    }

    private fun init() {
        RecordData.reload()
        GroupList.reload()


        val sdf = SimpleDateFormat("yyyy.MM.dd HH:mm:ss")
        val now = Calendar.getInstance()
        // 清理内存的时间
        val ccStartTime: Long =
            sdf.parse(
                "${now.get(Calendar.YEAR)}." +
                        "${now.get(Calendar.MONTH) + 1}." +
                        "${now.get(Calendar.DAY_OF_MONTH) + 1} 00:00:00"
            ).time
        // 发送词云的时间
        val asStartTime: Long =
            sdf.parse(
                "${now.get(Calendar.YEAR)}." +
                        "${now.get(Calendar.MONTH) + 1}." +
                        "${now.get(Calendar.DAY_OF_MONTH)} 23:45:00"
            ).time
        try {
            Timer().schedule(clearCache, Date(ccStartTime), 24 * 60 * 60 * 1000)
            Timer().schedule(autoSend, Date(asStartTime), 24 * 60 * 60 * 1000)
            logger.info("定时任务已添加")
        } catch (e: Exception) {
            logger.error("定时任务添加失败")
        }
    }
}