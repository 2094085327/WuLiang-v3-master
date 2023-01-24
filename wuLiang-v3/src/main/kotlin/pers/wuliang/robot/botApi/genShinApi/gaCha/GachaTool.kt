package pers.wuliang.robot.botApi.genShinApi.gaCha

/**
 *@Description:
 *@Author zeng
 *@Date 2023/1/24 16:40
 *@User 86188
 */
class GachaTool {
    companion object {
        val instance: GachaTool by lazy { GachaTool() }
    }

    /**
     * 抽卡的uid
     */
    var uid: String = ""

    /**
     * 分析后的数据
     */
    var dataArray = mutableListOf<GachaData.ItemData>()

    /**
     * 抽卡总次数
     */
    var allTimes = 0

    /**
     * 出货次数
     */
    var allRightTimes = 0

    /**
     * 总概率
     */
    var finalProbability = 0f

    fun reset() {
        uid = ""
        dataArray = mutableListOf()
        allTimes = 0
        allRightTimes = 0
        finalProbability = 0f
    }

}