package fi.reaktor.training.video

import java.lang.ProcessBuilder
import scala.collection.JavaConversions._
import java.io.{ByteArrayOutputStream, File}
import io.Source
import net.lag.logging.Logger

class Transcoder (executable: String, params: Iterable[String]) {
  private val log = Logger.get

  def transcode(inputFile: String, outputFile: String, callback: => Unit) = {
    val allParams = List(executable) ++ List("-y","-i", inputFile) ++ params ++ List(outputFile)
    val process = new ProcessBuilder(allParams)
    new Thread(new Runnable {
      def run(){
        log.info("Starting to transcode file:" + inputFile + " to: " + outputFile)
        val inProcess = process.start
        inProcess.waitFor
        log.info("Encoding finished:" + inputFile + " to: " + outputFile)
        handleResult(inProcess, callback)
      }
    }).start
  }

  private def handleResult(inProcess: Process, callback: => Unit): Unit = {
    require(inProcess.exitValue == 0, "Encoding Failed:" + Source.fromInputStream(inProcess.getErrorStream).toString)
    log.info("Encoding finished:")
    callback
  }
}