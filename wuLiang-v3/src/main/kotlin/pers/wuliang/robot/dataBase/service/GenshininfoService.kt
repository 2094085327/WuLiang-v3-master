package pers.wuliang.robot.dataBase.service

import com.baomidou.mybatisplus.extension.service.IService
import pers.wuliang.robot.dataBase.entity.GenshinInfo

/**
 *
 *
 * 服务类
 *
 *
 * @author zeng
 * @since 2023-01-20
 */
interface GenshininfoService : IService<GenshinInfo?> {
    /**
     * 根据uid插入数据
     */
    fun insertByUid(uid: String, qqId: String, sToken: String,state: Int)

    /**
     * 根据QQ号查询数据
     */
    fun selectByQqId(qqId: String):String
}