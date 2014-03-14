package com.carematics.util


import java.util.Collection
import java.util.Random

import groovy.json.JsonOutput
import groovy.time.TimeCategory

class Util {
  static Random random = new Random()
  static String humanReadableByteCount(def bytes, boolean si) {
    int unit = si ? 1000 : 1024
    if (bytes < unit) return bytes + " B"
    int exp = (int) (Math.log(bytes) / Math.log(unit))
    String pre = ((si ? "kMGTPE" : "KMGTPE").charAt(exp-1) as String) + (si ? "" : "i")
    return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre)
  }
  static String humanReadableMilliseconds(long interval) {
    TimeCategory.minus( new Date(interval), new Date(0))
  }
  static prettyPrint(forJsonEncoding) {
    JsonOutput.prettyPrint(JsonOutput.toJson(forJsonEncoding))
  }
}
