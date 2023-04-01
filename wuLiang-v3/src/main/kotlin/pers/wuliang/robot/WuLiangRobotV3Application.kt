package pers.wuliang.robot

import love.forte.simboot.spring.autoconfigure.EnableSimbot
import net.mamoe.mirai.utils.BotConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling
import xyz.cssxsh.mirai.tool.FixProtocolVersion

@EnableSimbot
@SpringBootApplication
@EnableScheduling
class WuLiangRobotV3Application

fun main(args: Array<String>) {
    FixProtocolVersion.update()
    FixProtocolVersion.info()
    runApplication<WuLiangRobotV3Application>(*args)
}
