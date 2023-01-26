package pers.wuliang.robot.dataBase.service

import com.baomidou.mybatisplus.extension.service.IService
import pers.wuliang.robot.botApi.genShinApi.gaCha.GachaData
import pers.wuliang.robot.dataBase.entity.GachaInfo

/**
 *
 *
 * 服务类
 *
 *
 * @author zeng
 * @since 2023-01-20
 */
interface GachaInfoService : IService<GachaInfo?> {
    /**
     * 根据uid查询数据
     */
    fun selectByUid(uid: String, type: String): MutableList<GachaData.ItemData>?

    /**
     * 重构查询，根据uid判断数据是否存在
     */
    fun selectByUid(uid: String): Boolean

    /**
     * 根据uid插入数据
     */
    fun insertByUid(uid: String, type: String, itemName: String, times: Int)


}