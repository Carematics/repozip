package com.carematics.util


import java.util.Collection
import java.util.Random

import groovy.json.JsonOutput
import groovy.time.TimeCategory

/**
 * An API similar to http://underscorejs.org/
 */
class _ {
  static Random random = new Random()
  /**
   * @return num random values from collection.
   */
  static Collection sample(Collection collection, int num) {
    if(collection.isEmpty() || num == 0 ) {
      return []
    }
    Collection toPluck = []
    while(toPluck.size() < num) {
      toPluck.addAll(collection)
    }
    Collection toReturn = []
    (1..num).each {
      def another = toPluck[random.nextInt(toPluck.size())]
      toPluck.remove(another)
      toReturn.add(another)
    }
    return toReturn
  }
  /**
   * @return a random integer between min and max (inclusive).
   */
  static int random(int min, int max) {
    return min + random.nextInt(max - min + 1)
  }
  /**
   * @return a random integer between 0 and max (inclusive).
   */
  static int random(int max) {
    random(0, max)
  }
}
