package pers.wuliang.robot.botApi.geographyApi

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

/**
 *@Description: 地理api的配置类
 *@Author zeng
 *@Date 2022/11/14 14:24
 *@User 86188
 */
@Component
class GeoConfig {
    companion object {
        var key: String? = null

    }

    @Value("\${weather.key}")
    fun setKey(key: String) {
        GeoConfig.key = key
    }
}