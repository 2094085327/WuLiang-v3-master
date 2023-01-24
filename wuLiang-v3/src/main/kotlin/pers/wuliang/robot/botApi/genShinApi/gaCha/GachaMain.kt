package pers.wuliang.robot.botApi.genShinApi.gaCha

import love.forte.simboot.annotation.Filter
import love.forte.simboot.annotation.FilterValue
import love.forte.simboot.filter.MatchType
import love.forte.simbot.event.GroupMessageEvent
import love.forte.simbot.message.Image.Key.toImage
import love.forte.simbot.resources.Resource.Companion.toResource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import pers.wuliang.robot.core.annotation.RobotListen
import pers.wuliang.robot.core.common.send
import pers.wuliang.robot.dataBase.service.GachaInfoService
import pers.wuliang.robot.dataBase.service.GenshininfoService

/**
 *@Description: 抽卡分析的主类
 *@Author zeng
 *@Date 2022/12/16 12:03
 *@User 86188
 */
@Component
class GachaMain {
    @Autowired
    lateinit var genshininfoService: GenshininfoService

    @Autowired
    lateinit var gachaInfoService: GachaInfoService

    /**
     * 整合历史数据和新数据
     */
    fun integrationData(type: String) {
        val gachaTool = GachaTool.instance
        val list = gachaInfoService.selectByUid(gachaTool.uid, type) as MutableList<GachaData.ItemData>
        // 将两个List合并后去重
        gachaTool.dataArray = (  gachaTool.dataArray+list).distinct().toMutableList()
        for (itemData in gachaTool.dataArray) {
            val itemName = itemData.key
            val times = itemData.value
            gachaInfoService.insertByUid(gachaTool.uid, type, itemName, times)
        }
    }

    @RobotListen(desc = "链接分析")
    @Filter(">链接{{url}}", matchType = MatchType.REGEX_MATCHES)
    suspend fun GroupMessageEvent.getUrl(@FilterValue("url") url: String) {
        val gachaData = GachaData()
        val gachaTool = GachaTool.instance
        val cleanUrl = gachaData.cleanUrl(url)
        val gachaType = gachaData.getGaChaType("up池1")
        gachaData.getPermanentData()
        val newUrl = gachaData.getUrl(url = cleanUrl, gachaType = gachaType, times = 1, endId = "0")
        val replyMsg = gachaData.checkUrl(newUrl)
        if (replyMsg.contains("验证通过")) {
            send(replyMsg)
            genshininfoService.insertByUid(
                uid = gachaTool.uid,
                qqId = author().id.toString(),
                sToken = cleanUrl,
                state = 1
            )

            gachaTool.dataArray = gachaData.getGachaData(cleanUrl, gachaData.getGaChaType("角色池"))
            integrationData("role")
            val roleData = PictureMake().poolImage("角色")
            send("角色池分析完成")

            gachaTool.dataArray = gachaData.getGachaData(cleanUrl, GachaData().getGaChaType("武器池"))
            integrationData("weapon")
            val armsData = PictureMake().poolImage("武器")
            send("武器池分析完成")

            gachaTool.dataArray = gachaData.getGachaData(cleanUrl, GachaData().getGaChaType("常驻池"))
            integrationData("permanent")
            val perData = PictureMake().poolImage("常驻")
            send("常驻池分析完成")
            val totalData = PictureMake().allDataMake()
            val allImage = PictureMake().compositePicture(totalData, roleData, armsData, perData)


            send(allImage.toResource().toImage())

            gachaTool.reset()
        } else {
            send(replyMsg)
        }
    }

    @RobotListen(desc = "上次分析")
    @Filter(">上次分析")
    suspend fun GroupMessageEvent.lastGacha() {
        val qqId = author().id.toString()
        val gachaTool = GachaTool.instance

        when (val message = genshininfoService.selectByQqId(qqId)) {
            "error" -> send("未知错误")
            "过期" -> send("链接已经过期了，请获取最新链接")
            "未绑定" -> send("你还没有绑定，请使用 >链接 你的链接 进行绑定")
            else -> {
                val gachaData = GachaData()
                val gachaType = gachaData.getGaChaType("up池1")
                gachaData.getPermanentData()
                val newUrl = gachaData.getUrl(url = message, gachaType = gachaType, times = 1, endId = "0")
                val replyMsg = gachaData.checkUrl(newUrl)
                if (replyMsg.contains("验证通过")) {
                    send(replyMsg)
                    gachaTool.dataArray = gachaData.getGachaData(message, GachaData().getGaChaType("角色池"))
                    integrationData("role")
                    val roleData = PictureMake().poolImage("角色")
                    send("角色池分析完成")

                    gachaTool.dataArray = gachaData.getGachaData(message, GachaData().getGaChaType("武器池"))
                    integrationData("weapon")
                    val armsData = PictureMake().poolImage("武器")
                    send("武器池分析完成")

                    gachaTool.dataArray = gachaData.getGachaData(message, GachaData().getGaChaType("常驻池"))
                    integrationData("permanent")
                    val perData = PictureMake().poolImage("常驻")
                    send("常驻池分析完成")
                    val totalData = PictureMake().allDataMake()
                    val allImage = PictureMake().compositePicture(totalData, roleData, armsData, perData)

                    send(allImage.toResource().toImage())

                    gachaTool.reset()
                } else {
                    send(replyMsg)
                }
            }
        }
    }

    @RobotListen(desc = "根据uid查询")
    @Filter(">历史记录{{uid}}", matchType = MatchType.REGEX_MATCHES)
    suspend fun GroupMessageEvent.getByUid(@FilterValue("uid") uid: String) {
        val gachaTool = GachaTool.instance

        gachaTool.dataArray = gachaInfoService.selectByUid(uid, "role") as MutableList<GachaData.ItemData>
        val roleData = PictureMake().poolImage("角色")


        gachaTool.dataArray = gachaInfoService.selectByUid(uid, "weapon") as MutableList<GachaData.ItemData>
        val armsData = PictureMake().poolImage("武器")


        gachaTool.dataArray = gachaInfoService.selectByUid(uid, "permanent") as MutableList<GachaData.ItemData>
        val perData = PictureMake().poolImage("常驻")

        val totalData = PictureMake().allDataMake()
        val allImage = PictureMake().compositePicture(totalData, roleData, armsData, perData)

        send(allImage.toResource().toImage())

        gachaTool.reset()
    }
}