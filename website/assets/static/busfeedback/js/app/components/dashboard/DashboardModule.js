
(function () {
    'use strict';

    angular
        .module('busfeedback.dashboard', [
            'busfeedback.dashboard.controllers',
            'busfeedback.dashboard.services'
        ]);

    angular
        .module('busfeedback.dashboard.controllers', [
            'ngMaterial',
            'chart.js',
            'heatmap']);

    angular
        .module('busfeedback.dashboard.services', []);
})();