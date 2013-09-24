(function($, undefined) {
  $(document).delegate('a[data-method], a[data-confirm]', 'click', function(event) {
    if (window.confirm($(event.target).data('confirm'))) {
      var csrfToken = $('meta[name=csrf-token]').attr("content") || $(event.target).data('csrf-token');
      $.ajax({
        url: event.target.href + "?csrfToken=" + csrfToken,
        method: $(event.target).data('method'),
        success: function(data) { window.location.reload(); }
      });
    }
    return false;
  });
})(jQuery);


