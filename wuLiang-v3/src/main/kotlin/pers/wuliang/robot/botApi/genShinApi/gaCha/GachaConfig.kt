package pers.wuliang.robot.botApi.genShinApi.gaCha

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.net.URLEncoder

/**
 *@Description:
 *@Author zeng
 *@Date 2022/12/16 11:51
 *@User 86188
 */
val String.urlCode: String? get() = URLEncoder.encode(this, "UTF-8")

@Component
class GachaConfig {
    companion object {
        var galleryPath: String? = ""
        var localPath: String? = ""
    }

    @Value("\${wuLiang.config.gallery}")
    fun setPath(path: String) {
        galleryPath = path
    }

    @Value("\${wuLiang.config.localPath}")
    fun setLocalPath(path: String) {
        localPath = path
    }
}