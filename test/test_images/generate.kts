#!/home/billf/bin/kotlinc -script
//
// A quick script to generate some images we can put in an image gallery for testing.
// I just ran this once and saved the result to the repository -- no need to ever run
// it again, really.  
//
// For the first four images, I took a picture of the result with my cellphone camera at different
// orientations, so that EXIF data is tested.

import java.awt.image.BufferedImage
import java.awt.Color
import java.awt.Font
import javax.imageio.ImageIO
import java.util.Random
import java.io.File

val r=Random()
// for (i in listOf(1, 18, 19, 27)) {
for (i in 1..28) {
    val im = BufferedImage(400 + r.nextInt(1600), 400 + r.nextInt(1600), BufferedImage.TYPE_INT_RGB)
    val g = im.getGraphics()
    g.setFont(Font("Courier", Font.BOLD, im.getHeight() / 2))
    g.setColor(Color(r.nextInt(255), r.nextInt(255), r.nextInt(255)))
    g.fillRect(0, 0, 4000, 4000)
    g.setColor(Color(r.nextInt(255), r.nextInt(255), r.nextInt(255)))
    g.drawString(i.toString() ,im.getWidth() / 3, im.getHeight() * 3 / 4)
    val out = File("image_$i.jpg")
    println("Writing $out")

    ImageIO.write(im, "JPEG", out)
}
