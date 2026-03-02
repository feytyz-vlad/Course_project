(function($) {
    'use strict';


    // Counterup
    $('.counter').counterUp({
        delay: 10,
        time: 2500
    });

	// Nice Select Js
	$('select').niceSelect();

// Slider Carousel
   $('.st__slider__carousel').owlCarousel({
    loop: true,
    autoplay: false,
    autoplayTimeout: 10000,
    dots: false,
    dotsEach: false,
    lazyLoad:true,
    center:true,
    active:true,
    mouseDrag: true,
    animateOut: 'fadeOut',
    smartSpeed: 1000,
    nav:true,
    navText: ["<i class='bi bi-chevron-left'></i>", "<i class='bi bi-chevron-right'></i>"],
    responsive: {
        0: {
            items: 1
        },
        768: {
            items: 1
        },
        992: {
            items: 1
        },
        1000: {
            items: 1
        },
        1199: {
            items: 1
        },
        1920: {
            items: 1
        }
    }
});   

$('.st__feature__carousel').owlCarousel({
    loop: true,
    autoplay: false,
    autoplayTimeout: 10000,
    dots: false,
    dotsEach: false,
    lazyLoad:true,
    margin:20,
    active:true,
    smartSpeed: 1000,
    nav:true,
    navText: ["<i class='bi bi-arrow-left'></i>", "<i class='bi bi-arrow-right'></i>"],
    responsive: {
        0: {
            items: 1
        },
        768: {
            items: 2
        },
        992: {
            items: 2
        },
        1000: {
            items: 3
        },
        1199: {
            items: 3
        },
        1920: {
            items: 4
        }
    }
});


$('.brand-carousel').owlCarousel({
    loop: true,
    autoplay: false,
    autoplayTimeout: 10000,
    dots: false,
    dotsEach: false,
    lazyLoad:true,
    margin:20,
    active:true,
    smartSpeed: 1000,
    nav:false,
    navText: ["<i class='bi bi-arrow-left'></i>", "<i class='bi bi-arrow-right'></i>"],
    responsive: {
        0: {
            items: 1
        },
        768: {
            items: 3
        },
        992: {
            items: 4
        },
        1000: {
            items: 4
        },
        1199: {
            items: 4
        },
        1920: {
            items: 5
        }
    }
});

// Venobox
$(document).ready(function(){
    $('.venobox').venobox(); 
});

// WOW Js
new WOW().init();


/*================
    Back to Top
==================*/

	if($('.prgoress_scrollup path').length){
	    var progressPath = document.querySelector('.prgoress_scrollup path');
	    var pathLength = progressPath.getTotalLength();
	    progressPath.style.transition = progressPath.style.WebkitTransition = 'none';
	    progressPath.style.strokeDasharray = pathLength + ' ' + pathLength;
	    progressPath.style.strokeDashoffset = pathLength;
	    progressPath.getBoundingClientRect();
	    progressPath.style.transition = progressPath.style.WebkitTransition = 'stroke-dashoffset 10ms linear';
	    var updateProgress = function () {
	      var scroll = $(window).scrollTop();
	      var height = $(document).height() - $(window).height();
	      var progress = pathLength - (scroll * pathLength / height);
	      progressPath.style.strokeDashoffset = progress;
	    };
	    updateProgress();
	    $(window).on('scroll', updateProgress);
	    var offset = 250;
	    var duration = 550;
	    jQuery(window).on('scroll', function () {
	      if (jQuery(this).scrollTop() > offset) {
	        jQuery('.prgoress_scrollup').addClass('active-progress');
	      } else {
	        jQuery('.prgoress_scrollup').removeClass('active-progress');
	      }
	    });
	    jQuery('.prgoress_scrollup').on('click', function (event) {
	      event.preventDefault();
	      jQuery('html, body').animate({ scrollTop: 0 }, duration);
	      return false;
	    });
	}


/*=====   Menu sticky   ======*/
	var windows = $(window);
	var screenSize = windows.width();
	var sticky = $('.header-sticky');
	var $html = $('html');
	var $body = $('body');

	windows.on('scroll', function () {
		var scroll = windows.scrollTop();
		var headerHeight = sticky.height();

		if (screenSize >= 320) {
			if (scroll < headerHeight) {
				sticky.removeClass('is-sticky');
			} else {
				sticky.addClass('is-sticky');
			}
		}

	});
 /*=====  End of Menu sticky    ======*/


 $('.my-accordion > li:eq(0) h2').addClass('active').next().slideDown();
		
 $('.my-accordion h2').click(function() {
     var dropDown = $(this).closest('li').find('p');

     $(this).closest('.my-accordion').find('p').not(dropDown).slideUp();

     if ($(this).hasClass('active')) {
         $(this).removeClass('active');
     } else {
         $(this).closest('.my-accordion').find('h2.active').removeClass('active');
         $(this).addClass('active');
     }

     dropDown.stop(false, true).slideToggle();

     j.preventDefault();
 });

// progress-bar Line
    if($('.progress-line').length) {
        $('.progress-line').appear(function(){
            var el = $(this);
            var percent = el.data('width');
            $(el).css('width', percent+'%');
        }, {accY: 0});
    }



    /*======================================
      Mobile Menu Js
      ========================================*/
      $("#mobile-menu").meanmenu({
        meanMenuContainer: ".mobile-menu",
        meanScreenWidth: "1200",
    });

    /*======================================
      Sidebar Toggle
      ========================================*/
    $(".offcanvas__close,.offcanvas__overlay").on("click", function () {
        $(".st__offcanvas__area").removeClass("info-open");
        $(".offcanvas__overlay").removeClass("overlay-open");
    });
    // Scroll to bottom then close navbar
    $(window).scroll(function(){
        if($("body").scrollTop() > 0 || $("html").scrollTop() > 0) {
            $(".st__offcanvas__area").removeClass("info-open");
            $(".offcanvas__overlay").removeClass("overlay-open");
        }
    });
    $(".sidebar__toggle").on("click", function () {
        $(".st__offcanvas__area").addClass("info-open");
        $(".offcanvas__overlay").addClass("overlay-open");
    });



// PreLoader Js	
$('.preloader__logo img').addClass('logo-blink');

(function(){
    function id(v){ return document.getElementById(v); }
    function loadbar() {
        var ovrl = id("loading"),
            prog = id("st-loading-line"),
            img = document.images,
            c = 0,
            tot = img.length;
        if(tot == 0) return doneLoading();
    
        function imgLoaded(){
        c += 1;
        var perc = ((100/tot*c) << 0) +"%";
        prog.style.width = perc;

        if(c===tot) return doneLoading();
        }
        function doneLoading(){
        
        setTimeout(function(){ 
            $("#loading").fadeOut(500);
        }, 100);
        }
        for(var i=0; i<tot; i++) {
        var tImg     = new Image();
        tImg.onload  = imgLoaded;
        tImg.onerror = imgLoaded;
        tImg.src     = img[i].src;
        }    
    }
    document.addEventListener('DOMContentLoaded', loadbar, false);
    }());


})(jQuery);