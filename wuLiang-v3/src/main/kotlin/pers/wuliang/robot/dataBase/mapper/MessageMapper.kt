package pers.wuliang.robot.dataBase.mapper

import com.baomidou.mybatisplus.core.mapper.BaseMapper
import org.apache.ibatis.annotations.Mapper
import pers.wuliang.robot.dataBase.enity.Messages

/**
 *@Description: 消息存储的Mapper
 *@Author zeng
 *@Date 2022/11/9 9:19
 *@User 86188
 */
@Mapper
interface MessageMapper : BaseMapper<Messages>