package pers.wuliang.robot.botApi.gameApi.lifeRestart

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper
import com.baomidou.mybatisplus.core.toolkit.Wrappers
import kotlinx.coroutines.withTimeout
import love.forte.simboot.annotation.Filter
import love.forte.simboot.annotation.FilterValue
import love.forte.simboot.filter.MatchType
import love.forte.simbot.Api4J
import love.forte.simbot.ExperimentalSimbotApi
import love.forte.simbot.component.mirai.event.MiraiGroupMessageEvent
import love.forte.simbot.event.*
import love.forte.simbot.tryToLong
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.message.data.ForwardMessage
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.message.data.buildForwardMessage
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import pers.wuliang.robot.core.annotation.RobotListen
import pers.wuliang.robot.core.common.send
import pers.wuliang.robot.dataBase.entity.Currency
import pers.wuliang.robot.dataBase.mapper.CurrencyMapper
import java.time.LocalDateTime
import java.util.*
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.minutes

/**
 *@Description:
 *@Author zeng
 *@Date 2023/3/28 23:13
 *@User 86188
 */
@Component
@EnableScheduling
class RestartMain {
    data class AllDataClass(
        var time: Long,
        val userId: String,
        val groupId: String,
        val data: LifeRestart.UserData
    )

    @Autowired
    lateinit var currencyMapper: CurrencyMapper

    val allDataList: ArrayList<AllDataClass> = arrayListOf()
    val cacheList: ArrayList<String> = arrayListOf()
    val blackList: ArrayList<Pair<Long, String>> = arrayListOf()

    /**
     * 每5分钟清除不活动的账号
     */
    @Scheduled(cron = "0 */5 * * * ?")
    fun cleanUser() {
        if (allDataList.isEmpty()) {
            return
        }
        val iterUserData = allDataList.iterator()
        while (iterUserData.hasNext()) {
            val userData = iterUserData.next()
            if (System.currentTimeMillis() - userData.time > 60 * 5 * 1000) {
                iterUserData.remove()
            }
        }
        val iterBlack = blackList.iterator()
        while (iterBlack.hasNext()) {
            val blackUser = iterBlack.next()
            if (System.currentTimeMillis() - blackUser.first > 60 * 10 * 1000) {
                iterBlack.remove()
            }
        }
//        for (userData in allDataList) {
//
//            if (System.currentTimeMillis() - userData.time > 60 * 5 * 1000) {
//                Sender.sendGroupMsg(userData.groupId, "你的账户已经5分钟未处于活动状态，进行自动销毁" + At(userData.userId.ID))
//                allDataList.remove(userData)
//            }
//        }
//        for (blackUser in blackList) {
//            if (System.currentTimeMillis() - blackUser.first > 60 * 1 * 1000) {
//                blackList.remove(blackUser)
//            }
//        }
    }

    /**
     * 判断余额是否足够
     * @param userId 用户id
     * @return 返回余额判断
     */
    fun lifeRestartCost(userId: String): Boolean {
        val queryWrapper: QueryWrapper<Currency> =
            Wrappers.query<Currency?>().apply { eq("qqId", userId) }
        val currencyExist = currencyMapper.selectOne(queryWrapper) ?: return false

        return if (currencyExist.money!! - 100 > 0) {
            val currency = Currency(
                money = currencyExist.money.plus(-100),
                updateTime = LocalDateTime.now(),
            )
            currencyMapper.update(currency, queryWrapper)
            true
        } else {
            false
        }
    }

    /**
     * 向账户中添加货币
     * @param userId 用户ID
     * @param money  用户获得的货币数
     */
    fun addCurrency(userId: String, money: Int) {
        val queryWrapper: QueryWrapper<Currency> =
            Wrappers.query<Currency?>().apply { eq("qqId", userId) }

        val currencyExist = currencyMapper.selectOne(queryWrapper)
        val currency = Currency(
            money = currencyExist.money!!.plus(money),
            updateTime = LocalDateTime.now(),
        )
        currencyMapper.update(currency, queryWrapper)
    }

    /**
     * 生成属性字符串
     * @param attStr 属性字符串前置
     * @param allData 用户数据
     */
    fun getAttributeStr(attStr: String, allData: AllDataClass): String {
        var attributeStr = attStr
        val attributes = listOf(
            "颜值" to allData.data.CHR,
            "智力" to allData.data.INT,
            "体质" to allData.data.STR,
            "家境" to allData.data.MNY,
            "快乐" to allData.data.SPR
        )
        for ((name, value) in attributes) {
            attributeStr = attributeStr.plus("$name: $value")
            if (name != "快乐") {
                attributeStr = attributeStr.plus("\n")
            }
        }
        return attributeStr
    }

    @OptIn(Api4J::class)
    @RobotListen(desc = "人生重开")
    @Filter(">人生重开", matchType = MatchType.REGEX_MATCHES)
    suspend fun GroupMessageEvent.lifeRestartRandomModel() {
        if (!lifeRestartCost(author().id.toString())) {
            reply("你的无量币余额不足哦~ 使用[签到]来获取无量币吧")
            return
        }
        if (blackList.find { it.second == author().id.toString() }?.second == author().id.toString()) {
            return
        }

        val queryWrapper: QueryWrapper<Currency> =
            Wrappers.query<Currency?>().apply { eq("qqId", author().id.toString()) }
        val currencyExist = currencyMapper.selectOne(queryWrapper)
        send("游戏开始，你还有${currencyExist.money}无量币，每次游戏花费100无量币\n你现在使用的是随机模式，通过输入 >人生重开分配 来自定义属性，注意数值总和不能超过可用属性")
        val userData = allDataList.find { it.userId == author().id.toString() }
        allDataList.remove(userData)
        val timestamp = System.currentTimeMillis()
        val allData = AllDataClass(
            time = timestamp,
            userId = author().id.toString(),
            groupId = group.id.toString(),
            data = LifeRestart().startGame()
        )
        allDataList.add(allData)

        var talentStr = "你获得了三个初始天赋:\n"
        for (i in 0 until 3) {
            talentStr = talentStr.plus("${allData.data.TLT!!.name[i]} ${allData.data.TLT!!.description[i]}")
            if (i != 2) {
                talentStr = talentStr.plus("\n")
            }
        }
        reply(talentStr)
        reply(getAttributeStr("你获得了以下初始属性:\n", allData))
    }

    @OptIn(Api4J::class, ExperimentalSimbotApi::class)
    @RobotListen(desc = "人生重开")
    @Filter(">人生重开分配", matchType = MatchType.REGEX_MATCHES)
    suspend fun GroupMessageEvent.lifeRestartChoiceModel(
        sessionContext: ContinuousSessionContext,
        groupMessageEvent: GroupMessageEvent,
        fooEvent: Event
    ) {
        val authorId = author().id.toString()
        if (!lifeRestartCost(authorId)) {
            reply("你的无量币余额不足哦~ 使用[签到]来获取无量币吧")
            return
        }
        if (blackList.find { it.second == authorId }?.second == author().id.toString()) {
            return
        }

        val queryWrapper: QueryWrapper<Currency> =
            Wrappers.query<Currency?>().apply { eq("qqId", authorId) }
        val currencyExist = currencyMapper.selectOne(queryWrapper)
        send("游戏开始，你还有${currencyExist.money}无量币，每次游戏花费100无量币")
        val userData = allDataList.find { it.userId == authorId }
        allDataList.remove(userData)
        val timestamp = System.currentTimeMillis()
        val allData = AllDataClass(
            time = timestamp,
            userId = authorId,
            groupId = group.id.toString(),
            data = LifeRestart().startGameChoiceTalent()
        )

        var talentStr = "你获得了三个初始天赋:\n"
        for (i in 0 until 3) {
            talentStr = talentStr.plus("${allData.data.TLT!!.name[i]} ${allData.data.TLT!!.description[i]}")
            if (i != 2) {
                talentStr = talentStr.plus("\n")
            }
        }
        reply(talentStr)

        send("请在一分钟内输入 颜值 智力 体质 家境 快乐 的数值，以空格隔开")
        withTimeout(1.minutes) {
            sessionContext.waitingForNextMessage { event ->
                val attList = arrayListOf<Int?>()
                var attSum = 0
                var negative = false
                event.messageContent.plainText.split(" ").forEach {
                    val attBool = it.toIntOrNull()
                    if (attBool != null) {
                        attSum += attBool
                        if (attBool < 0) {
                            negative = true
                        }
                    }
                    attList.add(attBool)
                }
                if (fooEvent is GroupMessageEvent && event is GroupMessageEvent) {
                    if (fooEvent.author.id != event.author.id) {
                        return@waitingForNextMessage false
                    } else if (fooEvent.author.id == event.author.id && attList.contains(null)) {
                        reply("你输入的属性中有不为数字的值，请重新输入")
                        return@waitingForNextMessage false
                    } else if (fooEvent.author.id == event.author.id && negative) {
                        reply("你输入了负数的属性，请重新输入")
                        return@waitingForNextMessage false
                    } else if (fooEvent.author.id == event.author.id && attSum > allData.data.STATES) {
                        reply("你输入的属性超过了可分配的属性值，最大可分配值为${allData.data.STATES}，请重新输入")
                        return@waitingForNextMessage false
                    } else if (attList.size != 5) {
                        reply("你分配的属性数量不正确，若不想分配某个属性请在对应位置输入0")
                        return@waitingForNextMessage false
                    } else {
                        LifeRestart().choiceModelAtt(attList, allData.data)
                        reply(getAttributeStr("你获得了以下初始属性:\n", allData))
                        return@waitingForNextMessage true
                    }
                }
                false
            }
        }
        allDataList.add(allData)
    }


    @RobotListen(desc = "人生重开查看属性")
    @Filter("查看属性", matchType = MatchType.REGEX_MATCHES)
    suspend fun GroupMessageEvent.propertyView() {
        if (blackList.find { it.second == author().id.toString() }?.second == author().id.toString()) {
            return
        }
        val allData = allDataList.find { it.userId == author().id.toString() }
        if (allData != null) {
            reply(getAttributeStr("你现在的属性:\n", allData))
        } else {
            send("你还没有属性，输入 >人生重开 开始游戏")
        }
    }

    @RobotListen(desc = "人生重开下一步")
    @Filter("继续", matchType = MatchType.REGEX_MATCHES)
    suspend fun GroupMessageEvent.lifeRestartNext() {
        val authorId = author().id.toString()
        if (blackList.find { it.second == authorId }?.second == authorId) {
            return
        }
        var userData = allDataList.find { it.userId == authorId }?.data
        val userDataAll = allDataList.find { it.userId == authorId }
        if (userData != null) {

            val lifeRestart = LifeRestart()
            val lifeEvent = lifeRestart.eventsJson
            userDataAll!!.time = System.currentTimeMillis()

            val evtSizeBefore = userData.EVT.size
            userData = lifeRestart.nextGameStep(userData)
            val evtSize = userData.EVT.size
            var hasBranchEvent = false
            userData.EVT.subList(evtSizeBefore, evtSize).forEach { newEvent ->
                val branchEvt = newEvent.trim('"').split(":")[0]
                val isBranchEvent = newEvent.contains(":B")
                val eventValue = lifeEvent[branchEvt]?.get("event")?.toString()?.trim('"') ?: ""
                val postEvent = lifeEvent[branchEvt]?.get("postEvent")?.toString()?.trim('"') ?: ""
                if (isBranchEvent && !hasBranchEvent) {
                    hasBranchEvent = true
                    send("${userData.AGE - 1}岁:" + eventValue)
                } else if (hasBranchEvent) {
                    send(eventValue)
                    if (lifeEvent[branchEvt]["postEvent"] != null && !isBranchEvent) send(postEvent)
                } else {
                    send("${userData.AGE - 1}岁:" + eventValue)
                    if (lifeEvent[newEvent]["postEvent"] != null) send(postEvent)
                }
                if (branchEvt == "10000" && userData.LIF > 0)
                    send("但是你因为你的天赋复活了。")
            }

            if (userData.LIF <= 0) {
                send(getAttributeStr("你的人生结束了\n你的人生总结:\n你活了${userData.AGE - 1}岁\n", userDataAll))
                if (userData.AGE - 1 > 40) {
                    val addMoney = addCurrency(authorId, userData.AGE - 1)
                    send("你获得了${addMoney}无量币")
                }
                allDataList.remove(userDataAll)
            }
        } else {
            reply("你还没有开始游戏，请先输入 >人生重开 来进行游戏")
        }
    }

    @OptIn(Api4J::class)
    @RobotListen(desc = "人生重开下N步")
    @Filter("继续{{times}}", matchType = MatchType.REGEX_MATCHES)
    suspend fun GroupMessageEvent.lifeRestartNextMore(
        @FilterValue("times") times: Double,
        event: MiraiGroupMessageEvent
    ) {
        val authorId = author().id.toString()
        if (blackList.find { it.second == authorId }?.second == authorId) {
            return
        }
        val allData = allDataList.find { it.userId == authorId }
        var userData = allData?.data

        val lifeRestart = LifeRestart()
        val lifeEvent = lifeRestart.eventsJson
        val groupContact: Group = event.group.originalContact
        val formatTimes: Int = times.roundToInt()
        if (formatTimes > 20) {
            if (authorId == cacheList.find { it == authorId }) {
                reply("这是你第二次尝试输入超过限制的数，你已被加入黑名单，人生重开 在10分钟内不对你开放")
                blackList.add(Pair(System.currentTimeMillis(), authorId))
                allDataList.remove(allData)
                return
            }
            reply("警告：你正在尝试输入一个过大的数，继续的最大步数限制为20，如果继续尝试输入过大的数，你将被添加入黑名单中")
            cacheList.add(authorId)
            return
        }
        if (formatTimes < 1) {
            reply("你输入的值过小，请输入一个>=1的值")
            return
        }

        if (allData != null) {
            allData.time = System.currentTimeMillis()
            val forward: ForwardMessage = buildForwardMessage(groupContact) {
                for (i in 1..formatTimes) {
                    val evtSizeBefore = userData!!.EVT.size
                    userData = lifeRestart.nextGameStep(userData!!)
                    val evtSize = userData!!.EVT.size
                    var hasBranchEvent = false
                    userData!!.EVT.subList(evtSizeBefore, evtSize).forEach { newEvent ->
                        val longAuthorId = author().id.tryToLong()
                        val branchEvt = newEvent.trim('"').split(":")[0]
                        val isBranchEvent = newEvent.contains(":B")
                        val eventValue = lifeEvent[branchEvt]?.get("event")?.toString()?.trim('"') ?: ""
                        val postEvent = lifeEvent[branchEvt]?.get("postEvent")?.toString()?.trim('"') ?: ""
                        if (isBranchEvent && !hasBranchEvent) {
                            hasBranchEvent = true
                            add(
                                longAuthorId,
                                author().nickOrUsername,
                                PlainText("${userData!!.AGE - 1}岁:" + eventValue)
                            )
                        } else if (hasBranchEvent) {
                            add(
                                longAuthorId,
                                author().nickOrUsername,
                                PlainText(eventValue)
                            )
                            if (lifeEvent[branchEvt]["postEvent"] != null && !isBranchEvent) {
                                add(
                                    longAuthorId,
                                    author().nickOrUsername,
                                    PlainText(postEvent)
                                )
                            }
                        } else if (!hasBranchEvent) {
                            add(
                                longAuthorId,
                                author().nickOrUsername,
                                PlainText("${userData!!.AGE - 1}岁:" + eventValue)
                            )
                            if (lifeEvent[newEvent]["postEvent"] != null) {
                                add(
                                    longAuthorId,
                                    author().nickOrUsername,
                                    PlainText(postEvent)
                                )
                            }
                        }
                        if (branchEvt == "10000" && userData!!.LIF > 0) {
                            add(
                                longAuthorId,
                                author().nickOrUsername,
                                PlainText("但是你因为你的天赋复活了。")
                            )
                        }
                    }

                    if (userData!!.LIF <= 0) {
                        add(
                            author().id.tryToLong(),
                            author().nickOrUsername,
                            PlainText(getAttributeStr("你的人生结束了\n你的人生总结:\n你活了${userData!!.AGE - 1}岁\n", allData))
                        )
                        if (userData!!.AGE - 1 > 40) {

                            val addMoney = (userData!!.AGE - 1) * 2.5.toInt()
                            addCurrency(authorId, addMoney)
                            add(
                                author().id.tryToLong(),
                                author().nickOrUsername,
                                PlainText("你获得了${addMoney}无量币")
                            )
                        }
                        allDataList.remove(allData)
                        break
                    }
                }
            }
            groupContact.sendMessage(forward)
        } else {
            reply("你还没有开始游戏，请先输入 >人生重开 来进行游戏")
        }
    }
}