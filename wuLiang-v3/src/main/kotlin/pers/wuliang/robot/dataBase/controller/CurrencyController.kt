package pers.wuliang.robot.dataBase.controller

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper
import com.baomidou.mybatisplus.core.toolkit.Wrappers
import love.forte.simboot.annotation.Filter
import love.forte.simboot.filter.MatchType
import love.forte.simbot.Api4J
import love.forte.simbot.event.GroupMessageEvent
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import pers.wuliang.robot.core.annotation.RobotListen
import pers.wuliang.robot.dataBase.enity.Currency
import pers.wuliang.robot.dataBase.mapper.CurrencyMapper
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*


/**
 *@Description: 签到的controller类
 *@Author zeng
 *@Date 2022/11/28 16:56
 *@User 86188
 */
@Component
class CurrencyController {
    @Autowired
    lateinit var currencyMapper: CurrencyMapper

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
     * 计算当前等级所需经验并判断是否升级
     */
    fun level(level: Int, exp: Int): Int? {
        val expNeed = 110 * level
        return if (exp > expNeed && level < 10) {
            level + 1
        } else {
            level
        }
    }

    fun exp(exp: Int?, improveExp: Int): Int? {
        val exps = if (exp!! > 990) {
            0
        } else {
            improveExp
        }
        return exps
    }

    /**
     * 判断是否在同一个月
     *
     * @return false:不在同一个月内，true在同一个月内
     */
    fun isMonth(date1: Date?, date2: Date?): Boolean {
        val calendar1 = Calendar.getInstance()
        calendar1.time = date1
        val calendar2 = Calendar.getInstance()
        calendar2.time = date2
        return calendar1[Calendar.YEAR] == calendar2[Calendar.YEAR] && calendar1[Calendar.MONTH] == calendar2[Calendar.MONTH]
    }

    /**
     * 计算间隔天数
     */
    fun getPastDay(date0: Date, date: String?): Long {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd")
        return (date0.time - dateFormat.parse(date).time) / 86400000
    }

    /**
     * 通过签到获取金币
     */
    @OptIn(Api4J::class)
    @RobotListen(desc = "签到获取金币")
    @Filter("签到", matchType = MatchType.REGEX_MATCHES)
    suspend fun GroupMessageEvent.currencySign() {
        // 时间戳格式化只取年月日
        val dateFormat = SimpleDateFormat("yyyy-MM-dd")
        val calendar = Calendar.getInstance()
        val queryWrapper: QueryWrapper<Currency> =
            Wrappers.query<Currency?>().apply { eq("qqId", author().id.toString()) }

        val currencyExist = currencyMapper.selectOne(queryWrapper)
        val randoms = (-500..800).random()
        if (currencyExist == null) {
            val currency = Currency(
                qqId = author().id.toString(),
                qqName = author().nickOrUsername,
                level = 1,
                exp = 3,
                money = 500 + randoms,
                updateTime = LocalDateTime.now(),
                signTime = dateFormat.format(calendar.time),
                times = 1
            )
            currencyMapper.insert(currency)
            replyBlocking(
                "[---------签到成功--------]\n" +
                        "昵称: ${author().nickOrUsername}\n\n" +
                        "${randomText(randoms)}" +
                        "你今天签到获得了 ${500 + randoms} 无量币\n\n" +
                        "余额:  ${500 + randoms} 无量币\n\n" +
                        "本月已签到: 1 天\n\n" +
                        "签到日期: ${LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))}"
            )
        } else {
            if (getPastDay(Date(), currencyExist.signTime) >= 1) {
                val currency = Currency(
                    qqName = author().nickOrUsername,
                    level = level(
                        currencyExist.level.toString().toInt(),
                        currencyExist.exp.toString().toInt() + exp(currencyExist.exp, 3).toString().toInt()
                    ),
                    exp = exp(currencyExist.exp, 3)?.let { currencyExist.exp?.plus(it) },
                    money = currencyExist.money?.plus(500 + randoms),
                    updateTime = LocalDateTime.now(),
                    signTime = dateFormat.format(calendar.time),
                    times = currencyExist.times?.plus(1)
                )
                currencyMapper.update(currency, queryWrapper)
                replyBlocking(
                    "[---------签到成功--------]\n" +
                            "昵称: ${author().nickOrUsername}\n\n" +
                            "${randomText(randoms)}" +
                            "你今天签到获得了 ${500 + randoms} 无量币\n\n" +
                            "等级: ${currency.level} LV\n\n" +
                            "余额: ${currencyExist.money?.plus(500 + randoms)} 无量币\n\n" +
                            "本月已签到: ${currency.times} 天\n\n" +
                            "签到日期: ${LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))}"
                )
            } else {
                replyBlocking("你今天已经签到过了，不能重复签到哦~")
            }
        }
    }

    /**
     * 补签
     */
    @OptIn(Api4J::class)
    @RobotListen(desc = "花费无量币进行补签")
    @Filter("补签", matchType = MatchType.REGEX_MATCHES)
    suspend fun GroupMessageEvent.countersign() {
        // 时间戳格式化只取年月日
        val dateFormat = SimpleDateFormat("yyyy-MM-dd")
        val calendar = Calendar.getInstance()
        val queryWrapper: QueryWrapper<Currency> =
            Wrappers.query<Currency?>().apply { eq("qqId", author().id.toString()) }

        val currencyExist = currencyMapper.selectOne(queryWrapper)
        val randoms = (-500..800).random()
        if (currencyExist == null) {
            replyBlocking("你还没有签到过，输入「签到」进行第一次签到吧")
        } else {
            if (isMonth(
                    Date(),
                    dateFormat.parse(currencyExist.signTime)
                ) && calendar.get(Calendar.DAY_OF_MONTH) > currencyExist.times.toString().toInt()
            ) {
                if (currencyExist.money?.minus(300)!! > 0) {
                    val currency = Currency(
                        qqName = author().nickOrUsername,
                        level = level(
                            currencyExist.level.toString().toInt(),
                            currencyExist.exp.toString().toInt() + exp(currencyExist.exp, 3).toString().toInt()
                        ),
                        exp = exp(currencyExist.exp, 3)?.let { currencyExist.exp?.plus(it) },
                        money = currencyExist.money.plus(500 + randoms),
                        updateTime = LocalDateTime.now(),
                        signTime = dateFormat.format(calendar.time),
                        times = currencyExist.times?.plus(1)
                    )
                    currencyMapper.update(currency, queryWrapper)
                    replyBlocking(
                        "[---------补签成功--------]\n" +
                                "昵称: ${author().nickOrUsername}\n\n" +
                                "${randomText(randoms)}" +
                                "你今天签到获得了 ${500 + randoms} 无量币\n\n" +
                                "等级: ${currency.level} LV\n\n" +
                                "余额: ${currencyExist.money.plus(500 + randoms)} 无量币\n\n" +
                                "本月已签到: ${currency.times} 天\n\n" +
                                "签到日期: ${LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))}"
                    )
                }


            } else {
                if (currencyExist.times == 0) {
                    replyBlocking("这个月目前为止你还未签到过哦，无法补签~")
                } else {
                    replyBlocking("这个月目前为止你已经全部签到过了哦，无法补签~")
                }
            }
        }
    }

    @OptIn(Api4J::class)
    @RobotListen(desc = "查询金币数")
    @Filter("查询", matchType = MatchType.REGEX_MATCHES)
    suspend fun GroupMessageEvent.selectMoney() {
        val queryWrapper: QueryWrapper<Currency> =
            Wrappers.query<Currency?>().apply { eq("qqId", author().id.toString()) }
        val user = currencyMapper.selectOne(queryWrapper)
        if (user != null) {
            replyBlocking(
                "[---------用户信息--------]\n" +
                        "昵称: ${author().nickOrUsername}\n" +
                        "等级: ${user.level} LV\n" +
                        "无量币: ${user.money} 无量币\n" +
                        "本月已签到: ${user.times} 天\n\n" +
                        "上次签到: ${user.signTime}"
            )
        } else {
            replyBlocking("你还没有余额哦，输入「签到」来获取无量币吧")
        }
    }


}

