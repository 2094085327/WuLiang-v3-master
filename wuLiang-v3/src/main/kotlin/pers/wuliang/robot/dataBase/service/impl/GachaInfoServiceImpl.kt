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
//                        key = itemInfo.itemName!!.split("-")[1],
                        key = itemInfo.itemName.toString(),
                        value = itemInfo.times!!.toInt()
                    )
                    gachaDataList.add(gachaArray)
                }
            }
            return gachaDataList
        }
    }

    override fun insertByUid(uid: String, type: String, itemName: String, times: Int) {
        val queryWrapper = QueryWrapper<GachaInfo>()
            .eq("uid", uid)
            .eq("type", type)
            .eq("item_name", itemName)
            .eq("times", times)

        val haveCostWrapper = QueryWrapper<GachaInfo>()
            .eq("uid", uid)
            .eq("type", type)
            .eq("item_name", "已抽次数")
        if (gachaInfoMapper.selectOne(haveCostWrapper) == null) {
            gachaInfoMapper.update(GachaInfo(times = times), haveCostWrapper)
        }

        if (gachaInfoMapper.selectOne(queryWrapper) == null) {
            GachaInfo(
                uid = uid,
                gachaType = type,
                itemName = itemName,
                times = times,
                updateTime = System.currentTimeMillis().toString()
            ).let {
                gachaInfoMapper.insert(it)
            }
        }
    }
}