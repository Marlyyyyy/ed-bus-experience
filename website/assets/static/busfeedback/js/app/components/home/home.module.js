/**
 * Created by Marci on 28/08/2015.
 */

(function () {
    'use strict';

    angular
        .module('runnerapp.home', [
            'runnerapp.home.controllers'
        ]);

    angular
        .module('runnerapp.home.controllers', ['ngRoute']);
})();