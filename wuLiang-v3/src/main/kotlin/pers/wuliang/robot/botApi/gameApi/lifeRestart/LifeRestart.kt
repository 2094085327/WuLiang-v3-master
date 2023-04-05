package pers.wuliang.robot.botApi.gameApi.lifeRestart

import com.fasterxml.jackson.databind.ObjectMapper
import java.io.File
import java.util.*

/**
 *@Description:
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

    //    System.currentTimeMillis()
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
        val CHR: Int = 0,// 颜值 charm CHR
        val INT: Int = 0,// 智力 intelligence INT
        val STR: Int = 0, // 体质 strength STR
        val MNY: Int = 0, // 家境 money MNY
        val SPR: Int = 0, // 快乐 spirit SPR
        val LIF: Int = 0, // 生命 life LIFE
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
     * 获取初始化天赋
     */
    private fun getTalent() {
        val map: Map<*, *>? = objectMapper.readValue(talentJson, Map::class.java)
        val talents = map?.entries?.mapNotNull { entry ->
            val talentData = entry.value as? Map<*, *> ?: return@mapNotNull null
            val excludeList = talentData["exclude"] as? List<*>
            val effectData = talentData["effect"] as? Map<*, *>
            val effect = Effect(
                CHR = effectData?.get("CHR")?.toString()?.toInt() ?: 0,
                INT = effectData?.get("INT")?.toString()?.toInt() ?: 0,
                STR = effectData?.get("STR")?.toString()?.toInt() ?: 0,
                MNY = effectData?.get("MNY")?.toString()?.toInt() ?: 0,
                SPR = effectData?.get("SPR")?.toString()?.toInt() ?: 0,
                LIF = effectData?.get("LIF")?.toString()?.toInt() ?: 0,
                RDM = effectData?.get("RDM")?.toString()?.toInt() ?: 0,
            )
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
     * 随机分配属性
     */
    private fun assignValue(random: Random, users: UserData, lastTalent: Boolean): Int {
        val remainingPoints = users.STATES
        val value = if (lastTalent) remainingPoints else random.nextInt(0, remainingPoints + 1)
        users.STATES -= value
        return value
    }

    /**
     * 分配额外的随机属性
     */
    private fun rdmValue(random: Random, effect: Effect, lastRDM: Boolean): Int {
        val remainingPoints = effect.RDM
        val value = if (lastRDM) remainingPoints else random.nextInt(0, remainingPoints + 1)
        effect.RDM -= value
        return value
    }

    /**
     * 判断是否满足条件
     */
    private fun isConditionSatisfied(condition: String, userData: UserData): Boolean {
        // 分割多个条件
        val firstCondition = condition.split("|")
//        println("firstCondition$firstCondition")
        var anyTrue = false
        var allFalse = true // 是否所有条件都为 false
        for (f in firstCondition) {
            val conditions = f.trim('"').replace("[()]".toRegex(), "").split("&")

            for (c in conditions) {
//                println("c$c")
                // 找到操作符
                val opIndex = c.indexOfAny(listOf("<", ">"))
                if (opIndex != -1) {
                    // 获取属性
                    val prop = c.substring(0, opIndex)
                    // 获取操作符
                    val op = c[opIndex].toString()
                    // 获取比较值
                    val valueStr = c.substring(opIndex + 1)
                    // 获取比较值
                    val value = valueStr.toInt()
                    val propValue =
                        userData.javaClass.getDeclaredField(prop).apply { isAccessible = true }.get(userData).toString()
                            .toInt()
//                    println(propValue)
//                    val propValue = userData::class.java.getDeclaredField(prop).also {
//                        it.isAccessible = true
//                    }.get(userData).toString().toInt()
//                    val propValue =getattr

                    when (op) {
                        "<" -> if (propValue < value) {
                            allFalse = false
                            anyTrue = true
                        } else allFalse = true
                        ">" -> if (propValue > value) {
                            allFalse = false
                            anyTrue = true
                        } else allFalse = true
//                        else -> allFalse = true
                    }
//                    value?.let { v ->
//                        val propValue = userData::class.java.getDeclaredField(prop).also {
//                            it.isAccessible = true
//                        }.get(userData).toString().toInt()
//                        when (op) {
//                            "<" -> if (propValue >= v) allFalse = true
//                            ">" -> if (propValue <= v) allFalse = true
//                            else -> allFalse = true
//                        }
//                    }

                } else {
                    if (c.startsWith("AGE?")) {
                        // 匹配 AGE?[70] 单数字的正则表达式
                        val regex1 = Regex("AGE\\?\\[(\\d+)]")
                        // 匹配 AGE?[70,80] 范围的正则表达式
                        val regex2 = Regex("AGE\\?\\[(\\d+),(\\d+)]")

                        val valid = regex1.matchEntire(c)?.groupValues?.get(1)?.toInt()?.let {
                            userData.AGE >= it
                        }
                            ?: regex2.matchEntire(c)?.groupValues?.let { values ->
                                Pair(values[1].toInt(), values[2].toInt())
                            }?.let {
                                userData.AGE in it.first..it.second
                            }
                            ?: false
                        if (valid) {
                            allFalse = false
                            anyTrue = true
                        } else {
                            allFalse = true
                        }
                    } else if (c.startsWith("TLT?")) {
                        val regex = Regex("\\[(.*?)]")
                        val matchResult = regex.find(c)
                        if (matchResult != null) {
                            val valuesStr = matchResult.groupValues[1]
                            val values = valuesStr.split(",").map { it }
                            for (value in values) {
                                if (userData.TLT!!.id.contains(value)) {
                                    allFalse = false
                                    anyTrue = true
                                }
                            }
//                            allFalse = true
                        }
                    } else if (c.trim('"').startsWith("EVT?")) {
                        val regex = Regex("\\[(.*?)]")
                        val matchResult = regex.find(c)
                        if (matchResult != null) {
                            val valuesStr = matchResult.groupValues[1]
                            val values = valuesStr.split(",").map { it }
//                        println(values)
                            for (value in values) {
//                                println(value)
//                                println(userData.EVT)
                                if (userData.EVT.contains(value)) {
                                    allFalse = false
                                    anyTrue = true
                                }
                            }
//                            allFalse = true
                        }
                    }
                }
            }
        }
        return if (firstCondition.size > 1) {
            anyTrue
        } else {
            !allFalse
        }
//        return true // 所有条件都满足
    }

    private fun judgingCondition(random: Random, userData: UserData): UserData {
        for (c in myTalent.condition) {
//            println(c.second)
//            println("isConditionSatisfied(c.second, userData)${isConditionSatisfied(c.second, userData)}")
            if (isConditionSatisfied(c.second, userData)) {
//                effect: Effect
//                val jsonNode = File(talentPath).absoluteFile.readText()
                val jsonData = objectMapper.readTree(talentJson)
                val effectData = jsonData[c.first]["effect"]
//                println(effectData)
                val effect = Effect(
                    CHR = effectData?.get("CHR")?.toString()?.toInt() ?: 0,
                    INT = effectData?.get("INT")?.toString()?.toInt() ?: 0,
                    STR = effectData?.get("STR")?.toString()?.toInt() ?: 0,
                    MNY = effectData?.get("MNY")?.toString()?.toInt() ?: 0,
                    SPR = effectData?.get("SPR")?.toString()?.toInt() ?: 0,
                    LIF = effectData?.get("LIF")?.toString()?.toInt() ?: 0,
                    RDM = effectData?.get("RDM")?.toString()?.toInt() ?: 0,
                )
                // TODO 条件成立时删除此条天赋
                return UserData(
                    CHR = userData.CHR + effect.CHR + rdmValue(random, effect, false),
                    INT = userData.INT + effect.INT + rdmValue(random, effect, false),
                    STR = userData.STR + effect.STR + rdmValue(random, effect, false),
                    MNY = userData.MNY + effect.MNY + rdmValue(random, effect, false),
                    SPR = userData.SPR + effect.SPR + rdmValue(random, effect, true),
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

    fun startGame(): UserData {
        getTalent()

        val users = UserData()
        users.STATES += myTalent.status
//        println(users.STATES)
        var userData = UserData(
            CHR = assignValue(random, users, false) +
                    myTalent.effect.CHR +
                    rdmValue(random, myTalent.effect, false),
            INT = assignValue(random, users, false) +
                    myTalent.effect.INT +
                    rdmValue(random, myTalent.effect, false),
            STR = assignValue(random, users, false) +
                    myTalent.effect.STR +
                    rdmValue(random, myTalent.effect, false),
            MNY = assignValue(random, users, false) +
                    myTalent.effect.MNY +
                    rdmValue(random, myTalent.effect, false),
            SPR = assignValue(random, users, true) +
                    myTalent.effect.SPR +
                    rdmValue(random, myTalent.effect, false),
            LIF = 1 + myTalent.effect.LIF,
            STATES = 20 + myTalent.effect.RDM
        )
//        userData.TLT.addAll(myTalent.id)

//        println(userData)
//        println(userData.STATES)

        if (myTalent.condition.size != 0) {
//            println(myTalent.condition)
            userData = judgingCondition(random, userData)
        }

        userData.TLT = myTalent

        return userData

    }

    fun nextGameStep(userData: UserData): UserData {
        val ageEvent = ageJson[userData.AGE.toString()]["event"]
        var randomAgeEvent = ageEvent[random.nextInt(0, ageEvent.size())].toString().split("*")[0].trim('"')
        val eventAll = eventsJson[randomAgeEvent]
        var eventExclude = eventAll["exclude"]
        var eventInclude = eventAll["include"]
        while (true) {
            if (eventExclude != null && eventInclude == null) {
                if (!isConditionSatisfied(eventExclude.toString(), userData)) {
                    break
                }
            } else if (eventInclude != null && eventExclude == null) {
                if (isConditionSatisfied(eventInclude.toString(), userData)) {
                    break
                }
            } else if (eventExclude != null) {
                if (!isConditionSatisfied(eventExclude.toString(), userData) &&
                    isConditionSatisfied(eventInclude.toString(), userData)
                ) {
                    break
                }
            } else {
                break
            }
            randomAgeEvent = ageEvent[random.nextInt(0, ageEvent.size())].toString().split("*")[0].trim('"')
            eventExclude = eventsJson[randomAgeEvent]["exclude"]
            eventInclude = eventsJson[randomAgeEvent]["include"]
        }
        val eventsEffect = eventsJson[randomAgeEvent]["effect"]
        if (eventsEffect != null) {
            userData.CHR += eventsEffect["CHR"]?.toString()?.toInt() ?: 0
            userData.INT += eventsEffect["INT"]?.toString()?.toInt() ?: 0
            userData.STR += eventsEffect["STR"]?.toString()?.toInt() ?: 0
            userData.MNY += eventsEffect["MNY"]?.toString()?.toInt() ?: 0
            userData.SPR += eventsEffect["SPR"]?.toString()?.toInt() ?: 0
            userData.LIF += eventsEffect["LIF"]?.toString()?.toInt() ?: 0
        }
        userData.EVT.add(randomAgeEvent)
        userData.AGE += 1
        println(userData)
        return userData
    }
}