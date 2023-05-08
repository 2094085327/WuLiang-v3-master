package pers.wuliang.robot.botApi.gameApi.lifeRestart

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import java.io.File
import java.util.*

/**
 *@Description: 人生重开模拟器的事件判定与数据处理
 *@Author zeng
 *@Date 2023/3/28 18:00
 *@User 86188
 */
class LifeRestart {
    private var objectMapper = ObjectMapper()
    private val rootPath = "resources/Json/lifeRestart/zh-cn"
    private val myTalent = MyTalent(effect = Effect(0, 0, 0, 0, 0, 0))
    private val talentJson = File("$rootPath/talents.json").absoluteFile.readText()
    private val ageJson = objectMapper.readTree(File("$rootPath/age.json").absoluteFile)
    val eventsJson = objectMapper.readTree(File("$rootPath/events.json"))!!

    private val random = Random()

    data class UserData(
        var AGE: Int = 0, // 年龄 age AGE
        var CHR: Int = 0, // 颜值 charm CHR
        var INT: Int = 0, // 智力 intelligence INT
        var STR: Int = 0, // 体质 strength STR
        var MNY: Int = 0, // 家境 money MNY
        var SPR: Int = 0, // 快乐 spirit SPR
        var LIF: Int = 1, // 生命 life LIFE
        val TMS: Int = 0, // 次数 times TMS
        var STATES: Int = 20, // 初始可用属性点
        val EVT: ArrayList<String> = arrayListOf(), // 事件 event EVT
        var TLT: MyTalent? = null // 天赋 talent TLT
    )

    data class Effect(
        val CHR: Int = 0,
        val INT: Int = 0,
        val STR: Int = 0,
        val MNY: Int = 0,
        val SPR: Int = 0,
        val LIF: Int = 0,
        var RDM: Int = 0,
    ) {
        operator fun plus(other: Effect): Effect {
            return Effect(
                CHR = this.CHR + other.CHR,
                INT = this.INT + other.INT,
                STR = this.STR + other.STR,
                MNY = this.MNY + other.MNY,
                SPR = this.SPR + other.SPR,
                LIF = this.LIF + other.LIF,
                RDM = this.RDM + other.RDM,
            )
        }
    }


    data class Talent(
        val id: String,
        val name: String?,
        val description: String,
        val status: Int = 0,
        val effect: Effect,
        val excludes: Set<Any?>,
        val condition: Pair<String, String>? = null, // 可空的 condition 字段
    )

    data class MyTalent(
        val id: ArrayList<String> = arrayListOf(),
        val name: ArrayList<String> = arrayListOf(),
        val description: ArrayList<String> = arrayListOf(),
        var status: Int = 0,
        var effect: Effect,
        var excludes: ArrayList<Any?> = arrayListOf(),
        var condition: ArrayList<Pair<String, String>> = arrayListOf(),
    )

    /**
     * 取得天赋的属性变化
     * @param effectData 属性数据
     * @return 返回改变后的数据类
     */
    private fun getEffect(effectData: JsonNode?): Effect {
        return Effect(
            CHR = effectData?.get("CHR")?.toString()?.toInt() ?: 0,
            INT = effectData?.get("INT")?.toString()?.toInt() ?: 0,
            STR = effectData?.get("STR")?.toString()?.toInt() ?: 0,
            MNY = effectData?.get("MNY")?.toString()?.toInt() ?: 0,
            SPR = effectData?.get("SPR")?.toString()?.toInt() ?: 0,
            LIF = effectData?.get("LIF")?.toString()?.toInt() ?: 0,
            RDM = effectData?.get("RDM")?.toString()?.toInt() ?: 0,
        )
    }

    /**
     * 改变用户属性
     * @param userData 用户数据
     * @param eventsEffect 获得的属性数据
     */
    private fun getUserDataEffect(userData: UserData, eventsEffect: JsonNode) {
        userData.CHR += eventsEffect["CHR"]?.toString()?.toInt() ?: 0
        userData.INT += eventsEffect["INT"]?.toString()?.toInt() ?: 0
        userData.STR += eventsEffect["STR"]?.toString()?.toInt() ?: 0
        userData.MNY += eventsEffect["MNY"]?.toString()?.toInt() ?: 0
        userData.SPR += eventsEffect["SPR"]?.toString()?.toInt() ?: 0
        userData.LIF += eventsEffect["LIF"]?.toString()?.toInt() ?: 0
    }


    /**
     * 获取初始化天赋
     */
    private fun getTalent() {
        val map: Map<*, *>? = objectMapper.readValue(talentJson, Map::class.java)
        val talents = map?.entries?.mapNotNull { entry ->
            val talentData = entry.value as? Map<*, *> ?: return@mapNotNull null
            val excludeList = talentData["exclude"] as? List<*>
            val effectData = talentData["effect"] as? JsonNode
            val effect = getEffect(effectData)
            val excludes = excludeList?.toSet() ?: emptySet()
            // 判断 condition 是否存在，存在则存储到 Talent 中
            val id = entry.key.toString()
            val condition = talentData["condition"]?.toString()
            Talent(
                id = id,
                name = talentData["name"].toString(),
                description = talentData["description"].toString(),
                status = talentData["status"]?.toString()?.toInt()?.plus(effect.RDM) ?: 0,
                effect = if (condition != null) Effect() else effect,
                excludes = excludes,
                condition = condition?.let { Pair(id, it) }
            )
        } ?: emptyList()

        var totalEffect = Effect(0, 0, 0, 0, 0, 0)

        val selectedTalents = mutableListOf<Talent>()
        while (selectedTalents.size < 3) {
            val availableTalents = talents.filter { it !in selectedTalents }
            val excludeTalents = selectedTalents.flatMap { it.excludes }.toSet()
            myTalent.excludes.addAll(excludeTalents)
            val selectableTalents = availableTalents.filter { it.id !in excludeTalents }
            // 通过返回一个元素顺序被打乱的新列表并获取它的第一个元素来取得天赋
            val selectedTalent = selectableTalents.shuffled().firstOrNull()
            if (selectedTalent != null) {
                selectedTalents.add(selectedTalent)
                totalEffect += selectedTalent.effect
                myTalent.status += selectedTalent.status
            }

            myTalent.id.add(selectedTalents.map { it.id }[selectedTalents.size - 1])
            myTalent.name.add(selectedTalents.map { it.name }[selectedTalents.size - 1].toString())
            myTalent.description.add(selectedTalents.map { it.description }[selectedTalents.size - 1])
            selectedTalents.map { it.condition }[selectedTalents.size - 1]?.let { myTalent.condition.add(it) }
        }
        myTalent.effect = totalEffect
    }

    /**
     * 判断事件是否满足条件
     * @param condition 条件
     * @param userData 用户数据
     * @return 返回布尔类型判断结果
     */
    private fun conditionEvt(condition: String, userData: UserData): Boolean {
        val andList = arrayListOf<Boolean>()
        val orList = arrayListOf<Boolean>()
        for (cond in condition.split("|")) { // 先假设所有条件都不符合要求
            val condList = cond.trim('"').replace("[()]".toRegex(), "").split("&")
            for (c in condList) {
                when {
                    c.startsWith("AGE") -> {
                        val (op, values) = getOpAndValues(c)
                        val propValue = userData.AGE
                        for (value in values) {
                            if ((op == "?" && propValue > value.toInt()) || (op == "!" && propValue > value.toInt())) {
                                andList.add(true)
                            } else {
                                andList.add(false)
                            }
                        }
                    }
                    c.startsWith("TLT") -> {
                        val (op, values) = getOpAndValues(c)
                        val tltList = arrayListOf<Boolean>()
                        for (value in values) {
                            if (op == "?" && userData.TLT!!.id.contains(value)) {
                                tltList.add(true)
                            } else if (op == "!" && userData.TLT!!.id.contains(value)) {
                                tltList.add(false)
                            } else {
                                tltList.add(false)
                            }
                        }
                        if (tltList.contains(true)) {
                            andList.add(true)
                        } else {
                            andList.add(false)
                        }
                    }
                    c.startsWith("EVT") -> {
                        val (op, values) = getOpAndValues(c)
                        val evtList = arrayListOf<Boolean>()
                        val userEvents = arrayListOf<String>()
                        userData.EVT.forEach { userEvt ->
                            userEvents.add(userEvt.split(":")[0])
                        }
                        for (value in values) {
                            if (op == "?" && userEvents.contains(value)) {
                                evtList.add(true)
                            } else if (op == "!" && userEvents.contains(value)) {
                                evtList.add(false)
                            } else {
                                evtList.add(false)
                            }
                        }
                        if (evtList.contains(true)) {
                            andList.add(true)
                        } else {
                            andList.add(false)
                        }
                    }
                }
                val matchCondition = arrayOf("CHR", "INT", "STR", "MNY", "SPR", "LIF")
                for (prefix in matchCondition) {
                    if (c.startsWith(prefix)) {
                        val (op, values) = getOpAndValues(c)
                        val propValue = userData.run {
                            javaClass.getDeclaredField(prefix).apply {
                                isAccessible = true
                            }.get(this).toString().toInt()
                        }
                        for (value in values) {
                            if (compare(propValue, op, value.toInt())) {
                                andList.add(true)
                                break
                            } else {
                                andList.add(false)
                                break
                            }
                        }
                    }
                }
            }
            if (andList.contains(false)) {
                orList.add(false)
            } else {
                orList.add(true)
            }
        }

        if (condition.split("|").size <= 1 && andList.contains(false)) {
            return false
        } else if (condition.split("|").size <= 1 && !andList.contains(false)) {
            return true
        } else if (condition.split("|").size > 1 && orList.contains(true)) {
            return true
        } else if (condition.split("|").size > 1 && !orList.contains(true)) {
            return false
        }
        return false
    }

    /**
     * 从条件表达式中提取出比较操作符和元素值
     * @param cond 条件表达式
     * @return 返回<运算符,数值数组>键值对
     */
    private fun getOpAndValues(cond: String): Pair<String, List<String>> {
        val opIndex = cond.indexOfAny(charArrayOf('<', '>', '?', '!'))
        val op = if (opIndex == -1) "" else cond[opIndex].toString()
        val valuesStr = Regex("\\[(.*?)]").find(cond)?.groupValues?.getOrNull(1) ?: ""
        var values = valuesStr.split(",").map { it.trim() }
        if (valuesStr.isBlank()) {
            val noBracketRegex = Regex("-?\\d+") // 匹配没有中括号的条件
            values = listOf(noBracketRegex.find(cond)?.value ?: "")
        }
        return Pair(op, values)
    }

    /**
     * 简化比较操作符的判断逻辑
     * @param val1 当前属性值
     * @param op   运算符
     * @param val2 目标值
     * @return 返回布尔类型
     */
    private fun compare(val1: Int, op: String, val2: Int): Boolean {
        return when (op) {
            "<" -> val1 < val2
            ">" -> val1 > val2
            else -> false
        }
    }

    /**
     * 天赋条件判断
     * @param userData 用户数据
     * @return 改动后的用户数据
     */
    private fun judgingCondition(userData: UserData): UserData {
        for (c in myTalent.condition) {
            if (conditionEvt(c.second, userData)) {
                val jsonData = objectMapper.readTree(talentJson)
                val effectData = jsonData[c.first]["effect"]
                val effect = getEffect(effectData)
                // TODO 条件成立时删除此条天赋
                val randomValue = generateRandomNumbers(effect.RDM)
                return UserData(
                    CHR = userData.CHR + effect.CHR + randomValue[0],
                    INT = userData.INT + effect.INT + randomValue[1],
                    STR = userData.STR + effect.STR + randomValue[2],
                    MNY = userData.MNY + effect.MNY + randomValue[3],
                    SPR = userData.SPR + effect.SPR + randomValue[4],
                    LIF = userData.LIF + effect.LIF,
                    EVT = userData.EVT,
                    TMS = userData.TMS,
                    STATES = userData.STATES,
                    TLT = myTalent
                )
            }
        }
        return userData
    }

    /**
     * 判断分支条件是否满足
     * @param eventAll 随机事件的全部信息
     * @param userData 用户数据
     * @param randomAgeEvent 当前随机事件
     */
    private fun branchEvent(eventAll: JsonNode, userData: UserData, randomAgeEvent: String) {
        val eventBranch = eventAll["branch"]
        var addBool = true
        eventBranch.forEach { branch ->
            val branchSplit = branch.toString().trim('"').split(":")
            val condition = branchSplit[0]
            val event = branchSplit[1]
            var ageEvent = randomAgeEvent
            if (conditionEvt(condition, userData)) {
                if (addBool) {
                    userData.EVT.add("$ageEvent:B")
                    addBool = false
                }
                val eventsEffect = eventsJson[event]["effect"]
                if (eventsJson[event]["effect"] != null) {
                    getUserDataEffect(userData, eventsEffect)
                }

                if (eventsJson[event]["branch"] != null) {
                    ageEvent = event
                    branchEvent(eventsJson[ageEvent], userData, ageEvent)
                }
            } else {
                if (addBool) {
                    userData.EVT.add(randomAgeEvent)
                    addBool = false
                }
            }
        }
        if (addBool) {
            userData.EVT.add(randomAgeEvent)
        }
    }


    /**
     *判断分支事件是否可行
     * @param event 事件JSON
     * @param userData 用户数据
     * @return 布尔类型的判断结果
     */
    private fun shouldIncludeEvent(event: JsonNode, userData: UserData): Boolean {
        val include = event["include"]?.toString() ?: ""
        val exclude = event["exclude"]?.toString() ?: ""
        return (include.isBlank() || conditionEvt(include, userData)) &&
                (exclude.isBlank() || !conditionEvt(exclude, userData))
    }

    /**
     * 使用递归使每次随机事件符合条件
     * @param ageEvent 当前年龄总事件
     * @param randomAgeEvent 当前年龄随机事件
     * @param userData 用户数据
     * @return 返回String类型的选中的事件
     */
    private fun getRandomEvent(ageEvent: JsonNode, randomAgeEvent: String, userData: UserData): String {
        var randomEvent = randomAgeEvent
        val eventAll = eventsJson[randomEvent]
        val includeEvent = shouldIncludeEvent(eventAll, userData)
        return if (includeEvent) {
            randomEvent
        } else {
            randomEvent = ageEvent[random.nextInt(0, ageEvent.size())].toString().split("*")[0].trim('"')
            getRandomEvent(ageEvent, randomEvent, userData)
        }
    }

    /**
     * 属性数组随机生成
     * @param max 属性总数
     */
    private fun generateRandomNumbers(max: Int): List<Int> {
        val count = 5
        val randomNumbers = mutableListOf<Int>()
        var remainingCount = count
        var remainingMax = max
        for (i in 1 until count) {
            val randomMax = remainingMax / remainingCount * 2
            val randomNumber = (0..randomMax).random()
            randomNumbers.add(randomNumber)
            remainingCount--
            remainingMax -= randomNumber
        }
        randomNumbers.add(remainingMax)
        if ((0..4).random() == 0) {
            val randomIndex = (0 until count).random()
            randomNumbers[randomIndex] += (1..remainingMax).random()
        }
        return randomNumbers
    }


    /**
     * 开始游戏
     * @return 返回角色数据
     */
    fun startGame(): UserData {
        getTalent()

        val users = UserData()
        users.STATES += myTalent.status + myTalent.effect.RDM

        val statesList = generateRandomNumbers(users.STATES)
        var userData = UserData(
            CHR = statesList[0] + myTalent.effect.CHR,
            INT = statesList[1] + myTalent.effect.INT,
            STR = statesList[2] + myTalent.effect.STR,
            MNY = statesList[3] + myTalent.effect.MNY,
            SPR = statesList[4] + myTalent.effect.SPR,
            LIF = 1 + myTalent.effect.LIF,
            STATES = users.STATES
        )

        if (myTalent.condition.size != 0) userData = judgingCondition(userData)
        userData.TLT = myTalent
        return userData
    }

    /**
     * 开始游戏-属性自定义模式
     * @return 返回角色数据
     */
    fun startGameChoiceTalent(): UserData {
        getTalent()

        val users = UserData()
        users.STATES += myTalent.status + myTalent.effect.RDM
        var userData = UserData(
            CHR = myTalent.effect.CHR,
            INT = myTalent.effect.INT,
            STR = myTalent.effect.STR,
            MNY = myTalent.effect.MNY,
            SPR = myTalent.effect.SPR,
            LIF = 1 + myTalent.effect.LIF,
            STATES = users.STATES
        )

        if (myTalent.condition.size != 0) userData = judgingCondition(userData)
        userData.TLT = myTalent
        return userData
    }

    /**
     * 选择模式自定义属性
     * @param attList 属性数组
     * @param userData 用户数据
     * @return 修改后的用户数据
     */
    fun choiceModelAtt(attList: ArrayList<Int?>, userData: UserData): UserData {
        userData.CHR += attList[0]!!
        userData.INT += attList[1]!!
        userData.STR += attList[2]!!
        userData.MNY += attList[3]!!
        userData.SPR += attList[4]!!
        return userData
    }

    /**
     * 下一步方法
     * @param userData 用户数据
     * @return 返回角色数据
     */
    fun nextGameStep(userData: UserData): UserData {
        val ageEvent = ageJson[userData.AGE.toString()]["event"]
        var randomAgeEvent = ageEvent[random.nextInt(0, ageEvent.size())].toString().split("*")[0].trim('"')

        randomAgeEvent = getRandomEvent(ageEvent, randomAgeEvent, userData)
        val eventAll = eventsJson[randomAgeEvent]
        val eventsEffect = eventAll["effect"]
        if (eventsEffect != null) {
            getUserDataEffect(userData, eventsEffect)
        }

        val eventBranch = eventAll["branch"]
        if (eventBranch != null) {
            branchEvent(eventAll, userData, randomAgeEvent)
        } else {
            userData.EVT.add(randomAgeEvent)
        }

        userData.AGE += 1
        return userData
    }
}