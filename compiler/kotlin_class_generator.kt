import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.File
import java.io.FileOutputStream

val N = 5000

fun main(args: Array<String>) {
  val kotlinSourcesDir = File("ktsources")
  kotlinSourcesDir.mkdir()

  val kotlincArgFile = File(kotlinSourcesDir, "sources")
  kotlincArgFile.delete()

  val outputLog = File(kotlinSourcesDir, "kotlinout")
  outputLog.delete()

  for(i in 1..N) {
    FileOutputStream(kotlincArgFile, true).bufferedWriter().use { sources ->
      val ktfilename = "KotlinTemp$i.kt"
      val ktclassfile = File(kotlinSourcesDir, ktfilename)
      ktclassfile.printWriter().use { out ->
        out.println("class KotlinTemp$i")
      }
      sources.write("$ktclassfile\n")
    }

    val process = ProcessBuilder()
        .command("/usr/bin/time", "/usr/local/bin/kotlinc", "@ktsources/sources", "-d", "ktsources")
        .start()

    val timeOutput = process.errorStream.bufferedReader().use(BufferedReader::readLine)
    val tokens = timeOutput.split("\\s+".toRegex())
    FileOutputStream(outputLog, true).bufferedWriter().use { log ->
      log.write("$i\t${tokens[1]}\t${tokens[3]}\t${tokens[5]}\n")
    }
  }
}