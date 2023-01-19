package pers.wuliang.robot.botApi.genShinApi.gameGuild

import cn.hutool.http.HttpUtil
import com.google.gson.Gson
import com.google.gson.JsonParser.parseString
import com.google.gson.annotations.SerializedName
import kotlin.reflect.full.memberProperties


/**
 *@Description:
 *@Author zeng
 *@Date 2023/1/13 20:04
 *@User 86188
 */
class GsonTest {
    data class DailyData(
        val monday: MutableMap<String, WeekDetail>,
        val tuesday: MutableMap<String, WeekDetail>,
        val wednesday: MutableMap<String, WeekDetail>,
        val thursday: MutableMap<String, WeekDetail>,
        val friday: MutableMap<String, WeekDetail>,
        val saturday: MutableMap<String, WeekDetail>
    ) {
        data class WeekDetail(val id: Int, val name: String, val reward: List<Int>, val city: Int)
    }

//    data class MaterialData(
//
//    )

    data class DailyInfo(
        @SerializedName("天赋") val avatar: WeekCn,
        @SerializedName("武器") val weapon: WeekCn
    ) {
        data class WeekCn(
            @SerializedName("周一", alternate = ["monday"])
            val monday: MutableMap<String, Any>,
            @SerializedName("周二", alternate = ["tuesday"])
            val tuesday: MutableMap<String, Any>,
            @SerializedName("周三", alternate = ["wednesday"])
            val wednesday: MutableMap<String, Any>,
            @SerializedName("周四", alternate = ["thursday"])
            val thursday: MutableMap<String, Any>,
            @SerializedName("周五", alternate = ["friday"])
            val friday: MutableMap<String, Any>,
            @SerializedName("周六", alternate = ["saturday"])
            val saturday: MutableMap<String, Any>
        ) {
            data class WeekDetail(val id: Int, val name: String, val reward: List<Int>, val city: Int)
        }
    }

    data class DetailData(
        val avatar: Items,
        val weapon: Items,
    ) {
        data class Items(
            val items: MutableMap<String, MutableMap<String, Any>>,
        )
    }

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


    fun getDailyMaterial(): String? {
        val dailyData =
            Gson().fromJson(parseString(HttpUtil.get(dailyUrl)).asJsonObject["data"], DailyData::class.java)

        val materialData = parseString(HttpUtil.get(materialUrl)).asJsonObject["data"].asJsonObject["items"]

        val upgradeData = parseString(HttpUtil.get(upgradeUrl)).asJsonObject["data"]

        val detailData = DetailData(
            avatar = Gson().fromJson(
                parseString(HttpUtil.get(characterUrl)).asJsonObject["data"],
                DetailData.Items::class.java
            ),
            weapon = Gson().fromJson(
                parseString(HttpUtil.get(weaponUrl)).asJsonObject["data"],
                DetailData.Items::class.java
            )
        )
        DailyData::class.memberProperties.forEach { it ->
            val domainData = it.get(dailyData) as MutableMap<*, DailyData.WeekDetail>
            val domainDataSort: MutableList<String> = mutableListOf()
            domainData.forEach { (sortKey) ->
                domainDataSort.add(sortKey.toString())
            }

            domainDataSort.forEach {
                val domainName = domainData[it]?.name.toString()
                val itemType = if (domainName.startsWith("精通秘境")) "avatar" else "weapon"
                val materialId = domainData[it]?.reward?.get(domainData[it]?.reward?.size?.minus(1) ?: 0)
                val material = materialData.asJsonObject[materialId.toString()]

                val used = mutableListOf<Pair<Any, String>>()

                val upgradeItem = upgradeData.asJsonObject[itemType].asJsonObject

                upgradeItem.keySet().forEach { id ->
                    if (material.asJsonObject["id"].toString() in upgradeItem[id].asJsonObject["items"].asJsonObject.keySet() && !upgradeItem[id].asJsonObject["icon"].toString()
                            .contains("Player")
                    ) {
                        used.add(
                            Pair(
                                upgradeItem[id],
                                parseString(Gson().toJson(detailData)).asJsonObject[itemType].asJsonObject["items"].asJsonObject[id].asJsonObject["name"].toString()
                            )
                        )
                    }
                    val arrayList: ArrayList<String> = arrayListOf()
                    for (u in used) {
                        arrayList.add(
                            "${parseString(Gson().toJson(u.first)).asJsonObject["rank"]}${
                                parseString(
                                    Gson().toJson(
                                        u.first
                                    )
                                ).asJsonObject["icon"].toString().replace("\"", "")
                            }-${u.second.replace("\"", "")}"
                        )
                    }
                    if (itemType == "avatar") {
//                        val avatar = parseString(Gson().toJson(detailData)).asJsonObject[itemType].asJsonObject["items"].asJsonObject[domainData[it]?.id.toString()]
//                        DailyInfo(avatar = arrayList)
                    } else {
//                        domainData[it]?.usedWeapon = arrayList
                    }

                    println(arrayList)
                }
                println(used)
            }
        }
        return null
    }
}

fun main() {
    val gsonTest = GsonTest()
//    gsonTest.testListToJson()
//    gsonTest.test2()
    gsonTest.getDailyMaterial()
}