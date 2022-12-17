package pers.wuliang.robot.dataBase.controller

import love.forte.simbot.Api4J
import love.forte.simbot.event.GroupMessageEvent
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import pers.wuLiang.robot.core.annotation.RobotListen
import pers.wuliang.robot.dataBase.enity.Messages
import pers.wuliang.robot.dataBase.mapper.MessageMapper
import java.time.LocalDateTime

/**
 *@Description: 群聊消息存储
 *@Author zeng
 *@Date 2022/11/9 9:30
 *@User 86188
 */
@Component
@SuppressWarnings("unused")
class MessageController {
    @Autowired
    lateinit var messageMapper: MessageMapper

    @OptIn(Api4J::class)
    @RobotListen(desc = "群组消息监听")
    suspend fun GroupMessageEvent.allEventListen() {
        var content: String = if (messageContent.plainText != "" ) {
            messageContent.plainText
        } else {
            messageContent.messages.toString()
        }
        if (content.length>200){
            content = "长消息，无法存储"
        }
        val msg = Messages(
            groupId = group.id.toString(),
            groupName = group.name,
            content = content,
            sendUserCode = author().id.toString(),
            sendUserName = author().nickOrUsername,
            time = LocalDateTime.now()
        )
        messageMapper.insert(msg)
        println(msg)
    }

}