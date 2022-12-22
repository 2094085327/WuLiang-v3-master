package pers.wuliang.robot.botApi.genShinApi.gaCha

import love.forte.simboot.annotation.Filter
import love.forte.simboot.annotation.FilterValue
import love.forte.simboot.filter.MatchType
import love.forte.simbot.event.GroupMessageEvent
import love.forte.simbot.message.Image.Key.toImage
import love.forte.simbot.resources.Resource.Companion.toResource
import org.springframework.stereotype.Component
import pers.wuliang.robot.core.annotation.RobotListen
import pers.wuliang.robot.core.common.send

/**
 *@Description: 抽卡分析的主类
 *@Author zeng
 *@Date 2022/12/16 12:03
 *@User 86188
 */
@Component
class GachaMain {
    companion object {
        var dataArray = mutableListOf<GachaData.ItemData>()
    }

    @RobotListen(desc = "链接分析")
    @Filter(">链接{{url}}", matchType = MatchType.REGEX_MATCHES)
    suspend fun GroupMessageEvent.getUrl(@FilterValue("url") url: String) {
        val cleanUrl = GachaData().cleanUrl(url)
        val gachaType = GachaData().getGaChaType("up池1")
        GachaData().getPermanentData()
        val newUrl = GachaData().getUrl(url = cleanUrl, gachaType = gachaType, times = 1, endId = "0")
        reply(GachaData().checkUrl(newUrl))

        dataArray = GachaData().getGachaData(cleanUrl, GachaData().getGaChaType("角色池"))
        GachaData().getProbability(90, 301)
        val roleData = PictureMake().poolImage("角色")
        send("角色池分析完成")

        dataArray = GachaData().getGachaData(cleanUrl, GachaData().getGaChaType("武器池"))
        GachaData().getProbability(80, 302)
        val armsData = PictureMake().poolImage("武器")
        send("武器池分析完成")

        dataArray = GachaData().getGachaData(cleanUrl, GachaData().getGaChaType("常驻池"))
        GachaData().getProbability(90, 200)
        val perData = PictureMake().poolImage("常驻")
        send("常驻池分析完成")
        val totalData = PictureMake().allDataMake()
        val allImage = PictureMake().compositePicture(totalData, roleData, armsData, perData)

        GachaData.finCount = 0
        GachaData.finItem = 0
        GachaData.finProbability = 0.0
        send(allImage.toResource().toImage())
    }
}