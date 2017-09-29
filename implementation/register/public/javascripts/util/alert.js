/**
 * Creates a new alert box.
 * @param type The alertion type: one of [alert-success, alert-info, alert-warning, alert-danger]
 * @param typeText A text equivalent that will be displayed. E.g. Success! for an alert-success
 * @param description An optional description of the result
 * @returns {string} The resulting html code.
 */
function createAlert(type, typeText, description) {

  var htmlString = '<div class="alert ' + type + ' alert-dismissable">' +
                    '<a href="#" class="close" data-dismiss="alert" aria-label="close">&times;</a>' +
                    '<strong>' + typeText + '</strong> ' + description + '</div>';

  return htmlString;
}

/**
 * Removes a given selection when a given time has passed.
 * @param selector The elements to remove
 * @param timeout The deadline (in ms), after which the elements should be removed.
 */
function removeAfter(selector, timeout) {
    window.setTimeout(function() {
        $(selector).fadeTo(500, 0).slideUp(500, function(){
            $(this).remove();
        });
    }, timeout);
}