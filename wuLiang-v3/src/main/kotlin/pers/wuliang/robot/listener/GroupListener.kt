package pers.wuliang.robot.listener

import com.fasterxml.jackson.databind.ObjectMapper
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
import pers.wuliang.robot.core.annotation.RobotListen
import pers.wuliang.robot.core.common.send
import java.io.File

@Component
class GroupListener {
    val objectMapper = ObjectMapper()

    // TODO 涩图抽卡功能
    /**
     * 帮助
     * @receiver GroupMessageEvent
     */
    @Listener
    @Filter(">h|>help|>帮助", matchType = MatchType.REGEX_MATCHES)
    suspend fun MessageEvent.menu() {
        // 项目文档
        val functionMenuUrl = "https://flowus.cn/share/c0675547-e7de-4a05-8999-143bf6814205"
        // 项目地址
        val gitHubUrl = "https://github.com/2094085327/WuLiang-v3-master"
        // 原神功能文档
        val genshinUrl = "https://flowus.cn/share/922a366d-29e4-48a3-b7e4-03e1afbc2989"
        // 菜单内容
        val result = "项目文档:$functionMenuUrl\n项目地址:$gitHubUrl\n原神功能文档:$genshinUrl"
        send(result)
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
}