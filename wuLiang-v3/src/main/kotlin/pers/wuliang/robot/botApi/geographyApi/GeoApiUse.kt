package pers.wuliang.robot.botApi.geographyApi

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import love.forte.simboot.annotation.Filter
import love.forte.simboot.annotation.FilterValue
import love.forte.simboot.filter.MatchType
import love.forte.simbot.Api4J
import love.forte.simbot.event.GroupMessageEvent
import love.forte.simbot.message.Image.Key.toImage
import love.forte.simbot.resources.Resource.Companion.toResource
import org.springframework.stereotype.Component
import pers.wuliang.robot.core.annotation.RobotListen
import pers.wuliang.robot.core.common.send
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

/**
 *@Description:城市信息查询接口
 *@Author zeng
 *@Date 2022/11/9 18:31
 *@User 86188
 */
@Component
class GeoApiUse {

    fun getCityData(data: String): String? {
        return City.jsonNode?.get(data)?.textValue()
    }

    fun getData(index: Int, data: String): String? {
        return Prediction.jsonNode?.get(index)?.get(data)?.textValue()
    }

    fun clearData() {
        City.jsonNode = null
        Weather.jsonNode = null
        Prediction.jsonNode = null
    }

    @OptIn(Api4J::class)
    @RobotListen(desc = "查询城市地理情况")
    @Filter("{{city}}地理", matchType = MatchType.REGEX_MATCHES)
    @Filter("/dl {{city}}", matchType = MatchType.REGEX_MATCHES)
    suspend fun GroupMessageEvent.findCity(@FilterValue("city") city: String) {
        // 将城市传回数据接口
        GeoApi().getCityData(city)
        var cityLon = getCityData("lon")?.toFloat()
        var cityLat = getCityData("lat")?.toFloat()
        var cityLonChange: String? = null
        var cityLatChange: String? = null

        // 将获得的经纬度调整为统一格式
        if (cityLon != null && cityLat != null) {
            if (cityLon < 0) {
                cityLon = -cityLon
                cityLonChange = "$cityLon°W"
            } else {
                cityLonChange = "$cityLon°E"
            }
            if (cityLat < 0) {
                cityLat = -cityLat
                cityLatChange = "$cityLat°S"
            } else {
                cityLatChange = "$cityLat°N"
            }
        }
        val msg: String = if (City.code == "200") {
            "城市名称:${getCityData("name")}\n" +
                    "城市ID:${getCityData("id")}\n" +
                    "所属国家:${getCityData("country")}\n" +
                    "上级行政区划:${getCityData("adm1")}\n" +
                    "城市维度:${cityLatChange}\n" +
                    "城市经度:${cityLonChange}\n" +
                    "所属时区:${getCityData("tz")}"
        } else {
            City.errorMsg.toString()
        }
        replyBlocking(msg)

        clearData()
        send(File(GeoConfig.Path.city).absoluteFile.toResource().toImage())
        // 删除生成的图片减少内存占用
        withContext(Dispatchers.IO) {
            Files.delete(Paths.get(File(GeoConfig.Path.city).absolutePath))
        }
        System.gc()
    }

    @RobotListen(desc = "查询城市天气情况")
    @Filter("{{city}}天气", matchType = MatchType.REGEX_MATCHES)
    @Filter("/tq {{city}}", matchType = MatchType.REGEX_MATCHES)
    suspend fun GroupMessageEvent.findWeather(@FilterValue("city") city: String) {
        // 将城市传回数据接口
        GeoApi().getCityData(city)
        GeoApi().getWeatherData()
        GeoApi().preWeather()
        GeoPicture().images()
        send(File(GeoConfig.Path.weather).absoluteFile.toResource().toImage())
        withContext(Dispatchers.IO) {
            Files.delete(Paths.get(File(GeoConfig.Path.weather).absolutePath))
        }
        clearData()
        System.gc()
    }

    @RobotListen(desc = "城市三天预报")
    @Filter("{{city}}预报", matchType = MatchType.REGEX_MATCHES)
    @Filter("/yb {{city}}", matchType = MatchType.REGEX_MATCHES)
    suspend fun GroupMessageEvent.perWeather(@FilterValue("city") city: String) {
        // 将城市传回数据接口
        GeoApi().getCityData(city)
        GeoApi().preWeather()
        var msg: String
        for (i in 0..2) {
            msg = "--------${getData(i, "fxDate")}-------\n" +
                    "日出时间:${getData(i, "sunrise")}  日落时间:${getData(i, "sunset")}\n" +
                    "最高气温: ${getData(i, "tempMax")}°C  最低气温: ${getData(i, "tempMin")}°C \n" +
                    "日间天气: ${getData(i, "textDay")}  夜间天气: ${getData(i, "textNight")}\n" +
                    "日间风力:\n${getData(i, "windScaleDay")}级${getData(i, "windDirDay")}-${
                        getData(
                            i,
                            "wind360Day"
                        )
                    }° ${getData(i, "windSpeedDay")}m/s\n" +
                    "夜间风力:\n${getData(i, "windScaleNight")}级${getData(i, "windDirNight")}-${
                        getData(
                            i,
                            "wind360Night"
                        )
                    }° ${getData(i, "windSpeedNight")}m/s\n" +
                    "相对湿度: ${getData(i, "humidity")} %\n" +
                    "大气压强: ${getData(i, "pressure")} 百帕\n" +
                    "能见度: ${getData(i, "vis")} 公里"
            send(msg)
        }
        clearData()
        System.gc()
    }
}