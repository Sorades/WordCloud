package org.charly.plugin.utils

import com.kennycason.kumo.CollisionMode
import com.kennycason.kumo.WordCloud
import com.kennycason.kumo.bg.PixelBoundaryBackground
import com.kennycason.kumo.bg.RectangleBackground
import com.kennycason.kumo.font.KumoFont
import com.kennycason.kumo.font.scale.LinearFontScalar
import com.kennycason.kumo.font.scale.SqrtFontScalar
import com.kennycason.kumo.nlp.FrequencyAnalyzer
import com.kennycason.kumo.nlp.tokenizers.ChineseWordTokenizer
import com.kennycason.kumo.palette.ColorPalette
import net.mamoe.mirai.utils.ExternalResource
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import java.awt.Color
import java.awt.Dimension
import java.awt.Font
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import javax.imageio.ImageIO
import org.charly.plugin.WordCloud as wc


object GenerateCloud {
    fun generateCloud(groupId: Long, data: MutableMap<Long, MutableList<String>>): ExternalResource? {
        //建立词频分析器，设置词频，以及词语最短长度
        val frequencyAnalyzer = FrequencyAnalyzer()
        frequencyAnalyzer.setWordFrequenciesToReturn(Config.wordFreToReturn)
        frequencyAnalyzer.setMinWordLength(Config.minWordLength)

        //引入中文解析器
        frequencyAnalyzer.setWordTokenizer(ChineseWordTokenizer())
        //指定数据，生成词频集合
        val wordFrequencyList = if (data.containsKey(groupId)) frequencyAnalyzer.load(data[groupId]) else null
        if (wordFrequencyList == null) {
            wc.logger.error("该群记录为空，无法生成词云")
            return null
        }

        // 打开遮罩图片
        val maskPicFile = File("${wc.dataFolder}/${Config.maskPicName}")
        var dimension = Dimension(600, 600)
        if (maskPicFile.exists()) {
            // 如果文件存在，设置词云遮罩
            val mask = ImageIO.read(maskPicFile)
            dimension = Dimension(mask.width, mask.height)
        }
        //此处的设置采用内置常量即可，生成词云对象
        val wordCloud = WordCloud(dimension, CollisionMode.PIXEL_PERFECT)

        //设置边界
        wordCloud.setPadding(Config.padding)

        // 设置背景
        if (maskPicFile.exists())
            wordCloud.setBackground(PixelBoundaryBackground(maskPicFile))
        else wordCloud.setBackground(RectangleBackground(dimension))

        // 设置字体，默认楷体
        var font = Font("楷体", 2, 6)
        try {
            val aixing = FileInputStream(File("${wc.dataFolder}/${Config.fontName}"))
            val dynamicFont = Font.createFont(Font.TRUETYPE_FONT, aixing)
            aixing.close()
            font = dynamicFont.deriveFont(2)
        } catch (e: Exception) {
            wc.logger.info("未检测到字体文件，默认使用楷体生成词云")
        }

        wordCloud.setKumoFont(KumoFont(font))
        //设置词云显示的颜色，越靠前设置表示词频越高的词语的颜色
        val colors = mutableListOf<Color>()
        Config.colors.forEach {
            colors.add(Color(it))
        }
        wordCloud.setColorPalette(ColorPalette(colors))
        wordCloud.setBackgroundColor(Color(Config.backColor))
//        wordCloud.setBackground(RectangleBackground(dimension))
        if (Config.fontScalar == 0)
            wordCloud.setFontScalar(LinearFontScalar(Config.fontMin, Config.fontMax))
        else if (Config.fontScalar == 1)
            wordCloud.setFontScalar(SqrtFontScalar(Config.fontMin, Config.fontMax))
        wordCloud.build(wordFrequencyList)

        lateinit var result: ExternalResource

        //转成externalResource
        try {
            val os = ByteArrayOutputStream()
            os.use { it.close() }
            ImageIO.write(wordCloud.bufferedImage, "png", os)
            result = os.toByteArray().toExternalResource()
            result.use { it.close() }
        } catch (e: IOException) {
            wc.logger.error(e)
        }
        return result
    }
}