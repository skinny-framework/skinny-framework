/**
 * ------
 * Skinny framework JS library which just provides the default way. 
 * If you don't need them, it's not necessary to use this.
 * ------
 * (The MIT License)  Copyright (c) 2013 skinny-framework.org
 */
(function($, undefined) {
  function toResourcesUrl(url) {
    var parts = url.split("/");
    parts.pop();
    return parts.join("/");
  }
  function hasQuestion(url) {
    return url.indexOf("?") != -1
  }
  function withCsrfToken(url, token) {
    return hasQuestion(url) ? url + '&csrf-token=' + token : url + '?csrf-token=' + token;
  }
  $(document).delegate('a[data-method], a[data-confirm]', 'click', function(event) {
    if (window.confirm($(event.target).data('confirm'))) {
      var csrfToken = $('meta[name=csrf-token]').attr("content") || $(event.target).data('csrf-token');
      var url = event.target.href;
      var method = $(event.target).data('method');
      $.ajax({
        url: withCsrfToken(url, csrfToken),
        method: method,
        success: function(data, status, xhr) {
          if (method === 'delete') location.href = toResourcesUrl(url);
          else window.location.reload();
        },
        error: function(xhr, status, error) {
          window.alert("Please try again later.");
        }
      });
    }
    return false;
  });
  $(document).delegate('select[data-method]', 'blur', function(event) {
    var csrfToken = $('meta[name=csrf-token]').attr("content") || $(event.target).data('csrf-token');
    var url = $(event.target.selectedOptions).first().data('url');
    var method = $(event.target).data('method');
    $.ajax({
      url: url,
      data: {'csrf-token': csrfToken},
      method: method,
      success: function(data, status, xhr) {
        if (method === 'delete') location.href = toResourcesUrl(url);
        else window.location.reload();
      },
      error: function(xhr, status, error) {
        window.alert("Please try again later.");
      }
    });
    return false;
  });
})(jQuery);

