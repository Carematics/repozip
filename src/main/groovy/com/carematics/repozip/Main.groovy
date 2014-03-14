package com.carematics.repozip
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import com.carematics.util.Util
class Main {
  static pprintln(x) {
    println Util.prettyPrint(x)
  }
  static Document getDoc(url) {
    Jsoup.connect(url).get()
  }
  static main(args) {
    getProject('http://google-voice-java.googlecode.com/svn/trunk/')
  }
  static getProject(url) {
    downloadProject(url)
    println 'downloaded, zipping...'
    def saveDir = new File(url2saveDir(url))
    def zipFile = new File('out/'+url2ProjectName(url)+'.zip')
    zipDir(saveDir, zipFile)
    saveDir.deleteDir()
    println 'done'
  }
  static downloadProject(projectURL) {
    println 'finding files'
    def links = []
    def getLinksFromURL
    getLinksFromURL = { url ->
      getDoc(url).select('li').select('a').collect{it.attr('href')}.each { String href ->
        if(!href.startsWith('..')) {
          def thisFilePath = url + href
          if(href.endsWith('/')) {
            getLinksFromURL(thisFilePath)
          } else {
            println "found file: $thisFilePath"
            links += thisFilePath
          }
        }
      }
      println "files found: " + links.size()
      return links
    }
    getLinksFromURL(projectURL)
    println "downloading ${links.size()} files"
    def numFiles = links.size()
    def numDone = 0
    def timeStart = System.currentTimeMillis()
    links.each {
      downloadFile(projectURL, it)
      numDone++
      def doneFraction = 1.0 * numDone / numFiles
      def timeLeft = (1.0 - doneFraction) * (System.currentTimeMillis() - timeStart) / doneFraction
      println 'downloading file: ' + it
      println 'percent complete: ' + ((int)Math.floor(100*doneFraction))
      println 'time left: ' + Util.humanReadableMilliseconds((int)Math.ceil(timeLeft))
    }
  }
  static url2ProjectName(url) {
    def doubleSlash = url.indexOf('//')
    def dotAfterDoubleSlash = url.indexOf('.', doubleSlash)-1
    def projectName = url[doubleSlash+2..dotAfterDoubleSlash]
    return projectName
  }
  static url2saveDir(url) {
    return 'out/' + url2ProjectName(url)
  }
  static downloadFile(String origURL, String url) {
    File dest = new File(url2saveDir(url), url[origURL.size()..-1])
    dest.getParentFile().mkdirs()
    dest << new URL(url).openConnection().getContent()
  }
  static zipDir(File dir, File outputFile) {
    ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(outputFile))
    zipRecurse(dir, dir.getName(), zos)
    zos.close()
  }
  static zipRecurse(File dir, String path, ZipOutputStream zos) {
    dir.eachFile() { file ->
      if(file.isFile()) {
        ZipEntry entry = new ZipEntry(path + '/' + file.getName())
        entry.time = file.lastModified()
        zos.putNextEntry(entry)
        if( file.isFile() ){
          def fis = new FileInputStream(file)
          zos << fis
          fis.close()
        }
      } else if (file.isDirectory()) {
        zipRecurse(file, path + '/' + file.getName(), zos)
      }
    }
  }
}
