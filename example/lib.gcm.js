/*global Ti: true, alert: true, require: true, setInterval: true, module: true */

(function (API) {	

	var gcm = require('net.iamyellow.gcmjs');

	API.doRegistration = function (callbacks) {
		gcm.registerForPushNotifications(callbacks);
	};

	API.doUnregistration = function () {
		gcm.unregisterForPushNotifications();
	};

})(module.exports);