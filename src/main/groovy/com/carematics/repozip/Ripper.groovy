package com.carematics.repozip
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import com.carematics.util.Util
class Ripper {
  static {
    Thread.start('Ripper Cleaner') {
      def fileExpirationAge = 60 * 60 * 1000
      def removeExpiredChildren = { File dir ->
        if(dir.isDirectory()) {
          def now = System.currentTimeMillis()
          dir.eachFile { file ->
            if(now - file.lastModified() > fileExpirationAge) {
              if(file.isDirectory()) {
                file.deleteDir()
              } else {
                file.delete()
              }
            }
          }
        }
      }
      File hostedJobs = new File('src/main/web/jobs')
      File outDir = new File('out')
      while(true) {
        removeExpiredChildren(hostedJobs)
        removeExpiredChildren(outDir)
        Thread.sleep(fileExpirationAge)
      }
    }
  }
  String id = null
  String projectURL = null
  String projectName = null
  String downloadURL = null
  String filesFound = 0
  String currentFile = null
  String timeLeft = null
  int percentComplete = 0
  String status = null
  Ripper(id, url, projectName) {
    this.projectURL = url
    this.projectName = projectName
    this.id = id
  }
  void start() {
    status = 'starting job'
    downloadProject()
    status = 'downloaded, zipping...'
    File saveDir = new File(getSaveDir())
    downloadURL = "jobs/$id/${projectName}.zip"
    File zipFile = new File("src/main/web/", downloadURL)
    zipFile.getParentFile().mkdirs()
    zipDir(saveDir, zipFile)
    saveDir.deleteDir()
    status = 'done'
  }
  void downloadProject() {
    status = 'finding files'
    def links = []
    def getLinksFromURL
    getLinksFromURL = { url ->
      getDoc(url).select('li').select('a').collect{it.attr('href')}.each { String href ->
        if(!href.startsWith('..')) {
          def thisFilePath = url + href
          if(href.endsWith('/')) {
            getLinksFromURL(thisFilePath)
          } else {
            currentFile = thisFilePath
            links += thisFilePath
          }
        }
      }
      filesFound = links.size()
      return links
    }
    getLinksFromURL(projectURL)
    status = "downloading ${links.size()} files"
    def numFiles = links.size()
    def numDone = 0
    def timeStart = System.currentTimeMillis()
    links.each {
      downloadFile(it)
      numDone++
      def doneFraction = 1.0 * numDone / numFiles
      def timeLeftMS = (1.0 - doneFraction) * (System.currentTimeMillis() - timeStart) / doneFraction
      this.currentFile = it
      this.percentComplete = ((int)Math.floor(100*doneFraction))
      this.timeLeft = Util.humanReadableMilliseconds((int)Math.ceil(timeLeftMS))
    }
  }
  String getSaveDir() {
    return "out/$id/$projectName"
  }
  void downloadFile(String url) {
    File dest = new File(getSaveDir(), url[projectURL.size()..-1])
    dest.getParentFile().mkdirs()
    dest << new URL(url).openConnection().getContent()
  }
  static Document getDoc(url) {
    Jsoup.connect(url).get()
  }
  static zipDir(File dir, File outputFile) {
    ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(outputFile))
    zipRecurse(dir, dir.getName(), zos)
    zos.close()
  }
  static zipRecurse(File dir, String path, ZipOutputStream zos) {
    dir.eachFile() { file ->
      def entryPath = path + '/' + file.getName()
      def entry = new ZipEntry(entryPath)
      entry.time = file.lastModified()
      zos.putNextEntry(entry)
      if(file.isFile()) {
        def fis = new FileInputStream(file)
        zos << fis
        fis.close()
      } else if (file.isDirectory()) {
        zipRecurse(file, entryPath, zos)
      }
    }
  }
}
