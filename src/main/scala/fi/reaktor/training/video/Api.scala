package fi.reaktor.training.video

import org.scalatra.ScalatraServlet
import org.scalatra.fileupload.FileUploadSupport
import collection.immutable.HashSet
import scala.collection.JavaConversions._
import java.text.SimpleDateFormat
import java.util.{Calendar, UUID}
import java.net.InetAddress
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import reflect.BeanProperty
import net.lag.logging.Logger
import java.io.{File}

class Api extends ScalatraServlet with FileUploadSupport {
  val VideoFormat = "mp4"
  val VideoPublishFolder = "/usr/local/WowzaMediaServer/content/"
  val ImagePublishFolder = "/home/miku/dev/www/images/"
  val ServerIp = InetAddress.getLocalHost().getHostAddress()

  val x264Transcoder = new Transcoder("/usr/local/bin/ffmpeg", List("-s", "hd720" ,"-vcodec", "libx264",
      "-preset", "medium", "-vb", "680000", "-acodec", "aac", "-ac", "2", "-strict", "experimental", "-deinterlace", "-threads", "0"))
  val imageTranscoder = new Transcoder("/usr/local/bin/ffmpeg", List("-vframes", "1", "-ss", "4"))
  val gson = new Gson
  val dateFormat = new SimpleDateFormat("yyyy.MM.dd G 'at' HH:mm:ss z")
  var videos = List[RtmpVideo](new RtmpVideo("sample", currentDate, videoUrl("sample"), imagePublishLocation("sample")))
  private val log = Logger.get

  before {
    contentType = "application/json"
  }

  get("/streamapp"){
    gson.toJson(new Host("rtmp://"+ ServerIp +"/vod"))
  }

  get("/list"){
    val javaVideoSet:java.util.List[RtmpVideo] = videos
    gson.toJson(javaVideoSet, new TypeToken[java.util.List[RtmpVideo]]() {}.getType())
  }

  post("/file_upload") {
    contentType = "text/html"
    val file = fileParams("file")
    val uploadedFile = "/tmp/" + file.getName
    file.write(new File(uploadedFile))
    val id = UUID.randomUUID.toString
    x264Transcoder.transcode(uploadedFile, videoPublishLocation(id), storeMetadata(id))
    <html><body>File uploaded, going to encode now. This may take some minutes id:{id}<br />  <a href="../video.html">Link to Videos</a></body></html>.toString
   }

  private def storeMetadata(id: String) {
    log.info("Storing video metadata")
    imageTranscoder.transcode(videoPublishLocation(id), ImagePublishFolder + imageName(id), {log.info("Image encoding done")})
    videos ::= new RtmpVideo(id, currentDate, videoUrl(id), imagePublishLocation(id))
  }

  private def currentDate() = dateFormat.format(Calendar.getInstance.getTime)

  private def videoName(id: String) = id + "." + VideoFormat

  private def videoPublishLocation(id: String) = VideoPublishFolder + videoName(id)

  private def imagePublishLocation(id: String) = "http://" + ServerIp + "/images/" + imageName(id)

  private def imageName(id: String) = id + ".png"

  private def videoUrl(id: String) = VideoFormat + ":" + videoName(id)
}

class RtmpVideo(@BeanProperty val id: String, @BeanProperty val generationTime: String, @BeanProperty val  url: String, @BeanProperty val  imageUrl: String)

class Host(@BeanProperty val host: String)