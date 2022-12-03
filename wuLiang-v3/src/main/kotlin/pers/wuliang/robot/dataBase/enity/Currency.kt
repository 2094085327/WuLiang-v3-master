package pers.wuliang.robot.dataBase.enity

import com.baomidou.mybatisplus.annotation.IdType
import com.baomidou.mybatisplus.annotation.TableField
import com.baomidou.mybatisplus.annotation.TableId
import com.baomidou.mybatisplus.annotation.TableName
import lombok.AllArgsConstructor
import lombok.NoArgsConstructor
import lombok.ToString
import java.time.LocalDateTime
import java.util.*

/**
 *@Description: QQ群货币
 *@Author zeng
 *@Date 2022/11/28 16:50
 *@User 86188
 */
@AllArgsConstructor
@NoArgsConstructor
@ToString
@TableName("currency")
data class Currency(
    @TableId("id", type = IdType.AUTO)
    val id: Int? = null,
    @TableField("qqId")
    val qqId: String? = null,
    @TableField("qqName")
    val qqName: String? = null,
    @TableField("level")
    val level: Int? = null,
    @TableField("exp")
    val exp: Int? = null,
    @TableField("money")
    val money: Int? = null,
    @TableField("updateTime")
    val updateTime: LocalDateTime? = null,
    @TableField("signTime")
    val signTime: String? = null ,
    @TableField("times")
    val times: Int? = null
)