
(function () {
    'use strict';

    angular
        .module('busfeedback.dashboard', [
            'busfeedback.dashboard.controllers'
        ]);

    angular
        .module('busfeedback.dashboard.controllers', [
            'ngMaterial',
            'chart.js']);
})();