package pers.wuliang.robot.dataBase.service.impl

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper
import com.baomidou.mybatisplus.core.toolkit.Wrappers
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import pers.wuliang.robot.dataBase.entity.GenshinInfo
import pers.wuliang.robot.dataBase.mapper.GenshinInfoMapper
import pers.wuliang.robot.dataBase.service.GenshininfoService
import java.time.LocalDateTime

/**
 * 服务实现类
 * @author zeng
 * @since 2023-01-20
 */
@Service
class GenshininfoServiceImpl : ServiceImpl<GenshinInfoMapper?, GenshinInfo?>(), GenshininfoService {
    @Autowired
    lateinit var genshininfoMapper: GenshinInfoMapper

    fun insertOrUpdate(genshininfo: GenshinInfo, uid: String) {
        val queryWrapper: QueryWrapper<GenshinInfo> =
            Wrappers.query<GenshinInfo?>().apply { eq("uid", uid) }
        if (genshininfoMapper.selectOne(queryWrapper) == null) {
            genshininfoMapper.insert(genshininfo)
        } else {
            genshininfoMapper.update(genshininfo, queryWrapper)
        }
    }

    override fun insertByUid(uid: String, qqId: String, sToken: String, state: Int) {
        val genshininfo = GenshinInfo(
            uid = uid,
            qqId = qqId,
            sToken = sToken,
            push = 0,
            deletes = 1,
            status = state,
            updateTime = LocalDateTime.now()
        )
        insertOrUpdate(genshininfo, uid)
    }

    override fun selectByQqId(qqId: String): String {
        QueryWrapper<GenshinInfo>().apply {
            eq("qqid", qqId)
        }.let {
            val genshininfo = genshininfoMapper.selectList(it) ?: return "未绑定"
            if (genshininfo.size == 1) {
                return if (genshininfo[0]?.status == 0) {
                    "过期"
                } else {
                    genshininfo[0]?.sToken.toString()
                }

            } else {
                genshininfo.forEach { _ ->
                    val latestData = genshininfo.maxByOrNull { it?.updateTime ?: LocalDateTime.MIN }
                    return latestData?.sToken.toString()
                }

            }
        }
        return "error"
    }

    override fun getUidByQqId(qqId: String): String {
        val queryWrapper = QueryWrapper<GenshinInfo>().eq("qqId", qqId).orderByDesc("updatetime")
        val genshininfo = genshininfoMapper.selectList(queryWrapper)
        return if (genshininfo.size == 0) {
            "无记录"
        } else {
            genshininfo[0]?.uid.toString()
        }
    }
}