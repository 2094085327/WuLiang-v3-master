package pers.wuliang.robot.dataBase.entity

import com.baomidou.mybatisplus.annotation.IdType
import com.baomidou.mybatisplus.annotation.TableField
import com.baomidou.mybatisplus.annotation.TableId
import com.baomidou.mybatisplus.annotation.TableName
import lombok.AllArgsConstructor
import lombok.NoArgsConstructor
import lombok.ToString
import java.time.LocalDateTime

/**
 *@Description: 群聊消息实体类
 *@Author zeng
 *@Date 2022/11/9 0:15
 *@User 86188
 */
@AllArgsConstructor
@NoArgsConstructor
@ToString
@TableName("message")
data class Messages(
    @TableId("id", type = IdType.AUTO)
    val id: Int? = null,
    @TableField("groupId")
    val groupId: String,
    @TableField("groupName")
    val groupName: String,
    @TableField("content")
    val content: String,
    @TableField("sendUserCode")
    val sendUserCode: String,
    @TableField("sendUserName")
    val sendUserName: String,
    @TableField("time")
    val time: LocalDateTime
)