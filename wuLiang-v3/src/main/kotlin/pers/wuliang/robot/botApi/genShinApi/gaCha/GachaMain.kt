package pers.wuliang.robot.botApi.genShinApi.gaCha

import love.forte.simboot.annotation.Filter
import love.forte.simboot.annotation.FilterValue
import love.forte.simboot.filter.MatchType
import love.forte.simbot.event.GroupMessageEvent
import love.forte.simbot.message.Image.Key.toImage
import love.forte.simbot.resources.Resource.Companion.toResource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import pers.wuliang.robot.botApi.genShinApi.makeRes.MakeWeapon
import pers.wuliang.robot.core.annotation.RobotListen
import pers.wuliang.robot.core.common.send
import pers.wuliang.robot.dataBase.service.GachaInfoService
import pers.wuliang.robot.dataBase.service.GenshininfoService
import java.awt.image.BufferedImage
import java.io.File

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
     * 在循环中获取数据
     * @param gachaData 数据类
     * @param gachaTool 工具类
     * @param cleanUrl 处理后的抽卡链接
     */
    fun GroupMessageEvent.getDataInLoop(gachaData: GachaData, gachaTool: GachaTool, cleanUrl: String) {
        val typeMap = mapOf("角色" to "role", "武器" to "weapon", "常驻" to "permanent")
        val poolDataList: ArrayList<BufferedImage> = arrayListOf()
        for ((key, value) in typeMap) {
            gachaTool.dataArray = gachaData.getGachaData(cleanUrl, gachaData.getGaChaType("${key}池"))
            integrationData(value)
            poolDataList.add(PictureMake().poolImage(key))
            send("${key}池分析完成")
        }
        val totalData = PictureMake().allDataMake()
        val allImage = PictureMake().compositePicture(totalData, poolDataList[0], poolDataList[1], poolDataList[2])
        send(allImage.toResource().toImage())

        gachaTool.reset()
    }

    fun GroupMessageEvent.getDataInLoop(uid: String) {
        val typeMap = mapOf("角色" to "role", "武器" to "weapon", "常驻" to "permanent")
        val poolDataList: ArrayList<BufferedImage> = arrayListOf()
        val gachaTool = GachaTool.instance
        for ((key, value) in typeMap) {
            gachaTool.dataArray = gachaInfoService.selectByUid(uid, value) as MutableList<GachaData.ItemData>
            poolDataList.add(PictureMake().poolImage(key))
        }
        val totalData = PictureMake().allDataMake()
        val allImage = PictureMake().compositePicture(totalData, poolDataList[0], poolDataList[1], poolDataList[2])
        send(allImage.toResource().toImage())
        gachaTool.reset()
    }

    /**
     * 整合历史数据和新数据
     * @param type 数据类型
     */
    fun integrationData(type: String) {
        val gachaTool = GachaTool.instance
        val list = gachaInfoService.selectByUid(gachaTool.uid, type) as MutableList<GachaData.ItemData>
        list.removeIf { it.key == "已抽次数" }
        // 将两个List合并后去重
        gachaTool.dataArray = (gachaTool.dataArray + list).distinct().toMutableList()
        gachaTool.dataArray =
            (gachaTool.dataArray.filter { it.key == "已抽次数" } + gachaTool.dataArray.filter { it.key != "已抽次数" }) as MutableList<GachaData.ItemData>
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
            getDataInLoop(gachaData, gachaTool, cleanUrl)
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
                val newUrl = gachaData.getUrl(url = message, gachaType = gachaType, times = 1, endId = "0")
                val replyMsg = gachaData.checkUrl(newUrl)
                if (replyMsg.contains("验证通过")) {
                    send(replyMsg)
                    getDataInLoop(gachaData, gachaTool, message)
                } else {
                    send(replyMsg)
                }
            }
        }
    }

    @RobotListen(desc = "根据uid查询")
    @Filter(">历史记录{{uid}}", matchType = MatchType.REGEX_MATCHES)
    suspend fun GroupMessageEvent.getByUid(@FilterValue("uid") uid: String) {
        if (gachaInfoService.selectByUid(uid.replace(" ", ""))) {
            getDataInLoop(uid.replace(" ", ""))
        } else {
            reply("这个UID还未分析过，没有抽卡记录哦")
        }
    }

    @RobotListen(desc = "根据QQ号查询历史记录")
    @Filter(">历史记录", matchType = MatchType.REGEX_MATCHES)
    suspend fun GroupMessageEvent.getHistory() {
        val uid = genshininfoService.getUidByQqId(author().id.toString())
        if (uid != "无记录") {
            getDataInLoop(uid)
        } else {
            reply("你的QQ还没有进行过抽卡分析，不存在抽卡记录哦")
        }
    }

    @RobotListen(desc = "图片测试")
    @Filter(">合成武器图片{{name}}", matchType = MatchType.REGEX_MATCHES)
    suspend fun GroupMessageEvent.getImage(@FilterValue("name") name: String) {
        var newName = name
        if (GachaData().getPermanentData().contains(name)) {
            newName = "$name(歪)"
        }
        val img = File("${GachaConfig.localPath}/武器图片/${newName}.png").absoluteFile
        if (img.exists()) {
            send("这张武器图片已经存在了，不需要再次合成")
        } else {
            MakeWeapon().makeImg(name)
            send("合成完成")
        }
    }


}