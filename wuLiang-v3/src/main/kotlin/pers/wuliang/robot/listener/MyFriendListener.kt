package pers.wuLiang.robot.listener

import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import love.forte.simboot.annotation.Filter
import love.forte.simboot.annotation.Listener
import love.forte.simboot.filter.MatchType
import love.forte.simbot.component.mirai.message.MiraiQuoteReply
import love.forte.simbot.component.mirai.message.SimbotOriginalMiraiMessage
import love.forte.simbot.event.GroupMessageEvent
import love.forte.simbot.event.MessageEvent
import net.mamoe.mirai.contact.PermissionDeniedException
import net.mamoe.mirai.message.data.MessageSource.Key.recall
import net.mamoe.mirai.message.data.QuoteReply
import org.springframework.stereotype.Component
import pers.wuLiang.robot.core.annotation.RobotListen
import pers.wuLiang.robot.core.common.Constant
import pers.wuLiang.robot.core.common.send

import java.io.File


val objectMapper = ObjectMapper()

@Component
class GroupListener {
    // TODO 涩图抽卡功能
    /**
     * 帮助
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

    @RobotListen(
        isBoot = true,
        desc = "撤回操作",
    )
    @Filter("撤回", matchType = MatchType.TEXT_STARTS_WITH)
    suspend fun GroupMessageEvent.messageRecall() {
        val messages = messageContent.messages

        val originMiraiQuoteReply = messages[MiraiQuoteReply].firstOrNull()?.originalMiraiMessage
            ?: messages.firstNotNullOf { element ->
                (element as? SimbotOriginalMiraiMessage)?.originalMiraiMessage as? QuoteReply
            }
        try {
            originMiraiQuoteReply.source.recall()

            messageContent.delete()

            val msg =
                "「${author().nickOrUsername}」 通过bot撤回了一条消息"
            send(msg)
        } catch (e: PermissionDeniedException) {
            send("我无权操作此消息")
        } catch (e: Exception) {
            e.printStackTrace()
            send("撤回失败，无法撤回此消息：${e.message}")
        }
    }

    @RobotListen(
        isBoot = true,
        desc = "撤回操作",
    )
    @Filter("1111", matchType = MatchType.TEXT_STARTS_WITH)
    suspend fun GroupMessageEvent.messageRecall2() {
        val messages = messageContent.messages

        println(messages)
        withContext(Dispatchers.IO) {
            Thread.sleep(10000)
        }


//        val originMiraiQuoteReply = messages[MiraiQuoteReply].firstOrNull()?.originalMiraiMessage
//            ?: messages.firstNotNullOf { element ->
//                (element as? SimbotOriginalMiraiMessage)?.originalMiraiMessage as? QuoteReply
//            }
//        try {
//            originMiraiQuoteReply.source.recall()
//
            messageContent.delete()
//
//            val msg =
//                "「${author().nickOrUsername}」 通过bot撤回了一条消息"
//            send(msg)
//        } catch (e: PermissionDeniedException) {
//            send("我无权操作此消息")
//        } catch (e: Exception) {
//            send("撤回失败，无法撤回此消息：${e.message}")
//        }
    }

}