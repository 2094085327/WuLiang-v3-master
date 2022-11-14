package pers.wuliang.robot.botApi.geographyApi

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Component
import pers.wuliang.robot.util.HttpUtil

var objectMapper = ObjectMapper()


/**
 *@Description: 调用天气接口获得数据
 *@Author zeng
 *@Date 2022/11/9 16:51
 *@User 86188
 */
// 城市信息
object City {
    var sendCity: String? = null
    var code: String? = null
    var jsonNode: JsonNode? = null
    var errorMsg: String? = null
}

// 城市天气
object Weather {
    var code: String? = null
    var updateTime: String? = null
    var jsonNode: JsonNode? = null
    var errorMsg: String? = null
}

// 每日提示
object DailyWeather {
    var code: String? = null
    var sportText: String? = "今天没有提示哦"
    var carText: String? = "今天没有提示哦"
    var errorMsg: String? = null
}

// 三天预报
object Prediction {
    var code: String? = null
    var jsonNode: JsonNode? = null
    var updateTime: String? = null
    var errorMsg: String? = null
}

@Component
class GeoApi {

    var key: String? = GeoConfig.key

    fun getCityData(city: String) {
        val url = "https://geoapi.qweather.com/v2/city/lookup?location=$city&key=${key}"
        try {
            City.sendCity = city
            val json = objectMapper.readTree(HttpUtil.get(url).response)
            City.code = json["code"].textValue()
            if (City.code == "200") {
                City.jsonNode = json["location"][0]
            } else {
                City.errorMsg = "阿姬没有找到${city}的地理信息哦\n只能查询城市和省/州的信息"
            }
        } catch (e: Exception) {
            e.printStackTrace()
            City.errorMsg = "接口肯定是炸了!快来修接口！"
        }
    }

    fun getWeatherData() {
        getDailyWeather()
        val url = "https://devapi.qweather.com/v7/weather/now?location=${
            City.jsonNode?.get("id")?.textValue()
        }&key=${key}"
        try {
            val json = objectMapper.readTree(HttpUtil.get(url).response)
            Weather.code = json["code"].textValue()
            if (Weather.code == "200") {
                Weather.updateTime = json["updateTime"].textValue()
                Weather.jsonNode = json["now"]
            } else {
                Weather.errorMsg = "阿姬没有找到${City.sendCity}的天气信息捏\n换一个试试吧"
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Weather.errorMsg = "接口肯定是炸了!快来修接口！"
        }
    }

    private fun getDailyWeather() {
        val url = "https://devapi.qweather.com/v7/indices/1d?type=1,2&location=${
            City.jsonNode?.get("id")?.textValue()
        }&key=${key}"
        try {
            val json = objectMapper.readTree(HttpUtil.get(url).response)
            if (json["code"].textValue() == "200") {
                DailyWeather.code = json["code"].textValue()
                try {
                    DailyWeather.sportText = json["daily"][0]["text"].textValue()
                    DailyWeather.carText = json["daily"][1]["text"].textValue()
                } catch (e: Exception) {
                    DailyWeather.carText = "这里的小提示提示不归阿姬管哦"
                    DailyWeather.sportText = "阿姬也不知道这里该怎么运动呢"
                }

            } else {
                DailyWeather.code = json["code"].textValue()
                DailyWeather.errorMsg = "阿姬今天没有什么提示哦"
            }
        } catch (e: Exception) {
            e.printStackTrace()
            DailyWeather.errorMsg = "接口肯定是炸了!快来修接口！"
        }
    }

    fun preWeather() {
        val url = "https://devapi.qweather.com/v7/weather/3d?location=${
            City.jsonNode?.get("id")?.textValue()
        }&key=${key}"
        try {
            val json = objectMapper.readTree(HttpUtil.get(url).response)
            Prediction.code = json["code"].textValue()
            if (json["code"].textValue() == "200") {
                Prediction.jsonNode = json["daily"]
                Prediction.updateTime = json["updateTime"].textValue()
            } else {
                Prediction.errorMsg = "阿姬没有找到天气预报哦"
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Prediction.errorMsg = "接口肯定是炸了!快来修接口！"
        }
    }
}