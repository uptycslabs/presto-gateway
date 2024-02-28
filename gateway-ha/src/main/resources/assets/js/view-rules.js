var entityData;
var editor;

if (!library)
   var library = {};

library.json = {
   replacer: function(match, pIndent, pKey, pVal, pEnd) {
      var key = '<span class=json-key>';
      var val = '<span class=json-value>';
      var str = '<span class=json-string>';
      var r = pIndent || '';
      if (pKey)
         r = r + key + pKey.replace(/[": ]/g, '') + '</span>: ';
      if (pVal)
         r = r + (pVal[0] == '"' ? str : val) + pVal + '</span>';
      return r + (pEnd || '');
      },
   prettyPrint: function(obj) {
      var jsonLine = /^( *)("[\w]+": )?("[^"]*"|[\w.+-]*)?([,[{])?$/mg;
      return JSON.stringify(obj, null, 3)
         .replace(/&/g, '&amp;').replace(/\\"/g, '&quot;')
         .replace(/</g, '&lt;').replace(/>/g, '&gt;')
         .replace(jsonLine, library.json.replacer);
      }
   };


function submitData(url, data, type) {
    if (data === undefined) {
        data = ""
    }
    return $.ajax({
        url: url,
        headers: {
            'Accept': 'application/json',
            'Content-Type': 'application/json'
        },
        type: type, // get,post,put,delete
        dataType: "json", // NOTE: expecting 'json' fails on empty 200 response
        data: data,
        statusCode: {
            404: function () {
                console.log("404 Error in response")
            },
            500: function () {
                console.log("500 Error in response")
            }
        }
    }).always(function () {
    })
}

function renderRulesConfig() {

    var readEntityUrl = "/api/getRules";
    var getParameters = "";

    submitData(readEntityUrl, getParameters, "get").done(function (data) {
        console.log(JSON.stringify(data, null, 4));
        //$('#rules').html(library.json.prettyPrint(data, null, 4));
        $('#rules').html(JSON.stringify(data, null, 4));
         //document.getElementById('prettyJSONFormat').value =JSON.stringify(data, null, 4)
    });
}