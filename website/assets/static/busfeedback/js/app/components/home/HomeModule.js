
(function () {
    'use strict';

    angular
        .module('runnerapp.home', [
            'runnerapp.home.controllers',
            'runnerapp.home.services'
        ]);

    angular
        .module('runnerapp.home.controllers', ['ngRoute']);

    angular
        .module('runnerapp.home.services', []);
})();