/*
    Document ready function.
*/
jQuery(document).ready(function () {

    /*
        Sticks the footer to the bottom of the window when the height of the
        body element is less than the height of the window.
    */
    if (jQuery("body").height() < jQuery(window).height()) {
        jQuery("#ft").css("position", "absolute").css("bottom", 0);
    }
});
