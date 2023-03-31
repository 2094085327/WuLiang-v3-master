package pers.wuliang.robot.botApi.gameApi.lifeRestart

import love.forte.simboot.annotation.Filter
import love.forte.simboot.filter.MatchType
import love.forte.simbot.event.GroupMessageEvent
import org.springframework.stereotype.Component
import pers.wuliang.robot.core.annotation.RobotListen
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


    @RobotListen(desc = "人生重开")
    @Filter(">人生重开", matchType = MatchType.REGEX_MATCHES)
    suspend fun GroupMessageEvent.lifeRestart() {
        val allDataList: ArrayList<AllDataClass> = arrayListOf()
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
            talentStr = talentStr.plus("${allData.data.TLT!!.name[i]} ${allData.data.TLT!!.description[i]}\n")
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
            attributeStr = attributeStr.plus("$name: $value\n")
        }
        reply(attributeStr)
    }
}