package pers.wuLiang.robot.util

import com.fasterxml.jackson.databind.ObjectMapper
import love.forte.simbot.message.Message


val objectMapper = ObjectMapper()


/**
 *@Description:
 *@Author zeng
 *@Date 2022/11/8 17:31
 *@User 86188
 */
class MessageUtil {
    fun encodeMessage(message: Message) = objectMapper.writeValueAsString(message)
}