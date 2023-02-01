package pers.wuliang.robot.botApi.genShinApi.gaCha

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import pers.wuliang.robot.util.HttpUtil


/**
 *@Description: 数据处理类
 *@Author zeng
 *@Date 2022/12/15 22:38
 *@User 86188
 */
class GachaData {
    private var objectMapper = ObjectMapper()

    private val baseUrl = "https://webstatic.mihoyo.com/hk4e/gacha_info/cn_gf01/"

    /**
     * 返回卡池id
     * @param gachaType 不同的卡池
     */
    fun getGaChaType(gachaType: String): Int {
        if (Regex("^角色|up池1|新角色池1|新限定1").containsMatchIn(gachaType))
            return 301
        if (Regex("^角色池2|up池2|新角色池2|新限定2").containsMatchIn(gachaType))
            return 400
        if (Regex("^常驻|普通池").containsMatchIn(gachaType))
            return 200
        return if (Regex("^武器|武器池").containsMatchIn(gachaType))
            302
        else
            0
    }

    /**
     * 获取当期卡池数据
     */
    private fun getInfoList(): JsonNode {
        val data = objectMapper.readTree(HttpUtil.get(baseUrl + "gacha/list.json").response)
        return data["data"]["list"]
    }

    /**
     * 获取卡池Id
     * @param data JsonNode类的数据
     * @param gachaType 卡池类型
     */
    @Suppress("SameParameterValue")
    private fun getGachaId(data: JsonNode, gachaType: Int): String {
        for (list in data) {
            if (list["gacha_type"].intValue() == gachaType)
                return list["gacha_id"].textValue()
        }
        return "error"
    }

    /**
     * 卡池具体信息
     * @param gachaId 卡池编号
     */
    private fun getGachaInfo(gachaId: String): JsonNode {
        return objectMapper.readTree(HttpUtil.get("$baseUrl$gachaId/zh-cn.json").response)
    }

    /**
     * 获取解析后的真实URL并加入参数
     * @param url 输入的未经过解析的URL
     * @return 返回解析后的URL
     */
    fun cleanUrl(url: String): String {
        // 中文正则，去除链接中的中文
        val regexChinese = "[\u4e00-\u9fa5]"
        // 获取无中文的链接
        val noChineseUrl = url.replace(regexChinese.toRegex(), "")
        // 从"#"处分割链接去除链接中的[#/log]并获取？后参数的链接
        val newUrl = noChineseUrl.substringBefore("#/log").substringAfter("?")
        // 含参链接
        return "https://hk4e-api.mihoyo.com/event/gacha_info/api/getGachaLog?$newUrl"
    }

    /**
     *处理后的链接
     * @param url 需要处理的链接
     * @param gachaType 卡池类型
     * @param times 页数
     * @param endId 当前页最后一个item的ID
     */
    fun getUrl(url: String, gachaType: Int, times: Int, endId: String): String {
        return "$url&gacha_type=$gachaType&page=$times&size=20&end_id=$endId"
    }

    /**
     * 对处理后链接进行验证
     * @param url 需要验证的链接
     */
    fun checkUrl(url: String): String {
        val data = objectMapper.readTree(HttpUtil.get(url).response)
        if (data.has("retcode")) {
            if (data["message"].textValue().equals("OK")) {
                GachaTool.instance.uid = data["data"]["list"][0]["uid"].textValue()
                return "验证通过，开始进行分析。请耐心等待，视抽卡数约需要30秒至1分钟"
            }
            if (data["message"].textValue().equals("authkey error")) {
                return "链接有错误，请重新获取链接"
            }
            return if (data["message"].textValue().equals("authkey timeout")) {
                "链接已经过期了，请获取最新链接"
            } else {
                "未知错误，错误信息:${data["message"]}"
            }
        } else {
            return "链接解析错误，请联系管理员处理"
        }
    }

    /**
     * 常驻池中常驻角色与物品
     */
    fun getPermanentData(): MutableList<String> {
        val infoList = getInfoList()
        val gachaId = getGachaId(infoList, 200)
        val data = getGachaInfo(gachaId)["r5_prob_list"]
        val r5Array = mutableListOf<String>()
        for (item in data) {
            if (item["is_up"].intValue() == 0) {
                r5Array.add(item["item_name"].textValue())
            }
        }
        return r5Array
    }

    data class ItemData(val key: String, val value: Int)

    /**
     * 将数据存入ItemData数据类中
     * @param item 存储着物品信息的数组
     * @param count 存储着每个物品对应抽数的数组
     */
    private fun createItemData(
        item: ArrayList<String>,
        count: Array<Int>,
    ): MutableList<ItemData> {
        val r5Array = getPermanentData()
        val itemLists: ArrayList<ItemData> = arrayListOf()
        for (i in item.indices) {
            val itemName = item[i].split("-")[1]

            if (r5Array.contains(itemName)) {
                itemLists.add(ItemData("${item[i]}(歪)", count[i + 1] + 1))
            } else {
                itemLists.add(ItemData(item[i], count[i + 1] + 1))
            }
        }
        itemLists.add(ItemData("已抽次数", count[0]))
        return itemLists
    }

    /**
     * 获取具体的抽卡信息
     * @param url 需要分析的链接
     * @param gachaType 卡池类型
     */
    fun getGachaData(url: String, gachaType: Int): MutableList<ItemData> {
        // 本页最后一条数据的id
        var endId = "0"
        // 总抽数，大保底次数，至五星为止的次数，已抽数，5星个数
        var alreadyCost = 0
        var costList: Array<Int> = arrayOf()
        val itemList: ArrayList<String> = arrayListOf()

        try {
            for (i in 1..9999) {
                // 接口URL地址
                val urls: String = getUrl(url, gachaType, i, endId)
                // 请求json数据
                val data = objectMapper.readTree(HttpUtil.get(urls).response)["data"]
                val length: Int = data["list"].size()

                // 当数组长度为0时(即没有抽卡记录时)跳出循环
                if (length == 0) {
                    costList += alreadyCost
                    alreadyCost = 0
                    break
                }
                endId = data["list"][length - 1]["id"].textValue()
                data["list"].forEach { item ->
                    if (item["rank_type"].textValue() == "5") {
                        costList += alreadyCost
                        itemList.add("${item["id"].textValue()}-${item["name"].textValue()}")
                        alreadyCost = 0
                    } else {
                        alreadyCost += 1
                    }
                }
                Thread.sleep(500)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        println("--卡池:$gachaType 分析完成--")
        return createItemData(itemList, costList)
    }
}
