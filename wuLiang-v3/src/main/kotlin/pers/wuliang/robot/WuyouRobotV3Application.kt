package pers.wuliang.robot

import love.forte.simboot.spring.autoconfigure.EnableSimbot
import org.mybatis.spring.annotation.MapperScan
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@EnableSimbot
@SpringBootApplication
class WuyouRobotV3Application

fun main(args: Array<String>) {
    runApplication<WuyouRobotV3Application>(*args)
}
