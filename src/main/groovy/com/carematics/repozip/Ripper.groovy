package com.carematics.repozip
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import com.carematics.util.Util
import com.google.common.io.Files
class Ripper {
  String id
  String projectURL
  String downloadURL = null
  String filesFound = 0
  String currentFile = ''
  String timeLeft = ''
  int percentComplete = 0
  String status = ''
  Ripper(id, url) {
    this.projectURL = url
    this.id = id
  }
  void start() {
    status = 'starting job'
    downloadProject()
    status = 'downloaded, zipping...'
    File saveDir = new File(getSaveDir())
    File zipFile = new File("out/${id}.zip")
    zipDir(saveDir, zipFile)
    saveDir.deleteDir()
    downloadURL = "jobs/${id}.zip"
    File fileForWeb = new File("src/main/web/" + downloadURL)
    fileForWeb.getParentFile().mkdirs()
    Files.copy(zipFile, fileForWeb) 
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
  String getProjectName() {
    def doubleSlash = projectURL.indexOf('//')
    def dotAfterDoubleSlash = projectURL.indexOf('.', doubleSlash)-1
    def projectName = projectURL[doubleSlash+2..dotAfterDoubleSlash]
    return projectName
  }
  String getSaveDir() {
    return 'out/' + getProjectName()
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
