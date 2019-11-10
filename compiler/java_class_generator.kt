import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.File
import java.io.FileOutputStream

val N = 5000

fun main(args: Array<String>) {
  val javaSourcesDir = File("javasources")
  javaSourcesDir.mkdir()

  val javacArgFile = File(javaSourcesDir, "sources")
  javacArgFile.delete()

  val outputLog = File(javaSourcesDir, "javaout")
  outputLog.delete()

  for(i in 1..N) {
    FileOutputStream(javacArgFile, true).bufferedWriter().use { sources ->
      val javafilename = "JavaTemp$i.java"
      val javaclassfile = File(javaSourcesDir, javafilename)
      javaclassfile.printWriter().use { out ->
        out.println("class JavaTemp$i {}")
      }
      sources.write("$javaclassfile\n")
    }

    val process = ProcessBuilder()
        .command("/usr/bin/time", "/usr/bin/javac", "@javasources/sources")
        .start()

    val timeOutput = process.errorStream.bufferedReader().use(BufferedReader::readLine)
    val tokens = timeOutput.split("\\s+".toRegex())
    FileOutputStream(outputLog, true).bufferedWriter().use { log ->
      log.write("$i\t${tokens[1]}\t${tokens[3]}\t${tokens[5]}\n")
    }
  }
}