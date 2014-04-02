$(function() {
  var pagePosition = $(window).scrollTop() + $(window).height();

  if (pagePosition == $(document).height()) {
      $('.info-text').removeClass('hidden');
    }

  $(window).scroll(function(e) {
    pagePosition = $(window).scrollTop() + $(window).height();

    if (pagePosition == $(document).height()) {
      $('.info-text').removeClass('hidden');
      $('.info-text').removeClass('fadeOutDown');
      $('.info-text').addClass('fadeInUp');
    } else if (pagePosition < $(document).height()) {
      if (!$('.info-text').hasClass('hidden')) {
        $('.info-text').removeClass('fadeInUp');
        $('.info-text').addClass('fadeOutDown');
        $('.info-text').addClass('hidden');
      }
    }
  });
});
