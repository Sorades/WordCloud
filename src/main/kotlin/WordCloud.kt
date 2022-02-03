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
import net.mamoe.mirai.event.subscribeFriendMessages
import net.mamoe.mirai.event.subscribeGroupMessages
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.utils.ExternalResource.Companion.uploadAsImage
import net.mamoe.mirai.utils.info
import okhttp3.internal.wait
import org.charly.plugin.utils.GenerateCloud
import org.charly.plugin.utils.Config
import org.charly.plugin.utils.Config.enable
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
    private val dailyTask = object : TimerTask() {
        override fun run() {
            logger.error("自动发送词云")
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
                            group?.sendImage(GenerateCloud.generateCloud(groupId, RecordData.record)!!)
                            group?.sendMessage("这是本群今日词云噢（￣︶￣）↗　")
                        } catch (e: RuntimeException) {
                            logger.info("${groupId}:词云图片上传出错")
                        }
                        delay(200)
                    }
                    RecordData.backup = RecordData.record
                    RecordData.record.clear()
                    logger.info("备份今日记录")
                }
            }
        }
    }

    override fun onEnable() {
        init()
        logger.info { "WordCloud Plugin loaded" }
        GlobalEventChannel.subscribeGroupMessages {
            matching(Regex(Config.commandRegex)) {
                if (!group.enable()) return@matching
                logger.info("发起昨日词云请求")
                try {
                    val imageMessage = GenerateCloud.generateCloud(group.id, RecordData.backup)?.uploadAsImage(group)
                    subject.sendMessage(imageMessage!!)
                } catch (e: RuntimeException) {
                    WordCloud.logger.error("图片上传失败, 请检查网络或backup")
                }
            }
        }
        GlobalEventChannel.subscribeFriendMessages {
            startsWith(Config.adminCommand) { msg ->
                if (sender.id != Config.admin) return@startsWith
                val groupId:Long
                try {
                    groupId = msg.replace(" ", "").toLong()
                    if (!RecordData.record.containsKey(groupId))
                        throw Exception("不存在该群的记录")
                }catch (e:Exception){
                    sender.sendMessage(e.toString())
                    return@startsWith
                }
                logger.info("发起今日词云请求")
                try {
                    val imageMessage = GenerateCloud.generateCloud(groupId, RecordData.record)?.uploadAsImage(sender)
                    subject.sendMessage(imageMessage!!)
                } catch (e: RuntimeException) {
                    WordCloud.logger.error("图片上传失败, 请检查网络或backup")
                }
            }
        }
        GlobalEventChannel.subscribeGroupMessages {
            startsWith("") { msg ->
                if (!group.enable()) return@startsWith
                for (v in Config.blockKey) {
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
        dailyTask.cancel()
        logger.info { "WordCloud 插件已卸载" }
    }

    private fun init() {
        RecordData.reload()
        Config.reload()


        val sdf = SimpleDateFormat("yyyy.MM.dd HH:mm:ss")
        val now = Calendar.getInstance()
        // 发送词云的时间

        val date = Date(
            sdf.parse(
                "${now.get(Calendar.YEAR)}." +
                        "${now.get(Calendar.MONTH) + 1}." +
                        "${now.get(Calendar.DAY_OF_MONTH)} ${Config.dailyTaskExecTime}"
            ).time
        )
        if (date.before(Date()))
            date.time += 24 * 60 * 60 * 1000

        try {
            Timer().schedule(dailyTask, date, 24 * 60 * 60 * 1000)
            logger.info("定时任务已添加")
        } catch (e: Exception) {
            logger.error("定时任务添加失败")
        }
    }
}