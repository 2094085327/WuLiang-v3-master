@file:Suppress("MemberVisibilityCanBePrivate")

package pers.wuliang.robot.core.common

import love.forte.simbot.bot.Bot
import love.forte.simbot.bot.OriginBotManager
import org.springframework.context.ApplicationContext
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import java.io.File
import java.util.*
import java.util.concurrent.ThreadLocalRandom
import javax.annotation.PostConstruct

/**
 * @author wuyou
 */
@Suppress("unused")
@Order(1)
@Component
class RobotCore( private var applicationContext: ApplicationContext) {

    @PostConstruct
    fun init() {
//        setApplicationContext()
//        initGroupBootMap()
    }

//    @Synchronized
//    private fun setApplicationContext() {
//        robotCore = this
//        Companion.applicationContext = applicationContext
//    }

//    private fun initGroupBootMap() {
//        val list = database.sequenceOf(GroupBootStates)
//        list.forEach {
//            BOOT_MAP[it.groupCode] = it.state
//        }
//
//    }

    companion object {
        lateinit var applicationContext: ApplicationContext

        /**
         * 项目名
         */
        const val PROJECT_NAME: String = "wuLiang-v3"

        /**
         * 项目路径
         */
        val PROJECT_PATH: String = System.getProperty("user.dir") + File.separator

        /**
         * 临时路径
         */
        val TEMP_PATH: String = System.getProperty("java.io.tmpdir") + File.separator + PROJECT_NAME + File.separator

        /**
         * python路径
         */
//        var PYTHON_PATH: String? = null

        /**
         * 机器人管理员
         */
        val ADMINISTRATOR: List<String> = listOf("")

        /**
         * 缓存群开关
         */
        val BOOT_MAP: MutableMap<String?, Boolean> = HashMap()

//        /**
//         * 全局随机数
//         */
//        val RANDOM: Random = ThreadLocalRandom.current()
//
//        var robotCore: RobotCore? = null
//
//        init {
//            val pythonEnvPath = "venv"
//            PYTHON_PATH = if (File(PROJECT_PATH + pythonEnvPath).exists()) {
//                PROJECT_PATH + "venv" + File.separator + "Scripts" + File.separator + "python"
//            } else {
//                null
//            }
//        }

        fun isBotAdministrator(accountCode: String): Boolean {
            return ADMINISTRATOR.contains(accountCode)
        }

        @Suppress("OPT_IN_USAGE")
        fun getBot(): Bot {
            return OriginBotManager.getAnyBot()
        }
    }
}

fun <T> getBean(requiredType: Class<T>): T = RobotCore.applicationContext.getBean(requiredType)

inline fun <T> T.isNull(block: () -> Unit): T {
    if (this == null) block()
    return this
}

inline fun Boolean.then(block: () -> Unit) = this.also { if (this) block() }

inline operator fun Boolean.invoke(block: () -> Unit) = this.then(block)

inline fun Boolean?.onElse(block: () -> Unit): Boolean = this.let {
    it?.not()?.then(block).isNull { block() }
    it ?: false
}

inline operator fun Boolean?.minus(block: () -> Unit): Boolean = this.onElse(block)

fun stringMutableList(vararg elements: String): MutableList<String> = mutableListOf(*elements)
@Suppress("unused")
fun String.substring(startStr: String = "", endStr: String = ""): String {
    val start = (if (startStr.isEmpty()) 0 else this.indexOf(startStr) + startStr.length).let {
        if (it > 0) it else 0
    }
    val end = if (endStr.isEmpty()) this.length else this.indexOf(endStr).let {
        if (it > 0) it else 0
    }
    return let {
        try {
            substring(start, end)
        } catch (e: StringIndexOutOfBoundsException) {
            ""
        }
    }
}
