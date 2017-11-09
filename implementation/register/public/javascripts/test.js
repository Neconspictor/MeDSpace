/**
 * Created by necon on 27.09.2017.
 */

function Service(name) {
    this.name = name;
}

Service.prototype = {
    getName: function() {
        return this.name;
    },
    setName: function(name) {
        this.name = name;
    }
}

function Datasource() {
    this.url = "";
    this.description = "";
    this.services = [];
};

Datasource.prototype = {

    getURL: function() {
        return this.url;
    },

    getDescription: function() {
        return this.description;
    },

    getServices: function() {
        return this.services;
    },

    setURL: function(url) {
        this.url = url;
    },
    setDescription: function(desc) {
        this.description = desc;
    },
    setServices: function(services) {
        this.services = services;
    },

    addService: function (service) {
      this.services.push(service);
    }
};

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

    if (data.success) {
        alertBox = createAlert("alert-success", "Success!", data.message);
    } else {
        alertBox = createAlert("alert-danger", "Error!", data.message);
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

function createDatasource(formContainer) {
    var url = formContainer.find(".url");
    var description = formContainer.find(".description");
    var services = formContainer.find(".fieldItem");

    var datasource = new Datasource();

    if (url.length > 0)
        datasource.setURL(url[0].value);
    if (description.length > 0)
        datasource.setDescription(description[0].value);

    for (var i = 0; i < services.length; ++i) {
        var inputField = $(services[i]).find("input")[0];
        var service = new Service(inputField.value);
        datasource.addService(service);
    }

    return datasource;
}

//  Submits the Info form to Controller
//  formContainer: (jQuery) The form model to submit
function submitInfo(formContainer) {

    var datasource = createDatasource(formContainer);

    $.ajax({
        url: jsRoutes.controllers.RegisterController.add().url,
        type: 'post',
        data: JSON.stringify(datasource),
        contentType: "application/json",
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
    var datasource = createDatasource(formContainer);
    $.ajax({
        url: jsRoutes.controllers.RegisterController.noResponse().url,
        type: 'post',
        data: JSON.stringify(datasource),
        contentType: "application/json",
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
    var datasource = createDatasource(formContainer);
    $.ajax({
        url: jsRoutes.controllers.RegisterController.remove().url,
        type: 'post',
        data: JSON.stringify(datasource),
        contentType: "application/json",
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

function sendInChunks2(file) {

    // file is an instance of File, e.g. from a file input.
    var xmlHttpRequest = new XMLHttpRequest();
    xmlHttpRequest.open("POST",
        jsRoutes.controllers.DataCollectorController.addPartialQueryResult(
            "666", "TURTLE", "http://medspace.com/indexTest").url,
        true);

    xmlHttpRequest.setRequestHeader("Content-Type", file.type);

// Send the binary data.
// Since a File is a Blob, we can send it directly.
    xmlHttpRequest.send(file);
}


function sendInChunks(file){
    var loaded = 0;
    var step = 1024//1024*1024; size of one chunk
    var total = file.size;  // total size of file
    var start = 0;          // starting position
    var reader = new FileReader();
    var blob = file.slice(start,step); //a single chunk in starting of step size
    reader.readAsArrayBuffer(blob);
    //reader.readAsBinaryString(blob);   // reading that chunk. when it read it, onload will be invoked

    reader.onload = function(e){
        var d = {file:reader.result}
        $.ajax({
            url: jsRoutes.controllers.DataCollectorController.addPartialQueryResult("666", "TURTLE", "http://medspace.com/indexTest").url,
            type:"POST",
            data: d.toString(),// d is the chunk got by readAsBinaryString(...)
            processData: false,
            success: function (data) {
                appendAlertBox(data, "#remove-datasource-alert-box");
            },
            error: function (jqXHR, textStatus, errorThrown) {
                appendErrorBox(jqXHR, textStatus, errorThrown, "#remove-datasource-alert-box");
            }
        }).done(function(r){           // if 'd' is uploaded successfully then ->
            //$('.record_reply_g').html(r);   //updating status in html view

            loaded += step;                 //increasing loaded which is being used as start position for next chunk
            //$('.upload_rpogress').html((loaded/total) * 100);

            if(loaded <= total){            // if file is not completely uploaded
                blob = file.slice(loaded,loaded+step);  // getting next chunk
                reader.readAsArrayBuffer(blob);        //reading it through file reader which will call onload again. So it will happen recursively until file is completely uploaded.
            } else {                       // if file is uploaded completely
                loaded = total;            // just changed loaded which could be used to show status.
            }
        })
    };
}

function sendDataCollectorAddPartielQueryResult(formContainer) {
    var inputField = formContainer.find("input")[0];
    var test = inputField.files[0];
    sendInChunks2(test);
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
        var infoForm = $(this).closest(".input-append");
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

    $(document).on('click', '#data-collector-file-upload-btn', function (e) {
        e.preventDefault();
        var form = $(this).closest("#data-collector-file-upload-form");
        sendDataCollectorAddPartielQueryResult(form);
    });
});