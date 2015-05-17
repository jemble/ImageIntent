/**
 * Created by Jeremy on 12/05/2015.
 */
var exec = require('cordova/exec');

exports.getExtra = function (arg0, success, error) {
    exec(success, error, "ImageIntent", "getExtra", [arg0]);
};
