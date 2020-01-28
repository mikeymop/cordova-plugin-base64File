/*global cordova, module*/

module.exports = {
    save: function (data, filename, folder,  successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "Base64File", "save", [data, filename, folder]);
    },
    load: function (filename, successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "Base64File", "load", [filename]);
    },
    open: function (filename, contentType, successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "Base64File", "open", [filename, contentType]);
    },
    launchNavigation: function (params) {
        cordova.exec(null, null, "Base64File", "launchNavigation", [params]);
    },
    watermarkImage: function (params) {
        cordova.exec(null, null, "Base64File", "watermarkImage", [params]);
    }
};
