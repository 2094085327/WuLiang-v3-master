package pers.wuliang.robot.botApi.gameApi.lifeRestart

import love.forte.simboot.annotation.Filter
import love.forte.simboot.annotation.FilterValue
import love.forte.simboot.filter.MatchType
import love.forte.simbot.Api4J
import love.forte.simbot.component.mirai.event.MiraiGroupMessageEvent
import love.forte.simbot.event.GroupMessageEvent
import love.forte.simbot.tryToLong
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.message.data.ForwardMessage
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.message.data.buildForwardMessage
import org.springframework.stereotype.Component
import pers.wuliang.robot.core.annotation.RobotListen
import pers.wuliang.robot.core.common.send
import java.time.Instant

/**
 *@Description:
 *@Author zeng
 *@Date 2023/3/28 23:13
 *@User 86188
 */
@Component
class RestartMain {
    data class AllDataClass(
        val time: Long,
        val userId: String,
        val data: LifeRestart.UserData
    )

    val allDataList: ArrayList<AllDataClass> = arrayListOf()

    @RobotListen(desc = "人生重开")
    @Filter(">人生重开", matchType = MatchType.REGEX_MATCHES)
    suspend fun GroupMessageEvent.lifeRestart() {
        println(allDataList)
        val userData = allDataList.find { it.userId == author().id.toString() }
        allDataList.remove(userData)
        val timestamp = Instant.now().epochSecond
        // 将时间戳转换为本地日期时间格式
//        val localDateTime = LocalDateTime.ofInstant(
//            Instant.ofEpochSecond(timestamp),
//            ZoneId.systemDefault()
//        )

        val allData = AllDataClass(
            time = timestamp,
            userId = author().id.toString(),
            data = LifeRestart().startGame()
        )

        allDataList.add(allData)


        var talentStr = "你获得了三个初始天赋:\n"
        for (i in 0 until 3) {
            talentStr = talentStr.plus("${allData.data.TLT!!.name[i]} ${allData.data.TLT!!.description[i]}")
            if (i != 2) {
                talentStr = talentStr.plus("\n")
            }
        }
        reply(talentStr)
        var attributeStr = "你获得了以下初始属性:\n"
        val attributes = listOf(
            "颜值" to allData.data.CHR,
            "智力" to allData.data.INT,
            "体质" to allData.data.STR,
            "家境" to allData.data.MNY,
            "快乐" to allData.data.SPR
        )
        for ((name, value) in attributes) {
            attributeStr = attributeStr.plus("$name: $value")
            if (name != "快乐") {
                attributeStr = attributeStr.plus("\n")
            }
        }
        reply(attributeStr)
    }


    @RobotListen(desc = "人生重开查看属性")
    @Filter("查看属性", matchType = MatchType.REGEX_MATCHES)
    suspend fun GroupMessageEvent.propertyView() {
        val userData = allDataList.find { it.userId == author().id.toString() }?.data
        if (userData != null) {
            var attributeStr = "你现在的属性:\n"
            val attributes = listOf(
                "颜值" to userData.CHR,
                "智力" to userData.INT,
                "体质" to userData.STR,
                "家境" to userData.MNY,
                "快乐" to userData.SPR
            )
            for ((name, value) in attributes) {
                attributeStr = attributeStr.plus("$name: $value")
                if (name != "快乐") {
                    attributeStr = attributeStr.plus("\n")
                }
            }
            reply(attributeStr)
        } else {
            send("你还没有属性，输入 >人生重开 开始游戏")
        }
    }

    @RobotListen(desc = "人生重开下一步")
    @Filter("继续", matchType = MatchType.REGEX_MATCHES)
    suspend fun GroupMessageEvent.lifeRestartNext() {
        var userData = allDataList.find { it.userId == author().id.toString() }?.data
        if (userData != null) {
            userData = LifeRestart().nextGameStep(userData)
            println(userData.AGE)
            println(userData.EVT)
            println(userData)
        } else {
            reply("你还没有开始游戏，请先输入 >人生重开 来进行游戏")
        }
    }

    @OptIn(Api4J::class)
    @RobotListen(desc = "人生重开下N步")
    @Filter("继续{{times}}", matchType = MatchType.REGEX_MATCHES)
    suspend fun GroupMessageEvent.lifeRestartNextMore(@FilterValue("times") times: Int, event: MiraiGroupMessageEvent) {
        var userData = allDataList.find { it.userId == author().id.toString() }?.data
        val lifeRestart = LifeRestart()
        val lifeEvent = lifeRestart.eventsJson
        val groupContact: Group = event.group.originalContact
        if (userData != null) {
            val forward: ForwardMessage = buildForwardMessage(groupContact) {
                for (i in 1..times) {
                    userData = lifeRestart.nextGameStep(userData!!)
                    println("main:$userData")
                    add(
                        author().id.tryToLong(),
                        author().nickOrUsername,
                        PlainText(
                            "${userData!!.AGE - 1}岁:" + lifeEvent[userData!!.EVT.last()]["event"].toString().trim('"')
                        )
                    )
                    println("${userData!!.AGE - 1}岁:" + lifeEvent[userData!!.EVT.last()]["event"].toString().trim('"'))
                    if (lifeEvent[userData!!.EVT.last()]["postEvent"] != null) {
                        add(
                            author().id.tryToLong(),
                            author().nickOrUsername,
                            PlainText(lifeEvent[userData!!.EVT.last()]["postEvent"].toString().trim('"'))
                        )
                    }
                    println("userData!!.LIF:${userData!!.LIF}")
                    if (userData!!.LIF == 0) {
                        add(
                            author().id.tryToLong(),
                            author().nickOrUsername,
                            PlainText("你的人生结束了")
                        )
                        val userDataAll = allDataList.find { it.userId == author().id.toString() }
                        allDataList.remove(userDataAll)
                        break
                    }
                }
            }
            groupContact.sendMessage(forward)
        }
        else {
            reply("你还没有开始游戏，请先输入 >人生重开 来进行游戏")
        }
    }
}