package pers.wuliang.robot.botApi.genShinApi.gaCha

import com.fasterxml.jackson.databind.JsonNode
import pers.wuliang.robot.botApi.geographyApi.objectMapper
import pers.wuliang.robot.util.HttpUtil


/**
 *@Description: 数据处理类
 *@Author zeng
 *@Date 2022/12/15 22:38
 *@User 86188
 */
class GachaData {
    private val baseUrl = "https://webstatic.mihoyo.com/hk4e/gacha_info/cn_gf01/"

    companion object {
        private val r5Array = mutableListOf<String>()
        var count: Int = 0
        var haveCost: Int = 0
        var right: Int = 0
        var probability: Double = 0.0
        var aveFive: String = ""
        var finProbability: Double = 0.0
        var finCount: Int = 0
        var finItem: Int = 0
    }

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
    fun getPermanentData() {
        val infoList = getInfoList()
        val gachaId = getGachaId(infoList, 200)
        val data = getGachaInfo(gachaId)["r5_prob_list"]
        for (item in data) {
            if (item["is_up"].intValue() == 0) {
                r5Array.add(item["item_name"].textValue())
            }
        }
    }

    data class ItemData(val key: String, val value: Int)

    /**
     * 将数据存入ItemData数据类中
     * @param role 存储着角色信息的数组
     * @param count 存储着每个角色对应抽数的数组
     */
    private fun createItemData(role: Array<String>, count: Array<Int>): MutableList<ItemData> {
        val itemList = mutableListOf<ItemData>()
        haveCost = count[0]
        for (i in role.indices) {
            if (r5Array.contains(role[i])) {
                itemList.add(ItemData("${role[i]}(歪)", count[i + 1] + 1))
            } else {
                itemList.add(ItemData(role[i], count[i + 1] + 1))
                right += 1
            }
        }
        return itemList
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
        var roleList: Array<String> = arrayOf()
        var costList: Array<Int> = arrayOf()
        for (i in 1..9999) {
            // 接口URL地址
            val urls: String = getUrl(url, gachaType, i, endId)

            // 请求json数据
            val data = objectMapper.readTree(HttpUtil.get(urls).response)["data"]
            val length: Int = data["list"].size()
            count += length

            // 当数组长度为0时(即没有抽卡记录时)跳出循环
            if (length == 0) {
                costList += alreadyCost
                alreadyCost = 0
                break
            }
            endId = data["list"][length - 1]["id"].textValue()
            data["list"].forEach { item ->
                val rankType: String = item["rank_type"].textValue()
                if (rankType == "5") {
                    costList += alreadyCost
                    roleList += item["name"].textValue()
                    alreadyCost = 0
                } else {
                    alreadyCost += 1
                }
            }
            Thread.sleep(500)
        }
        return createItemData(roleList, costList)
    }

    /**
     * 获取每个卡池对应的概率
     * @param times 每个卡池的保底次数
     * @param gachaType 卡池类型
     */
    fun getProbability(times: Int, gachaType: Int) {
        // 欧非判断公式 以(1-平均出金数/90)+(不歪的几率*50%) 作为概率
        if (count == 0) {
            probability = 50.0
            aveFive = "--"
        }
        if (count > 73 && GachaMain.dataArray.size == 0) {
            probability = 20.0
            aveFive = "--"
        } else {
            right = if (gachaType == 200) GachaMain.dataArray.size else right
            probability =
                (1 - (count.toFloat() / GachaMain.dataArray.size) / times + (right.toFloat() / GachaMain.dataArray.size) * 0.5) * 100
            aveFive =
                if (GachaMain.dataArray.size == 1) GachaMain.dataArray[0].value.toFloat().toString() else String.format(
                    "%.1f",
                    count.toFloat() / GachaMain.dataArray.size
                )
        }
        finProbability += probability
        finCount += count
        finItem += GachaMain.dataArray.size
    }
}
