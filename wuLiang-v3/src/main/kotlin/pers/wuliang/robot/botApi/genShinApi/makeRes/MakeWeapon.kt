package pers.wuliang.robot.botApi.genShinApi.makeRes

import com.jhlabs.image.GaussianFilter
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import pers.wuliang.robot.botApi.genShinApi.gaCha.GachaConfig
import pers.wuliang.robot.botApi.genShinApi.gaCha.GachaData
import pers.wuliang.robot.botApi.genShinApi.gaCha.PictureMake
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.geom.RoundRectangle2D
import java.awt.image.BufferedImage
import java.awt.image.ConvolveOp
import java.awt.image.Kernel
import java.io.File
import java.net.URL
import javax.imageio.ImageIO

/**
 *@Description:
 *@Author zeng
 *@Date 2023/1/29 15:23
 *@User 86188
 */
class MakeWeapon {
    private fun getImageUrl(imageName: String): String {
        val url = "https://wiki.biligame.com/ys/%E6%AD%A6%E5%99%A8%E5%9B%BE%E9%89%B4"
        val doc: Document = Jsoup.connect(url).get()
        val img = doc.select("img[alt=${imageName}.png]")
        println(img.attr("src"))
        return img.attr("src")
    }

    /**
     * 成比例缩放
     * @param newHeight 图片新高度
     * @param newWidth 图片新宽度
     */
    @Suppress("unused")
    fun resizeProImage(image: BufferedImage, newWidth: Int, newHeight: Int): BufferedImage {

        val widthFactor: Double = newWidth.toDouble() / image.width
        val heightFactor: Double = newHeight.toDouble() / image.height
        val scaleFactor = widthFactor.coerceAtMost(heightFactor)
        val scaledWidth = (scaleFactor * image.width).toInt()
        val scaledHeight = (scaleFactor * image.height).toInt()
        val resizedImage = BufferedImage(scaledWidth, scaledHeight, image.type)
        val g2d = resizedImage.createGraphics()

        // 启用双线性插值算法
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC)

        // 缩放并绘制原始图像到调整大小的图像上
        g2d.drawImage(image, 0, 0, scaledWidth, scaledHeight, null)
        g2d.dispose()

        // 高斯模糊
        val kernel = GaussianFilter(3f)
        val filteredImage = kernel.filter(resizedImage, null)
        // 卷积核锐化
        val sharpen = Kernel(3, 3, floatArrayOf(-1f, -1f, -1f, -1f, 9f, -1f, -1f, -1f, -1f))
        val op = ConvolveOp(sharpen)
        return op.filter(filteredImage, null)
    }


    private fun getRoundedImage(image: BufferedImage): BufferedImage {
        val roundedImage = BufferedImage(image.width, image.height, BufferedImage.TYPE_INT_ARGB)
        val gd = roundedImage.createGraphics()
        gd.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)

        gd.clip(RoundRectangle2D.Float(0f, 0f, image.width.toFloat(), image.height.toFloat(), 50f, 50f))
        gd.drawImage(image, 0, 0, null)
        gd.dispose()
        return roundedImage
    }

    fun makeImg(itemName: String) {
        val imgUrl = getImageUrl(itemName)
        val r5Array = GachaData().getPermanentData()
        var lastName = itemName
        if (r5Array.contains(itemName)) {
            lastName = "$itemName(歪)"
        }
        val weaponImg = resizeProImage(ImageIO.read(URL(imgUrl)), 335, 335)

        val weaponBg = PictureMake().judgeImg("其他图片/weaponBg.png")
        val weaponImgRound = getRoundedImage(weaponImg)

        val gd: Graphics2D = weaponBg.createGraphics()

        gd.drawImage(weaponImgRound, 22, 24, null)
        gd.dispose()
        ImageIO.write(
            weaponBg,
            "png",
            File(GachaConfig.localPath + "武器图片/${lastName}.png")
        )
    }
}