package pers.wuliang.robot

import love.forte.simboot.spring.autoconfigure.EnableSimbot
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@EnableSimbot
@SpringBootApplication
class WuLiangRobotV3Application

fun main(args: Array<String>) {
    runApplication<WuLiangRobotV3Application>(*args)
}
