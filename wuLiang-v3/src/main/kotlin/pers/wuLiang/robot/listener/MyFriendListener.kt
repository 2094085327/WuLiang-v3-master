package pers.wuLiang.robot.listener

import com.fasterxml.jackson.databind.ObjectMapper
import love.forte.simboot.annotation.Filter
import love.forte.simboot.annotation.Listener
import love.forte.simboot.filter.MatchType
import love.forte.simbot.Api4J
import love.forte.simbot.event.GroupMessageEvent
import love.forte.simbot.event.MessageEvent
import love.forte.simbot.message.Message
import org.springframework.stereotype.Component
import pers.wuLiang.robot.core.annotation.RobotListen
import pers.wuLiang.robot.core.common.Constant
import pers.wuLiang.robot.core.common.send
import java.io.File


val objectMapper = ObjectMapper()

data class Data(
    var groupId: String,
    var groupName: String,
    var content: Message,
    var sendUserCode: String,
    var sendUserName: String,
    var time: Long,
)

@Component
//@Beans
class GroupListener {
    @OptIn(Api4J::class)
    @RobotListen(desc = "群组消息监听")
    suspend fun GroupMessageEvent.allEventListen() {
        val data = Data(
            groupId = group.id.toString(),
            groupName = group.name,
            content = messageContent.messages,
            sendUserCode = author().id.toString(),
            sendUserName = author().nickOrUsername,
            time = System.currentTimeMillis(),
        )
        val json1 = objectMapper.writeValueAsString(data)
        println(json1)
    }


    // TODO 涩图抽卡功能
    /**
     * 抽奖
     * @receiver GroupMessageEvent
     */
    @Listener
    @Filter("\\.h|\\.help", matchType = MatchType.REGEX_MATCHES)
    suspend fun MessageEvent.menu() {

        val functionMenuUrl = "https://www.yuque.com/qingsi-zwnmu/xyuvvi/wrbzgy"    // 项目文档
        val gitHubUrl = "https://github.com/Chenyuxin221/huahua-robot"           // 项目地址
        val result = "功能菜单：${functionMenuUrl}\n项目地址：${gitHubUrl}"         // 菜单内容
        println(result)                                           // 发送菜单
        send(Constant.HELP)
    }

    @Listener
    @Filter("json|json2", matchType = MatchType.REGEX_MATCHES)
    suspend fun MessageEvent.sendJson() {
        val json4 = objectMapper.readTree(
            File("C:\\Users\\86188\\Desktop\\wuyou-robot-v3-master\\wuLiang-v3\\src\\main\\resources\\JSON\\test.json")
        )

        println("json4:$json4")
        println(json4["date"])
        send("json:" + json4["date"])
//        val plainText = messageContent.plainText.trim()
//        val data = Data(plainText, "1", "1")
//        println("data:$data")
//        val json1 = objectMapper.writeValueAsString(data)
//        println(json1)

    }
}