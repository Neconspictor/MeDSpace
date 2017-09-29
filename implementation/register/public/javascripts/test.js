/**
 * Created by necon on 27.09.2017.
 */

// include registerCommon.js
jQuery.ajax({
    type:'GET',
    url: jsRoutes.controllers.Assets.versioned("javascripts/registerCommon.js").url,
    dataType:'script'
});

// include util/alert.js
jQuery.ajax({
    type:'GET',
    url: jsRoutes.controllers.Assets.versioned("javascripts/util/alert.js").url,
    dataType:'script'
});

function appendAlertBox(data, selector) {
    var alertBox;

    if (data.result) {
        alertBox = createAlert("alert-success", "Success!", data.response);
    } else {
        alertBox = createAlert("alert-danger", "Error!", data.response);
    }

    var alertPlace = $(selector);
    alertPlace.append(alertBox);
    removeAfter(alertPlace.find(".alert"), 2000);
}

function appendErrorBox(jqXHR, textStatus, errorThrown, selector) {
    var alertBox = createAlert("alert-danger", "Error!", errorThrown);
    var alertPlace = $(selector);
    alertPlace.append(alertBox);
    removeAfter(alertPlace.find(".alert"), 5000);
}

//  Submits the Info form to Controller
//  formContainer: (jQuery) The form model to submit
function submitInfo(formContainer) {
    $.ajax({
        url: jsRoutes.controllers.RegisterController.add().url,
        type: 'post',
        data: formContainer.serialize(),
        success: function (data) {
            // Clear the input tags
            formContainer.find("input[type='text']").each(function (i, element) {
                $(this).val('');
            });

            appendAlertBox(data, "#alertion-box");
        },
        error: function (jqXHR, textStatus, errorThrown) {
            appendErrorBox(jqXHR, textStatus, errorThrown, "#alertion-box");
        }
    });
}

function submitNoResponseForm(formContainer) {
    $.ajax({
        url: jsRoutes.controllers.RegisterController.noResponse().url,
        type: 'post',
        data: formContainer.serialize(),
        success: function (data) {
            // Clear the input tags
            formContainer.find("input[type='text']").each(function (i, element) {
                $(this).val('');
            });

            appendAlertBox(data, "#no-response-alert-box");
        },
        error: function (jqXHR, textStatus, errorThrown) {
            appendErrorBox(jqXHR, textStatus, errorThrown, "#no-response-alert-box");
        }
    });
}

function submitRemoveDatasourceForm(formContainer) {
    $.ajax({
        url: jsRoutes.controllers.RegisterController.remove().url,
        type: 'post',
        data: formContainer.serialize(),
        success: function (data) {
            // Clear the input tags
            formContainer.find("input[type='text']").each(function (i, element) {
                $(this).val('');
            });

            appendAlertBox(data, "#remove-datasource-alert-box");
        },
        error: function (jqXHR, textStatus, errorThrown) {
            appendErrorBox(jqXHR, textStatus, errorThrown, "#remove-datasource-alert-box");
        }
    });
}


function createServiceInputField() {
    return [
        '<div class="fieldItem">',
        '<input type="text" />',
        '<button class="btn remove" type="button">-</button>',
        '</div>'
    ].join('\n');
}


$(document).ready(function () {
    //$("a").tooltip();
    var add = true;
    $(document).on('click', '.add-more', function () {
        var newField = createServiceInputField();
        $(this).closest('.fieldList').append(newField);
    });
    $(document).on('click', '.remove', function () {
        $(this).closest('.fieldItem').remove();
    });

    $(document).on('click', '.btn-submit', function (e) {
        e.preventDefault();
        var test = $(this).closest(".input-append");
        var test2 = test.find(".fieldList");
        var fieldList = test2.find(".fieldItem");
        for (var i = 0; i < fieldList.length; ++i) {
            var fieldItem = fieldList.get(i);
            var inputField = $(fieldItem).find("input").get(0);
            inputField.name = 'services[' + i + ']';
        }

       //var submitButton = $(".btn-submit");
        var infoForm = $(".input-append");
        submitInfo(infoForm);
    });

    $(document).on('click', '#remove-datasource-btn', function (e) {
        e.preventDefault();
        var form = $(this).closest("#remove-datasource-form");
        submitRemoveDatasourceForm(form);
    });

    $(document).on('click', '#no-response-btn', function (e) {
        e.preventDefault();
        var form = $(this).closest("#no-response-form");
        submitNoResponseForm(form);
    });
});