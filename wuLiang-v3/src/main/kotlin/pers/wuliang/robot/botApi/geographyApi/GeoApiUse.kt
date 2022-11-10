package pers.wuliang.robot.botApi.geographyApi

import love.forte.simboot.annotation.Filter
import love.forte.simboot.annotation.FilterValue
import love.forte.simboot.filter.MatchType
import love.forte.simbot.event.GroupMessageEvent
import org.springframework.stereotype.Component
import pers.wuLiang.robot.core.annotation.RobotListen
import pers.wuLiang.robot.core.common.send

/**
 *@Description:|\/dl {{city}}
 *@Author zeng
 *@Date 2022/11/9 18:31
 *@User 86188
 */
@Component
class GeoApiUse {

    fun getData(index: Int, data: String): String? {
        return Prediction.jsonNode?.get(index)?.get(data)?.textValue()
    }

    @RobotListen(desc = "查询城市地理情况")
    @Filter("{{city}}地理", matchType = MatchType.REGEX_MATCHES)
    @Filter("/dl {{city}}", matchType = MatchType.REGEX_MATCHES)
    suspend fun GroupMessageEvent.findCity(@FilterValue("city") city: String) {
        // 将城市传回数据接口
        GeoApi().getCityData(city)
        var cityLon = City.lon?.toFloat()
        var cityLat = City.lat?.toFloat()
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
            "城市名称:${City.name}\n" +
                    "城市ID:${City.id}\n" +
                    "所属国家:${City.country}\n" +
                    "上级行政区划:${City.adm1}\n" +
                    "城市维度:${cityLatChange}\n" +
                    "城市经度:${cityLonChange}\n" +
                    "所属时区:${City.tz}"
        } else {
            City.errorMsg.toString()
        }
        send(msg)
        City.adm1 = null
        City.adm2 = null
        City.id = null
    }

    @RobotListen(desc = "查询城市天气情况")
    @Filter("{{city}}天气", matchType = MatchType.REGEX_MATCHES)
    @Filter("/tq {{city}}", matchType = MatchType.REGEX_MATCHES)
    suspend fun GroupMessageEvent.findWeather(@FilterValue("city") city: String) {
        // 将城市传回数据接口
        GeoApi().getCityData(city)
        GeoApi().getWeatherData()
        GeoApi().preWeather()
        val msg: String = if (Weather.code == "200") {
            "无量姬·零贰已经获取到了 ${City.adm1 + City.adm2} 的天气呢\n" +
                    "更新时间: \n" +
                    "${Weather.updateTime}\n" +
                    "气温: ${Weather.temp}°C   体感温度: ${Weather.feelsLike}°C\n" +
                    "天气状况: ${Weather.text}\n" +
                    "风力情况: ${Weather.windDir}-${Weather.wind360}°-${Weather.windScale} 级\n" +
                    "相对湿度: ${Weather.humidity} %\n" +
                    "大气压强: ${Weather.pressure} 百帕\n" +
                    "能见度: ${Weather.vis} 公里\n" +
                    "洗车指南: ${DailyWeather.carText}\n" +
                    "阿姬小提示: ${DailyWeather.sportText}\n" +
                    "--------${getData(1, "fxDate")}-------\n" +
                    "日出时间:${getData(1, "sunrise")}   日落时间:${getData(1, "sunset")}\n" +
                    "最高气温:${getData(1, "tempMax")}°C    最低气温:${getData(1, "tempMin")}°C \n" +
                    "日间天气:${getData(1, "textDay")}    夜间天气:${getData(1, "textNight")}"
        } else {
            Weather.errorMsg.toString()
        }
        send(msg)
        City.adm1 = null
        City.adm2 = null
        City.id = null
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
                    "日间风力:\n${getData(i, "windScaleDay")}级${getData(i, "windDirDay")}-${getData(i, "wind360Day")}° ${getData(i, "windSpeedDay")}m/s\n" +
                    "夜间风力:\n${getData(i, "windScaleNight")}级${getData(i, "windDirNight")}-${getData(i, "wind360Night")}° ${getData(i, "windSpeedNight")}m/s\n" +
                    "相对湿度: ${getData(i, "humidity")} %\n" +
                    "大气压强: ${getData(i, "pressure")} 百帕\n" +
                    "能见度: ${getData(i, "vis")} 公里"
            send(msg)
        }
        City.adm1 = null
        City.adm2 = null
        City.id = null
    }
}