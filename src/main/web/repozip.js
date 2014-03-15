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
    $('#projectURL').attr('disabled','disabled');
    $('#downloadButton').attr('disabled','disabled');
    var url = $('#projectURL').val();
    $.ajax({
      url: 'newjob',
      data: {
        url: url
      },
      success: function(x) {
        updateStatus(x.id);
      }
    })
  }
  function updateStatus(id) {
    $.ajax({
      url: 'jobstatus/' + id,
      success: function(x) {
        if(x.status === 'done') {
          setDownloadLink(x.downloadURL, 'Download Now');
          $('#ripperStatus').text('done')
          $('#ripperPercent').text(100)
          $('#ripperTime').text('')
          $('#ripperFile').text('')
        } else {
          $('#ripperJobId').text(id)
          $('#ripperStatus').text(x.status)
          $('#ripperPercent').text(x.percentComplete)
          $('#ripperTime').text(x.timeLeft)
          $('#ripperFile').text(x.currentFile)
          $('#ripperNumFiles').text(x.filesFound)
          setTimeout(_.partial(updateStatus, id), 400)
        }
      }
    })
  }
  function setDownloadLink(url, linkText) {
    $('#downloadLinkArea').html($('<a>').attr('href',url).text(linkText));
  }
  $('#downloadButton').click(function(){
    startDownload();
  });
  $('#projectURL').keyup(function(e){
    if(e.keyCode === 13) {
      startDownload();
    }
  });
  $('#projectURL')[0].focus();
  window.log = log;
  window.logEraseLine = logEraseLine;
  window.setDownloadLink = setDownloadLink;
});
