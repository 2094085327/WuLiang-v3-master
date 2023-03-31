package pers.wuliang.robot

import love.forte.simboot.spring.autoconfigure.EnableSimbot
import net.mamoe.mirai.utils.BotConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import xyz.cssxsh.mirai.tool.FixProtocolVersion

@EnableSimbot
@SpringBootApplication
class WuLiangRobotV3Application

// 升级协议版本
fun update() {
    update()
}

// 获取协议版本信息 你可以用这个来检查update是否正常工作
fun info(): Map<BotConfiguration.MiraiProtocol?, String?>? {
    return info()
}

fun main(args: Array<String>) {
    FixProtocolVersion.update()
    FixProtocolVersion.info()
    runApplication<WuLiangRobotV3Application>(*args)
}
