package pers.wuliang.robot.botApi.geographyApi

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import pers.wuliang.robot.util.HttpUtil

var objectMapper = ObjectMapper()

/**
 *@Description:
 *@Author zeng
 *@Date 2022/11/9 16:51
 *@User 86188
 */
// 城市信息
object City {
        var sendCity: String? = null
        var code: String? = null
        var name: String? = null
        var id: String? = null
        var lat: String? = null
        var lon: String? = null
        var country: String? = null
        var adm1: String? = null
        var adm2: String? = null
        var tz: String? = null
        var errorMsg: String? = null
}

// 城市天气
object Weather {
        var code: String? = null
        var updateTime: String? = null
        var temp: String? = null
        var feelsLike: String? = null
        var text: String? = null
        var wind360: String? = null
        var windDir: String? = null
        var windScale: String? = null
        var windSpeed: String? = null
        var humidity: String? = null
        var pressure: String? = null
        var vis: String? = null
        var cloud: String? = null
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

class GeoApi {
    fun getCityData(city: String) {
        val url = "https://geoapi.qweather.com/v2/city/lookup?location=$city&key=1b48b4a69c8a4f7cb17674cbf4cea29b"
        try {
            City.sendCity = city
            val json = objectMapper.readTree(HttpUtil.get(url).response)
            if (json["code"].textValue() == "200") {
                City.code = json["code"].textValue()
                City.name = json["location"][0]["name"].textValue()
                City.id = json["location"][0]["id"].textValue()
                City.lat = json["location"][0]["lat"].textValue()
                City.lon = json["location"][0]["lon"].textValue()
                City.country = json["location"][0]["country"].textValue()
                City.adm1 = json["location"][0]["adm1"].textValue()
                City.adm2 = json["location"][0]["adm2"].textValue()
                City.tz = json["location"][0]["tz"].textValue()
            } else {
                City.code = json["code"].textValue()
                City.errorMsg = "错误码:${City.code}\n阿姬没有找到${city}的地理信息捏\n只能查询城市和省/州的信息哦"
            }
        } catch (e: Exception) {
            e.printStackTrace()
            City.errorMsg = "接口肯定是炸了!快来修接口！"
        }
    }

    fun getWeatherData() {
        getDailyWeather()
        val url = "https://devapi.qweather.com/v7/weather/now?location=${City.id}&key=1b48b4a69c8a4f7cb17674cbf4cea29b"
        try {
            val json = objectMapper.readTree(HttpUtil.get(url).response)
            if (json["code"].textValue() == "200") {
                Weather.code = json["code"].textValue()
                Weather.updateTime = json["updateTime"].textValue()
                Weather.temp = json["now"]["temp"].textValue()
                Weather.feelsLike = json["now"]["feelsLike"].textValue()
                Weather.text = json["now"]["text"].textValue()
                Weather.wind360 = json["now"]["wind360"].textValue()
                Weather.windDir = json["now"]["windDir"].textValue()
                Weather.windScale = json["now"]["windScale"].textValue()
                Weather.windSpeed = json["now"]["windSpeed"].textValue()
                Weather.humidity = json["now"]["humidity"].textValue()
                Weather.pressure = json["now"]["pressure"].textValue()
                Weather.vis = json["now"]["vis"].textValue()
                Weather.cloud = json["now"]["cloud"].textValue()
            } else {
                Weather.code = json["code"].textValue()
                Weather.errorMsg = "错误码:${Weather.code}\n阿姬没有找到${City.sendCity}的天气信息捏"
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Weather.errorMsg = "接口肯定是炸了!快来修接口！"
        }
    }

    private fun getDailyWeather() {
        val url =
            "https://devapi.qweather.com/v7/indices/1d?type=1,2&location=${City.id}&key=1b48b4a69c8a4f7cb17674cbf4cea29b"
        try {
            val json = objectMapper.readTree(HttpUtil.get(url).response)
            if (json["code"].textValue() == "200") {
                DailyWeather.code = json["code"].textValue()
                DailyWeather.sportText = json["daily"][0]["text"].textValue()
                DailyWeather.carText = json["daily"][1]["text"].textValue()
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
        val url =
            "https://devapi.qweather.com/v7/weather/3d?location=${City.id}&key=1b48b4a69c8a4f7cb17674cbf4cea29b"
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