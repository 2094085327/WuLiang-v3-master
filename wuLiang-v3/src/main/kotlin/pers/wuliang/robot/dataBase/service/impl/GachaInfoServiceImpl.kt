package pers.wuliang.robot.dataBase.service.impl

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import pers.wuliang.robot.botApi.genShinApi.gaCha.GachaData
import pers.wuliang.robot.dataBase.entity.GachaInfo
import pers.wuliang.robot.dataBase.mapper.GachaInfoMapper
import pers.wuliang.robot.dataBase.service.GachaInfoService

/**
 * 服务实现类
 * @author zeng
 * @since 2023-01-20
 */
@Service
class GachaInfoServiceImpl : ServiceImpl<GachaInfoMapper?, GachaInfo?>(), GachaInfoService {
    @Autowired
    lateinit var gachaInfoMapper: GachaInfoMapper

    override fun selectByUid(uid: String, type: String): MutableList<GachaData.ItemData>? {
        val queryWrapper = QueryWrapper<GachaInfo>().eq("uid", uid).eq("type", type).orderByDesc("item_name")
        val gachaBefore = gachaInfoMapper.selectList(queryWrapper)
        if (gachaBefore == null) {
            return null
        } else {
            val gachaDataList = mutableListOf<GachaData.ItemData>()

            for (itemInfo in gachaBefore) {
                if (itemInfo != null) {
                    val gachaArray = GachaData.ItemData(
                        key = itemInfo.itemName.toString(),
                        value = itemInfo.times!!.toInt()
                    )
                    gachaDataList.add(gachaArray)
                }
            }
            return gachaDataList
        }
    }

    override fun selectByUid(uid: String): Boolean {
        val queryWrapper = QueryWrapper<GachaInfo>().eq("uid", uid)
        val gachaInfo = gachaInfoMapper.selectList(queryWrapper)
        return gachaInfo.size != 0
    }

    override fun insertByUid(uid: String, type: String, itemName: String, times: Int) {
        val gachaInfo = GachaInfo(
            uid = uid,
            gachaType = type,
            itemName = itemName,
            times = times,
            updateTime = System.currentTimeMillis().toString()
        )
        val queryWrapper = QueryWrapper<GachaInfo>()
            .eq("uid", uid)
            .eq("type", type)
            .eq("item_name", itemName)
        val existGachaInfo = gachaInfoMapper.selectOne(queryWrapper)
        if (existGachaInfo != null) {
            if (itemName == "已抽次数") {
                gachaInfoMapper.update(gachaInfo, queryWrapper)
            }
        } else {
            gachaInfoMapper.insert(gachaInfo)
        }
    }
}