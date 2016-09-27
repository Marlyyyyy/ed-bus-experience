
(function () {
    'use strict';

    angular
        .module('busfeedback.home', [
            'busfeedback.home.controllers',
            'busfeedback.home.services'
        ]);

    angular
        .module('busfeedback.home.controllers', ['ngRoute']);

    angular
        .module('busfeedback.home.services', []);
})();