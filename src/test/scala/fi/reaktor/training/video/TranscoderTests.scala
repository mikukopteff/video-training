package fi.reaktor.training.video

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers


class TranscoderTests extends FunSuite with ShouldMatchers {

  test("transcoding starts and ends a process"){
    val transcoder = new Transcoder("/usr/local/bin/ffmpeg", List("-s", "hd720" ,"-vcodec", "libx264",
      "-preset", "medium", "-vb", "680000", "-acodec", "aac", "-ac", "2", "-strict", "experimental", "-deinterlace" ,"-ss", "0", "-t", "5"))
    val result = transcoder.transcode("lyhyt.mp4", "testifile.mp4", {println("callback invoked, yey!")})
  }

  test("transcoder converts to an image"){
    val transcoder = new Transcoder("/usr/local/bin/ffmpeg", List("-s", "hd720" ,"-vcodec", "libx264",
      "-preset", "medium", "-vb", "680000", "-acodec", "aac", "-ac", "2", "-strict", "experimental", "-deinterlace" ,"-ss", "0", "-t", "5"))
    val result = transcoder.transcode("lyhyt.mp4", "testifile.mp4", {println("callback invoked, yey!")})
  }
}