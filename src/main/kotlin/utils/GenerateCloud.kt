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
import org.charly.plugin.utils.RecordData.record
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
    fun generateCloud(groupId: Long): ExternalResource? {
        //建立词频分析器，设置词频，以及词语最短长度
        val frequencyAnalyzer = FrequencyAnalyzer()
        frequencyAnalyzer.setWordFrequenciesToReturn(600)
        frequencyAnalyzer.setMinWordLength(2)

        //引入中文解析器
        frequencyAnalyzer.setWordTokenizer(ChineseWordTokenizer())
        //指定数据，生成词频集合
        val wordFrequencyList = if (record.containsKey(groupId)) frequencyAnalyzer.load(record[groupId]) else null
        if (wordFrequencyList == null) {
            wc.logger.error("该群记录为空，无法生成词云")
            return null
        }
        //设置图片分辨率
        val dimension = Dimension(565, 565)
        //此处的设置采用内置常量即可，生成词云对象
        val wordCloud = WordCloud(dimension, CollisionMode.PIXEL_PERFECT)
        //设置边界与字体
        wordCloud.setPadding(2)
        wordCloud.setBackground(PixelBoundaryBackground("${wc.dataFolder}/1.png"))

        var font=Font("楷体",2,6)
        try {
            val aixing = FileInputStream(File("${wc.dataFolder}/字体.ttf"))
            val dynamicFont = Font.createFont(Font.TRUETYPE_FONT, aixing)
            aixing.close()
            font = dynamicFont.deriveFont(2)
        } catch (e: Exception) {
            wc.logger.error(e)
        }

        wordCloud.setKumoFont(KumoFont(font))
        //设置词云显示的颜色，越靠前设置表示词频越高的词语的颜色
        wordCloud.setColorPalette(ColorPalette(Color(0xcde11d), Color(0x90d743), Color(0x27ad81), Color(0x3e4c8a),Color(0x3e4c8a),Color(0x46085c),Color(0xfbd023)))
        wordCloud.setBackgroundColor(Color.WHITE)
//        wordCloud.setBackground(RectangleBackground(dimension))
        wordCloud.setFontScalar(LinearFontScalar(4, 80))
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