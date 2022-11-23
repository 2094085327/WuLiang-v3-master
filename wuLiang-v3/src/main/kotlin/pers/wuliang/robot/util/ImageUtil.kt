package pers.wuliang.robot.util

import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import javax.swing.ImageIcon

/**
 *@Description:
 *@Author zeng
 *@Date 2022/11/22 20:13
 *@User 86188
 */
class ImageUtil {
    fun scaleImage(bgPath: String, nextPath: String, width: Int, height: Int) {
        try {
            val bgImage = ImageIcon(File(bgPath).absolutePath)
            val bi = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
            val g2d = bi.createGraphics()

            // 设置图片品质
            g2d.addRenderingHints(
                RenderingHints(
                    RenderingHints.KEY_RENDERING,
                    RenderingHints.VALUE_RENDER_QUALITY
                )
            )
            g2d.drawImage(bgImage.image, 0, 0, width, height, null)
            ImageIO.write(bi, "png",     File(nextPath).absoluteFile)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}