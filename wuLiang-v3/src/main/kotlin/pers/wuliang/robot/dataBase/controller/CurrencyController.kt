package pers.wuliang.robot.dataBase.controller

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper
import com.baomidou.mybatisplus.core.toolkit.Wrappers
import love.forte.simboot.annotation.Filter
import love.forte.simboot.filter.MatchType
import love.forte.simbot.Api4J
import love.forte.simbot.event.GroupMessageEvent
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import pers.wuLiang.robot.core.annotation.RobotListen
import pers.wuliang.robot.dataBase.enity.Currency
import pers.wuliang.robot.dataBase.mapper.CurrencyMapper
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter


/**
 *@Description:
 *@Author zeng
 *@Date 2022/11/28 16:56
 *@User 86188
 */
@Component
class CurrencyController {
    @Autowired
    lateinit var currencyMapper: CurrencyMapper

    /**
     * 计算两个时间点的天数差
     * @param dt1 第一个时间点
     * @param dt2 第二个时间点
     * @return int，即要计算的天数差
     */
    fun dateDiff(dt1: LocalDateTime, dt2: LocalDateTime): Int {
        val t1 = dt1.toEpochSecond(ZoneOffset.ofHours(0))
        val day1 = t1 / (60 * 60 * 24)
        val t2 = dt2.toEpochSecond(ZoneOffset.ofHours(0))
        val day2 = t2 / (60 * 60 * 24)
        return (day2 - day1).toInt()
    }

    fun randomText(randoms: Int): String? {
        var text: String? = null
        if (randoms > 0) {
            text = "无量姬: 现在本小姐心情好，这些是额外送给你的!\n\n" +
                    "额外获得 $randoms 无量币\n\n"
        }
        if (randoms < 0) {
            text = "无量姬: 不知道为什么突然好难受，决定从你的签到奖励中拿走一些无量币\n\n" +
                    "你损失了 ${-randoms} 无量币\n\n"
        }
        if (randoms == 0) {
            text = "无量姬: 哎呀呀~平静的一天\n\n" +
                    "你既没有损失也没有收获\n\n"
        }
        return text
    }

    /**
     * 通过签到获取金币
     */
    @OptIn(Api4J::class)
    @RobotListen(desc = "签到获取金币")
    @Filter("签到", matchType = MatchType.REGEX_MATCHES)
    suspend fun GroupMessageEvent.currencySign() {
        val queryWrapper: QueryWrapper<Currency> =
            Wrappers.query<Currency?>().apply { eq("qqId", author().id.toString()) }

        val currencyExist = currencyMapper.selectOne(queryWrapper)
        val randoms = (-500..800).random()
        if (currencyExist == null) {
            val currency = Currency(
                qqId = author().id.toString(),
                qqName = author().nickOrUsername,
                money = 500 + randoms,
                updateTime = LocalDateTime.now(),
                signTime = LocalDateTime.now()
            )
            currencyMapper.insert(currency)
            replyBlocking(
                "[---------签到成功--------]\n" +
                        "昵称: ${author().nickOrUsername}\n\n" +
                        "${randomText(randoms)}" +
                        "你今天签到获得了 ${500 + randoms} 无量币\n\n" +
                        "余额:  ${500 + randoms} 无量币\n\n" +
                        "签到日期: ${LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))}"
            )
        } else {
            if (currencyExist.signTime?.let { dateDiff(LocalDateTime.now(), it) }!! >= 1) {
                val currency = Currency(
                    qqName = author().nickOrUsername,
                    money = currencyExist.money?.plus(500 + randoms),
                    updateTime = LocalDateTime.now()
                )
                currencyMapper.update(currency, queryWrapper)
                replyBlocking(
                    "[---------签到成功--------]\n" +
                            "昵称: ${author().nickOrUsername}\n\n" +
                            "${randomText(randoms)}" +
                            "你今天签到获得了 ${500 + randoms} 无量币\n\n" +
                            "余额: ${currencyExist.money?.plus(500 + randoms)} 无量币\n\n" +
                            "签到日期: ${LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))}"
                )
            } else {
                replyBlocking("你今天已经签到过了，不能重复签到哦~")
            }
        }
    }
}