
(function () {
    'use strict';

    angular
        .module('runnerapp.home', [
            'runnerapp.home.controllers'
        ]);

    angular
        .module('runnerapp.home.controllers', ['ngRoute']);
})();