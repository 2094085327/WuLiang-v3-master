package pers.wuliang.robot.botApi.genShinApi.gaCha

import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import net.coobird.thumbnailator.Thumbnails
import java.awt.*
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream
import java.net.URL
import javax.imageio.ImageIO
import kotlin.math.ceil


/**
 *@Description: 原神抽卡分析的图片处理类
 *@Author zeng
 *@Date 2022/12/16 11:26
 *@User 86188
 */
class PictureMake {

    private val imageCache = mutableMapOf<String, BufferedImage>()

    private fun judgeImg(imagePath: String): BufferedImage {
        val img = File(GachaConfig.localPath + imagePath).absoluteFile
        if (img.exists()) {
            return ImageIO.read(img)
        }

        if (imageCache.containsKey(imagePath)) {
            return imageCache[imagePath]!!
        }

        val bi: BufferedImage = try {
            ImageIO.read(URL(GachaConfig.galleryPath + imagePath.urlCode))
        } catch (e: Exception) {
            println("图片获取失败：$imagePath")
            ImageIO.read(URL(GachaConfig.galleryPath + "其他图片/default1.png".urlCode))
        }
        val parent = img.parentFile
        if (!parent.exists()) {
            parent.mkdirs()
        }
        ImageIO.write(bi, "png", img)
        imageCache[imagePath] = bi

        return bi
    }

    /**
     * 成比例缩放
     * @param imagePath 图片路径
     * @param newHeight 图片新高度
     * @param newWidth 图片新宽度
     */
    fun resizeProImage(imagePath: String, newWidth: Int, newHeight: Int): BufferedImage? {
        val imageUrl = URL(GachaConfig.galleryPath + imagePath.urlCode)
        val image = ImageIO.read(imageUrl)
        if (image != null) {
            // 确定新尺寸的比例因子
            val widthScaleFactor: Double = newWidth.toDouble() / image.width
            val heightScaleFactor: Double = newHeight.toDouble() / image.height
            val scaleFactor = widthScaleFactor.coerceAtMost(heightScaleFactor)

            // 计算图像的新大小
            val scaledWidth = (scaleFactor * image.width).toInt()
            val scaledHeight = (scaleFactor * image.height).toInt()

            // 使用新大小创建新的空BufferedImage对象
            val resizedImage = BufferedImage(scaledWidth, scaledHeight, image.type)

            // 缩放原始图像并将其绘制到调整大小的图像上
            val scaledImage: Image = image.getScaledInstance(scaledWidth, scaledHeight, Image.SCALE_SMOOTH)
            resizedImage.graphics.drawImage(scaledImage, 0, 0, null)

            return resizedImage
        } else {
            println("图片获取失败，可能原因为图库中不存在此图片或未配置图库路径")
            return null
        }
    }

    /**
     * 指定长宽缩放
     * @param images 图片缓存流
     * @param newHeight 图片新高度
     * @param newWidth 图片新宽度
     */
    private fun resizeImage(images: BufferedImage, newWidth: Int, newHeight: Int): BufferedImage {
        val resizedImage = BufferedImage(newWidth, newHeight, images.type)
        val gd: Graphics2D = resizedImage.createGraphics()

        // 设置图片品质
        gd.addRenderingHints(RenderingHints(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY))
        gd.drawImage(images, 0, 0, newWidth, newHeight, null)
        gd.dispose()
        // 返回调整大小的图像
        return resizedImage

    }

    /**
     * 根据概率判断运气等级
     * @param probability 概率
     */
    private fun choseLuck(probability: Double? = GachaData.probability): BufferedImage {
        val luck: String = when {
            probability!! > 80 -> "欧"
            probability > 60 -> "吉"
            probability > 40 -> "平"
            probability > 20 -> "惨"
            else -> "寄"
        }
        return judgeImg("运气图片/${luck}.png")
    }

    /**
     *返回每个池子的数据
     * @param gachaType 池子类型
     */
    fun poolImage(gachaType: String): BufferedImage {
        val white = judgeImg("其他图片/withe.png")
        val string2 = judgeImg("其他图片/${gachaType}文字.png")
        val countWhite = judgeImg("其他图片/计数块.png")
        val luckImage = choseLuck()

        val lines = ceil((GachaMain.dataArray.size / 7.0))
        val newWhite = resizeImage(white, white.width, (white.height + 506 * lines + 60 * lines).toInt())
        // 创建绘画对象
        val gd: Graphics2D = newWhite.createGraphics()
        gd.drawImage(luckImage, 58, 58, luckImage.width, luckImage.height, null)
        gd.drawImage(string2, 1112, 100, string2.width, string2.height, null)

        var roleX = 58
        var n = 1
        var counts = 0
        var lineX = 1

        runBlocking {
            // 并行下载图像
            val images = GachaMain.dataArray.map {
                async { judgeImg("${gachaType}图片/${it.key}.png") }
            }

            images.forEachIndexed { index, deferredImage ->
                // 获取图像
                val roleImage = deferredImage.await()
                gd.drawImage(
                    roleImage,
                    roleX + roleImage.width * (lineX - 1),
                    luckImage.height + 106 + (roleImage.height + 50) * (n - 1),
                    roleImage.width,
                    roleImage.height,
                    null
                )

                gd.drawImage(
                    countWhite,
                    roleX + roleImage.width * (lineX - 1),
                    luckImage.height + 482 + (roleImage.height + 50) * (n - 1),
                    countWhite.width,
                    countWhite.height,
                    null
                )

                // 设置画笔颜色为黑色，画笔字体样式为微软雅黑，斜体，文字大小为20px
                gd.color = Color(89, 87, 87)
                gd.font = Font("汉仪青云简", Font.ITALIC, 100)

                gd.drawString(
                    GachaMain.dataArray[index].value.toString(),
                    roleX + 147 + roleImage.width * (lineX - 1),
                    luckImage.height + 575 + (roleImage.height + 50) * (n - 1)
                )
                roleX += 52
                counts++
                lineX++
                if (counts.toFloat() / 7 == 1f) {
                    n++
                    counts = 0
                    lineX = 1
                    roleX = 58
                }
            }
        }
        gd.color = Color.BLACK
        gd.font = Font("微软雅黑", Font.ITALIC, 206)
        gd.drawString(GachaData.aveFive, 1218, 770)
        gd.drawString(GachaData.count.toString(), 1910, 770)
        gd.drawString("${GachaData.right}/${GachaMain.dataArray.size}", 2575, 770)

        gd.color = Color(255, 192, 0)
        gd.font = Font("微软雅黑", Font.ITALIC, 101)
        gd.drawString(GachaData.haveCost.toString(), 1336, 412)
        gd.dispose()
        GachaData.count = 0
        GachaData.haveCost = 0
        GachaData.right = 0

        return newWhite
    }

    /**
     * 生成最终统计数据的图片
     */
    fun allDataMake(): BufferedImage {
        val white = judgeImg("其他图片/withe.png")
        val allData = judgeImg("其他图片/总数据.png")
        val luckImage = choseLuck()

        val gd = white.createGraphics()
        gd.drawImage(luckImage, 58, 58, luckImage.width, luckImage.height, null)
        gd.drawImage(allData, 1112, 100, allData.width, allData.height, null)

        gd.color = Color.BLACK
        gd.font = Font("微软雅黑", Font.ITALIC, 206)
        gd.drawString(String.format("%.1f", GachaData.finCount / GachaData.finItem.toFloat()), 1190, 770)
        gd.drawString(GachaData.finCount.toString(), 1864, 770)
        gd.drawString(GachaData.finItem.toString(), 2591, 770)
        gd.dispose()
        return white
    }

    /**
     * 合成完整图片
     * @param totalData 总数据流
     * @param roleData 角色数据流
     * @param armsData 武器数据流
     * @param perData 常驻数据流
     */
    fun compositePicture(
        totalData: BufferedImage,
        roleData: BufferedImage,
        armsData: BufferedImage,
        perData: BufferedImage
    ): InputStream {
        val bg = judgeImg("其他图片/sec.png")
        val newBg =
            resizeImage(
                bg,
                bg.width,
                500 + totalData.height + roleData.height + armsData.height + perData.height
            )
        val gd = newBg.createGraphics()
        gd.drawImage(totalData, 100, 100, totalData.width, totalData.height, null)
        gd.drawImage(roleData, 100, 1242, roleData.width, roleData.height, null)
        gd.drawImage(armsData, 100, 1342 + roleData.height, armsData.width, armsData.height, null)
        gd.drawImage(
            perData,
            100,
            1442 + roleData.height + armsData.height,
            perData.width,
            perData.height,
            null
        )
        gd.dispose()

        // 转换成InputStream输出
        val byStream = ByteArrayOutputStream()
        ImageIO.write(Thumbnails.of(newBg).scale(0.25).asBufferedImage(), "png", byStream)

        return ByteArrayInputStream(byStream.toByteArray())
    }
}