/*global Ti: true, alert: true, require: true, setInterval: true, module: true*/

(function (API) {

	// ****************************************************************************************************************
	// ****************************************************************************************************************
	// private helpers

	function log (msg) {
		Ti.API.info('>>> ' + msg);
	}

	// ****************************************************************************************************************
	// ****************************************************************************************************************
	// module API

	API.start = function () {
		log('App has started.');

		require('view.main').show();
	};

	API.onGuiReady = function () {
		log('GUI is ready, start GCM regitration.');

		var gcm = require('lib.gcm');
		gcm.doRegistration({
			success: function (ev) {
				log('GCM success, deviceToken = ' + ev.deviceToken);
			},
			error: function (ev) {
				log('GCM error = ' + ev.error);
			},
			callback: function (data) {
				var dataStr = JSON.stringify(data);

				log('GCM notification while in foreground. Data is:');
				log(dataStr);

				require('view.white').show(dataStr);
			},
			unregister: function (ev) {
				log('GCM: unregister, deviceToken =' + ev.deviceToken);
			}
		});
	};

})(module.exports);