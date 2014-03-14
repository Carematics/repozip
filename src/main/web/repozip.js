/*global window, _, $, WizardEngine, template, Pager */
/*jshint unused:true, undef: true, eqnull:true */
(function() {
  'use strict';
  function RepoZip() {
    // should only run as a constructor
    if (!(this instanceof RepoZip)) {
      return new RepoZip();
    }
  }
})();
$(function() {
  var $log = $('#log');
  function logEraseLine() {
    var html = $log.html().trim();
    var lastIndex = html.lastIndexOf('<br>');
    var trimmed = html.substring(0, lastIndex);
    $log.html(trimmed);
  }
  function log(s) {
    s = s.replace(/\n/g,'<br>')
    if($log.text().trim().length) {
      s = '<br/>' + s;
    }
    $log.append(s);
  }
  function startDownload() {
    $('#gooProjectName').attr('disabled','disabled');
    $('#downloadButton').attr('disabled','disabled');
    log('Scanning...');
  }
  function setDownloadLink(url, linkText) {
    $('#downloadLinkArea').html($('<a>').attr('href',url).text(linkText));
  }
  $('#downloadButton').click(function(){
    startDownload();
  });
  $('#gooProjectName').keyup(function(e){
    if(e.keyCode === 13) {
      startDownload();
    }
  });
  window.log = log;
  window.logEraseLine = logEraseLine;
  window.setDownloadLink = setDownloadLink;
});
