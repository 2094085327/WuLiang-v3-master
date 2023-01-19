package pers.wuliang.robot.botApi.genShinApi.gameGuild

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import love.forte.simboot.annotation.Filter
import love.forte.simboot.annotation.FilterValue
import love.forte.simboot.filter.MatchType
import love.forte.simbot.event.GroupMessageEvent
import love.forte.simbot.message.Image.Key.toImage
import love.forte.simbot.resources.Resource.Companion.toResource
import org.springframework.stereotype.Component
import pers.wuliang.robot.core.annotation.RobotListen
import pers.wuliang.robot.core.common.send
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.net.URL
import javax.imageio.ImageIO

/**
 *@Description: 原神攻略
 *@Author zeng
 *@Date 2023/1/12 19:36
 *@User 86188
 */
@Component
class GuildMain {

    @RobotListen(desc = "角色攻略")
    @Filter(">{{name}}攻略", matchType = MatchType.REGEX_MATCHES)
    suspend fun GroupMessageEvent.genShinGuide(@FilterValue("name") name: String) {
        val realName = DailyMaterial().getNameByAlias(name)
        try {
            val img = withContext(Dispatchers.IO) {
                ImageIO.read(URL("https://static.cherishmoon.fun/LittlePaimon/XFGuide/${realName}.jpg"))
            }
            val byStream = ByteArrayOutputStream()
            withContext(Dispatchers.IO) {
                ImageIO.write(img, "png", byStream)
            }
            send(ByteArrayInputStream(byStream.toByteArray()).toResource().toImage())
        } catch (e: Exception) {
            reply("没有找到${realName}的攻略,请尝试使用原名")
            return
        }
    }

    @RobotListen(desc = "天赋")
    @Filter(">{{week}}天赋", matchType = MatchType.REGEX_MATCHES)
    suspend fun GroupMessageEvent.genShinTalent(@FilterValue("week") week: String) {
        when (val weekDay = DailyMaterial().week(week)) {
            "error" -> reply("请输入今天，明天，后天或者周一到周日，也可以输入角色的全名")
            "周日" -> reply("周日可以刷全部材料哦")
            "true" -> reply(DailyMaterial().getMaterialByName(week))
            else -> {
                val materialData = DailyMaterial().getDailyMaterial()
                reply(DailyMaterial().getMaterial(materialData, "天赋", weekDay))
            }
        }
    }

    @RobotListen(desc = "武器")
    @Filter(">{{week}}武器", matchType = MatchType.REGEX_MATCHES)
    suspend fun GroupMessageEvent.genShinWeapon(@FilterValue("week") week: String) {
        when (val weekDay = DailyMaterial().week(week)) {
            "error" -> reply("请输入今天，明天，后天或者周一到周日，也可以输入武器的全名")
            "周日" -> reply("周日可以刷全部材料哦")
            "true" -> reply(DailyMaterial().getMaterialByName(week))
            else -> {
                val materialData = DailyMaterial().getDailyMaterial()
                reply(DailyMaterial().getMaterial(materialData, "武器", weekDay))
            }
        }
    }
}