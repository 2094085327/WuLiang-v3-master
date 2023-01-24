package pers.wuliang.robot.dataBase.entity

import com.baomidou.mybatisplus.annotation.*
import lombok.*
import java.util.*

/**
 *
 * @author zeng
 * @since 2023-01-22
 */
@AllArgsConstructor
@NoArgsConstructor
@ToString
@TableName("gachainfo")
data class GachaInfo(
    @TableId(value = "id", type = IdType.AUTO)
    private val id: Int? = null,

    /**
     * 用户UID
     */
    @TableField(value = "uid")
    private val uid: String? = null,

    /**
     * 卡池类型
     */
    @TableField(value = "type")
    private val gachaType: String? = null,

    /**
     * 物品名称
     */
    @TableField(value = "item_name") val itemName: String? = null,

    /**
     * 所抽次数
     */
    @TableField(value = "times") val times: Int? = null,

    /**
     * 更新时间
     */
    @TableField(value = "update_time")
    val updateTime: String? = null

)