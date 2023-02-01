package pers.wuliang.robot.botApi.genShinApi.gameGuild

import cn.hutool.http.HttpUtil
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody
import pers.wuliang.robot.botApi.genShinApi.genShinConfig.GenShinConfig
import java.io.File
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.*

/**
 *@Description:
 *@Author zeng
 *@Date 2023/1/12 21:18
 *@User 86188
 */
@Controller
class DailyMaterial {
    private var objectMapper = ObjectMapper()

    private val weekCn = mapOf(
        "monday" to "周一",
        "tuesday" to "周二",
        "wednesday" to "周三",
        "thursday" to "周四",
        "friday" to "周五",
        "saturday" to "周六"
    )

    private val materialUrl = "https://api.ambr.top/v2/chs/material"
    private val characterUrl = "https://api.ambr.top/v2/chs/avatar"
    private val weaponUrl = "https://api.ambr.top/v2/chs/weapon"
    private val dailyUrl = "https://api.ambr.top/v2/chs/dailyDungeon"
    private val upgradeUrl = "https://api.ambr.top/v2/static/upgrade"

    /**
     * 从接口中获取数据并保存到本地json文件中
     */
    @RequestMapping("/dailyMaterial")
    @ResponseBody
    fun getDailyMaterial(): JsonNode {
        val path = GenShinConfig().genShinJson + "material.json"
        if (isFileModified(path)) {
            val dailyInfo =
                mutableMapOf("天赋" to mutableMapOf<String, MutableMap<String, Any>>(), "武器" to mutableMapOf())
            val dailyData = objectMapper.readTree(HttpUtil.get(dailyUrl))["data"]
            val materialData = objectMapper.readTree(HttpUtil.get(materialUrl))["data"]
            val upgradeData = objectMapper.readTree(HttpUtil.get(upgradeUrl))["data"]
            val avatarData = objectMapper.readTree(HttpUtil.get(characterUrl))["data"]
            val weaponData = objectMapper.readTree(HttpUtil.get(weaponUrl))["data"]
            val detailData = mapOf("avatar" to avatarData, "weapon" to weaponData)
            // 遍历dailyData获取全部key
            for (week in dailyData.fieldNames()) {
                // 判断是否为周日，如果是周日则跳过
                if (week == "sunday") {
                    continue
                } else {
                    dailyInfo["天赋"] =
                        dailyInfo["天赋"]?.plus(mutableMapOf(weekCn[week].toString() to mutableMapOf())) as MutableMap<String, MutableMap<String, Any>>
                    dailyInfo["武器"] =
                        dailyInfo["武器"]?.plus(mutableMapOf(weekCn[week].toString() to mutableMapOf())) as MutableMap<String, MutableMap<String, Any>>
                    val domainData = dailyData[week]
                    val domainDataSort: MutableList<String> = mutableListOf()
                    for (domainKey in dailyData[week].fieldNames()) {
                        domainDataSort.add(domainKey)
                    }

                    // 对数据按照"city"的值进行排序
                    domainDataSort.sortedBy { domainData[it]["city"].textValue() }
                    for (domainKeys in domainDataSort) {
                        val itemType =
                            if (domainData[domainKeys]["name"].textValue().startsWith("精通秘境")) "avatar" else "weapon"

                        val rewards =
                            domainData[domainKeys]["reward"][domainData[domainKeys]["reward"].size() - 1].toString()
                        val material = materialData["items"][rewards]
                        val used = mutableListOf<Pair<JsonNode, String?>>()
                        val upgradeDataItems = upgradeData[itemType]
                        val materialId = material["id"].toString()

                        // 获取升级材料
                        val map =
                            dailyInfo[if (itemType == "avatar") "天赋" else "武器"]!![weekCn[week].toString()] as MutableMap<String, Any>
                        for (key in upgradeDataItems.fieldNames()) {
                            val items = upgradeDataItems[key]["items"]
                            if (materialId in items.fieldNames().asSequence()
                                    .toList() && !upgradeData[itemType][key]["icon"].toString().contains("Player")
                            ) {
                                used.add(
                                    Pair(
                                        upgradeData[itemType][key],
                                        detailData[itemType]?.get("items")?.get(key)?.get("name")?.textValue()
                                    )
                                )
                            }
                            val arrayList: ArrayList<String> = arrayListOf()
                            for (u in used) {
                                arrayList.add("${u.first["rank"]}${u.first["icon"].textValue()}-${u.second}")
                            }
                            map[domainData[domainKeys]["name"].textValue()] =
                                mutableMapOf("${material["name"].textValue()}-${material["icon"].textValue()}" to arrayList)
                        }
                        dailyInfo[if (itemType == "avatar") "天赋" else "武器"]!![weekCn[week].toString()] = map
                    }
                }
            }
            // 转换为json格式并写入文件
            val jsonStr = objectMapper.writeValueAsString(dailyInfo)
            File(path).absoluteFile.writeText(jsonStr)
            return objectMapper.readTree(jsonStr)
        } else {
            return objectMapper.readTree(File(path).absoluteFile.readText())
        }
    }

    /**
     * 判断文件修改时间是否超过3天
     * @param path 文件路径
     */
    fun isFileModified(path: String): Boolean {
        val file = File(path).absoluteFile
        if (!file.parentFile.exists()) {
            file.parentFile.mkdirs()
        }
        return if (file.exists()) {
            val lastModified = file.lastModified()
            val currentTime = System.currentTimeMillis()
            val oneDay = 24 * 60 * 60 * 1000 * 15
            if (currentTime - lastModified > oneDay) {
                println("文件已经超过15天，重新写入")
                true
            } else {
                println("文件未超过15天，不重新写入")
                false
            }
        } else {
            println("文件不存在，重新写入")
            true
        }
    }

    /**
     * 判断输入的日期为周几
     * @param week 日期
     */
    fun week(week: String): String {
        val today = LocalDate.now()

        val day: String = when (week) {
            in arrayListOf("今天", "今日", "现在") -> today.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.CHINA)
            in arrayListOf("明天", "明日") -> today.plusDays(1).dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.CHINA)
            in arrayListOf("后天", "后日") -> today.plusDays(2).dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.CHINA)
            in arrayListOf("周一", "周二", "周三", "周四", "周五", "周六", "周日") -> week
            else -> "error"
        }
        if (day == "error") {
            if (getMaterialByName(week) != "error") {
                return true.toString()
            }
        }
        return day
    }

    /**
     * 根据星期获取对应的数据
     * @param dailyData json数据
     * @param week 星期
     * @param itemType 材料类型
     */
    fun getMaterial(dailyData: JsonNode, itemType: String, week: String): String {
        var message = "${week}${itemType}:\n"
        dailyData[itemType][week].fieldNames().forEach { mysteryName ->
            message += "${mysteryName}\n"
            dailyData[itemType][week][mysteryName].fieldNames().forEach { itemName ->
                val arrayNode = dailyData[itemType][week][mysteryName][itemName]
                var name = ""
                for (i in 0 until arrayNode.size()) {
                    name += " ${arrayNode[i].textValue().split("-")[1]}"
                }
                message += "${itemName.split("-")[0]}:\n${name}\n\n"
            }
        }
        return message
    }

    /**
     * 根据星期对数据进行分类
     */
    @RequestMapping("/weekMaterial")
    @ResponseBody
    fun makeMaterialFromWeek(): JsonNode {
        val path = GenShinConfig().genShinJson + "materialWeek.json"
        if (isFileModified(path)) {
            val jsonNode = getDailyMaterial()
            val sourceNode = objectMapper.readTree(
                """{"周一": {},"周二": {},"周三": {},"周四": {},"周五": {},"周六": {}}""".trimMargin()
            )
            val weekDays = arrayOf("周一", "周二", "周三", "周四", "周五", "周六")
            val typeNames = arrayOf("天赋", "武器")

            weekDays.forEach { weekDay ->
                typeNames.forEach { typeName ->
                    jsonNode[typeName][weekDay].fieldNames().forEach { mysteryName ->
                        (sourceNode[weekDay] as ObjectNode).set<JsonNode>(
                            mysteryName,
                            jsonNode[typeName][weekDay][mysteryName]
                        )
                    }
                }
            }
            // 转换为json格式并写入文件
            val jsonStr = objectMapper.writeValueAsString(sourceNode)
            File(path).absoluteFile.writeText(jsonStr)
            return objectMapper.readTree(jsonStr)
        } else {
            return objectMapper.readTree(File(path).absoluteFile.readText())
        }
    }

    /**
     * 根据物品名获取对应的数据
     * @param name 物品名
     */
    fun getMaterialByName(name: String): String {
        val jsonNode = makeMaterialFromWeek()
        val weekMap = mapOf(
            "周一" to "周一/周四/周日",
            "周二" to "周二/周五/周日",
            "周三" to "周三/周六/周日",
            "周四" to "周一/周四/周日",
            "周五" to "周二/周五/周日",
            "周六" to "周三/周六/周日"
        )
        val paths = arrayOf("周一", "周二", "周三", "周四", "周五", "周六")

        for (path in paths) {
            val week = jsonNode.path(path)
            for (mystery in week.fields()) {
                val mysteryName = mystery.key
                for (item in mystery.value.fields()) {
                    val itemName = item.key
                    val items = item.value.toString()
                    if (items.contains(name)) {
                        return "日期:${weekMap[path]}\n秘境:$mysteryName\n物品:${itemName.split("-")[0]}\n角色/武器:$name"
                    }
                }
            }
        }
        return "error"
    }

    /**
     * 通过别名获取真名
     * @param name 别名
     */
    fun getNameByAlias(name: String): String? {
        val file = File(GenShinConfig().characterJson).absoluteFile
        if (!file.exists()) {
            println("文件不存在")
            return name
        }
        val characterAlias: JsonNode = objectMapper.readTree(file)
        for (character in characterAlias) {
            for (names in character) {
                if (names.asText().equals(name)) {
                    return character[0].textValue()
                }
            }
        }
        return name
    }
}
