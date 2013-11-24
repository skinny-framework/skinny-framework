/**
 * ------
 * Skinny framework JS library which just provides the default way. 
 * If you don't need them, it's not necessary to use this.
 * ------
 * (The MIT License)  Copyright (c) 2013 skinny-framework.org
 */
(function($, undefined) {
  $(document).delegate('a[data-method], a[data-confirm]', 'click', function(event) {
    if (window.confirm($(event.target).data('confirm'))) {
      var csrfToken = $('meta[name=csrf-token]').attr("content") || $(event.target).data('csrf-token');
      $.ajax({
        url: event.target.href + '?csrf-token=' + csrfToken,
        method: $(event.target).data('method'),
        success: function(data) { window.location.reload(); }
      });
    }
    return false;
  });
  $(document).delegate('select[data-method]', 'blur', function(event) {
    var csrfToken = $('meta[name=csrf-token]').attr("content") || $(event.target).data('csrf-token');
    $.ajax({
      url: $(event.target.selectedOptions).first().data('url'),
      data: {'csrf-token': csrfToken},
      method: $(event.target).data('method'),
      success: function(data) { window.location.reload(); }
    });
    return false;
  });
})(jQuery);

