package pers.wuliang.robot.botApi.geographyApi

import java.awt.*
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

/**
 *@Description: 将从接口获得的数据进行图片化
 *@Author zeng
 *@Date 2022/11/13 10:36
 *@User 86188
 */
class GeoPicture {
    private fun getCityData(data: String): String? {
        return City.jsonNode?.get(data)?.textValue()
    }

    private fun getWeaData(data: String): String? {
        return Weather.jsonNode?.get(data)?.textValue()
    }

    private fun getPreData(index: Int, data: String): String? {
        return Prediction.jsonNode?.get(index)?.get(data)?.textValue()
    }

    private fun getWidth(fm: FontMetrics, width: Float, text: String): Float {
        return width + (490 - fm.stringWidth(text)) / 2f
    }

    private fun getNextWidth(fm: FontMetrics, width: Float, text: String): Float {
        return width + (326f - fm.stringWidth(text)) / 2f
    }

    private var image: BufferedImage =
        ImageIO.read(File(GeoConfig.Path.geoBg).absoluteFile)

    fun images() {
        val gd: Graphics2D = image.createGraphics()
        // 设置图片品质
        gd.addRenderingHints(RenderingHints(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY))
        // 设置画笔颜色为黑色
        gd.color = Color.white
        // 设置画笔字体样式为微软雅黑，斜体，文字大小为20px
        gd.font = Font("微软雅黑", Font.PLAIN, 100)
        // 计算文字长度，计算居中的x点坐标
        var fm: FontMetrics = gd.getFontMetrics(gd.font)
        var tempText = "${getWeaData("temp")}°C ${getWeaData("text")}"
        var textWidth = fm.stringWidth(tempText)
        val line: Int
        val width: Float
        gd.drawString(tempText, (image.width - textWidth) / 2f, 180f)
        // 粗体白字
        gd.font = Font("微软雅黑", Font.BOLD, 60)
        fm = gd.getFontMetrics(gd.font)

        gd.drawString("${getWeaData("feelsLike")}°C", getWidth(fm, 50f, "${getWeaData("feelsLike")}°C"), 860f)

        gd.drawString("${getWeaData("temp")}°C", getWidth(fm, 540f, "${getWeaData("temp")}°C"), 860f)

        gd.drawString("${getWeaData("humidity")}%", getWidth(fm, 50f, "${getWeaData("humidity")}%"), 1020f)

        gd.drawString(getWeaData("text"), getWidth(fm, 540f, getWeaData("text").toString()), 1020f)

        gd.drawString("${getWeaData("pressure")} hpa", getWidth(fm, 50f, "${getWeaData("pressure")} hpa"), 1180f)

        gd.drawString("${getWeaData("vis")} km", getWidth(fm, 540f, "${getWeaData("vis")} km"), 1180f)

        gd.drawString(
            "${getWeaData("windDir") + getWeaData("wind360")}°",
            getWidth(fm, 50f, "${getWeaData("windDir") + getWeaData("wind360")}°"),
            1340f
        )

        gd.drawString(
            "${getWeaData("windScale")}级-${getWeaData("windSpeed")} m/s",
            getWidth(fm, 540f, "${getWeaData("windScale")}级-${getWeaData("windSpeed")} m/s"),
            1340f
        )

        gd.drawString(
            "${getPreData(1, "sunrise")}",
            getNextWidth(fm, 50f, "${getPreData(1, "sunrise")}"),
            1600f
        )
        gd.drawString(
            "${getPreData(1, "sunset")}",
            (image.width - fm.stringWidth("${getPreData(1, "sunset")}")) / 2f,
            1600f
        )

        gd.drawString(
            "${getPreData(1, "textDay")}",
            getNextWidth(fm, 703f, "${getPreData(1, "textDay")}"),
            1600f
        )

        gd.drawString(
            "${getPreData(1, "tempMax")}°C",
            getNextWidth(fm, 50f, "${getPreData(1, "tempMax")}°C"),
            1760f
        )

        gd.drawString(
            "${getPreData(1, "tempMin")}°C",
            (image.width - fm.stringWidth("${getPreData(1, "tempMax")}°C")) / 2f,
            1760f
        )

        gd.drawString(
            "${getPreData(1, "textNight")}",
            getNextWidth(fm, 703f, "${getPreData(1, "textNight")}"),
            1760f
        )

        gd.font = Font("微软雅黑", Font.PLAIN, 40)
        fm = gd.getFontMetrics(gd.font)
        tempText = "今天白天${getPreData(0, "textDay")}，夜晚${getPreData(0, "textNight")}"
        textWidth = fm.stringWidth(tempText)
        gd.drawString(tempText, (image.width - textWidth) / 2f, 270f)

        val carText = "${DailyWeather.carText}".replace("。", "")

        if (carText.length > 25) {
            val carText1 = carText.substring(0, 25)
            width = (image.width - fm.stringWidth(carText1)) / 2f

            gd.drawString(carText1, width, 360f)
            gd.drawString(carText.substring(25), width, 450f)
            line = 2
        } else {
            width = (image.width - fm.stringWidth(carText)) / 2f
            gd.drawString(carText, width, 360f)
            line = 1
        }

        val sportText = "${DailyWeather.sportText}".replace("。", "")

        if (sportText.length > 25) {
            gd.drawString(sportText.substring(0, 25), width, 390f + line * 90f)
            gd.drawString(sportText.substring(25), width, 480f + line * 90f)
        } else {
            gd.drawString(sportText, width, 360f + line * 90f)
        }

        // 灰字
        gd.color = Color(146, 146, 146)
        gd.drawString(getWeaData("obsTime")?.replace("T", " ")?.replace("+08:00", ""), 703f, 65f)
        gd.color = Color(182, 182, 182)

        val textArray = arrayListOf("体感温度", "外界温度", "相对湿度", "天气情况", "大气压强", "能见度", "风向", "风速")
        var lines = 0
        for (i in 0 until textArray.size) {
            if (i % 2 == 0) {
                gd.drawString(textArray[i], getWidth(fm, 50f, textArray[i]), 920f + 160 * lines)
            }
            if (i % 2 == 1) {
                gd.drawString(textArray[i], getWidth(fm, 540f, textArray[i]), 920f + 160 * lines)
                lines += 1
            }
        }

        val nextArray = arrayListOf("日出时间", "日落时间", "日间天气", "最高气温", "最低气温", "夜间天气")
        var lines2 = 0
        for (i in 0 until nextArray.size) {
            if (i % 3 == 0) {
                gd.drawString(nextArray[i], getNextWidth(fm, 50f, textArray[i]), 1670f + 160 * lines2)
            }
            if (i % 3 == 1) {
                gd.drawString(nextArray[i], (image.width - fm.stringWidth(nextArray[i])) / 2f, 1670f + 160 * lines2)
            }
            if (i % 3 == 2) {
                gd.drawString(nextArray[i], getNextWidth(fm, 703f, nextArray[i]), 1670f + 160 * lines2)
                lines2 += 1
            }
        }

        // 城市名
        gd.color = Color.white
        val cityName = getCityData("name")
        gd.drawString(cityName, 42 + (360 - fm.stringWidth(cityName.toString())) / 2f, 63f)

        val nextText = "----------------${getPreData(1, "fxDate")}----------------"
        gd.drawString(nextText, (image.width - fm.stringWidth(nextText)) / 2f, 1485f)

        gd.dispose()
        ImageIO.write(
            image, "png",
            File(GeoConfig.Path.weather).absoluteFile
        )
    }
}